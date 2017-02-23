package org.sakaiproject.sitestats.api.event.detailed;

import java.util.Date;
import java.util.List;
import org.sakaiproject.sitestats.api.DetailedEvent;
import org.sakaiproject.sitestats.api.UserModel;

/**
 * Manages retrieval of detailed event and user tracking data
 *
 * @author plukasew, bjones86
 */
public interface DetailedEventsManager
{
	/**
	 * Returns the detailed events matching the given tracking parameters, limited by the paging parameters,
	 * and in the order specified by the sorting parameters
	 *
	 * @param trackingParams parameters related to site, user, event types, and date range. Date range will be automatically adjusted to account for UTC as required.
	 * @param pagingParams parameters related to query paging
	 * @param sortingParams parameters related to sort order of the results
	 * @return a sorted list of detailed events that match the tracking and paging specifications
	 */
	public List<DetailedEvent> getDetailedEvents(final TrackingParams trackingParams, final PagingParams pagingParams, final SortingParams sortingParams);

	/**
	 * Retrieves a detailed event by id
	 * @param id the id of the detailed event
	 * @return list of detailed events that match the criteria, possibly empty or possibly containing more than one result
	 */
	public List<DetailedEvent> getDetailedEventById(final long id);

	/**
	 * Returns true if the event ref for the given event type can be resolved to provide additional details about the event.
	 * @param eventType the event id
	 * @return true if the event ref can be resolved to provide additional details
	 */
	public boolean isResolvable(String eventType);

	/**
	 * For the given event and reference, attempts to resolve the ref to provide additional details about the event
	 * @param eventType the event id
	 * @param eventRef the event ref to resolve
	 * @param eventDate the event date
	 * @param siteID the site the event occurred in
	 * @return a (possibly empty) list of ResolvedRef objects representing additional details about the event
	 */
	public List<ResolvedRef> resolveEventReference(String eventType, String eventRef, Date eventDate, String siteID);

	/**
	 *
	 * @param eventType
	 * @param eventRef
	 * @param eventDate
	 * @param siteID
	 * @return
	 */
	public ResolvedEventData resolveForumsOrCalendarOrLessonsEvent(String eventType, String eventRef, Date eventDate, String siteID);

	/**
	 * Returns whether detailed event dates are stored internally in UTC or not.
	 *
	 * @return true if dates are stored in UTC
	 */
	public boolean userTrackingConvertUTC();

	/**
	 * Get a list of (minified) User objects who can be tracked in the given site (for the user tracking tool's user drop down).
	 * @param siteID the ID of the site to retrieve users for
	 * @return a List of UserModel objects from the site who can be tracked
	 */
	public List<UserModel> getUsersForTracking(String siteID);
}
