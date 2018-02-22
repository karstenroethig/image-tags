package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class ImageImportServiceImpl
{
	private static final Path IMAGES_IMPORT_DIRECTORY = Paths.get("data/new");
	private static final String[] IMAGE_FILE_EXTENSIONS = new String[] {"gif", "GIF", "jpg", "JPG", "jpeg", "JPEG", "png", "PNG"};

	@Autowired
	protected ImageRepository imageRepository;

	@Autowired
	protected ImageFileServiceImpl imageFileService;

	@PostConstruct
	public void execute()
	{
		if (!Files.exists(IMAGES_IMPORT_DIRECTORY))
		{
			log.info("import of images skipped (directory 'data/new' does not exist)");

			return;
		}

		try
		{
			// find all new files for import
			List<Path> allNewImages = Files.walk(IMAGES_IMPORT_DIRECTORY, FileVisitOption.FOLLOW_LINKS)
				.sorted(Comparator.reverseOrder())
				.filter(path -> isImageFile(path))
//				.peek(System.out::println)
				.collect(Collectors.toList());

			// import the images
			importImages(allNewImages);

			// clean up the import directory
			cleanupImportDirectory();
		}
		catch (IOException ex)
		{
			log.error("error on importing images", ex);
		}
	}

	private void importImages(List<Path> imagePaths) throws IOException
	{
		log.info("start import of " + imagePaths.size() + " new images");

		for (Path imagePath : imagePaths)
		{
			importImage(imagePath);
		}
	}

	private void importImage(Path imagePath) throws IOException
	{
		log.info("importing image file " + imagePath.toString());

		Image image = createImage(imagePath);

		if (!doesImageExistInDatabase(image.getHash()))
		{
			// save new image in database
			imageRepository.save(image);

			// copy file
			imageFileService.saveImage(imagePath, image.getId());
		}
		else
		{
			log.info("image " + imagePath.toString() + " already exists and will be ignored");
		}

		// delete the source file
		Files.delete(imagePath);
	}

	private Image createImage(Path imagePath) throws IOException
	{
		Image image = new Image();

		image.setExtension(FilenameUtils.getExtension(imagePath.getFileName().toString()));
		image.setSize(Files.size(imagePath));

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

	private void cleanupImportDirectory() throws IOException
	{
		List<Path> emptyDirectories = Files.walk(IMAGES_IMPORT_DIRECTORY, FileVisitOption.FOLLOW_LINKS)
			.sorted(Comparator.reverseOrder())
			.filter(path -> isEmptyDirectory(path) && !path.equals(IMAGES_IMPORT_DIRECTORY))
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
}
