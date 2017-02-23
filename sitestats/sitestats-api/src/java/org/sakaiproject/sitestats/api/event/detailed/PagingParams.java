package org.sakaiproject.sitestats.api.event.detailed;

/**
 * Immutable class to hold parameters for paging of detailed events queries
 *
 * @author plukasew
 */
public final class PagingParams
{
	private final long start;
	private final long pageSize;

	/**
	 * Constructor requiring all parameters
	 *
	 * @param start the starting row offset for results
	 * @param count the number of results to return
	 */
	public PagingParams(long start, long count)
	{
		this.start = start;
		pageSize = count;
	}

	public int getStart()
	{
		return (int) start;
	}

	public int getPageSize()
	{
		return (int) pageSize;
	}
}
