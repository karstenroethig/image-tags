package karstenroethig.imagetags.webapp.service.impl;

import java.awt.Point;
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
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.config.properties.ImageDataProperties;
import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.domain.Storage;
import karstenroethig.imagetags.webapp.domain.enums.ImageResolutionStatusEnum;
import karstenroethig.imagetags.webapp.domain.enums.ImageThumbStatusEnum;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class ImageImportServiceImpl
{
	private static final String[] IMAGE_FILE_EXTENSIONS = new String[] {"gif", "GIF", "jpg", "JPG", "jpeg", "JPEG", "png", "PNG"};
	private static final Point RESOLUTION_DEFAULT = new Point(0, 0);

	@Autowired
	protected ImageDataProperties imageDataProperties;

	@Autowired
	protected ImageRepository imageRepository;

	@Autowired
	protected ImageOperationServiceImpl imageOperationService;

	@Autowired
	protected StorageServiceImpl storageService;

	@PostConstruct
	public void execute()
	{
		Path importDirectory = imageDataProperties.getImportDirectory();

		if (!Files.exists(importDirectory))
		{
			log.info("import of images skipped (directory '" + importDirectory + "' does not exist)");

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

		log.info(String.format("start import of %s new images", totalFileCount));

		Storage currentStorage = null;
		String currentStorageKey = null;
		FileSystem currentFileSystem = null;
		FileSystem currentFileSystemThumbs = null;

		try
		{
			for (Path imagePath : imagePaths)
			{
				currentFileCount++;

				currentStorage = storageService.findOrCreateStorage(currentStorage, Files.size(imagePath));

				if (!currentStorage.getKey().equals(currentStorageKey))
				{
					currentStorageKey = currentStorage.getKey();

					IOUtils.closeQuietly(currentFileSystem);
					storageService.createStorageFileIfItDoesNotExist(currentStorageKey, false);
					Path storagePath = storageService.createStoragePath(currentStorageKey, false);
					currentFileSystem = FileSystems.newFileSystem(storagePath, null);

					IOUtils.closeQuietly(currentFileSystemThumbs);
					storageService.createStorageFileIfItDoesNotExist(currentStorageKey, true);
					storagePath = storageService.createStoragePath(currentStorageKey, true);
					currentFileSystemThumbs = FileSystems.newFileSystem(storagePath, null);
				}

				Image image = importImage(imagePath, currentStorage, currentFileCount, totalFileCount, currentFileSystem, currentFileSystemThumbs);

				if (image != null)
				{
					currentStorage = storageService.addAndSaveFilesize(currentStorage.getId(), image.getSize());
				}
			}
		}
		finally
		{
			IOUtils.closeQuietly(currentFileSystem);
			IOUtils.closeQuietly(currentFileSystemThumbs);
		}
	}

	private Image importImage(Path imagePath, Storage storage, int currentFileCount, int totalFileCount, FileSystem fileSystem, FileSystem fileSystemThumbs) throws IOException
	{
		log.info(String.format("importing image file [%s/%s]: %s", currentFileCount, totalFileCount, imagePath.toString()));

		Image image = createImage(imagePath);

		if (!doesImageExistInDatabase(image.getHash()))
		{
			// save new image in database
			image.setStorage(storage);
			imageRepository.save(image);

			// copy file
			storageService.saveImage(imagePath, image.getId(), fileSystem, false);

			// create thumbnail and copy it to the storage
			try {
				byte[] thumbData = imageOperationService.createImageThumbnail(imagePath);
				storageService.saveImage(thumbData, image.getId(), image.getExtension(), fileSystemThumbs, true);
				image.setThumbStatusEnum(ImageThumbStatusEnum.THUMB_100_100);
			}
			catch (Exception ex)
			{
				log.warn(String.format("failed to generate thumbnail of image %s", imagePath.toString()), ex);
				image.setThumbStatusEnum(ImageThumbStatusEnum.GENERATION_ERROR);
			}
			imageRepository.save(image);
		}
		else
		{
			log.info(String.format("image %s already exists and will be ignored", imagePath.toString()));
			image = null;
		}

		// delete the source file
		Files.delete(imagePath);

		return image;
	}

	private Image createImage(Path imagePath) throws IOException
	{
		Image image = new Image();

		image.setExtension(FilenameUtils.getExtension(imagePath.getFileName().toString()));
		image.setSize(Files.size(imagePath));
		image.setThumbStatusEnum(ImageThumbStatusEnum.NO_THUMB);
		image.setImportPath(findRelativeImportPath(imagePath));

		try (InputStream inputStream = Files.newInputStream(imagePath))
		{
			image.setHash(DigestUtils.md5Hex(inputStream));
		}

		try
		{
			Point resolution = imageOperationService.resolveImageResolution(imagePath);
			image.setResolutionWidth(resolution.x);
			image.setResolutionHeight(resolution.y);
			image.setResolutionStatusEnum(ImageResolutionStatusEnum.GENERATION_SUCCESS);
		}
		catch (Exception ex)
		{
			log.warn(String.format("error on resolving resolution of image %s", imagePath.toString()), ex);
			image.setResolutionWidth(RESOLUTION_DEFAULT.x);
			image.setResolutionHeight(RESOLUTION_DEFAULT.y);
			image.setResolutionStatusEnum(ImageResolutionStatusEnum.GENERATION_ERROR);
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
}
