package org.sakaiproject.gradebookng.tool.panels.finalgrades;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.SectionStatisticsPanel.SectionStats;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.SubmissionHistoryPanel.SubmissionHistory;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.SubmitAndApprovePanel.SubmitAndApproveStatus;

/**
 *
 * @author plukasew
 */
public class CourseGradeSubmissionPanel extends Panel
{
	private SectionStatisticsPanel statsPanel;
	private SubmissionHistoryPanel historyPanel;
	private SubmitAndApprovePanel submitPanel;
	
	public CourseGradeSubmissionPanel(String id, IModel<CourseGradeSubmissionData> model)
	{
		super(id, model);
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		IModel<CourseGradeSubmissionData> dataModel = getDataModel();
		add(statsPanel = new SectionStatisticsPanel("sectionStatisticsPanel", dataModel));
		add(historyPanel = new SubmissionHistoryPanel("submissionHistoryPanel", dataModel));
		historyPanel.setOutputMarkupId(true);
		add(submitPanel = new SubmitAndApprovePanel("submitAndApprovePanel", dataModel));
		submitPanel.setOutputMarkupId(true);
	}
	
	public CourseGradeSubmissionData getData()
	{
		return (CourseGradeSubmissionData) getDefaultModelObject();
	}
	
	public IModel<CourseGradeSubmissionData> getDataModel()
	{
		return (IModel<CourseGradeSubmissionData>) getDefaultModel();
	}
	
	public void redrawButtons(AjaxRequestTarget target)
	{
		submitPanel.setDefaultModelObject(getData());
		target.add(submitPanel);
	}
	
	public void redrawStats(AjaxRequestTarget target)
	{
		statsPanel.setDefaultModelObject(getData());
		statsPanel.redraw(target);
	}
	
	public void redrawHistory(AjaxRequestTarget target)
	{
		historyPanel.setDefaultModelObject(getData());
		target.add(historyPanel);
	}
	
	public void redrawFeedback(AjaxRequestTarget target)
	{
		submitPanel.redrawFeedback(target);
	}
	
	public static class CourseGradeSubmissionData implements Serializable
	{
		@Getter @Setter
		private SectionStats stats;
		
		@Getter @Setter
		private SubmissionHistory history;
		
		@Getter @Setter
		private SubmitAndApproveStatus buttonStatus;
	}
}
