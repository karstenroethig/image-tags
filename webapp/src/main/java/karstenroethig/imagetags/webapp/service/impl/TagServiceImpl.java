package karstenroethig.imagetags.webapp.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.model.domain.Tag;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.model.enums.TagTypeEnum;
import karstenroethig.imagetags.webapp.repository.TagRepository;
import karstenroethig.imagetags.webapp.repository.specification.TagSpecifications;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.validation.ValidationException;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class TagServiceImpl
{
	public static final String TAG_NEW = "NEW";
	public static final String TAG_DELETE = "DELETE";

	@Autowired private ImageServiceImpl imageService;

	@Autowired private TagRepository tagRepository;

	public TagDto create()
	{
		return new TagDto();
	}

	public ValidationResult validate(TagDto tag)
	{
		ValidationResult result = new ValidationResult();

		if (tag == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(tag));

		return result;
	}

	private void checkValidation(TagDto tag)
	{
		ValidationResult result = validate(tag);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(TagDto tag)
	{
		ValidationResult result = new ValidationResult();

		Tag existing = tagRepository.findOneByNameIgnoreCase(tag.getName()).orElse(null);
		if (existing != null
				&& (tag.getId() == null
				|| !existing.getId().equals(tag.getId())))
			result.addError("name", MessageKeyEnum.COMMON_VALIDATION_ALREADY_EXISTS);

		return result;
	}

	public TagDto save(TagDto tagDto)
	{
		checkValidation(tagDto);

		Tag tag = new Tag();
		merge(tag, tagDto);

		return transform(tagRepository.save(tag));
	}

	public TagDto update(TagDto tagDto)
	{
		checkValidation(tagDto);

		Tag tag = tagRepository.findById(tagDto.getId()).orElse(null);
		if (tag == null)
			return null;

		merge(tag, tagDto);

		return transform(tagRepository.save(tag));
	}

	public ValidationResult validateDelete(TagDto tag)
	{
		ValidationResult result = new ValidationResult();

		if (tag == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		ImageSearchDto imageSearch = new ImageSearchDto();
		imageSearch.setTags(List.of(tag));
		long totalImagesUsedBy = imageService.countBySearchParams(imageSearch);
		if (totalImagesUsedBy > 0)
			result.addError(MessageKeyEnum.TAG_DELETE_INVALID_STILL_IN_USE_BY_IMAGES, totalImagesUsedBy);

		return result;
	}

	private void checkValidationDelete(TagDto tag)
	{
		ValidationResult result = validateDelete(tag);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	public boolean delete(Long id)
	{
		Tag tag = tagRepository.findById(id).orElse(null);
		if (tag == null)
			return false;

		TagDto tagDto = transform(tag);
		checkValidationDelete(tagDto);

		tagRepository.delete(tag);

		return true;
	}

	public long count()
	{
		return tagRepository.count();
	}

	public long count(TagTypeEnum type)
	{
		return tagRepository.count(TagSpecifications.matchesType(type));
	}

	public List<TagDto> findAll()
	{
		Page<Tag> page = tagRepository.findAll(Pageable.unpaged());
		Page<TagDto> pageDto = page.map(this::transform);
		return pageDto.getContent();
	}

	public List<TagDto> findAll(TagTypeEnum type)
	{
		Page<Tag> page = tagRepository.findAll(TagSpecifications.matchesType(type), Pageable.unpaged());
		Page<TagDto> pageDto = page.map(this::transform);
		return pageDto.getContent();
	}

	public Page<TagDto> findAll(Pageable pageable)
	{
		Page<Tag> page = tagRepository.findAll(pageable);
		return page.map(this::transform);
	}

	public TagDto find(Long id)
	{
		return transform(tagRepository.findById(id).orElse(null));
	}

	private Tag merge(Tag tag, TagDto tagDto)
	{
		if (tag == null || tagDto == null )
			return null;

		tag.setName(tagDto.getName());
		tag.setType(tagDto.getType());

		return tag;
	}

	protected TagDto transform(Tag tag)
	{
		if (tag == null)
			return null;

		TagDto tagDto = new TagDto();

		tagDto.setId(tag.getId());
		tagDto.setName(tag.getName());
		tagDto.setType(tag.getType());

		return tagDto;
	}

	protected Tag transform(TagDto tagDto)
	{
		if (tagDto == null || tagDto.getId() == null)
			return null;

		return tagRepository.findById(tagDto.getId()).orElse(null);
	}

	protected boolean equals(TagDto tag1, TagDto tag2)
	{
		if (tag1 == null && tag2 == null)
			return true;

		if (tag1 == null || tag2 == null)
			return false;

		return Objects.equals(tag1.getId(), tag2.getId());
	}

	public Tag findOrCreate(String name)
	{
		Tag tag = tagRepository.findOneByNameIgnoreCase(name).orElse(null);

		if (tag != null)
			return tag;

		tag = new Tag();
		tag.setName(name);
		tag.setType(TagTypeEnum.CATEGORY);

		return tagRepository.save(tag);
	}

	public TagDto findOrCreateDto(String name)
	{
		Tag tag = findOrCreate(name);
		return transform(tag);
	}
}
