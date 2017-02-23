package org.sakaiproject.sitestats.api.event.detailed;

/**
 * Common empty interface to tag the various data objects returned by ref resolvers.
 *
 * @author plukasew, bjones86
 */
public interface ResolvedEventData
{
	public static final class NoData			implements ResolvedEventData {}
	public static final class PermissionError	implements ResolvedEventData {}

	public static final NoData			NO_DATA		= new NoData();
	public static final PermissionError	PERM_ERROR	= new PermissionError();
}
