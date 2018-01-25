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
	
	public CategoryScoreData(double score, List<Long> includedItems)
	{
		this.score = score;
		this.includedItems = includedItems;
	}
}
