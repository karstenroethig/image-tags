package karstenroethig.imagetags.webapp.util;

import lombok.Getter;

public enum MessageKeyEnum
{
	BEAN_VALIDATION("bean.validation"),

	APPLICATION_VERSION("application.version"),
	APPLICATION_REVISION("application.revision"),
	APPLICATION_BUILD_DATE("application.buildDate"),

	COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY("common.validation.objectCannotBeEmpty"),
	COMMON_VALIDATION_OBJECT_MUST_BE_EMPTY("common.validation.objectMustBeEmpty"),
	COMMON_VALIDATION_ALREADY_EXISTS("common.validation.alreadyExists"),
	COMMON_VALIDATION_VALUE_GIVEN_MULTIPLE_TIMES("common.validation.valueGivenMultipleTimes"),
	COMMON_VALIDATION_RANGE_START_GREATER_EQUAL_END("common.validation.range.startGreaterEqualEnd"),
	COMMON_VALIDATION_NOT_ALLOWED("common.validation.notAllowed"),
	COMMON_VALIDATION_NOT_CHANGED("common.validation.notChanged"),
	COMMON_VALIDATION_WRONG_PASSWORD("common.validation.wrongPassword"),

	TAG_SAVE_INVALID("tag.save.invalid"),
	TAG_SAVE_SUCCESS("tag.save.success"),
	TAG_SAVE_ERROR("tag.save.error"),
	TAG_UPDATE_INVALID("tag.update.invalid"),
	TAG_UPDATE_SUCCESS("tag.update.success"),
	TAG_UPDATE_ERROR("tag.update.error"),
	TAG_DELETE_INVALID("tag.delete.invalid"),
	TAG_DELETE_INVALID_STILL_IN_USE_BY_IMAGES("tag.delete.invalid.stillInUseByImages"),
	TAG_DELETE_SUCCESS("tag.delete.success"),
	TAG_DELETE_ERROR("tag.delete.error"),

	IMAGE_UPDATE_INVALID("image.update.invalid"),
	IMAGE_UPDATE_SUCCESS("image.update.success"),
	IMAGE_UPDATE_ERROR("image.update.error"),
	IMAGE_DELETE_INVALID("image.delete.invalid"),
	IMAGE_DELETE_SUCCESS("image.delete.success"),
	IMAGE_DELETE_ERROR("image.delete.error");

	@Getter
	private String key;

	private MessageKeyEnum(String key)
	{
		this.key = key;
	}
}
