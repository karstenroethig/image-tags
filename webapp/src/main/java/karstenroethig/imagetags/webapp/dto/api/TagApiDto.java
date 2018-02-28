package karstenroethig.imagetags.webapp.dto.api;

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
public class TagApiDto
{
	private Long id;
	private String name;
	private int amount;
}
