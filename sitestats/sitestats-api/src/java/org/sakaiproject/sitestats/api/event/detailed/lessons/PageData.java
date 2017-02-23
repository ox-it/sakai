package org.sakaiproject.sitestats.api.event.detailed.lessons;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * Data about a Lessons page
 * @author plukasew
 */
public class PageData implements ResolvedEventData
{
	private final String title;
	private final String pageHierarchy;  // OWLTODO: at the very least this should be an ordered list of strings, the view layer should determine display format
	private final boolean topLevel;

	public static final String TOP_LEVEL = "TOP";  // OWLTODO: possibly replace with a Java 8 Optional for page hierarchy

	public static final PageData DELETED_PAGE = new PageData("Deleted", TOP_LEVEL);

	public PageData(String title, String hierarchy)
	{
		this.title = Objects.requireNonNull(StringUtils.trimToNull(title), "Page title must be non-null, non-empty");
		pageHierarchy = Objects.requireNonNull(StringUtils.trimToNull(hierarchy), "Hierarchy must be non-null, non-empty. Use TOP_LEVEL constant for top-level pages");
		topLevel = TOP_LEVEL.equals(pageHierarchy);
	}

	public String getTitle()
	{
		return title;
	}

	public String getHierarchy()
	{
		return pageHierarchy;
	}

	public boolean isTopLevel()
	{
		return topLevel;
	}
}
