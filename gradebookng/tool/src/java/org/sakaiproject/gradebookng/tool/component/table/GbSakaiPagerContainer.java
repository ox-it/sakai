/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sakaiproject.gradebookng.tool.component.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;



/**
 *
 * @author plukasew
 */
public class GbSakaiPagerContainer extends Panel
{	
	public GbSakaiPagerContainer(String id, final SakaiDataTable table)
	{
		super(id);
		setOutputMarkupId(true);
		
		add(new SakaiPagingNavigator("navigator", table)
		{
			@Override
			protected void onAjaxEvent(AjaxRequestTarget target)
			{
				super.onAjaxEvent(target);
				GradebookPage page = (GradebookPage) getPage();
				page.redrawSpreadsheet(target);
			}
		});
		add(new SakaiNavigatorLabel("navigatorLabel", table));
	}
}
