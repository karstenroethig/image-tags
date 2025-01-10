package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.model.domain.Image;
import karstenroethig.imagetags.webapp.model.domain.Image_;
import karstenroethig.imagetags.webapp.model.domain.Tag;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import karstenroethig.imagetags.webapp.repository.specification.ImageSpecifications;
import karstenroethig.imagetags.webapp.util.DateUtils;
import karstenroethig.imagetags.webapp.util.FilesizeUtils;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.validation.ValidationException;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class ImageServiceImpl
{
	@Autowired private TagServiceImpl tagService;
	@Autowired private StorageServiceImpl storageService;

	@Autowired private ImageRepository imageRepository;

	@Autowired private EntityManager entityManager;

	public ImageDto create()
	{
		return new ImageDto();
	}

	public ValidationResult validate(ImageDto image)
	{
		ValidationResult result = new ValidationResult();

		if (image == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(image));

		return result;
	}

	private void checkValidation(ImageDto image)
	{
		ValidationResult result = validate(image);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	public ValidationResult validateUniqueness(ImageDto image)
	{
		ValidationResult result = new ValidationResult();

		List<Image> images = imageRepository.findAll(ImageSpecifications.matchesUniqueProperties(image.getId(), image.getHash()));
		if (images != null && !images.isEmpty())
			result.addError("hash", MessageKeyEnum.COMMON_VALIDATION_ALREADY_EXISTS);

		return result;
	}

	public ImageDto save(ImageDto imageDto)
	{
		checkValidation(imageDto);

		Image image = new Image();
		merge(image, imageDto);

		LocalDateTime now = DateUtils.normalizedNowForDatabase();
		image.setCreatedDatetime(now);

		Image savedImage = imageRepository.save(image);

		return transform(savedImage);
	}

	public ImageDto update(ImageDto imageDto)
	{
		checkValidation(imageDto);

		List<Image> images = imageRepository.findAll(ImageSpecifications.matchesId(imageDto.getId()));
		Image image = images.stream().findFirst().orElse(null);
		//Image image = imageRepository.findById(imageDto.getId()).orElse(null);
		if (image == null)
			return null;

		merge(image, imageDto);

		Image updatedImage = imageRepository.save(image);

		return transform(updatedImage);
	}

	public boolean delete(Long id)
	{
		Image image = imageRepository.findById(id).orElse(null);
		if (image == null)
			return false;

		try {
			storageService.deleteImage(image);
		}
		catch (IOException ex)
		{
			log.error("unable to delete file with id " + image.getId());
		}

		imageRepository.delete(image);

		return true;
	}

	public long count()
	{
		return imageRepository.count();
	}

	public long countBySearchParams(ImageSearchDto imageSearchDto)
	{
		return imageRepository.count(ImageSpecifications.matchesSearchParam(imageSearchDto));
	}

	public Page<ImageDto> findBySearchParams(ImageSearchDto imageSearchDto, Pageable pageable)
	{
		Page<Image> page = imageRepository.findAll(
			Specification.where(ImageSpecifications.matchesSearchParam(imageSearchDto)), pageable);
		return page.map(this::transform);
	}

	public String findSizeBySearchParams(ImageSearchDto imageSearchDto)
	{
		Specification<Image> specification = ImageSpecifications.matchesSearchParam(imageSearchDto);
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Image> root = query.from(Image.class);
		Predicate predicate = specification.toPredicate(root, query, builder);
		if (predicate != null)
			query.where(predicate);
		Long fileSize = entityManager.createQuery(query.select(builder.sum(root.<Long>get(Image_.SIZE)))).getSingleResult();

		return FilesizeUtils.formatFilesize(fileSize != null ? fileSize : 0);
	}

	public Page<ImageDto> findAll(Pageable pageable)
	{
		Page<Image> page = imageRepository.findAll(pageable);
		return page.map(this::transform);
	}

	public ImageDto find(Long id)
	{
		List<Image> images = imageRepository.findAll(
			Specification.where(ImageSpecifications.matchesId(id)));
		return transform(images.stream().findAny().orElse(null));
	}

	private Image merge(Image image, ImageDto imageDto)
	{
		if (image == null || imageDto == null )
			return null;

		image.setExtension(imageDto.getExtension());
		image.setSize(imageDto.getSize());
		image.setHash(imageDto.getHash());
		image.setDescription(imageDto.getDescription());
		image.setThumbStatus(imageDto.getThumbStatus());
		image.setResolutionWidth(imageDto.getResolutionWidth());
		image.setResolutionHeight(imageDto.getResolutionHeight());
		image.setResolutionStatus(imageDto.getResolutionStatus());

		mergeTags(image, imageDto);

		return image;
	}

	private Image mergeTags(Image image, ImageDto imageDto)
	{
		// delete unassigned tags
		List<Tag> previousAssignedTags = new ArrayList<>(image.getTags());
		List<Long> newlyAssignedTagIds = imageDto.getTags().stream().map(TagDto::getId).collect(Collectors.toList());
		Set<Long> alreadyAssignedTagIds = new HashSet<>();

		for (Tag tag : previousAssignedTags)
		{
			if (newlyAssignedTagIds.contains(tag.getId()))
				alreadyAssignedTagIds.add(tag.getId());
			else
				image.removeTag(tag);
		}

		// add new assigned tags
		for (TagDto tag : imageDto.getTags())
		{
			if (!alreadyAssignedTagIds.contains(tag.getId()))
				image.addTag(tagService.transform(tag));
		}

		// assign the NEW tag if no tags have been set
		if (image.getTags().isEmpty())
		{
			Tag tagNew = tagService.findOrCreate(TagServiceImpl.TAG_NEW);
			image.addTag(tagNew);
		}

		return image;
	}

	protected ImageDto transform(Image image)
	{
		if (image == null)
			return null;

		ImageDto imageDto = new ImageDto();

		imageDto.setId(image.getId());
		imageDto.setStorage(storageService.transform(image.getStorage()));
		imageDto.setStorageFilename(image.getStorageFilename());
		imageDto.setExtension(image.getExtension());
		imageDto.setSize(image.getSize());
		imageDto.setSizeFormatted(FilesizeUtils.formatFilesize(image.getSize()));
		imageDto.setHash(image.getHash());
		imageDto.setDescription(image.getDescription());
		imageDto.setThumbStatus(image.getThumbStatus());
		imageDto.setResolutionWidth(image.getResolutionWidth());
		imageDto.setResolutionHeight(image.getResolutionHeight());
		imageDto.setResolutionStatus(image.getResolutionStatus());
		imageDto.setCreatedDatetime(image.getCreatedDatetime());

		Set<Tag> tags = image.getTags();
		if (tags != null && !tags.isEmpty())
			for (Tag tag : tags)
				imageDto.addTag(tagService.transform(tag));

		return imageDto;
	}
}
