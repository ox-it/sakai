package org.sakaiproject.sitestats.api.event.detailed.lessons;

import java.util.Objects;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Represents a Lessons item which has its content embedded in the page as opposed to presented as an external content link.
 */
public class EmbeddedItemData implements ResolvedEventData
{
	private final String desc;  // OWLTODO: Java 8 optional
	private final PageData parentPage;

	/**
	 * Constructor
	 * @param description an optional description for the items (as shown on the page in Lessons)
	 * @param parentPage the page the item is embedded into, must not be null
	 */
	public EmbeddedItemData(String description, PageData parentPage)
	{
		desc = description;
		this.parentPage = Objects.requireNonNull(parentPage, "Embedded items must have a parent page");
	}

	public String getDescription()
	{
		return desc;
	}

	public PageData getParentPage()
	{
		return parentPage;
	}
}
