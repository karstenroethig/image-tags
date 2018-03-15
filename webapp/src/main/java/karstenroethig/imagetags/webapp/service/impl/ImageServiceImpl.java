package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.domain.Tag;
import karstenroethig.imagetags.webapp.dto.DtoTransformer;
import karstenroethig.imagetags.webapp.dto.ImageDataDto;
import karstenroethig.imagetags.webapp.dto.ImageDto;
import karstenroethig.imagetags.webapp.dto.ImageSearchDto;
import karstenroethig.imagetags.webapp.dto.TagDto;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import karstenroethig.imagetags.webapp.repository.TagRepository;
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
	protected ImageFileServiceImpl imageFileService;

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
		StringBuffer sql = new StringBuffer();

		sql.append("SELECT i.id ");
		sql.append("FROM   Image i ");
		sql.append("LEFT   JOIN Image_Tag it ON it.image_id = i.id ");
		sql.append("WHERE  it.image_id IS NULL;");

		return jdbcTemplate.queryForList(sql.toString(), Long.class);
	}

	private List<Long> findTaggedImages(List<TagDto> tags)
	{
		List<Long> tagIds = new ArrayList<>();
		StringBuffer sql = new StringBuffer();

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

		sql.append(";");

		return jdbcTemplate.queryForList(sql.toString(), tagIds.toArray(), Long.class);
	}

	public ImageDto findImage(Long imageId)
	{
		return transform(imageRepository.findOne(imageId));
	}

	public ImageDataDto getImageData(Long imageId) throws IOException
	{
		Image image = imageRepository.findOne(imageId);

		if (image == null)
		{
			throw new NotFoundException(String.valueOf(imageId));
		}

		ImageDataDto imageData = new ImageDataDto();
		imageData.setData(imageFileService.loadImage(image.getId(), image.getExtension()));
		imageData.setFilename(imageFileService.buildFilename(imageId, image.getExtension()));
		imageData.setSize(image.getSize());

		return imageData;
	}

	public ImageDataDto getImageThumbnailData(Long imageId) throws IOException
	{
		Image image = imageRepository.findOne(imageId);

		if (image == null)
		{
			throw new NotFoundException(String.valueOf(imageId));
		}

		ImageDataDto imageData = new ImageDataDto();
		imageData.setData(imageFileService.loadImageThumbnail(image.getId(), image.getExtension()));
		imageData.setFilename("thumb." + imageFileService.buildFilename(imageId, image.getExtension()));
		imageData.setSize(new Long(imageData.getData().length));

		return imageData;
	}

	public ImageDto editImage(ImageDto imageDto)
	{
		Image image = imageRepository.findOne(imageDto.getId() );

		image = merge(image, imageDto);

		return transform(imageRepository.save(image));
	}

	private Image merge(Image image, ImageDto imageDto)
	{
		if (image == null || imageDto == null)
		{
			return null;
		}

		image.clearTags();

		for (TagDto tagDto : imageDto.getTags())
		{
			image.addTag(tagRepository.findOne(tagDto.getId()));
		}

		return image;
	}

	private ImageDto transform(Image image)
	{
		if (image == null)
		{
			return null;
		}

		ImageDto imageDto = new ImageDto();

		imageDto.setId(image.getId());
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
		Image image = imageRepository.findOne(imageId);

		if (image == null)
		{
			throw new NotFoundException(String.valueOf(imageId));
		}

		try {
			imageFileService.deleteImage(imageId, image.getExtension());
		}
		catch (IOException ex)
		{
			log.error("unable to delete file with id " + imageId);
		}

		imageRepository.delete(imageId);
	}
}
