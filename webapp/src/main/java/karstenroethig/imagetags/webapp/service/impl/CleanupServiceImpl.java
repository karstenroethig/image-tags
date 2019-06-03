package karstenroethig.imagetags.webapp.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.dto.ImageSearchDto;
import karstenroethig.imagetags.webapp.dto.TagDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class CleanupServiceImpl
{
	private static final String TAG_FOR_DELETE = "DELETE";

	@Autowired private TagServiceImpl tagService;
	@Autowired private ImageServiceImpl imageService;

	@PostConstruct
	public void execute()
	{
		List<TagDto> tags = tagService.getAllTags();
		Optional<TagDto> deleteTag = tags.stream().filter(tag -> StringUtils.equals(tag.getName(), TAG_FOR_DELETE)).findFirst();

		if (!deleteTag.isPresent())
		{
			log.info("clean-up task is skipped (tag '{}' does not exist)", TAG_FOR_DELETE);
			return;
		}

		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setTags(Stream.of(deleteTag.get()).collect(Collectors.toList()));

		List<Long> imageIds = imageService.findImages(searchParams);

		int totalImageCount = imageIds.size();
		int currentImageCount = 0;

		log.info(String.format("start clean-up of %s images", totalImageCount));

		for (Long imageId : imageIds)
		{
			currentImageCount++;

			log.info(String.format("deleting image [%s/%s]: %s", currentImageCount, totalImageCount, imageId));

			imageService.deleteImage(imageId);
		}
	}
}
