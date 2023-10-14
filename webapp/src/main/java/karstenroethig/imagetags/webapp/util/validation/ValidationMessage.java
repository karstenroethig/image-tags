package karstenroethig.imagetags.webapp.util.validation;

import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class ValidationMessage
{
	private ValidationState state;
	private String defaultMessage;
	private MessageKeyEnum messageKey;
	private Object[] params;

	public ValidationMessage(ValidationState state, String defaultMessage, MessageKeyEnum messageKey, Object... params)
	{
		this.state = state;
		this.defaultMessage = defaultMessage;
		this.messageKey = messageKey;
		this.params = params;
	}
}
