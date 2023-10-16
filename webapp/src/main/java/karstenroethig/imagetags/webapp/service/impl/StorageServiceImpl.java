package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.Deflater;
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
	private static final String ROOT_PATH_DELIMITER = "/";

	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private StorageRepository storageRepository;

	public Resource loadAsResource(ImageDto image) throws IOException
	{
		Storage storage = storageRepository.findById(image.getStorage().getId()).orElse(null);
		if (storage == null)
			return null;

		Path storageArchivePath = createAndGetStorageArchiveIfItDoesNotExist(storage.getKey());
		try (FileSystem storageFileSystem = FileSystems.newFileSystem(storageArchivePath))
		{
			Path pathToFileInArchive = storageFileSystem.getPath(ROOT_PATH_DELIMITER + image.getStorageKey());
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

	private Path createAndGetStorageArchiveIfItDoesNotExist(String storageKey) throws IOException
	{
		Path storageArchivePath = resolvePathToStorageArchive(storageKey);

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

	private Path resolvePathToStorageArchive(String storageKey) throws IOException
	{
		Path storageDirectory = createAndGetStorageDirectory();
		String storageArchiveFilename = buildStorageFilename(storageKey);
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

	private String buildStorageFilename(String storageKey)
	{
		StringBuilder filename = new StringBuilder();

		filename.append("images");

		if (StringUtils.isNotBlank(storageKey))
		{
			filename.append("_");
			filename.append(storageKey);
		}

		filename.append(".zip");

		return filename.toString();
	}
}
