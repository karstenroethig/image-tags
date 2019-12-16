package karstenroethig.imagetags.webapp.service.impl;

import java.awt.Point;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.domain.Image;
import karstenroethig.imagetags.webapp.domain.enums.ImageResolutionStatusEnum;
import karstenroethig.imagetags.webapp.domain.enums.ImageThumbStatusEnum;
import karstenroethig.imagetags.webapp.dto.ImageDataDto;
import karstenroethig.imagetags.webapp.repository.ImageRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@Service
public class UpgradeServiceImpl implements DisposableBean
{
	@Autowired
	private ImageServiceImpl imageService;

	@Autowired
	private ImageOperationServiceImpl imageOperationService;

	@Autowired
	private StorageServiceImpl storageService;

	@Autowired
	private ImageRepository imageRepository;

	private ExecutorService upgradeThreadPool;

	/**
	 * Creates the thread pool and executes the thread for the upgrades. Its called on startup of the application.
	 */
	@PostConstruct
	public void execute()
	{
		log.info("starting thread pool for upgrades");

		upgradeThreadPool = Executors.newSingleThreadExecutor();
		upgradeThreadPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					upgradeForVersion1_1();
					upgradeForVersion1_2();
					upgradeForVersion1_5();
				}
				catch (Exception ex)
				{
					log.error("error on executing upgrades", ex);
				}
			}
		});

		log.info("thread pool for upgrades has been started");
	}

	/**
	 * Shutdown the thread executors. Its called on shutdown of the application.
	 */
	@Override
	public void destroy() throws Exception
	{
		log.info("terminating thread pool for upgrades");

		if (upgradeThreadPool != null )
		{
			upgradeThreadPool.shutdown();
		}

		log.info("thread pool for upgrades has been terminated");
	}

	private void upgradeForVersion1_1()
	{
		String step = "upgrades for version 1.1";
		log.info(step + " :: starting");

		/*
		 * ================================================================================
		 * generate thumbnail for all images with thumb_status "NO_THUMB"
		 * ================================================================================
		 */

		List<Image> imagesWithoutThumbnail = imageRepository.findByThumbStatus(ImageThumbStatusEnum.NO_THUMB.getKey());

		int totalImageCount = imagesWithoutThumbnail.size();
		int currentImageCount = 0;

		log.info(String.format(step + " :: start thumbnail upgrade of %s images", totalImageCount));

		for (Image image : imagesWithoutThumbnail)
		{
			currentImageCount++;

			log.info(String.format(step + " :: generating thumbnail of image file [%s/%s]: %s", currentImageCount, totalImageCount, image.getId()));

			try
			{
				ImageDataDto imageData = imageService.getImageData(image.getId(), false);
				byte[] thumbData = imageOperationService.createImageThumbnail(imageData.getData(), image.getExtension());

				String storageKey = image.getStorage() != null ? image.getStorage().getKey() : null;
				Path storagePath = storageService.createStoragePath(storageKey, true);
				FileSystem fileSystemThumbs = FileSystems.newFileSystem(storagePath, null);

				storageService.saveImage(thumbData, image.getId(), image.getExtension(), fileSystemThumbs, true);
				image.setThumbStatusEnum(ImageThumbStatusEnum.THUMB_100_100);
			}
			catch (Exception ex)
			{
				log.warn(String.format("failed to generate thumbnail of image %s", image.getId()), ex);
				image.setThumbStatusEnum(ImageThumbStatusEnum.GENERATION_ERROR);
			}

			imageRepository.save(image);
		}
	}

	private void upgradeForVersion1_2()
	{
		String step = "upgrades for version 1.2";
		log.info(step + " :: starting");

		/*
		 * ================================================================================
		 * resolve image resolution for all images with resolution_status "NO_RESOLUTION"
		 * ================================================================================
		 */

		List<Image> imagesWithoutResolution = imageRepository.findByResolutionStatus(ImageResolutionStatusEnum.NO_RESOLUTION.getKey());

		int totalImageCount = imagesWithoutResolution.size();
		int currentImageCount = 0;

		log.info(String.format(step + " :: start resolution upgrade of %s images", totalImageCount));

		for (Image image : imagesWithoutResolution)
		{
			currentImageCount++;

			log.info(String.format(step + " :: resolving resolution of image file [%s/%s]: %s", currentImageCount, totalImageCount, image.getId()));

			try
			{
				ImageDataDto imageData = imageService.getImageData(image.getId(), false);
				Point resolution = imageOperationService.resolveImageResolution(imageData.getData());

				image.setResolutionWidth(resolution.x);
				image.setResolutionHeight(resolution.y);
				image.setResolutionStatusEnum(ImageResolutionStatusEnum.GENERATION_SUCCESS);
			}
			catch (Exception ex)
			{
				log.error(String.format(step + " :: error on resolving resolution of image: %s", image.getId()), ex);
				image.setResolutionStatusEnum(ImageResolutionStatusEnum.GENERATION_ERROR);
			}

			imageRepository.save(image);
		}
	}

	private void upgradeForVersion1_5()
	{
		String step = "upgrades for version 1.5";
		log.info(step + " :: starting");

		/*
		 * ================================================================================
		 * set a creation date for each image that does not yet have one
		 * ================================================================================
		 */

		List<Image> imagesWithoutCreationDate = imageRepository.findByCreatedDateNull();

		int totalImageCount = imagesWithoutCreationDate.size();
		int currentImageCount = 0;

		log.info(String.format(step + " :: start creation date upgrade of %s images", totalImageCount));

		for (Image image : imagesWithoutCreationDate)
		{
			currentImageCount++;

			log.info(String.format(step + " :: calculating creation date of image file [%s/%s]: %s", currentImageCount, totalImageCount, image.getId()));

			LocalDateTime startTime = LocalDateTime.of(2019, Month.JANUARY, 1, 0, 0);
			LocalDateTime createdDate = startTime.plusSeconds(image.getId());

			image.setCreatedDate(createdDate);

			imageRepository.save(image);
		}
	}
}
