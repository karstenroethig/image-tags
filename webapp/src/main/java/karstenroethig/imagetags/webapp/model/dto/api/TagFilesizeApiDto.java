package karstenroethig.imagetags.webapp.model.dto.api;

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
public class TagFilesizeApiDto
{
	private Long id;
	private String filesize;
}
