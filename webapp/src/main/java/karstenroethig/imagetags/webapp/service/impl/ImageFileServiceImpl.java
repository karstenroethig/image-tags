package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipOutputStream;

import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.config.properties.ImageDataProperties;

@Service
@Transactional
@EnableConfigurationProperties(ImageDataProperties.class)
public class ImageFileServiceImpl
{
	@Autowired
	protected ImageDataProperties imageDataProperties;

	public void saveImage(Path imageFilePath, Long imageId) throws IOException
	{
		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getZipPath(), null))
		{
			String extension = FilenameUtils.getExtension(imageFilePath.getFileName().toString());
			String filename = buildFilename(imageId, extension);
			Path path = fileSystem.getPath("/"+filename);

			Files.copy(imageFilePath, path);
		}
	}

	public byte[] loadImage(Long imageId, String extension) throws IOException
	{
		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getZipPath(), null))
		{
			String filename = buildFilename(imageId, extension);
			Path path = fileSystem.getPath("/"+filename);

			return readDataFromPath(path);
		}
	}

	public void deleteImage(Long imageId, String extension) throws IOException
	{
		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getZipPath(), null))
		{
			String filename = buildFilename(imageId, extension);
			Path path = fileSystem.getPath("/"+filename);

			Files.deleteIfExists(path);
		}
	}

	private void createZipFileIfItDoesNotExist() throws IOException
	{
		if (Files.exists(imageDataProperties.getZipPath()))
		{
			return;
		}

		try (ZipOutputStream out = new ZipOutputStream(
				Files.newOutputStream(
						imageDataProperties.getZipPath(),
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING)))
		{
			out.closeEntry();
		}
	}

	public String buildFilename(Long imageId, String extension)
	{
		return StringUtils.leftPad(imageId.toString(), 12, "0") + "." + extension;
	}

	private byte[] readDataFromPath(Path path) throws IOException
	{
		try (InputStream inputStream = Files.newInputStream(path))
		{
			return IOUtils.toByteArray(inputStream);
		}
	}
}
