package org.sakaiproject.gradebookng.tool.component.table;

import java.util.Arrays;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;



/**
 *
 * @author plukasew
 */
public class GbSakaiPagerContainer extends Panel
{	
	public static final List<String> STANDARD_PAGE_SIZES = Arrays.asList( new String[] { "5", "10", "20", "50", "100", "200", "500", "1000"} );
	
	public GbSakaiPagerContainer(String id, final SakaiDataTable table)
	{
		this(id, table, STANDARD_PAGE_SIZES);
	}
	
	public GbSakaiPagerContainer(String id, final SakaiDataTable table, List<String> pageSizes)
	{
		super(id);
		setOutputMarkupId(true);
		
		add(new SakaiPagingNavigator("navigator", table, null, pageSizes)
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
