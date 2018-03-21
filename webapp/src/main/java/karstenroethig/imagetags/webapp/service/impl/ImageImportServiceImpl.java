package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.config.properties.ImageDataProperties;
import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
@EnableConfigurationProperties(ImageDataProperties.class)
public class ImageImportServiceImpl
{
	private static final String[] IMAGE_FILE_EXTENSIONS = new String[] {"gif", "GIF", "jpg", "JPG", "jpeg", "JPEG", "png", "PNG"};

	@Autowired
	protected ImageDataProperties imageDataProperties;

	@Autowired
	protected ImageRepository imageRepository;

	@Autowired
	protected ImageFileServiceImpl imageFileService;

	@PostConstruct
	public void execute()
	{
		Path importDirectory = imageDataProperties.getImportDirectory();

		if (!Files.exists(importDirectory))
		{
			log.info("import of images skipped (directory 'data/new' does not exist)");

			return;
		}

		try
		{
			// find all new files for import
			List<Path> allNewImages = Files.walk(importDirectory, FileVisitOption.FOLLOW_LINKS)
				.sorted(Comparator.reverseOrder())
				.filter(path -> isImageFile(path))
//				.peek(System.out::println)
				.collect(Collectors.toList());

			// import the images
			importImages(allNewImages);

			// clean up the import directory
			cleanupImportDirectory(importDirectory);
		}
		catch (IOException ex)
		{
			log.error("error on importing images", ex);
		}
	}

	private void importImages(List<Path> imagePaths) throws IOException
	{
		int totalFileCount = imagePaths.size();
		int currentFileCount = 0;

		log.info(
			String.format("start import of %s new images", totalFileCount)
		);

		imageFileService.createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getZipPath(), null))
		{
			for (Path imagePath : imagePaths)
			{
				currentFileCount++;
				importImage(imagePath, currentFileCount, totalFileCount, fileSystem);
			}
		}
	}

	private void importImage(Path imagePath, int currentFileCount, int totalFileCount, FileSystem fileSystem) throws IOException
	{
		log.info(
			String.format("importing image file [%s/%s]: %s", currentFileCount, totalFileCount, imagePath.toString())
		);

		Image image = createImage(imagePath);

		if (!doesImageExistInDatabase(image.getHash()))
		{
			// save new image in database
			imageRepository.save(image);

			// copy file
//			imageFileService.saveImage(imagePath, image.getId());
			saveImage(imagePath, image.getId(), fileSystem);
		}
		else
		{
			log.info(
				String.format("image %s already exists and will be ignored", imagePath.toString())
			);
		}

		// delete the source file
		Files.delete(imagePath);
	}

	private Image createImage(Path imagePath) throws IOException
	{
		Image image = new Image();

		image.setExtension(FilenameUtils.getExtension(imagePath.getFileName().toString()));
		image.setSize(Files.size(imagePath));
		image.setImportPath(findRelativeImportPath(imagePath));

		try (InputStream inputStream = Files.newInputStream(imagePath))
		{
			image.setHash(DigestUtils.md5Hex(inputStream));
		}

		return image;
	}

	private boolean doesImageExistInDatabase(String hash)
	{
		List<Image> images = imageRepository.findByHashIgnoreCase(hash);

		return images != null && !images.isEmpty();
	}

	private void cleanupImportDirectory(Path importDirectory) throws IOException
	{
		List<Path> emptyDirectories = Files.walk(importDirectory, FileVisitOption.FOLLOW_LINKS)
			.sorted(Comparator.reverseOrder())
			.filter(path -> isEmptyDirectory(path) && !path.equals(importDirectory))
			.collect(Collectors.toList());

		for (Path emptyDirectory : emptyDirectories)
		{
			Files.deleteIfExists(emptyDirectory);
		}
	}

	private boolean isImageFile(Path path)
	{
		if (path == null || Files.isDirectory(path))
		{
			return false;
		}

		return FilenameUtils.isExtension(path.getFileName().toString(), IMAGE_FILE_EXTENSIONS);
	}

	private String findRelativeImportPath(Path filePath)
	{
		return imageDataProperties.getImportDirectory().relativize(filePath).toString();
	}

	private boolean isEmptyDirectory(Path path)
	{
		if (path == null || !Files.isDirectory(path))
		{
			return false;
		}

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path))
		{
			return !directoryStream.iterator().hasNext();
		}
		catch (IOException ex)
		{
			log.warn("error on resolving empty directory", ex);
		}

		return false;
	}

	private void saveImage(Path imageFilePath, Long imageId, FileSystem fileSystem) throws IOException
	{
		String extension = FilenameUtils.getExtension(imageFilePath.getFileName().toString());
		String filename = imageFileService.buildFilename(imageId, extension);

		Path path = fileSystem.getPath("/"+filename);
		Files.copy(imageFilePath, path);
	}
}
