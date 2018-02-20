package karstenroethig.imagetags.webapp.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import karstenroethig.imagetags.webapp.domain.enums.TagTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class TagDto
{
	private Long id;

	@NotNull
	@Size(
		min = 1,
		max = 255
	)
	private String name;

	@NotNull
	private TagTypeEnum type;
}
