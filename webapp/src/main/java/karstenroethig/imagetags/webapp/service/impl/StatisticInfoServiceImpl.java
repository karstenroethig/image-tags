package karstenroethig.imagetags.webapp.service.impl;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.dto.info.StatisticInfoDto;

@Service
public class StatisticInfoServiceImpl
{
	private static final long GB = 1024l * 1024l * 1024l;
	private static final long MB = 1024l * 1024l;
	private static final long KB = 1024l;

	private static final NumberFormat GB_FORMAT = new DecimalFormat("#,###.0#");
	private static final NumberFormat MB_FORMAT = new DecimalFormat("#,###.#");
	private static final NumberFormat LITTLE_SIZE_FORMAT = new DecimalFormat("#,###");

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
		String totalFilesizeFormated = formatFilesize(totalFilesize);

		/*
		 * create statistic object
		 */
		StatisticInfoDto stats = new StatisticInfoDto();
		stats.setTotalImages(totalImages);
		stats.setUntaggedImages(untaggedImages);
		stats.setTotalFilesizeFormated(totalFilesizeFormated);

		return stats;
	}

	private static String formatFilesize(long bytes)
	{
		if (bytes > GB)
		{
			float gb = (float)bytes / GB;
			return GB_FORMAT.format(gb) + " GB";
		}
		else if (bytes > MB)
		{
			float mb = (float)bytes / MB;
			return MB_FORMAT.format(mb) + " MB";
		}
		else if (bytes > KB)
		{
			float kb = (float)bytes / KB;
			return LITTLE_SIZE_FORMAT.format(kb) + " kB";
		}

		return LITTLE_SIZE_FORMAT.format(bytes) + " Byte(s)";
	}
}