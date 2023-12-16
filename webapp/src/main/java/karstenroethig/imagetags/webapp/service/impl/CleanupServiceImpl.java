package karstenroethig.imagetags.webapp.service.impl;

import java.util.List;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class CleanupServiceImpl
{
	@Autowired private ImageServiceImpl imageService;
	@Autowired private TagServiceImpl tagService;

	public void execute()
	{
		TagDto tagDelete = tagService.findOrCreateDto(TagServiceImpl.TAG_DELETE);
		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setTags(List.of(tagDelete));

		Pageable pageRequest = PageRequest.of(0, 50, Direction.ASC, AbstractEntityId_.ID);
		boolean first = true;
		boolean hasNext = false;
		long totalElements = 0;
		long currentImageCount = 0;

		do
		{
			Page<ImageDto> page = imageService.findBySearchParams(searchParams, pageRequest);

			if (first)
			{
				first = false;
				totalElements = page.getTotalElements();
				log.info("start clean-up of {} images", totalElements);
			}

			for (ImageDto image : page.getContent())
			{
				currentImageCount++;

				log.info(String.format("deleting image [%s/%s]: %s", currentImageCount, totalElements, image.getId()));

				imageService.delete(image.getId());
			}

			hasNext = page.hasNext();
		}
		while (hasNext);
	}
}