package org.sakaiproject.gradebookng.tool.panels.finalgrades;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;

/**
 *
 * @author plukasew
 */
public class SubmitAndApprovePanel extends Panel
{
	public SubmitAndApprovePanel(String id)
	{
		super(id);
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		final GbAjaxButton submitButton = new GbAjaxButton("submitFinalGrades")
		{
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form)
			{

			}
		};
		add(submitButton);
		
		final GbAjaxButton approveButton = new GbAjaxButton("approveFinalGrades")
		{
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form)
			{

			}
		};
		add(approveButton);
	}
}
