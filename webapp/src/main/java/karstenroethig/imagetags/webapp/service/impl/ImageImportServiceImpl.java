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
import org.apache.commons.lang3.Strings;
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
	private static final String[] IMAGE_FILE_EXTENSIONS = new String[] {"gif", "jpg", "jpeg", "png", "webp"};
	private static final Point RESOLUTION_DEFAULT = new Point(0, 0);

	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private ImageOperationServiceImpl imageOperationService;
	@Autowired private StorageServiceImpl storageService;
	@Autowired private TagServiceImpl tagService;

	@Autowired private ImageRepository imageRepository;

	public void execute()
	{
		Path importDirectory = applicationProperties.getImportDirectory();

		if (!Files.exists(importDirectory))
		{
			log.info("import of images skipped (directory '{}' does not exist)", importDirectory);
			return;
		}

		try
		{
			// find all new files for import
			List<Path> allNewImages = Files.walk(importDirectory, FileVisitOption.FOLLOW_LINKS)
				.sorted(Comparator.reverseOrder())
				.filter(path -> isImageFile(path))
				.filter(path -> !isInThumbsDirectory(path))
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
				log.info("importing image file [{}/{}]: {}", ++currentFileCount, totalFileCount, imagePath);

				String hash = null;
				try (InputStream inputStream = Files.newInputStream(imagePath))
				{
					hash = DigestUtils.md5Hex(inputStream);
				}

				Image existingImage = findImageByHash(hash);

				if (existingImage == null)
				{
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

					Image image = importImage(imagePath, currentStorage, hash, currentFileSystem, currentFileSystemThumbs);
					currentStorage = storageService.addAndSaveFilesize(currentStorage.getId(), image.getSize());
				}
				else
				{
					log.info("-> image {} already exists and will be ignored", imagePath);

					String extension = FilenameUtils.getExtension(imagePath.getFileName().toString());
					boolean newExtension = !Strings.CI.equals(extension, existingImage.getExtension());
					if (newExtension)
						log.info("-> image has new extension ({} -> {})", existingImage.getExtension(), extension);

					String imageFileName = imagePath.getFileName().toString();
					Path imageThumbPath = imagePath.getParent().resolve("thumbs", imageFileName);
					boolean newThumbnail = Files.exists(imageThumbPath);
					if (newThumbnail)
						log.info("-> image has new thumbnail");

					if (newExtension || newThumbnail)
					{
						log.info("-> data is being updated");

						currentStorage = existingImage.getStorage();

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

						if (newExtension)
						{
							String filenameOld = existingImage.getStorageFilename();
							String filenameNew = Strings.CI.removeEnd(filenameOld, existingImage.getExtension()) + extension;

							storageService.renameImage(filenameOld, filenameNew, currentFileSystem, false);
							storageService.renameImage(filenameOld, filenameNew, currentFileSystemThumbs, true);

							existingImage.setStorageFilename(filenameNew);
							existingImage.setExtension(extension);
						}

						if (newThumbnail)
						{
							storageService.saveImage(imageThumbPath, existingImage.getStorageFilename(), currentFileSystemThumbs, true);

							existingImage.setThumbStatus(ImageThumbStatusEnum.IMPORTED);

							Files.delete(imageThumbPath);
						}

						imageRepository.save(existingImage);
					}
				}

				// delete the source file
				Files.delete(imagePath);
			}
		}
		finally
		{
			IOUtils.closeQuietly(currentFileSystem);
			IOUtils.closeQuietly(currentFileSystemThumbs);
		}
	}

	private Image importImage(Path imagePath, Storage storage, String hash, FileSystem fileSystem, FileSystem fileSystemThumbs) throws IOException
	{
		Image image = createImage(imagePath, hash);

		// save new image in database
		image.setStorage(storage);
		imageRepository.save(image);

		// copy file
		storageService.saveImage(imagePath, image.getStorageFilename(), fileSystem, false);

		// thumbnail
		String imageFileName = imagePath.getFileName().toString();
		Path imageThumbPath = imagePath.getParent().resolve("thumbs", imageFileName);

		if (Files.exists(imageThumbPath))
		{
			// copy thmubnail from inport directory
			byte[] thumbData = Files.readAllBytes(imageThumbPath);
			storageService.saveImage(thumbData, image.getStorageFilename(), fileSystemThumbs, true);
			image.setThumbStatus(ImageThumbStatusEnum.IMPORTED);
		}
		else
		{
			// create thumbnail and copy it to the storage
			try
			{
				byte[] thumbData = imageOperationService.createImageThumbnail(imagePath);
				storageService.saveImage(thumbData, image.getStorageFilename(), fileSystemThumbs, true);
				image.setThumbStatus(ImageThumbStatusEnum.THUMB_120_120);
			}
			catch (Exception ex)
			{
				log.warn(String.format("failed to generate thumbnail of image %s", imagePath.toString()), ex);
				image.setThumbStatus(ImageThumbStatusEnum.GENERATION_ERROR);
			}
		}

		imageRepository.save(image);

		return image;
	}

	private Image createImage(Path imagePath, String hash) throws IOException
	{
		String extension = FilenameUtils.getExtension(imagePath.getFileName().toString());
		String filename = String.format("%s.%s", UUID.randomUUID().toString(), extension);

		Image image = new Image();

		image.setStorageFilename(filename);
		image.setExtension(extension);
		image.setSize(Files.size(imagePath));
		image.setThumbStatus(ImageThumbStatusEnum.NO_THUMB);
		image.setDescription(findRelativeImportPath(imagePath));
		image.setCreatedDatetime(LocalDateTime.now());
		image.setHash(hash);

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

	private Image findImageByHash(String hash)
	{
		List<Image> images = imageRepository.findByHashIgnoreCase(hash);
		return images.stream().findFirst().orElse(null);
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
			return false;

		String filename = path.getFileName().toString().toLowerCase();
		if (Strings.CS.startsWith(filename, "."))
			return false;

		return FilenameUtils.isExtension(filename, IMAGE_FILE_EXTENSIONS);
	}

	private boolean isInThumbsDirectory(Path path)
	{
		if (path == null || Files.isDirectory(path))
			return false;

		Path parentPath = path.getParent();
		return Strings.CS.equals(parentPath.getFileName().toString(), "thumbs");
	}

	private String findRelativeImportPath(Path filePath)
	{
		return applicationProperties.getImportDirectory().relativize(filePath).toString();
	}

	private boolean isEmptyDirectory(Path path)
	{
		if (path == null || !Files.isDirectory(path))
			return false;

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