package org.sakaiproject.service.gradebook.shared;

import java.util.List;

/**
 *
 * @author plukasew
 */
public class CategoryScoreData
{
	public final double score;
	public final List<Long> includedItems;
	public final List<Long> droppedItems;
	
	public CategoryScoreData(double score, List<Long> includedItems, List<Long> droppedItems)
	{
		this.score = score;
		this.includedItems = includedItems;
		this.droppedItems = droppedItems;
	}
}
