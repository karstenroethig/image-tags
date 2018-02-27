package karstenroethig.imagetags.webapp.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagesSearchDto
{
	private List<TagDto> tags = new ArrayList<>();
}
