package karstenroethig.imagetags.webapp.controller.formatter;

import karstenroethig.imagetags.webapp.model.dto.TagDto;

public class TagFormatter extends AbstractIdFormatter<TagDto>
{
	@Override
	protected TagDto createInstance()
	{
		return new TagDto();
	}
}
