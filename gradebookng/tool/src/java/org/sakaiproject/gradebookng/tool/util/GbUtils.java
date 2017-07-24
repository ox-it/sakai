package org.sakaiproject.gradebookng.tool.util;

import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;

/**
 *
 * @author plukasew
 */
public class GbUtils
{
	/**
	 * Returns the parent cell for given component
	 * @param component the component whose parent we want to find
	 * @param parentId the wicket id of the parent cell component
	 * @return the parent component, or empty if not found
	 */
	public static Optional<Component> getParentCellFor(final Component component, final String parentId)
	{	
		if (component == null)
		{
			return Optional.empty();
		}
		
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), parentId))
		{
			return Optional.of(component);
		}
		
		return getParentCellFor(component.getParent(), parentId);
	}
}
