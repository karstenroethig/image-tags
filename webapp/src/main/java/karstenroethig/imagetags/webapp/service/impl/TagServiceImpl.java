package karstenroethig.imagetags.webapp.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.domain.Tag;
import karstenroethig.imagetags.webapp.dto.DtoTransformer;
import karstenroethig.imagetags.webapp.dto.TagDto;
import karstenroethig.imagetags.webapp.dto.TagTypeWrapper;
import karstenroethig.imagetags.webapp.dto.api.TagApiDto;
import karstenroethig.imagetags.webapp.repository.TagRepository;
import karstenroethig.imagetags.webapp.service.exceptions.TagAlreadyExistsException;

@Service
@Transactional
public class TagServiceImpl
{
	@Autowired
	protected JdbcTemplate jdbcTemplate;

	@Autowired
	protected TagRepository tagRepository;

	public TagDto newTag()
	{
		TagDto tagDto = new TagDto();

		return tagDto;
	}

	public TagDto saveTag(TagDto tagDto) throws TagAlreadyExistsException
	{
		List<Tag> existingTags = tagRepository.findByNameIgnoreCase(
			StringUtils.trim(tagDto.getName()));

		if (existingTags != null && existingTags.isEmpty() == false)
		{
			throw new TagAlreadyExistsException();
		}

		Tag tag = new Tag();

		tag = DtoTransformer.merge(tag, tagDto);

		return DtoTransformer.transform(tagRepository.save(tag));
	}

	public Boolean deleteTag(Long tagId)
	{
		Tag temp = tagRepository.findOne(tagId);

		if (temp != null)
		{
			tagRepository.delete(temp);

			return true;
		}

		return false;
	}

	public TagDto editTag(TagDto tagDto) throws TagAlreadyExistsException
	{
		List<Tag> existingTags = tagRepository.findByNameIgnoreCase(
			StringUtils.trim(tagDto.getName()));

		if (existingTags != null
			&& existingTags.isEmpty() == false
			&& existingTags.get(0).getId().equals(tagDto.getId()) == false)
		{
			throw new TagAlreadyExistsException();
		}

		Tag tag = tagRepository.findOne(tagDto.getId());

		tag = DtoTransformer.merge(tag, tagDto);

		return DtoTransformer.transform(tagRepository.save(tag));
	}

	public TagDto findTag(Long tagId)
	{
		return DtoTransformer.transform(tagRepository.findOne(tagId));
	}

	public List<TagDto> getAllTags()
	{
		return transformTags(tagRepository.findAll());
	}

	private List<TagDto> transformTags(Iterable<Tag> tags)
	{
		return transformTags(StreamSupport.stream(tags.spliterator(), false));
	}

	private List<TagDto> transformTags(Stream<Tag> tagsStream)
	{
		List<TagDto> transformedTags = tagsStream
			.map(DtoTransformer::transform )
			.sorted(Comparator.comparing(TagDto::getName ))
			.collect(Collectors.toList());

		return transformedTags;
	}

	public TagTypeWrapper getAllTagsByType()
	{
		TagTypeWrapper tagTypeWrapper = new TagTypeWrapper();

		for (Tag tag : tagRepository.findAll()) {
			tagTypeWrapper.add(DtoTransformer.transform(tag));
		}

		return tagTypeWrapper;
	}

	public List<TagApiDto> findAllTagsWithOccurrences()
	{
		StringBuffer sql = new StringBuffer();

		sql.append("SELECT it.tag_id AS id, t.name AS name, COUNT(it.image_id) AS amount ");
		sql.append("FROM   Image_Tag it ");
		sql.append("JOIN   Tag t ON t.id = it.tag_id ");
		sql.append("GROUP  BY it.tag_id;");

		return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(TagApiDto.class));
	}
}
