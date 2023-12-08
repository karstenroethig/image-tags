package karstenroethig.imagetags.webapp.model.dto;

import java.util.HashSet;
import java.util.Set;

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
public class ImageTagsUpdateDto extends AbstractDtoId
{
	private Set<TagDto> tags = new HashSet<>();

	public void addTag(TagDto tag)
	{
		tags.add(tag);
	}
}
