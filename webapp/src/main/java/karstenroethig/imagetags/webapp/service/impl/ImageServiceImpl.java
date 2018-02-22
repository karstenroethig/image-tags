package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.dto.DtoTransformer;
import karstenroethig.imagetags.webapp.dto.ImagesPageDto;
import karstenroethig.imagetags.webapp.repository.ImageRepository;

@Service
@Transactional
public class ImageServiceImpl
{
	@Autowired
	protected ImageRepository imageRepository;

	@Autowired
	protected ImageFileServiceImpl imageFileService;

	public ImagesPageDto findImages(Integer page)
	{
		if (page == null)
		{
			page = 1;
		}

		// no tags -> show untagged images
		return findUntaggedImages(page);

		// TODO tag available -> show images matching tags
	}

	private ImagesPageDto findUntaggedImages(Integer page)
	{
		ImagesPageDto imagesPage = new ImagesPageDto();
		int count = 0;

		for (Image image : imageRepository.findAll())
		{
			count++;

			if (count == page)
			{
				imagesPage.setCurrentImage(DtoTransformer.transform(image));
			}
		}

		imagesPage.setCurrentPageNumber(page);
		imagesPage.setMaxPageNumber(count);

		return imagesPage;
	}

	public byte[] getImageData(Long imageId) throws IOException
	{
		Image image = imageRepository.findOne(imageId);

		if (image == null)
		{
			throw new NotFoundException(String.valueOf(imageId));
		}

		return imageFileService.loadImage(image.getId(), image.getExtension());
	}
}
