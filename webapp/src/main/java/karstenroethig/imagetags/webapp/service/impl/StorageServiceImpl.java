package karstenroethig.imagetags.webapp.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.config.ApplicationProperties;
import karstenroethig.imagetags.webapp.model.domain.Image;
import karstenroethig.imagetags.webapp.model.domain.Storage;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.StorageDto;
import karstenroethig.imagetags.webapp.model.enums.ImageThumbStatusEnum;
import karstenroethig.imagetags.webapp.repository.StorageRepository;

@Service
@Transactional
public class StorageServiceImpl
{
	private static final long STORAGE_MAX_SIZE = 500_000_000l;
	private static final String ROOT_PATH_DELIMITER = "/";

	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private StorageRepository storageRepository;

	public void saveImage(Path imageFilePath, String storageFilename, FileSystem fileSystem, boolean thumbnail) throws IOException
	{
		Path path = fileSystem.getPath((thumbnail ? "/thumbs/" : "/") + storageFilename);
		Files.copy(imageFilePath, path, StandardCopyOption.REPLACE_EXISTING);
	}

	public void saveImage(byte[] imageData, String storageFilename, FileSystem fileSystem, boolean thumbnail) throws IOException
	{
		Path path = fileSystem.getPath((thumbnail ? "/thumbs/" : "/") + storageFilename);
		Files.copy(new ByteArrayInputStream(imageData), path);
	}

	public void saveImage(byte[] imageData, String storageFilename, StorageDto storageDto, boolean thumb) throws IOException
	{
		Storage storage = storageRepository.findById(storageDto.getId()).orElse(null);
		if (storage == null)
			return;

		Path storageArchivePath = createAndGetStorageArchiveIfItDoesNotExist(storage.getKey(), thumb);
		try (FileSystem storageFileSystem = FileSystems.newFileSystem(storageArchivePath))
		{
			String thumbsPath = thumb ? "/thumbs" : StringUtils.EMPTY;
			Path pathToFileInArchive = storageFileSystem.getPath(thumbsPath + ROOT_PATH_DELIMITER + storageFilename);

			Files.copy(new ByteArrayInputStream(imageData), pathToFileInArchive);
		}
	}

	public Resource loadAsResource(ImageDto image, boolean thumb) throws IOException
	{
		if (thumb &&
			(image.getThumbStatus() == ImageThumbStatusEnum.NO_THUMB
			|| image.getThumbStatus() == ImageThumbStatusEnum.GENERATION_ERROR))
		{
			try (InputStream input = StorageServiceImpl.class.getResourceAsStream("no_thumb.png"))
			{
				return new ByteArrayResource(IOUtils.toByteArray(input));
			}
		}

		Storage storage = storageRepository.findById(image.getStorage().getId()).orElse(null);
		if (storage == null)
			return null;

		Path storageArchivePath = createAndGetStorageArchiveIfItDoesNotExist(storage.getKey(), thumb);
		try (FileSystem storageFileSystem = FileSystems.newFileSystem(storageArchivePath))
		{
			String thumbsPath = thumb ? "/thumbs" : StringUtils.EMPTY;
			Path pathToFileInArchive = storageFileSystem.getPath(thumbsPath + ROOT_PATH_DELIMITER + image.getStorageFilename());
			return new ByteArrayResource(Files.readAllBytes(pathToFileInArchive));
		}
	}

	public void renameImage(String storageFilenameOld, String storageFilenameNew, FileSystem fileSystem, boolean thumbnail) throws IOException
	{
		Path pathOld = fileSystem.getPath((thumbnail ? "/thumbs/" : "/") + storageFilenameOld);
		if (!Files.exists(pathOld))
			return;

		Path pathNew = fileSystem.getPath((thumbnail ? "/thumbs/" : "/") + storageFilenameNew);
		Files.move(pathOld, pathNew);
	}

	protected StorageDto transform(Storage storage)
	{
		if (storage == null)
			return null;

		StorageDto storageDto = new StorageDto();

		storageDto.setId(storage.getId());
		storageDto.setKey(storage.getKey());

		return storageDto;
	}

	public void deleteImage(Image image) throws IOException
	{
		String storageKey = image.getStorage().getKey();
		String storageFilename = image.getStorageFilename();

		deleteImage(storageKey, storageFilename, false);
		deleteImage(storageKey, storageFilename, true);

		subtractAndSaveFilesize(image.getStorage().getId(), image.getSize());
	}

	private void deleteImage(String storageKey, String storageFilename, boolean thumbnail) throws IOException
	{
		createStorageFileIfItDoesNotExist(storageKey, thumbnail);

		Path storagePath = createStoragePath(storageKey, thumbnail);

		try (FileSystem fileSystem = FileSystems.newFileSystem(storagePath))
		{
			Path path = fileSystem.getPath((thumbnail?"/thumbs/":"/") + storageFilename);
			Files.deleteIfExists(path);
		}
	}

	private Path createAndGetStorageArchiveIfItDoesNotExist(String storageKey, boolean thumbs) throws IOException
	{
		Path storageArchivePath = resolvePathToStorageArchive(storageKey, thumbs);

		if (!Files.exists(storageArchivePath))
		{
			try (ZipOutputStream out = new ZipOutputStream(
					Files.newOutputStream(
							storageArchivePath,
							StandardOpenOption.CREATE,
							StandardOpenOption.TRUNCATE_EXISTING)))
			{
				out.setLevel(Deflater.NO_COMPRESSION);

				if (thumbs)
					out.putNextEntry(new ZipEntry("thumbs/"));

				out.closeEntry();
			}
		}

		return storageArchivePath;
	}

	private Path resolvePathToStorageArchive(String storageKey, boolean thumbs) throws IOException
	{
		Path storageDirectory = createAndGetStorageDirectory();
		String storageArchiveFilename = buildStorageFilename(storageKey, thumbs);
		Path storageArchivePath = storageDirectory.resolve(storageArchiveFilename);

		return storageArchivePath;
	}

	private Path createAndGetStorageDirectory() throws IOException
	{
		Path storageDirectory = applicationProperties.getStorageDirectory();
		if (!Files.exists(storageDirectory))
		{
			Files.createDirectories(storageDirectory);
		}

		return storageDirectory;
	}

	protected void createStorageFileIfItDoesNotExist(String storageKey, boolean thumbnail) throws IOException
	{
		Path storagePath = createStoragePath(storageKey, thumbnail);

		if (Files.exists(storagePath))
			return;

		try (ZipOutputStream out = new ZipOutputStream(
				Files.newOutputStream(
						storagePath,
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING)))
		{
			out.setLevel(Deflater.NO_COMPRESSION);

			if (thumbnail)
				out.putNextEntry(new ZipEntry("thumbs/"));

			out.closeEntry();
		}
	}

	public Storage addAndSaveFilesize(Long storageId, long delta)
	{
		Storage storage = storageRepository.findById(storageId).orElse(null);

		if (storage == null)
			return null;

		storage.setSize(storage.getSize() + delta);
		return storageRepository.save(storage);
	}

	private void subtractAndSaveFilesize(Long storageId, long delta)
	{
		Storage storage = storageRepository.findById(storageId).orElse(null);

		if (storage == null)
			return;

		long newSize = storage.getSize() - delta;

		if (newSize < 0)
			newSize = 0l;

		storage.setSize(newSize);
		storageRepository.save(storage);
	}

	public Storage findOrCreateStorage(Storage currentStorage, long nextFileSize)
	{
		if (isAcceptableStorage(currentStorage, nextFileSize))
			return currentStorage;

		for (Storage storage : storageRepository.findBySizeLessThanOrderBySizeAsc(STORAGE_MAX_SIZE))
		{
			if (isAcceptableStorage(storage, nextFileSize))
				return storage;
		}

		Storage storage = new Storage();
		storage.setKey(UUID.randomUUID().toString());
		storage.setSize(0l);

		return storageRepository.save(storage);
	}

	private boolean isAcceptableStorage(Storage storage, long nextFileSize)
	{
		if (storage == null)
			return false;

		long finalFileSize = storage.getSize() + nextFileSize;

		return finalFileSize <= STORAGE_MAX_SIZE;
	}

	private String buildStorageFilename(String storageKey, boolean thumbs)
	{
		StringBuilder filename = new StringBuilder();

		filename.append("images");

		if (thumbs)
			filename.append("_thumbs");

		if (StringUtils.isNotBlank(storageKey))
		{
			filename.append("_");
			filename.append(storageKey);
		}

		filename.append(".zip");

		return filename.toString();
	}

	public Path createStoragePath(String storageKey, boolean thumbnail)
	{
		String storageFilename = buildStorageFilename(storageKey, thumbnail);
		Path storagePath = applicationProperties.getStorageDirectory().resolve(storageFilename);

		return storagePath;
	}
}
