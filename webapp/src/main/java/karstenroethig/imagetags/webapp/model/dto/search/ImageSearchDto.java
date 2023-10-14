package karstenroethig.imagetags.webapp.model.dto.search;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ImageSearchDto
{
	private String text;

	public boolean hasParams()
	{
		return StringUtils.isNotBlank(text);
	}
}
