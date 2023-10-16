package karstenroethig.imagetags.webapp.model.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import karstenroethig.imagetags.webapp.model.enums.ImageResolutionStatusEnum;
import karstenroethig.imagetags.webapp.model.enums.ImageThumbStatusEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ImageDto extends AbstractDtoId
{
	private StorageDto storage;
	private String storageKey;

	private String extension;
	private Long size;
	private String sizeFormatted;
	private String hash;
	private String importPath;
	private ImageThumbStatusEnum thumbStatus;
	private Integer resolutionWidth;
	private Integer resolutionHeight;
	private ImageResolutionStatusEnum resolutionStatus;
	private LocalDateTime createdDatetime;
	private Set<TagDto> tags = new HashSet<>();

	public void addTag(TagDto tag)
	{
		tags.add(tag);
	}
}
