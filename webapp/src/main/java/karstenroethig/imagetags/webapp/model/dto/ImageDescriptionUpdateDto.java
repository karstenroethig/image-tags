package karstenroethig.imagetags.webapp.model.dto;

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
public class ImageDescriptionUpdateDto extends AbstractDtoId
{
	@Size(max = 256)
	private String description;
}
