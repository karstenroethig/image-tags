package karstenroethig.imagetags.webapp.model.json;

import karstenroethig.imagetags.webapp.model.enums.TagTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagJson
{
	private String name;
	private TagTypeEnum type;
}
