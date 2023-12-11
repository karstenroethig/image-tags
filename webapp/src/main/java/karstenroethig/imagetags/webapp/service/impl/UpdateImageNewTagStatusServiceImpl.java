package karstenroethig.imagetags.webapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.model.domain.AbstractEntityId_;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.model.enums.ImageNewTagStatusEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class UpdateImageNewTagStatusServiceImpl
{
	@Autowired private ImageServiceImpl imageService;
	@Autowired private TagServiceImpl tagService;

	public void execute()
	{
		TagDto tagNew = tagService.findOrCreateDto(TagServiceImpl.TAG_NEW);

		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setNewTagStatus(ImageNewTagStatusEnum.UNCHECKED);

		Pageable pageRequest = PageRequest.of(0, 10, Direction.ASC, AbstractEntityId_.ID);
		boolean first = true;
		int currentImageCount = 0;

		Page<ImageDto> page = null;

		do
		{
			page = imageService.findBySearchParams(searchParams, pageRequest);

			if (first)
			{
				log.info("start checking of {} images", page.getTotalElements());
				first = false;
			}

			for (ImageDto image : page.getContent())
			{
				currentImageCount++;

				log.info(String.format("checking image [%s/%s]: %s", currentImageCount, page.getTotalElements(), image.getId()));

				if (image.getTags().isEmpty())
					image.addTag(tagNew);
				image.setNewTagStatus(ImageNewTagStatusEnum.CHECKED);

				imageService.update(image);
			}
		}
		while (page != null && !page.getContent().isEmpty());
	}
}