package karstenroethig.imagetags.webapp.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import karstenroethig.imagetags.webapp.model.enums.TagTypeEnum;
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
public class TagDto extends AbstractDtoId
{
	@NotNull
	@Size(min = 1, max = 191)
	private String name;

	@NotNull
	private TagTypeEnum type;
}
