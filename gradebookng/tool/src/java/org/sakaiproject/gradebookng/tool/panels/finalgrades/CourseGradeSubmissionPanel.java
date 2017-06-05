package org.sakaiproject.gradebookng.tool.panels.finalgrades;

import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author plukasew
 */
public class CourseGradeSubmissionPanel extends Panel
{
	public CourseGradeSubmissionPanel(String id)
	{
		super(id);
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		add(new SectionStatisticsPanel("sectionStatisticsPanel"));
		add(new SubmissionHistoryPanel("submissionHistoryPanel"));
		add(new SubmitAndApprovePanel("submitAndApprovePanel"));
	}
}
