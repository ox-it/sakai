package org.sakaiproject.sitestats.tool.wicket.util;

import java.text.Collator;
import java.util.Comparator;
import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.tool.facade.Locator;

/**
 * @author plukasew
 */
public class Comparators
{
	public static final Comparator<String> getStringComparator(final Collator collator)
	{
		return new Comparator<String>()
		{
			public int compare(String o1, String o2)
			{
				return collator.compare(o1, o2);
			}
		};
	}

	public static final Comparator<ToolInfo> getToolInfoComparator(final Collator collator)
	{
		return new Comparator<ToolInfo>()
		{
			public int compare(ToolInfo o1, ToolInfo o2)
			{
				String toolName1 = Locator.getFacade().getEventRegistryService().getToolName(o1.getToolId());
				String toolName2 = Locator.getFacade().getEventRegistryService().getToolName(o2.getToolId());
				return collator.compare(toolName1, toolName2);
			}
		};
	}

	public static final Comparator<Object> getOptionRendererComparator(final Collator collator, final IOptionRenderer renderer)
	{
		return new Comparator<Object>()
		{
			public int compare(Object o1, Object o2)
			{
				return collator.compare(renderer.getDisplayValue(o1), renderer.getDisplayValue(o2));
			}
		};
	}

	public static final Comparator<Object> getChoiceRendererComparator(final Collator collator, final IChoiceRenderer renderer)
	{
		return new Comparator<Object>()
		{
			public int compare(Object o1, Object o2)
			{
				return collator.compare(renderer.getDisplayValue(o1), renderer.getDisplayValue(o2));
			}
		};
	}
}
