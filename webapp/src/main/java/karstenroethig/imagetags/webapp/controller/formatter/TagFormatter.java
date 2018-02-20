package karstenroethig.imagetags.webapp.controller.formatter;

import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.Formatter;

import karstenroethig.imagetags.webapp.dto.TagDto;

public class TagFormatter implements Formatter<TagDto>
{
	@Override
	public String print( TagDto tag, Locale locale )
	{
		if ( ( tag == null ) || ( tag.getId() == null ) )
		{
			return StringUtils.EMPTY;
		}

		return tag.getId().toString();
	}

	@Override
	public TagDto parse( String id, Locale locale ) throws ParseException
	{
		if ( StringUtils.isBlank( id ) && ( StringUtils.isNumeric( id ) == false ) )
		{
			return null;
		}

		TagDto tag = new TagDto();

		tag.setId( Long.parseLong( id ) );

		return tag;
	}
}
