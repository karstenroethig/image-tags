package karstenroethig.imagetags.webapp.service.impl;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.config.AsyncTaskExecutorConfig;
import karstenroethig.imagetags.webapp.model.domain.Image;
import karstenroethig.imagetags.webapp.model.domain.Storage;
import karstenroethig.imagetags.webapp.model.domain.Tag;
import karstenroethig.imagetags.webapp.model.dto.backup.RestoreDto;
import karstenroethig.imagetags.webapp.model.dto.backup.RestoreInfoDto;
import karstenroethig.imagetags.webapp.model.json.BackupJson;
import karstenroethig.imagetags.webapp.model.json.ImageJson;
import karstenroethig.imagetags.webapp.model.json.StorageJson;
import karstenroethig.imagetags.webapp.model.json.TagJson;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import karstenroethig.imagetags.webapp.repository.StorageRepository;
import karstenroethig.imagetags.webapp.repository.TagRepository;
import karstenroethig.imagetags.webapp.util.FileTypeUtils;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.validation.ValidationException;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class RestoreServiceImpl
{
	private static RestoreInfoDto restoreInfo = new RestoreInfoDto();

	@Autowired private TagRepository tagRepository;
	@Autowired private StorageRepository storageRepository;
	@Autowired private ImageRepository imageRepository;

	public RestoreDto create()
	{
		return new RestoreDto();
	}

	public ValidationResult validate(RestoreDto restore)
	{
		ValidationResult result = new ValidationResult();

		if (restore == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateFilePath(restore));

		return result;
	}

	private void checkValidation(RestoreDto restore)
	{
		ValidationResult result = validate(restore);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateFilePath(RestoreDto restore)
	{
		ValidationResult result = new ValidationResult();

		Path filePath = Paths.get(restore.getFilePath());
		if (!Files.exists(filePath))
			result.addError("filePath", MessageKeyEnum.RESTORE_EXECUTE_INVALID_FILE_PATH_DOES_NOT_EXIST);
		else if (!Files.isReadable(filePath))
			result.addError("filePath", MessageKeyEnum.RESTORE_EXECUTE_INVALID_FILE_PATH_NOT_READABLE);
		else if (!Files.isRegularFile(filePath))
			result.addError("filePath", MessageKeyEnum.RESTORE_EXECUTE_INVALID_FILE_PATH_NOT_A_FILE);

		return result;
	}

	public RestoreInfoDto getRestoreInfo()
	{
		return restoreInfo;
	}

	protected boolean blockRestoreProcess()
	{
		if (restoreInfo.isRunning())
			return false;

		restoreInfo.intializeNewRestore();
		return true;
	}

	@Async(AsyncTaskExecutorConfig.BACKUP_TASK_EXECUTOR)
	protected void performRestoreAsync(RestoreDto restore)
	{
		try (IBackupFileSystemWrapper backupFileSystemWrapper = createBackupFileSystemWrapper(restore))
		{
			checkValidation(restore);

			BackupJson backup = readBackup(backupFileSystemWrapper);

			determineTotalWork(backup);

			resetDatabase();

			importTags(backup.getTags());
			importStorages(backup.getStorages());
			importImages(backup.getImages());
		}
		catch (Exception ex)
		{
			log.error("error on restore process", ex);
			restoreInfo.addErrorMessage(ex.getMessage());
		}
		finally
		{
			restoreInfo.done();
		}
	}

	private IBackupFileSystemWrapper createBackupFileSystemWrapper(RestoreDto restore) throws IOException
	{
		Path backupFilePath = Paths.get(restore.getFilePath());

		if (FileTypeUtils.isJsonFile(backupFilePath))
			return new BackupJsonFileSystemWrapper(backupFilePath);
		else if (FileTypeUtils.isZipArchive(backupFilePath))
			return new BackupZipFileSystemWrapper(backupFilePath);

		throw new IllegalStateException("invalid backup file format");
	}

	private BackupJson readBackup(IBackupFileSystemWrapper backupFileSystemWrapper) throws IOException, JAXBException
	{
		try (InputStream in = backupFileSystemWrapper.getBackupJsonInputStream())
		{
			ObjectReader objectReader = new ObjectMapper()
				.registerModule(new JavaTimeModule())
 				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.reader();

			return objectReader.readValue(in, BackupJson.class);
		}
	}

	private void determineTotalWork(BackupJson backup)
	{
		int totalWork = 0;
		totalWork += backup.getTags().size();
		totalWork += backup.getStorages().size();
		totalWork += backup.getImages().size();

		restoreInfo.setTotalWork(totalWork);
	}

	private void resetDatabase() throws IOException
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_RESET_DATABASE, 0);

		tagRepository.deleteAllInBatch();
		imageRepository.deleteAllInBatch();
		storageRepository.deleteAllInBatch();
	}

	private void importTags(List<TagJson> tags)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_TAGS, tags.size());

		for (TagJson tagJson : tags)
		{
			Tag tag = new Tag();
			tag.setName(tagJson.getName());
			tag.setType(tagJson.getType());

			tagRepository.save(tag);

			restoreInfo.worked(1);
		}
	}

	private void importStorages(List<StorageJson> storages)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_STORAGES, storages.size());

		for (StorageJson storageJson : storages)
		{
			Storage storage = new Storage();
			storage.setKey(storageJson.getKey());
			storage.setSize(storageJson.getSize());

			storageRepository.save(storage);

			restoreInfo.worked(1);
		}
	}

	private void importImages(List<ImageJson> images)
	{
		restoreInfo.beginTask(MessageKeyEnum.RESTORE_TASK_IMPORT_IMAGES, images.size());

		for (ImageJson imageJson : images)
		{
			Storage storage = storageRepository.findOneByKey(imageJson.getStorageKey()).orElseThrow();

			Image image = new Image();
			image.setStorage(storage);
			image.setStorageFilename(imageJson.getStorageFilename());
			image.setExtension(imageJson.getExtension());
			image.setSize(imageJson.getSize());
			image.setHash(imageJson.getHash());
			image.setImportPath(imageJson.getImportPath());
			image.setThumbStatus(imageJson.getThumbStatus());
			image.setResolutionWidth(imageJson.getResolutionWidth());
			image.setResolutionHeight(imageJson.getResolutionHeight());
			image.setResolutionStatus(imageJson.getResolutionStatus());
			image.setCreatedDatetime(imageJson.getCreatedDatetime());

			if (imageJson.getTags() != null)
			{
				for (String tagName : imageJson.getTags())
				{
					Tag tag = tagRepository.findOneByNameIgnoreCase(tagName).orElseThrow();
					image.addTag(tag);
				}
			}

			imageRepository.save(image);

			restoreInfo.worked(1);
		}
	}

	private interface IBackupFileSystemWrapper extends Closeable
	{
		public InputStream getBackupJsonInputStream() throws IOException;
	}

	@RequiredArgsConstructor
	private class BackupJsonFileSystemWrapper implements IBackupFileSystemWrapper
	{
		private final Path backupJsonFilePath;

		@Override
		public void close() throws IOException
		{
			// Nothing to do
		}

		@Override
		public InputStream getBackupJsonInputStream() throws IOException
		{
			return Files.newInputStream(backupJsonFilePath);
		}
	}

	private class BackupZipFileSystemWrapper implements IBackupFileSystemWrapper
	{
		private final FileSystem backupArchiveFileSystem;

		public BackupZipFileSystemWrapper(Path backupZipArchivePath) throws IOException
		{
			backupArchiveFileSystem = FileSystems.newFileSystem(backupZipArchivePath);
		}

		private Path resolveFile(String relativePathToFile)
		{
			return backupArchiveFileSystem.getPath("/" + relativePathToFile);
		}

		@Override
		public void close() throws IOException
		{
			if (backupArchiveFileSystem == null)
				return;
			backupArchiveFileSystem.close();
		}

		@Override
		public InputStream getBackupJsonInputStream() throws IOException
		{
			Path pathToFileInArchive = resolveFile("backup.json");
			return Files.newInputStream(pathToFileInArchive);
		}
	}
}
