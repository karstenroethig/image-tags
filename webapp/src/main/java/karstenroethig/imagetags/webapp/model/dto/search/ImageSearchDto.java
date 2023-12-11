package karstenroethig.imagetags.webapp.model.dto.search;

import java.util.List;

import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.enums.ImageNewTagStatusEnum;
import karstenroethig.imagetags.webapp.model.enums.ImageThumbStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ImageSearchDto
{
	private List<TagDto> tags;
	private ImageNewTagStatusEnum newTagStatus;
	private ImageThumbStatusEnum thumbStatus;

	public boolean hasParams()
	{
		return (tags != null && !tags.isEmpty())
			|| newTagStatus != null
			|| thumbStatus != null;
	}
}
