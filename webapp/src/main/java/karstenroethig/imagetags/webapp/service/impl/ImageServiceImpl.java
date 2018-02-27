package karstenroethig.imagetags.webapp.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.dto.ImagesSearchDto;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
@Transactional
public class ImageServiceImpl
{
	@Autowired
	protected ImageRepository imageRepository;

	@Autowired
	protected ImageFileServiceImpl imageFileService;

	public List<Long> findImages(ImagesSearchDto searchParams)
	{
		// no tags -> show untagged images
		return findUntaggedImages();

		// TODO tag available -> show images matching tags
	}

	private List<Long> findUntaggedImages()
	{
		Iterable<Image> imagesIterator = imageRepository.findAll();
		Stream<Image> imagesStream = StreamSupport.stream(imagesIterator.spliterator(), false);

		return imagesStream
				.map(image -> image.getId())
				.collect(Collectors.toList());
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

	public void deleteImage(Long imageId)
	{
		Image image = imageRepository.findOne(imageId);

		if (image == null)
		{
			throw new NotFoundException(String.valueOf(imageId));
		}

		try {
			imageFileService.deleteImage(imageId, image.getExtension());
		}
		catch (IOException ex)
		{
			log.error("unable to delete file with id " + imageId);
		}

		imageRepository.delete(imageId);
	}
}
