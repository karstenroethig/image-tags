package karstenroethig.imagetags.webapp.dto.info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemoryInfoDto
{
	private String totalFormated;
	private String usedFormated;
	private long freePercentage;
	private String freeFormated;
}