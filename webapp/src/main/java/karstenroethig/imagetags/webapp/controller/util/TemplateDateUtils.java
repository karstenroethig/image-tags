package karstenroethig.imagetags.webapp.controller.util;

import java.time.LocalDateTime;

import karstenroethig.imagetags.webapp.util.DateUtils;

public class TemplateDateUtils
{
	public static final TemplateDateUtils INSTANCE = new TemplateDateUtils();

	private TemplateDateUtils() {}

	public long secondsFromDateTimeUntilNow(LocalDateTime datetime)
	{
		return DateUtils.secondsFromDateTimeUntilNow(datetime);
	}
}
