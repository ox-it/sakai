package org.sakaiproject.sitestats.api.event.detailed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * An immutable class to hold the parameters used for detailed events queries for user tracking.
 *
 * @author plukasew
 */
public final class TrackingParams implements Serializable
{
	private final String siteId;
	private final List<String> events;
	private final List<String> userIds;
	private final Date startDate;
	private final Date endDate;

	public static final Date NO_DATE = new Date(0);

	public static final TrackingParams EMPTY_PARAMS = new TrackingParams("", Collections.<String>emptyList(),
			Collections.<String>emptyList(), NO_DATE, NO_DATE);

	/**
	 * Constructor requiring all parameters.
	 *
	 * @param siteId the site id
	 * @param events list of event IDs
	 * @param users list of user UUIDs
	 * @param start start of date range
	 * @param end end of date range
	 */
	public TrackingParams(String siteId, List<String> events, List<String> users, Date start, Date end)
	{
		this.siteId = Objects.requireNonNull(siteId);
		this.events = Collections.unmodifiableList(new ArrayList<String>(events));
		userIds = Collections.unmodifiableList(new ArrayList<String>(users));
		startDate = new Date(Objects.requireNonNull(start).getTime());
		endDate = new Date(Objects.requireNonNull(end).getTime());
	}

	public String getSiteId()
	{
		return siteId;
	}

	public List<String> getEvents()
	{
		return events;
	}

	public List<String> getUsers()
	{
		return userIds;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public Date getEndDate()
	{
		return endDate;
	}
}
