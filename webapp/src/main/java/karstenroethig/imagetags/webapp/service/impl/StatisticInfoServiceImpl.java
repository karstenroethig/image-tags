package karstenroethig.imagetags.webapp.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.dto.info.StatisticInfoDto;
import karstenroethig.imagetags.webapp.util.FilesizeUtils;

@Service
public class StatisticInfoServiceImpl
{
	@Autowired
	private ImageServiceImpl imageService;

	public StatisticInfoDto getStatistic()
	{
		/*
		 * total images
		 */
		Long totalImages = imageService.findTotalImages();

		/*
		 * untagged images
		 */
		List<Long> untaggedImageIds = imageService.findImages(null);
		Long untaggedImages = new Long(untaggedImageIds.size());

		/*
		 * filesize
		 */
		Long totalFilesize = imageService.findTotalFilesize();
		String totalFilesizeFormated = FilesizeUtils.formatFilesize(totalFilesize);

		/*
		 * create statistic object
		 */
		StatisticInfoDto stats = new StatisticInfoDto();
		stats.setTotalImages(totalImages);
		stats.setUntaggedImages(untaggedImages);
		stats.setTotalFilesizeFormated(totalFilesizeFormated);

		return stats;
	}
}