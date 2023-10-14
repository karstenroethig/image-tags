package karstenroethig.imagetags.webapp.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.model.domain.Image;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import karstenroethig.imagetags.webapp.repository.specification.ImageSpecifications;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.validation.ValidationException;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class ImageServiceImpl
{
	@Autowired private ImageRepository imageRepository;

	public ImageDto create()
	{
		ImageDto image = new ImageDto();

		return image;
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

		List<Image> technologies = imageRepository.findAll(ImageSpecifications.matchesUniqueProperties(image.getId(), image.getTitle()));
		if (technologies != null && !technologies.isEmpty())
			result.addError("title", MessageKeyEnum.COMMON_VALIDATION_ALREADY_EXISTS);

		return result;
	}

	public ImageDto save(ImageDto imageDto)
	{
		checkValidation(imageDto);

		Image image = new Image();
		merge(image, imageDto);

		// TODO: LocalDateTime now = DateUtils.normalizedNowForDatabase();
		// image.setCreatedDatetime(now);

		Image savedImage = imageRepository.save(image);

		return transform(savedImage);
	}

	public ImageDto update(ImageDto imageDto)
	{
		checkValidation(imageDto);

		List<Image> technologies = imageRepository.findAll(ImageSpecifications.matchesId(imageDto.getId()));
		Image image = technologies.stream().findFirst().orElse(null);
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

	public Page<ImageDto> findAll(Pageable pageable)
	{
		Page<Image> page = imageRepository.findAll(pageable);
		return page.map(this::transform);
	}

	public ImageDto find(Long id)
	{
		List<Image> technologies = imageRepository.findAll(
			Specification.where(ImageSpecifications.matchesId(id)));
		return transform(technologies.stream().findAny().orElse(null));
	}

	private Image merge(Image image, ImageDto imageDto)
	{
		if (image == null || imageDto == null )
			return null;

		image.setTitle(imageDto.getTitle());
		image.setDescription(imageDto.getDescription());

		return image;
	}

	protected ImageDto transform(Image image)
	{
		if (image == null)
			return null;

		ImageDto imageDto = new ImageDto();

		imageDto.setId(image.getId());
		imageDto.setTitle(image.getTitle());
		imageDto.setDescription(image.getDescription());

		return imageDto;
	}
}
