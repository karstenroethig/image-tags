package karstenroethig.imagetags.webapp.util.validation;

import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class PropertyValidationMessage extends ValidationMessage
{
	private String propertyId;

	public PropertyValidationMessage(String propertyId, ValidationState state, String defaultMessage, MessageKeyEnum key, Object... params)
	{
		super(state, defaultMessage, key, params);
		this.propertyId = propertyId;
	}
}
