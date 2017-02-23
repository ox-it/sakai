package org.sakaiproject.sitestats.api;

import java.util.Calendar;
import java.util.Date;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;

/**
 *
 * @author plukasew
 */
public class StatsDates
{
	public static final int DAYS_AGO_7 = 6;
	public static final int DAYS_AGO_30 = 29;
	public static final int DAYS_AGO_365 = 364;

	public static Date daysAgo(Calendar c, int days)
	{
		c.add(Calendar.DATE, -days);

		return c.getTime();
	}

	public static Date daysAgo(Date date, int days)
	{
		Calendar c = clearTimeFromDate(date);

		return daysAgo(c, days);
	}

	public static Calendar clearTime(Calendar c)
	{
		c.set(Calendar.HOUR_OF_DAY, 00);
		c.set(Calendar.MINUTE, 00);
		c.set(Calendar.SECOND, 00);

		return c;
	}

	public static Calendar clearTimeFromDate(Date date)
	{
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return clearTime(c);
	}

	public static Date nextDay(Date date)
	{
		Calendar c = clearTimeFromDate(date);
		c.add(Calendar.DAY_OF_MONTH, 1);

		return c.getTime();
	}

	public static Date firstOfMonth(Date date)
	{
		Calendar c = clearTimeFromDate(date);
		c.set(Calendar.DAY_OF_MONTH, 1);

		return c.getTime();
	}

	public static Date firstOfNextMonth(Date date)
	{
		Calendar c = clearTimeFromDate(date);
		c.set(Calendar.MONTH, 1);

		return c.getTime();
	}

	public static String formatForDisplay(Date date, TimeService timeServ)
	{
		return formatForDisplay(timeServ.newTime(date.getTime()));
	}

	public static String formatForDisplay(Time time)
	{
		return time.toStringLocalFull();
	}
}
