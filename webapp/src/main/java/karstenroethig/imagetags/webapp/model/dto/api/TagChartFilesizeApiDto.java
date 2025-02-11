package karstenroethig.imagetags.webapp.model.dto.api;

import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder
public class TagChartFilesizeApiDto
{
	private List<String> names;
	private List<Float> filesize;
}
