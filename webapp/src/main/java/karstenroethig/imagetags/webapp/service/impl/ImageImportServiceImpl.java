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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.config.ApplicationProperties;
import karstenroethig.imagetags.webapp.model.domain.Image;
import karstenroethig.imagetags.webapp.model.domain.Storage;
import karstenroethig.imagetags.webapp.model.domain.Tag;
import karstenroethig.imagetags.webapp.model.enums.ImageResolutionStatusEnum;
import karstenroethig.imagetags.webapp.model.enums.ImageThumbStatusEnum;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class ImageImportServiceImpl
{
	private static final String[] IMAGE_FILE_EXTENSIONS = new String[] {"gif", "GIF", "jpg", "JPG", "jpeg", "JPEG", "png", "PNG", "webp", "WEBP"};
	private static final Point RESOLUTION_DEFAULT = new Point(0, 0);

	@Autowired private ApplicationProperties applicationProperties;

	@Autowired protected ImageOperationServiceImpl imageOperationService;
	@Autowired protected StorageServiceImpl storageService;
	@Autowired protected TagServiceImpl tagService;

	@Autowired protected ImageRepository imageRepository;

	public void execute()
	{
		Path importDirectory = applicationProperties.getImportDirectory();

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
					currentFileSystem = FileSystems.newFileSystem(storagePath);

					IOUtils.closeQuietly(currentFileSystemThumbs);
					storageService.createStorageFileIfItDoesNotExist(currentStorageKey, true);
					storagePath = storageService.createStoragePath(currentStorageKey, true);
					currentFileSystemThumbs = FileSystems.newFileSystem(storagePath);
				}

				Image image = importImage(imagePath, currentStorage, currentFileCount, totalFileCount, currentFileSystem, currentFileSystemThumbs);

				if (image != null)
					currentStorage = storageService.addAndSaveFilesize(currentStorage.getId(), image.getSize());
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
			storageService.saveImage(imagePath, image.getStorageFilename(), fileSystem, false);

			// create thumbnail and copy it to the storage
			try {
				byte[] thumbData = imageOperationService.createImageThumbnail(imagePath);
				storageService.saveImage(thumbData, image.getStorageFilename(), fileSystemThumbs, true);
				image.setThumbStatus(ImageThumbStatusEnum.THUMB_100_100);
			}
			catch (Exception ex)
			{
				log.warn(String.format("failed to generate thumbnail of image %s", imagePath.toString()), ex);
				image.setThumbStatus(ImageThumbStatusEnum.GENERATION_ERROR);
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
		String extension = FilenameUtils.getExtension(imagePath.getFileName().toString());
		String filename = String.format("%s.%s", UUID.randomUUID().toString(), extension);

		Image image = new Image();

		image.setStorageFilename(filename);
		image.setExtension(extension);
		image.setSize(Files.size(imagePath));
		image.setThumbStatus(ImageThumbStatusEnum.NO_THUMB);
		image.setImportPath(findRelativeImportPath(imagePath));
		image.setCreatedDatetime(LocalDateTime.now());

		try (InputStream inputStream = Files.newInputStream(imagePath))
		{
			image.setHash(DigestUtils.md5Hex(inputStream));
		}

		try
		{
			Point resolution = imageOperationService.resolveImageResolution(imagePath);
			image.setResolutionWidth(resolution.x);
			image.setResolutionHeight(resolution.y);
			image.setResolutionStatus(ImageResolutionStatusEnum.GENERATION_SUCCESS);
		}
		catch (Exception ex)
		{
			log.warn(String.format("error on resolving resolution of image %s", imagePath.toString()), ex);
			image.setResolutionWidth(RESOLUTION_DEFAULT.x);
			image.setResolutionHeight(RESOLUTION_DEFAULT.y);
			image.setResolutionStatus(ImageResolutionStatusEnum.GENERATION_ERROR);
		}

		Tag tagNew = tagService.findOrCreate(TagServiceImpl.TAG_NEW);
		image.addTag(tagNew);

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
		return applicationProperties.getImportDirectory().relativize(filePath).toString();
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