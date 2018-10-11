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
	public final List<Long> droppedItems; // ids of the gradebook items dropped due to drop highest/lowest settings
	public final AjaxRequestTarget target;

	public CategoryScoreChangedEvent(String studentUuid, long categoryId, List<Long> includedItems,
			List<Long> droppedItems, AjaxRequestTarget target)
	{
		this.studentUuid = studentUuid;
		this.categoryId = categoryId;
		this.includedItems = includedItems;
		this.droppedItems = droppedItems;
		this.target = target;
	}
}
