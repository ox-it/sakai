package org.sakaiproject.sitestats.tool.wicket.models;

import java.util.List;
import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.sitestats.api.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.tool.facade.Locator;

/**
 * @author plukasew, bjones86
 */
public class LoadableDetailedEventModel extends LoadableDetachableModel<DetailedEvent>
{
	private final long id;

	public LoadableDetailedEventModel(long id)
	{
		this.id = id;
	}

	public LoadableDetailedEventModel(DetailedEvent event)
	{
		this.id = event.getId();
		setObject(event);
	}

	@Override
	protected DetailedEvent load()
	{
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();
		List<DetailedEvent> events = dem.getDetailedEventById(id);
		if (events.size() == 1)
		{
			return events.get(0);
		}

		return null;
	}
}
