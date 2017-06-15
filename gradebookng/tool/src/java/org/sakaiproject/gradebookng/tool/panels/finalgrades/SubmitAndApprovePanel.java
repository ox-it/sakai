package org.sakaiproject.gradebookng.tool.panels.finalgrades;

import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.component.GbFeedbackPanel;
import org.sakaiproject.gradebookng.tool.component.SakaiAjaxButton;
import org.sakaiproject.gradebookng.tool.pages.CourseGradesPage;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.CourseGradeSubmissionPanel.CourseGradeSubmissionData;

/**
 *
 * @author plukasew
 */
public class SubmitAndApprovePanel extends Panel
{	
	private Label statusLabel;
	private GbFeedbackPanel feedback;
	
	public SubmitAndApprovePanel(String id, IModel<CourseGradeSubmissionData> model)
	{
		super(id, model);
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		add(statusLabel = new Label("currentStatus", Model.of("")));
		
		final SakaiAjaxButton submitButton = new SakaiAjaxButton("submitFinalGrades")
		{
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form)
			{
				CourseGradesPage page = (CourseGradesPage) getPage();
				page.submitGrades(target);
			}
			
			@Override
			public boolean isEnabled()
			{
				return getData().isSubmitReady();
			}
		};
		submitButton.setWillRenderOnClick(true);
		submitButton.setVisible(getData().isCanSubmit());
		add(submitButton);
		
		final SakaiAjaxButton approveButton = new SakaiAjaxButton("approveFinalGrades")
		{
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form)
			{
				CourseGradesPage page = (CourseGradesPage) getPage();
				page.approveGrades(target);
			}
			
			@Override
			public boolean isEnabled()
			{
				return getData().isApproveReady();
			}
		};
		approveButton.setWillRenderOnClick(true);
		approveButton.setVisible(getData().isCanApprove());
		add(approveButton);
		
		add(feedback = new GbFeedbackPanel("submitAndApproveFeedback"));
		feedback.setOutputMarkupId(true);
		feedback.setFilter(new ComponentFeedbackMessageFilter(getParent()));
	}
	
	public void redrawFeedback(AjaxRequestTarget target)
	{
		target.add(feedback);
	}
	
	@Override
	protected void onBeforeRender()
	{	
		super.onBeforeRender();
		
		statusLabel.setDefaultModelObject(getData().getStatusMsg());
	}
	
	private SubmitAndApproveStatus getData()
	{
		CourseGradeSubmissionData data = (CourseGradeSubmissionData) getDefaultModel().getObject();
		return data.getButtonStatus();
	}
	
	@Builder
	public static class SubmitAndApproveStatus implements Serializable
	{
		@Getter
		private final boolean canSubmit, canApprove, submitReady, approveReady;
		@Getter
		private final String statusMsg;
	}
}
