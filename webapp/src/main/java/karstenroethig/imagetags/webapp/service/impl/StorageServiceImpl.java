package karstenroethig.imagetags.webapp.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.config.properties.ImageDataProperties;
import karstenroethig.imagetags.webapp.domain.Storage;
import karstenroethig.imagetags.webapp.repository.StorageRepository;

@Service
@Transactional
@EnableConfigurationProperties(ImageDataProperties.class)
public class StorageServiceImpl
{
	private static final long STORAGE_MAX_SIZE = 500000000l;

	@Autowired
	protected ImageDataProperties imageDataProperties;

	@Autowired
	protected StorageRepository storageRepository;

	public void saveImage(Path imageFilePath, Long imageId, FileSystem fileSystem, boolean thumbnail) throws IOException
	{
		String extension = FilenameUtils.getExtension(imageFilePath.getFileName().toString());
		String filename = buildImageFilename(imageId, extension);
		Path path = fileSystem.getPath((thumbnail ? "/thumbs/" : "/")+filename);

		Files.copy(imageFilePath, path);
	}

	public void saveImage(byte[] imageData, Long imageId, String extension, FileSystem fileSystem, boolean thumbnail) throws IOException
	{
		String filename = buildImageFilename(imageId, extension);
		Path path = fileSystem.getPath((thumbnail ? "/thumbs/" : "/")+filename);

		Files.copy(new ByteArrayInputStream(imageData), path);
	}

	public byte[] loadImage(Long imageId, String extension, String storageKey, boolean thumbnail) throws IOException
	{
		createStorageFileIfItDoesNotExist(storageKey, thumbnail);

		Path storagePath = createStoragePath(storageKey, thumbnail);

		try (FileSystem fileSystem = FileSystems.newFileSystem(storagePath, null))
		{
			String filename = buildImageFilename(imageId, extension);
			Path path = fileSystem.getPath((thumbnail?"/thumbs/":"/")+filename);

			try (InputStream inputStream = Files.newInputStream(path))
			{
				return IOUtils.toByteArray(inputStream);
			}
		}
	}

	public void deleteImage(Long imageId, String extension, String storageKey) throws IOException
	{
		deleteImage(imageId, extension, storageKey, false);
		deleteImage(imageId, extension, storageKey, true);
	}

	private void deleteImage(Long imageId, String extension, String storageKey, boolean thumbnail) throws IOException
	{
		createStorageFileIfItDoesNotExist(storageKey, thumbnail);

		String filename = buildImageFilename(imageId, extension);
		Path storagePath = createStoragePath(storageKey, thumbnail);

		try (FileSystem fileSystem = FileSystems.newFileSystem(storagePath, null))
		{
			Path path = fileSystem.getPath((thumbnail?"/thumbs/":"/")+filename);
			Files.deleteIfExists(path);
		}
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
		Storage storage = storageRepository.findOne(storageId);

		if (storage == null)
			return null;

		storage.setSize(storage.getSize() + delta);
		return storageRepository.save(storage);
	}

	public void subtractAndSaveFilesize(Long storageId, long delta)
	{
		Storage storage = storageRepository.findOne(storageId);

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

	public String buildStorageFilename(String storageKey, boolean thumbnail)
	{
		StringBuffer filename = new StringBuffer();

		filename.append("images");

		if (thumbnail)
		{
			filename.append("_thumbs");
		}

		if (StringUtils.isNotBlank(storageKey))
		{
			filename.append("_");
			filename.append(storageKey);
		}

		filename.append(".zip");

		return filename.toString();
	}

	public String buildImageFilename(Long imageId, String extension)
	{
		return StringUtils.leftPad(imageId.toString(), 12, "0") + "." + extension;
	}

	public Path createStoragePath(String storageKey, boolean thumbnail)
	{
		String storageFilename = buildStorageFilename(storageKey, thumbnail);
		Path storagePath = imageDataProperties.getStorageDirectory().resolve(storageFilename);

		return storagePath;
	}
}
