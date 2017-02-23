package org.sakaiproject.sitestats.api.event.detailed.forums;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 *
 * @author plukasew
 */
public final class ForumData implements ResolvedEventData
{
	private final String title;

	public ForumData(String title)
	{
		this.title = title;
	}

	public String getTitle()
	{
		return title;
	}
}
