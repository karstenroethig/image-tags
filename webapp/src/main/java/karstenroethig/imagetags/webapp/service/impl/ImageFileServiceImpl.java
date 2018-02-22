package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipOutputStream;

import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ImageFileServiceImpl
{
	private static final Path IMAGES_ZIP_PATH = Paths.get("data/images.zip");

	public void saveImage(Path imageFilePath, Long imageId) throws IOException
	{
		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(IMAGES_ZIP_PATH, null))
		{
			String extension = FilenameUtils.getExtension(imageFilePath.getFileName().toString());
			String filename = createFilename(imageId, extension);
			Path path = fileSystem.getPath("/"+filename);

			Files.copy(imageFilePath, path);
		}
	}

	public byte[] loadImage(Long imageId, String extension) throws IOException
	{
		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(IMAGES_ZIP_PATH, null))
		{
			String filename = createFilename(imageId, extension);
			Path path = fileSystem.getPath("/"+filename);

			return readDataFromPath(path);
		}
	}

	public void deleteImage(Long imageId, String extension) throws IOException
	{
		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(IMAGES_ZIP_PATH, null))
		{
			String filename = createFilename(imageId, extension);
			Path path = fileSystem.getPath("/"+filename);

			Files.deleteIfExists(path);
		}
	}

	private void createZipFileIfItDoesNotExist() throws IOException
	{
		if (Files.exists(IMAGES_ZIP_PATH))
		{
			return;
		}

		try (ZipOutputStream out = new ZipOutputStream(
				Files.newOutputStream(
						IMAGES_ZIP_PATH,
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING)))
		{
			out.closeEntry();
		}
	}

	private String createFilename(Long imageId, String extension)
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
