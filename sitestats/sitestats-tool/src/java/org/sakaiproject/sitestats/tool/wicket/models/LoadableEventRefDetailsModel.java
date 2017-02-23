package org.sakaiproject.sitestats.tool.wicket.models;

import java.util.Date;
import java.util.List;
import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.util.CalendarResolvedRefTransformer;
import org.sakaiproject.sitestats.tool.util.ForumsResolvedRefTransformer;
import org.sakaiproject.sitestats.tool.util.LessonsResolvedRefTransformer;

/**
 * @author plukasew, bjones86
 */
public class LoadableEventRefDetailsModel extends LoadableDetachableModel<List<ResolvedRef>>
{
	private final String eventType, eventRef, siteID;
	private final Date eventDate;

	public LoadableEventRefDetailsModel(String eventType, String eventRef, Date eventDate, String siteID)
	{
		this.eventType = eventType;
		this.eventRef = eventRef;
		this.siteID = siteID;
		this.eventDate = eventDate;
	}

	@Override
	protected List<ResolvedRef> load()
	{
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();

		// here we are (temporarily) adding a hacky check to see if this is a forums or calendar or lessons event
		// if so, we'll call a different DEM method and transform the results back into List<ResolvedRef>
		if (eventType.startsWith("forums"))
		{
			return ForumsResolvedRefTransformer.transform(dem.resolveForumsOrCalendarOrLessonsEvent(eventType, eventRef, eventDate, siteID), eventType);
		}
		else if (eventType.startsWith("calendar"))
		{
			return CalendarResolvedRefTransformer.transform(dem.resolveForumsOrCalendarOrLessonsEvent(eventType, eventRef, eventDate, siteID), eventType);
		}
		else if (eventType.startsWith("lessonbuilder"))
		{
			return LessonsResolvedRefTransformer.transform(dem.resolveForumsOrCalendarOrLessonsEvent(eventType, eventRef, eventDate, siteID), eventType);
		}

		return dem.resolveEventReference(eventType, eventRef, eventDate, siteID);
	}
}
