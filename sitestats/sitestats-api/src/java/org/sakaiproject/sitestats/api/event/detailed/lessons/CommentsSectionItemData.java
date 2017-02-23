package org.sakaiproject.sitestats.api.event.detailed.lessons;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;

/**
 * There are two variations on a comments section, normal comments a user adds, and "forced comments" for student pages which
 * are added automatically. Due to Lessons design choices, forced comments cannot be tied back to their student page and therefore
 * will have no page hierarchy.
 * 
 * @author plukasew
 */
public class CommentsSectionItemData implements ResolvedEventData
{
	// OWLTODO: if these variations are mutually exclusive, split into two classes, CommentsSection and ForcedCommentsSection?

	private final PageData parent;  // OWLTODO: Java 8 Optional? See above comment
	private final boolean forcedComments;

	public CommentsSectionItemData(final PageData parent, final boolean studentPageComments)
	{
		this.parent = parent;
		forcedComments = studentPageComments;
	}

	public PageData getParentPage()
	{
		return parent;
	}

	public boolean isStudentPageComments()
	{
		return forcedComments;
	}
}
