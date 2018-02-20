package karstenroethig.imagetags.webapp.domain.enums;

import lombok.Getter;

@Getter
public enum TagTypeEnum
{
	CATEGORY(1),
	PERSON(2),
	ALBUM(3);

	private int key;

	private TagTypeEnum(int key)
	{
		this.key = key;
	}

	public static TagTypeEnum getTypeForKey(Integer key)
	{
		if (key == null)
		{
			return null;
		}

		for (TagTypeEnum type : values())
		{
			if (key.equals(type.getKey()))
			{
				return type;
			}
		}

		return null;
	}
}
