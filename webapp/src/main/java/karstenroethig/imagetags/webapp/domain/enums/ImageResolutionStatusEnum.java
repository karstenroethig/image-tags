package karstenroethig.imagetags.webapp.domain.enums;

import lombok.Getter;

@Getter
public enum ImageResolutionStatusEnum
{
	GENERATION_ERROR(-1),
	NO_RESOLUTION(0),
	GENERATION_SUCCESS(1);

	private int key;

	private ImageResolutionStatusEnum(int key)
	{
		this.key = key;
	}

	public static ImageResolutionStatusEnum getStatusForKey(Integer key)
	{
		if (key == null)
		{
			return NO_RESOLUTION;
		}

		for (ImageResolutionStatusEnum type : values())
		{
			if (key.equals(type.getKey()))
			{
				return type;
			}
		}

		return NO_RESOLUTION;
	}
}
