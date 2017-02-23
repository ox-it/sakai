package org.sakaiproject.sitestats.api.event.detailed.calendar;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.time.api.TimeRange;

/**
 *
 * @author plukasew
 */
public class CalendarEntryData implements ResolvedEventData
{
	public static final String FREQ_ONCE = "Once";

	private final String title;
	private final TimeRange range;
	private final int interval;
	private final String frequencyUnit;

	public CalendarEntryData(String title, TimeRange range, int interval, String frequencyUnit)
	{
		this.title = title;
		this.range = range;
		this.interval = interval;
		this.frequencyUnit = frequencyUnit;
	}

	public String getTitle()
	{
		return title;
	}

	public TimeRange getRange()
	{
		return range;
	}

	public int getInterval()
	{
		return interval;
	}

	public String getFrequencyUnit()
	{
		return frequencyUnit;
	}
}
