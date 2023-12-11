package karstenroethig.imagetags.webapp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.model.domain.AbstractEntityId_;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.model.enums.ImageThumbStatusEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class UpdateImageThumbStatusServiceImpl
{
	@Autowired private ImageServiceImpl imageService;
	@Autowired private ImageOperationServiceImpl imageOperationService;
	@Autowired private StorageServiceImpl storageService;

	public void execute()
	{
		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setThumbStatus(ImageThumbStatusEnum.THUMB_100_100);

		Pageable pageRequest = PageRequest.of(0, 10, Direction.ASC, AbstractEntityId_.ID);
		boolean first = true;
		int currentImageCount = 0;

		Page<ImageDto> page = null;

		do
		{
			page = imageService.findBySearchParams(searchParams, pageRequest);

			if (first)
			{
				log.info("start regenerating thumb of {} images", page.getTotalElements());
				first = false;
			}

			for (ImageDto image : page.getContent())
			{
				currentImageCount++;

				log.info(String.format("regenerating thumb of image [%s/%s]: %s", currentImageCount, page.getTotalElements(), image.getId()));

				try
				{
					Resource fileResource = storageService.loadAsResource(image, false);
					byte[] thumbData = imageOperationService.createImageThumbnail(fileResource.getContentAsByteArray(), image.getExtension());
					storageService.saveImage(thumbData, image.getStorageFilename(), image.getStorage(), true);
					image.setThumbStatus(ImageThumbStatusEnum.THUMB_120_120);
				}
				catch (Exception ex)
				{
					log.warn(String.format("failed to generate thumbnail of image %s", image.getId()), ex);
					image.setThumbStatus(ImageThumbStatusEnum.GENERATION_ERROR);
				}

				imageService.update(image);
			}
		}
		while (page != null && !page.getContent().isEmpty());
	}
}