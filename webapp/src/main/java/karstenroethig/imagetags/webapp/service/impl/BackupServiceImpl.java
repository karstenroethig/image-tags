package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.config.ApplicationProperties;
import karstenroethig.imagetags.webapp.config.ApplicationProperties.BackupSettings;
import karstenroethig.imagetags.webapp.config.AsyncTaskExecutorConfig;
import karstenroethig.imagetags.webapp.model.domain.AbstractEntityId_;
import karstenroethig.imagetags.webapp.model.domain.Album;
import karstenroethig.imagetags.webapp.model.domain.Image;
import karstenroethig.imagetags.webapp.model.domain.Storage;
import karstenroethig.imagetags.webapp.model.domain.Tag;
import karstenroethig.imagetags.webapp.model.dto.backup.BackupInfoDto;
import karstenroethig.imagetags.webapp.model.json.AlbumJson;
import karstenroethig.imagetags.webapp.model.json.BackupJson;
import karstenroethig.imagetags.webapp.model.json.ImageJson;
import karstenroethig.imagetags.webapp.model.json.StorageJson;
import karstenroethig.imagetags.webapp.model.json.TagJson;
import karstenroethig.imagetags.webapp.repository.AlbumRepository;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import karstenroethig.imagetags.webapp.repository.StorageRepository;
import karstenroethig.imagetags.webapp.repository.TagRepository;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class BackupServiceImpl
{
	private static BackupInfoDto backupInfo = new BackupInfoDto();

	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private TagRepository tagRepository;
	@Autowired private StorageRepository storageRepository;
	@Autowired private ImageRepository imageRepository;
	@Autowired private AlbumRepository albumRepository;

	public BackupInfoDto getBackupInfo()
	{
		return backupInfo;
	}

	protected boolean blockBackupProcess()
	{
		if (backupInfo.isRunning())
			return false;

		backupInfo.intializeNewBackup();
		return true;
	}

	@Async(AsyncTaskExecutorConfig.BACKUP_TASK_EXECUTOR)
	protected void performBackupAsync()
	{
		Path backupArchivePath = null;

		try
		{
			determineTotalWork();

			backupArchivePath = createBackupArchive();

			BackupJson backup = new BackupJson();
			backup.setTags(convertTags());
			backup.setAlbums(convertAlbums());
			backup.setStorages(convertStorages());
			backup.setImages(convertImages());

			writeBackupToBackupArchive(backupArchivePath, backup);
		}
		catch (Exception ex)
		{
			log.error("error on backup process", ex);

			if (backupArchivePath != null)
			{
				try
				{
					Files.delete(backupArchivePath);
				}
				catch (Exception e)
				{
					log.error("error deleting corrupt backup archive", e);
				}
			}
		}
		finally
		{
			backupInfo.done();
		}
	}

	private void determineTotalWork()
	{
		int totalWork = 0;
		totalWork += tagRepository.count();
		totalWork += albumRepository.count();
		totalWork += storageRepository.count();
		totalWork += imageRepository.count();

		backupInfo.setTotalWork(totalWork);
	}

	private Path createBackupArchive() throws IOException
	{
		BackupSettings backupSettings = applicationProperties.getBackup();
		Path backupDirectoryPath = backupSettings.getBackupDirectory();
		Files.createDirectories(backupDirectoryPath);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(backupSettings.getBackupFileDatePattern());
		String backupFilenameDate = backupInfo.getStartedDatetime().format(dateTimeFormatter);
		String backupFilename = String.format("%s%s.zip", backupSettings.getBackupFilePrefix(), backupFilenameDate);
		Path backupArchivePath = backupDirectoryPath.resolve(backupFilename);

		try (ZipOutputStream out = new ZipOutputStream(
				Files.newOutputStream(
						backupArchivePath,
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING)))
		{
			out.setLevel(Deflater.NO_COMPRESSION);
			out.closeEntry();
		}

		return backupArchivePath;
	}

	private List<TagJson> convertTags()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_TAGS, (int)tagRepository.count());

		List<TagJson> tags = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<Tag> page = tagRepository.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (tags == null)
				tags = new ArrayList<>();

			for (Tag tag : page.getContent())
			{
				TagJson tagJson = convertTag(tag);
				if (tagJson != null)
					tags.add(tagJson);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return tags;
	}

	private TagJson convertTag(Tag tag)
	{
		if (tag == null)
			return null;

		TagJson tagJson = new TagJson();
		tagJson.setName(tag.getName());
		tagJson.setType(tag.getType());

		return tagJson;
	}

	private List<AlbumJson> convertAlbums()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_ALBUMS, (int)albumRepository.count());

		List<AlbumJson> albums = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<Album> page = albumRepository.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (albums == null)
				albums = new ArrayList<>();

			for (Album album : page.getContent())
			{
				AlbumJson albumJson = convertAlbum(album);
				if (albumJson != null)
					albums.add(albumJson);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return albums;
	}

	private AlbumJson convertAlbum(Album album)
	{
		if (album == null)
			return null;

		AlbumJson albumJson = new AlbumJson();
		albumJson.setName(album.getName());
		albumJson.setAuthor(album.getAuthor());

		return albumJson;
	}

	private List<StorageJson> convertStorages()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_STORAGES, (int)storageRepository.count());

		List<StorageJson> storages = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<Storage> page = storageRepository.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (storages == null)
				storages = new ArrayList<>();

			for (Storage storage : page.getContent())
			{
				StorageJson storageJson = convertStorage(storage);
				if (storageJson != null)
					storages.add(storageJson);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return storages;
	}

	private StorageJson convertStorage(Storage storage)
	{
		if (storage == null)
			return null;

		StorageJson storageJson = new StorageJson();
		storageJson.setKey(storage.getKey());
		storageJson.setSize(storage.getSize());

		return storageJson;
	}

	private List<ImageJson> convertImages()
	{
		backupInfo.beginTask(MessageKeyEnum.BACKUP_TASK_EXPORT_IMAGES, (int)imageRepository.count());

		List<ImageJson> images = null;

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);

		do
		{
			Page<Image> page = imageRepository.findAll(pageRequest);

			if (!page.hasContent())
				break;

			if (images == null)
				images = new ArrayList<>();

			for (Image image : page.getContent())
			{
				ImageJson imageJson = convertImage(image);
				if (imageJson != null)
					images.add(imageJson);

				backupInfo.worked(1);
			}

			if (page.hasNext())
				pageRequest = page.nextPageable();
			else
				pageRequest = null;
		}
		while (pageRequest != null);

		return images;
	}

	private ImageJson convertImage(Image image)
	{
		if (image == null)
			return null;

		ImageJson imageJson = new ImageJson();
		imageJson.setStorageKey(image.getStorage().getKey());
		imageJson.setStorageFilename(image.getStorageFilename());
		imageJson.setExtension(image.getExtension());
		imageJson.setSize(image.getSize());
		imageJson.setHash(image.getHash());
		imageJson.setImportPath(image.getImportPath());
		imageJson.setThumbStatus(image.getThumbStatus());
		imageJson.setResolutionWidth(image.getResolutionWidth());
		imageJson.setResolutionHeight(image.getResolutionHeight());
		imageJson.setResolutionStatus(image.getResolutionStatus());
		imageJson.setCreatedDatetime(image.getCreatedDatetime());

		List<String> tags = image.getTags().stream()
			.map(Tag::getName)
			.toList();
		if (!tags.isEmpty())
			imageJson.setTags(tags);

		if (image.getAlbum() != null)
		{
			imageJson.setAlbum(image.getAlbum().getName());
			imageJson.setAlbumPage(image.getAlbumPage());
		}

		return imageJson;
	}

	private void writeBackupToBackupArchive(Path backupArchivePath, BackupJson backup) throws IOException
	{
		ObjectWriter objectWriter = new ObjectMapper()
			.registerModule(new JavaTimeModule())
 			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.writer()
			.withDefaultPrettyPrinter();

		try (FileSystem backupArchiveFileSystem = FileSystems.newFileSystem(backupArchivePath))
		{
			Path pathToFileInArchive = backupArchiveFileSystem.getPath("/backup.json");

			try (OutputStream out = Files.newOutputStream(pathToFileInArchive))
			{
				objectWriter.writeValue(out, backup);
			}
		}
	}
}
