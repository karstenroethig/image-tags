package karstenroethig.imagetags.webapp.service.impl;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.coobird.thumbnailator.Thumbnails;

@Service
@Transactional
public class ImageOperationServiceImpl
{
	private static final int THUMBNAIL_SIZE = 100;

	public byte[] createImageThumbnail(Path imageFilePath) throws IOException
	{
		String extension = FilenameUtils.getExtension(imageFilePath.getFileName().toString());
		BufferedImage originalImage = ImageIO.read(imageFilePath.toFile());

		return createImageThumbnail(originalImage, extension);
	}

	public byte[] createImageThumbnail(byte[] imageData, String extension) throws IOException
	{
		ByteArrayInputStream input = new ByteArrayInputStream(imageData);
		BufferedImage image = ImageIO.read(input);

		return createImageThumbnail(image, extension);
	}

	private byte[] createImageThumbnail(BufferedImage originalImage, String extension) throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Rectangle cropRectangle = calculateCropRectangle(originalImage.getWidth(), originalImage.getHeight());

		if (cropRectangle != null)
		{
			BufferedImage croppedImage = originalImage.getSubimage(cropRectangle.x, cropRectangle.y, cropRectangle.width, cropRectangle.height);
			Thumbnails.of(croppedImage).size(THUMBNAIL_SIZE, THUMBNAIL_SIZE).outputFormat(extension).toOutputStream(out);
		}
		else
		{
			Thumbnails.of(originalImage).size(THUMBNAIL_SIZE, THUMBNAIL_SIZE).outputFormat(extension).toOutputStream(out);
		}

		return out.toByteArray();
	}

	public Point resolveImageResolution(Path imageFilePath) throws IOException
	{
		BufferedImage image = ImageIO.read(imageFilePath.toFile());

		return resolveImageResolution(image);
	}

	public Point resolveImageResolution(byte[] imageData) throws IOException
	{
		ByteArrayInputStream input = new ByteArrayInputStream(imageData);
		BufferedImage image = ImageIO.read(input);

		return resolveImageResolution(image);
	}

	private Point resolveImageResolution(BufferedImage image) throws IOException
	{
		int width = image.getWidth();
		int height = image.getHeight();

		return new Point(width, height);
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