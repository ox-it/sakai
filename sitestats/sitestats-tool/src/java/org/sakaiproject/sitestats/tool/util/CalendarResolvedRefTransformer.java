package org.sakaiproject.sitestats.tool.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.StatsDates;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.api.event.detailed.calendar.CalendarEntryData;

/**
 * Class to temporarily convert a ResolvedEventData object into a List<ResolvedRef> so that we
 * don't need to change the presentation code or any of the other ref resolvers at this time.
 *
 * View-layer logic for presenting the data contained in the ResolvedEventData object lives here and will probably remain,
 * even if we change the mechanism of presentation from a simple K/V list
 *
 * @author plukasew
 */
public class CalendarResolvedRefTransformer
{
	public static List<ResolvedRef> transform(ResolvedEventData resolved, String eventType)
	{
		// OWLTODO: localization would go in this area

		if (resolved instanceof CalendarEntryData)
		{
			CalendarEntryData calEntry = (CalendarEntryData) resolved;
			List<ResolvedRef> details = new ArrayList<>(4);
			details.add(ResolvedRef.newText("Title", calEntry.getTitle()));

			String start = StatsDates.formatForDisplay(calEntry.getRange().firstTime());
			String end = StatsDates.formatForDisplay(calEntry.getRange().lastTime());
			details.add(ResolvedRef.newText("Duration", start + " to " + end));

			final String OCCURS = "Every %s %s";
			String occurs = String.format(OCCURS, String.valueOf(calEntry.getInterval()), calEntry.getFrequencyUnit());
			if (CalendarEntryData.FREQ_ONCE.equals(calEntry.getFrequencyUnit()))
			{
				occurs = "Once";
			}

			details.add(ResolvedRef.newText("Occurs", occurs));
			return details;
		}

		return Collections.emptyList();
	}
}
