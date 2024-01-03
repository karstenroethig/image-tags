package karstenroethig.imagetags.webapp.model.dto.backup;

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
@EqualsAndHashCode
public class RestoreDto
{
	@NotNull
	@Size(min = 1)
	private String filePath;
}
