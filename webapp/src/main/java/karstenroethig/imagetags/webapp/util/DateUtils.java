package karstenroethig.imagetags.webapp.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DateUtils
{
	private DateUtils() {}

	public static ZonedDateTime normalizedNow()
	{
		return ZonedDateTime.now(ZoneOffset.UTC);
	}

	public static LocalDateTime normalizedNowForDatabase()
	{
		return LocalDateTime.now(ZoneOffset.UTC);
	}

	public static ZonedDateTime convertNormalizedDatetime(LocalDateTime dateTime)
	{
		if (dateTime == null)
			return null;
		return ZonedDateTime.of(dateTime, ZoneOffset.UTC);
	}

	public static long secondsFromDateTimeUntilNow(LocalDateTime datetime)
	{
		return datetime.until(LocalDateTime.now(), ChronoUnit.SECONDS);
	}
}
