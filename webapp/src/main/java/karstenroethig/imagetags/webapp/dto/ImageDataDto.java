package karstenroethig.imagetags.webapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageDataDto
{
	private String filename;
	private Long size;
	private byte[] data;
}
