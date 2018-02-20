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
}
