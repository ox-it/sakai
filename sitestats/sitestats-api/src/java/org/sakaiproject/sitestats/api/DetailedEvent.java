package org.sakaiproject.sitestats.api;

import java.util.Date;

/**
 *
 * @author plukasew
 */
public interface DetailedEvent
{
	public long getId();

	public void setId(long id);

	public String getSiteId();

	public void setSiteId(String siteId);

	public String getUserId();

	public void setUserId(String userId);

	public String getEventId();

	public void setEventId(String eventId);

	public String getEventRef();

	public void setEventRef(String eventRef);

	public Date getEventDate();

	public void setEventDate(Date date);

}
