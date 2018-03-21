package karstenroethig.imagetags.webapp.service.impl;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.config.properties.ImageDataProperties;
import net.coobird.thumbnailator.Thumbnails;

@Service
@Transactional
@EnableConfigurationProperties(ImageDataProperties.class)
public class ImageFileServiceImpl
{
	@Autowired
	protected ImageDataProperties imageDataProperties;

	public void saveImage(Path imageFilePath, Long imageId) throws IOException
	{
		String extension = FilenameUtils.getExtension(imageFilePath.getFileName().toString());
		String filename = buildFilename(imageId, extension);

		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getZipPath(), null))
		{
			Path path = fileSystem.getPath("/"+filename);
			Files.copy(imageFilePath, path);
		}

		createThumbsZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getThumbsZipPath(), null))
		{
			Path pathThumbnail = fileSystem.getPath("/thumbs/"+filename);
			byte[] imageThumbnailData = createImageThumbnail(imageFilePath);
			Files.copy(new ByteArrayInputStream(imageThumbnailData), pathThumbnail);
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

	public byte[] loadImageThumbnail(Long imageId, String extension) throws IOException
	{
		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getThumbsZipPath(), null))
		{
			String filename = buildFilename(imageId, extension);
			Path path = fileSystem.getPath("/thumbs/"+filename);

			return readDataFromPath(path);
		}
	}

	public void deleteImage(Long imageId, String extension) throws IOException
	{
		String filename = buildFilename(imageId, extension);

		createZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getZipPath(), null))
		{
			Path path = fileSystem.getPath("/"+filename);
			Files.deleteIfExists(path);
		}

		createThumbsZipFileIfItDoesNotExist();

		try (FileSystem fileSystem = FileSystems.newFileSystem(imageDataProperties.getThumbsZipPath(), null))
		{
			Path pathThumbnail = fileSystem.getPath("/thumbs/"+filename);
			Files.deleteIfExists(pathThumbnail);
		}
	}

	public void createZipFileIfItDoesNotExist() throws IOException
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
			out.setLevel(Deflater.NO_COMPRESSION);
			out.closeEntry();
		}
	}

	private void createThumbsZipFileIfItDoesNotExist() throws IOException
	{
		if (Files.exists(imageDataProperties.getThumbsZipPath()))
		{
			return;
		}

		try (ZipOutputStream out = new ZipOutputStream(
				Files.newOutputStream(
						imageDataProperties.getThumbsZipPath(),
						StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING)))
		{
			out.setLevel(Deflater.NO_COMPRESSION);
			out.putNextEntry(new ZipEntry("thumbs/"));
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

	private byte[] createImageThumbnail(Path imageFilePath) throws IOException
	{
		String extension = FilenameUtils.getExtension(imageFilePath.getFileName().toString());
		BufferedImage originalImage = ImageIO.read(imageFilePath.toFile());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Rectangle cropRectangle = calculateCropRectangle(originalImage.getWidth(), originalImage.getHeight());

		if (cropRectangle != null)
		{
			BufferedImage croppedImage = originalImage.getSubimage(cropRectangle.x, cropRectangle.y, cropRectangle.width, cropRectangle.height);
			Thumbnails.of(croppedImage).size(100, 100).outputFormat(extension).toOutputStream(out);
		}
		else
		{
			Thumbnails.of(originalImage).size(100, 100).outputFormat(extension).toOutputStream(out);
		}

		return out.toByteArray();
	}

	private Rectangle calculateCropRectangle(int imageWidth, int imageHeight)
	{
		if (imageWidth == imageHeight)
		{
			return null;
		}

		int x = 0;
		int y = 0;
		int width = Math.min(imageWidth, imageHeight);
		int height = width;

		int diff = Math.abs(imageWidth-imageHeight);

		if (imageWidth > imageHeight)
		{
			x = diff / 2;
		}
		else
		{
			y = diff / 2;
		}

		return new Rectangle(x, y, width, height);
	}
}
