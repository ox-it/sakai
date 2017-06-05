package org.sakaiproject.gradebookng.tool.component.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;



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
				IGradesPage page = (IGradesPage) getPage();
				page.redrawSpreadsheet(target);
			}
		});
		add(new SakaiNavigatorLabel("navigatorLabel", table));
	}
}
