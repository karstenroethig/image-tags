package karstenroethig.imagetags.webapp.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageDto
{
	private Long id;
	private String importPath;
	private List<TagDto> tags = new ArrayList<>();

	public void addTag(TagDto tag)
	{
		tags.add(tag);
	}
}
