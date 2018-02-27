package karstenroethig.imagetags.webapp.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import karstenroethig.imagetags.webapp.domain.enums.TagTypeEnum;

public class TagTypeWrapper
{
	private Map<TagTypeEnum, List<TagDto>> tagsByTypeMap = new HashMap<>();

	public void add(TagDto tag)
	{
		List<TagDto> tagsForType;

		if (tagsByTypeMap.containsKey(tag.getType()))
		{
			tagsForType = tagsByTypeMap.get(tag.getType());
		}
		else
		{
			tagsForType = new ArrayList<>();
			tagsByTypeMap.put(tag.getType(), tagsForType);
		}

		tagsForType.add(tag);
	}

	public boolean hasTagsForType(TagTypeEnum type) {
		return tagsByTypeMap.containsKey(type);
	}

	public List<TagDto> getTagsByType(TagTypeEnum type)
	{
		List<TagDto> tags = tagsByTypeMap.get(type);

		if (tags == null) {
			return Collections.<TagDto>emptyList();
		}

		return tags.stream()
			.sorted( Comparator.comparing( TagDto::getName))
			.collect(Collectors.toList());
	}

	public List<TagDto> getAllTags()
	{
		List<TagDto> tags = new ArrayList<>();

		for (List<TagDto> list : tagsByTypeMap.values())
		{
			tags.addAll(list);
		}

		return tags;
	}
}
