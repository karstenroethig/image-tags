package karstenroethig.imagetags.webapp.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.config.ApplicationProperties;
import karstenroethig.imagetags.webapp.model.domain.Storage;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.StorageDto;
import karstenroethig.imagetags.webapp.repository.StorageRepository;

@Service
@Transactional
public class StorageServiceImpl
{
	private static final long STORAGE_MAX_SIZE = 500000000l;
	private static final String ROOT_PATH_DELIMITER = "/";

	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private StorageRepository storageRepository;

	public void saveImage(Path imageFilePath, String storageFilename, FileSystem fileSystem, boolean thumbnail) throws IOException
	{
		Path path = fileSystem.getPath((thumbnail ? "/thumbs/" : "/") + storageFilename);
		Files.copy(imageFilePath, path);
	}

	public void saveImage(byte[] imageData, String storageFilename, FileSystem fileSystem, boolean thumbnail) throws IOException
	{
		Path path = fileSystem.getPath((thumbnail ? "/thumbs/" : "/") + storageFilename);
		Files.copy(new ByteArrayInputStream(imageData), path);
	}

	public Resource loadAsResource(ImageDto image, boolean thumb) throws IOException
	{
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

	protected StorageDto transform(Storage storage)
	{
		if (storage == null)
			return null;

		StorageDto storageDto = new StorageDto();

		storageDto.setId(storage.getId());
		storageDto.setKey(storage.getKey());

		return storageDto;
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

	public void subtractAndSaveFilesize(Long storageId, long delta)
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
