package karstenroethig.imagetags.webapp.model.dto;

import jakarta.validation.constraints.NotNull;
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
public class ImageAlbumPageUpdateDto extends AbstractDtoId
{
	@NotNull
	private Integer albumPage;
}
