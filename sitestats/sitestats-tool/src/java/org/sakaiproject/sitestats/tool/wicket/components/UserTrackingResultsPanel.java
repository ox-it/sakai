package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.components.paging.infinite.SakaiInfinitePagingDataTable;
import org.sakaiproject.sitestats.tool.wicket.providers.UserTrackingDataProvider;

/**
 * @author plukasew, bjones86
 */
public class UserTrackingResultsPanel extends Panel
{
	private final UserTrackingDataProvider provider;

	private String siteID;

	private static final int DEFAULT_PAGE_SIZE = 20;

	private SakaiInfinitePagingDataTable resultsTable;

	public UserTrackingResultsPanel(String id, TrackingParams trackingParams)
	{
		super(id);
		siteID = trackingParams.getSiteId();
		provider = new UserTrackingDataProvider(trackingParams);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		resultsTable = new SakaiInfinitePagingDataTable("table", getTableColumns(), provider, DEFAULT_PAGE_SIZE);
		add(resultsTable);
	}

	public void setTrackingParams(TrackingParams value)
	{
		provider.setTrackingParams(value);
		siteID = value.getSiteId();
		resultsTable.setOffset(0); // new params, reset paging
	}

	private List<IColumn> getTableColumns()
	{
		List<IColumn> cols = new ArrayList<>();

		cols.add(new PropertyColumn<DetailedEvent, String>(new ResourceModel("de_resultsTable_timestamp"), "eventDate", "eventDate")
		{
			@Override
			protected IModel createLabelModel(IModel rowModel)
			{
				// Get the event date
				DetailedEvent event = (DetailedEvent) rowModel.getObject();
				Date date = event.getEventDate();
				String dateStr;

				// If dates are stored in UTC time, we need to apply the offset from UTC to local time zone so the dates are accurate
				if(Locator.getFacade().getDetailedEventsManager().userTrackingConvertUTC())
				{
					// Apply the offset
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					int offset = cal.getTimeZone().getOffset(date.getTime());
					cal.add(Calendar.MILLISECOND, offset);
					Date realDate = cal.getTime();

					// Re-initialize the date in the model object with the new adjusted date
					event.setEventDate(realDate);
					rowModel.setObject(event);
					dateStr = Locator.getFacade().getTimeService().newTime(realDate.getTime()).toStringLocalFull();
				}
				else
				{
					dateStr = Locator.getFacade().getTimeService().newTime(date.getTime()).toStringLocalFull();
				}

				return new Model<>(dateStr);
			}
		});

		// OWLTODO: combine events and details into single column like in mockup?
		cols.add(new PropertyColumn<DetailedEvent, String>(new ResourceModel("de_resultsTable_event"), "eventId", "eventId")
		{
			@Override
			public boolean isSortable()
			{
				return false;
			}

			@Override
			protected IModel createLabelModel(IModel rowModel)
			{
				// Get the event ID
				DetailedEvent event = (DetailedEvent) rowModel.getObject();
				String eventID = event.getEventId();

				// Get the fancy name for the event ID
				String fancyName = Locator.getFacade().getEventRegistryService().getEventName(eventID);
				return new Model<>(fancyName);
			}
		});

		cols.add(new AbstractColumn<DetailedEvent, String>(new ResourceModel("de_resultsTable_details"), "")
		{
			@Override
			public void populateItem(final Item<ICellPopulator<DetailedEvent>> item, final String componentId, final IModel<DetailedEvent> rowModel)
			{
				item.add(new EventRefDetailsLinkPanel(componentId, rowModel, siteID));
			}

			@Override
			public boolean isSortable()
			{
				return false;
			}
		});

		return cols;
	}
}
