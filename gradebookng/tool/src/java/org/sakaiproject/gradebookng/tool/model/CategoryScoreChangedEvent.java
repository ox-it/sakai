package org.sakaiproject.gradebookng.tool.model;

import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 * Event for notifying all the gradebook item cells that a category score was updated in case they need
 * to update themselves accordingly (for example, to indicate they were dropped from the calculation)
 * @author plukasew
 */
public class CategoryScoreChangedEvent
{
	public final String studentUuid;
	public final long categoryId;
	public final List<Long> includedItems; // ids of the gradebook items included in the calculation of the category score
	public final AjaxRequestTarget target;

	public CategoryScoreChangedEvent(String studentUuid, long categoryId, List<Long> includedItems, AjaxRequestTarget target)
	{
		this.studentUuid = studentUuid;
		this.categoryId = categoryId;
		this.includedItems = includedItems;
		this.target = target;
	}
}
