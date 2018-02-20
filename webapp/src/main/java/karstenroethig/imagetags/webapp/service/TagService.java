package karstenroethig.imagetags.webapp.service;

import java.util.Collection;

import karstenroethig.imagetags.webapp.dto.TagDto;
import karstenroethig.imagetags.webapp.service.exceptions.TagAlreadyExistsException;

public interface TagService
{
	public TagDto newTag();

	public TagDto saveTag( TagDto tagDto ) throws TagAlreadyExistsException;

	public Boolean deleteTag( Long tagId );

	public TagDto editTag( TagDto tagDto ) throws TagAlreadyExistsException;

	public TagDto findTag( Long tagId );

	public Collection<TagDto> getAllTags();
}
