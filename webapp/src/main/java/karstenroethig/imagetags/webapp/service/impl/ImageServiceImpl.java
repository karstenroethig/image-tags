package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.domain.Tag;
import karstenroethig.imagetags.webapp.domain.enums.ImageThumbStatusEnum;
import karstenroethig.imagetags.webapp.dto.DtoTransformer;
import karstenroethig.imagetags.webapp.dto.ImageDataDto;
import karstenroethig.imagetags.webapp.dto.ImageDto;
import karstenroethig.imagetags.webapp.dto.ImageSearchDto;
import karstenroethig.imagetags.webapp.dto.TagDto;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import karstenroethig.imagetags.webapp.repository.TagRepository;
import karstenroethig.imagetags.webapp.util.FilesizeUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class ImageServiceImpl
{
	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Autowired
	protected ImageRepository imageRepository;

	@Autowired
	protected TagRepository tagRepository;

	@Autowired
	protected StorageServiceImpl storageService;

	public List<Long> findImages(ImageSearchDto searchParams)
	{
		// tags available -> show images matching tags
		if (searchParams != null
			&& searchParams.getTags() != null
			&& !searchParams.getTags().isEmpty())
		{
			return findTaggedImages(searchParams.getTags());
		}

		// no tags -> show untagged images
		return findUntaggedImages();
	}

	private List<Long> findUntaggedImages()
	{
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT i.id ");
		sql.append("FROM   Image i ");
		sql.append("LEFT   JOIN Image_Tag it ON it.image_id = i.id ");
		sql.append("WHERE  it.image_id IS NULL ");
		sql.append("ORDER  BY i.created_date DESC;");

		return jdbcTemplate.queryForList(sql.toString(), Long.class);
	}

	private List<Long> findTaggedImages(List<TagDto> tags)
	{
		List<Long> tagIds = new ArrayList<>();
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT i.id ");
		sql.append("FROM   Image i ");
		sql.append("WHERE  1=1 ");

		for (TagDto tag : tags)
		{
			tagIds.add(tag.getId());

			sql.append("AND ");
			sql.append("EXISTS ( ");
			sql.append("    SELECT it.image_id ");
			sql.append("    FROM   image_tag it ");
			sql.append("    WHERE  i.id = it.image_id AND it.tag_id = ? ");
			sql.append(") ");
		}

		sql.append("ORDER  BY i.created_date DESC;");

		return jdbcTemplate.queryForList(sql.toString(), tagIds.toArray(), Long.class);
	}

	public Long findTotalImages()
	{
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT COUNT(id) ");
		sql.append("FROM   Image;");

		return jdbcTemplate.queryForObject(sql.toString(), Long.class);
	}

	public Long findTotalFilesize()
	{
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT SUM(file_size) ");
		sql.append("FROM   Image;");

		Long totalFilesize = jdbcTemplate.queryForObject(sql.toString(), Long.class);

		return totalFilesize != null ? totalFilesize : 0l;
	}

	public ImageDto findImage(Long imageId)
	{
		return transform(imageRepository.findById(imageId).orElse(null));
	}

	public ImageDataDto getImageData(Long imageId, boolean thumbnail) throws IOException
	{
		Image image = imageRepository.findById(imageId).orElse(null);

		if (image == null)
			throw new NotFoundException(String.valueOf(imageId));

		String storageKey = image.getStorage() == null ? null : image.getStorage().getKey();

		ImageDataDto imageData = new ImageDataDto();

		if (!thumbnail || image.getThumbStatusEnum() == ImageThumbStatusEnum.THUMB_100_100)
		{
			imageData.setData(storageService.loadImage(image.getId(), image.getExtension(), storageKey, thumbnail));
		}
		else
		{
			try(InputStream input = ImageServiceImpl.class.getResourceAsStream("thumb_error.png"))
			{
				imageData.setData(IOUtils.toByteArray(input));
			}
		}

		imageData.setFilename(
			(thumbnail ? "thumb." : StringUtils.EMPTY)
			+ storageService.buildImageFilename(imageId, image.getExtension()));
		imageData.setSize(thumbnail ? Long.valueOf(imageData.getData().length) : image.getSize());

		return imageData;
	}

	public ImageDto editImage(ImageDto imageDto)
	{
		Image image = imageRepository.findById(imageDto.getId()).orElse(null);

		image = merge(image, imageDto);

		return transform(imageRepository.save(image));
	}

	private Image merge(Image image, ImageDto imageDto)
	{
		if (image == null || imageDto == null)
			return null;

		image.clearTags();

		for (TagDto tagDto : imageDto.getTags())
		{
			image.addTag(tagRepository.findById(tagDto.getId()).orElse(null));
		}

		return image;
	}

	private ImageDto transform(Image image)
	{
		if (image == null)
			return null;

		ImageDto imageDto = new ImageDto();

		imageDto.setId(image.getId());
		imageDto.setSizeFormatted(FilesizeUtils.formatFilesize(image.getSize()));
		imageDto.setResolutionWidth(image.getResolutionWidth());
		imageDto.setResolutionHeight(image.getResolutionHeight());
		imageDto.setImportPath(image.getImportPath());

		Set<Tag> tags = image.getTags();

		if (tags != null && !tags.isEmpty())
		{
			for (Tag tag : tags)
			{
				imageDto.addTag(DtoTransformer.transform(tag));
			}
		}

		return imageDto;
	}

	public void deleteImage(Long imageId)
	{
		Image image = imageRepository.findById(imageId).orElse(null);

		if (image == null)
			throw new NotFoundException(String.valueOf(imageId));

		String storageKey = image.getStorage() == null ? null : image.getStorage().getKey();

		try {
			storageService.deleteImage(imageId, image.getExtension(), storageKey);
		}
		catch (IOException ex)
		{
			log.error("unable to delete file with id " + imageId);
		}

		if (image.getStorage() != null)
		{
			storageService.subtractAndSaveFilesize(image.getStorage().getId(), image.getSize());
		}

		imageRepository.deleteById(imageId);
	}
}
