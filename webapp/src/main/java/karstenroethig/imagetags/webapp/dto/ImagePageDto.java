package karstenroethig.imagetags.webapp.dto;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagePageDto
{
	private Integer currentPageNumber;
	private Integer maxPageNumber;
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
		return !hasError();
	}

	public boolean hasError()
	{
		return StringUtils.isNotBlank(errorMessageKey);
	}
}
