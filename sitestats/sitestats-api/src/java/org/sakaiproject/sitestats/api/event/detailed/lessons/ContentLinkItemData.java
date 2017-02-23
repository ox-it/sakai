package org.sakaiproject.sitestats.api.event.detailed.lessons;

import java.util.Objects;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Represents a Lessons item which is presented on the page as an external content link as opposed
 * to having its content embedded in the page.
 */
public class ContentLinkItemData implements ResolvedEventData
{
	private final String name;
	private final PageData parentPage;

	/**
	 * Constructor
	 * @param name the name of the item as it appears on the Lessons page
	 * @param parentPage the page the item appears on, must not be null
	 */
	public ContentLinkItemData(String name, PageData parentPage)
	{
		this.name = name;
		this.parentPage = Objects.requireNonNull(parentPage);
	}

	public String getName()
	{
		return name;
	}

	public PageData getParentPage()
	{
		return parentPage;
	}
}
