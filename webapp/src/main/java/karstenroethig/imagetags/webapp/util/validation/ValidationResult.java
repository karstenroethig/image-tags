package karstenroethig.imagetags.webapp.util.validation;

import java.util.ArrayList;
import java.util.List;

import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class ValidationResult
{
	private List<ValidationMessage> errors = new ArrayList<>();
	private List<ValidationMessage> warnings = new ArrayList<>();
	private List<ValidationMessage> infos = new ArrayList<>();

	public boolean isOk()
	{
		return errors.isEmpty();
	}

	public boolean hasErrors()
	{
		return !errors.isEmpty();
	}

	public void addError(MessageKeyEnum messageKey, Object... params)
	{
		errors.add(createMessage(ValidationState.ERROR, null, messageKey, params));
	}

	public void addError(String propertyId, MessageKeyEnum messageKey, Object... params)
	{
		addError(propertyId, null, messageKey, params);
	}

	public void addError(String propertyId, String defaultMessage, MessageKeyEnum messageKey, Object... params)
	{
		errors.add(createMessage(propertyId, ValidationState.ERROR, defaultMessage, messageKey, params));
	}

	public boolean hasWarnings()
	{
		return !warnings.isEmpty();
	}

	public void addWarning(MessageKeyEnum messageKey, Object... params)
	{
		warnings.add(createMessage(ValidationState.WARNING, null, messageKey, params));
	}

	public void addWarning(String propertyId, MessageKeyEnum messageKey, Object... params)
	{
		addWarning(propertyId, null, messageKey, params);
	}

	public void addWarning(String propertyId, String defaultMessage, MessageKeyEnum messageKey, Object... params)
	{
		warnings.add(createMessage(propertyId, ValidationState.WARNING, defaultMessage, messageKey, params));
	}

	public boolean hasInfos()
	{
		return !infos.isEmpty();
	}

	public void addInfo(MessageKeyEnum messageKey, Object... params)
	{
		infos.add(createMessage(ValidationState.INFO, null, messageKey, params));
	}

	public void addInfo(String propertyId, MessageKeyEnum messageKey, Object... params)
	{
		addInfo(propertyId, null, messageKey, params);
	}

	public void addInfo(String propertyId, String defaultMessage, MessageKeyEnum messageKey, Object... params)
	{
		infos.add(createMessage(propertyId, ValidationState.INFO, defaultMessage, messageKey, params));
	}

	private ValidationMessage createMessage(ValidationState state, String defaultMessage, MessageKeyEnum messageKey, Object... params)
	{
		return new ValidationMessage(state, defaultMessage, messageKey, params);
	}

	private PropertyValidationMessage createMessage(String propertyId, ValidationState state, String defaultMessage, MessageKeyEnum messageKey, Object... params)
	{
		return new PropertyValidationMessage(propertyId, state, defaultMessage, messageKey, params);
	}

	public void add(ValidationResult validationResult)
	{
		if (validationResult == null)
			return;

		errors.addAll(validationResult.getErrors());
		warnings.addAll(validationResult.getWarnings());
		infos.addAll(validationResult.getInfos());
	}
}
