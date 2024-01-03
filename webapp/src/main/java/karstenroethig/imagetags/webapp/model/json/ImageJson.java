package karstenroethig.imagetags.webapp.model.json;

import java.time.LocalDateTime;
import java.util.List;

import karstenroethig.imagetags.webapp.model.enums.ImageResolutionStatusEnum;
import karstenroethig.imagetags.webapp.model.enums.ImageThumbStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageJson
{
	private String storageKey;
	private String storageFilename;
	private String extension;
	private Long size;
	private String hash;
	private String importPath;
	private ImageThumbStatusEnum thumbStatus;
	private Integer resolutionWidth;
	private Integer resolutionHeight;
	private ImageResolutionStatusEnum resolutionStatus;
	private LocalDateTime createdDatetime;
	private List<String> tags;
}
