package karstenroethig.imagetags.webapp.model.dto.search;

import java.util.List;

import karstenroethig.imagetags.webapp.model.dto.AlbumDto;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
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
	private AlbumDto album;

	public boolean hasParams()
	{
		return (tags != null && !tags.isEmpty())
				|| album != null;
	}
}
