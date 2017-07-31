/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.panels.AddOrEditGradeItemPanel;

/**
 * Button to show the add gradebook item (ie. column) dialog. Can only be used on GradebookPage
 * @author plukasew
 */
public class GbAddColumnButton extends SakaiAjaxButton
{
	public GbAddColumnButton(String id)
	{
		super(id);
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		setDefaultFormProcessing(false);
		setOutputMarkupId(true);
		setVisible(((GradebookPage) getPage()).getCurrentUserRole() == GbRole.INSTRUCTOR);
	}
	
	@Override
	public void onSubmit(final AjaxRequestTarget target, final Form form)
	{
		final GbModalWindow window = ((GradebookPage) getPage()).getAddOrEditGradeItemWindow();
		window.setTitle(getString("heading.addgradeitem"));
		window.setComponentToReturnFocusTo(this);
		window.setContent(new AddOrEditGradeItemPanel(window.getContentId(), window, null));
		window.show(target);
	}	
}
