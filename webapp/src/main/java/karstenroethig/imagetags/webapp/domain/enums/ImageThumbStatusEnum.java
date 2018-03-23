package karstenroethig.imagetags.webapp.domain.enums;

import lombok.Getter;

@Getter
public enum ImageThumbStatusEnum
{
	GENERATION_ERROR(-1),
	NO_THUMB(0),
	THUMB_100_100(1);

	private int key;

	private ImageThumbStatusEnum(int key)
	{
		this.key = key;
	}

	public static ImageThumbStatusEnum getStatusForKey(Integer key)
	{
		if (key == null)
		{
			return NO_THUMB;
		}

		for (ImageThumbStatusEnum type : values())
		{
			if (key.equals(type.getKey()))
			{
				return type;
			}
		}

		return NO_THUMB;
	}
}
