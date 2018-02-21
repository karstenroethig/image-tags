package karstenroethig.imagetags.webapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagesPageDto
{
	private Integer currentPageNumber;
	private Integer maxPageNumber;
	private ImageDto currentImage;

	public boolean hasPreviousPage()
	{
		return currentPageNumber > 1;
	}

	public boolean hasNextPage()
	{
		return maxPageNumber > currentPageNumber;
	}
}
