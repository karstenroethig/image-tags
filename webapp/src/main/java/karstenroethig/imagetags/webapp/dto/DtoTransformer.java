package karstenroethig.imagetags.webapp.dto;

import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.domain.Tag;

public class DtoTransformer
{
	private DtoTransformer() {}

	/*
	 * ===
	 * Tag
	 * ===
	 */

	public static Tag merge(Tag tag, TagDto tagDto)
	{
		if (tag == null || tagDto == null )
		{
			return null;
		}

		tag.setName(tagDto.getName());
		tag.setTypeEnum(tagDto.getType());

		return tag;
	}

	public static TagDto transform(Tag tag)
	{
		if (tag == null)
		{
			return null;
		}

		TagDto tagDto = new TagDto();

		tagDto.setId(tag.getId());
		tagDto.setName(tag.getName());
		tagDto.setType(tag.getTypeEnum());

		return tagDto;
	}

	/*
	 * =====
	 * Image
	 * =====
	 */

	public static ImageDto transform(Image image)
	{
		if (image == null)
		{
			return null;
		}

		ImageDto imageDto = new ImageDto();

		imageDto.setId(image.getId());

		return imageDto;
	}
}
