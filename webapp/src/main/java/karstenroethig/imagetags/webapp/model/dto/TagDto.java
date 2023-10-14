package karstenroethig.imagetags.webapp.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

	private String description;

	private Boolean archived;
}
