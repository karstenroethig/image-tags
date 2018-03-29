package karstenroethig.imagetags.webapp.dto.info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatisticInfoDto
{
	private Long totalImages;
	private Long untaggedImages;
	private String totalFilesizeFormated;
}