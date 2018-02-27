package karstenroethig.imagetags.webapp.dto;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagesPageDto
{
	private Integer currentPageNumber;
	private Integer maxPageNumber;
	private ImageDto currentImage;
	private String errorMessageKey;

	public boolean hasPreviousPage()
	{
		return currentPageNumber > 1;
	}

	public boolean hasNextPage()
	{
		return maxPageNumber > currentPageNumber;
	}

	public boolean hasImage()
	{
		return currentImage != null;
	}

	public boolean hasError()
	{
		return StringUtils.isNotBlank(errorMessageKey);
	}
}
