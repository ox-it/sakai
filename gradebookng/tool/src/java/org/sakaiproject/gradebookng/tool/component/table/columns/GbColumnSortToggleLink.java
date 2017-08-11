package org.sakaiproject.gradebookng.tool.component.table.columns;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;

/**
 *
 * @author plukasew
 */
public abstract class GbColumnSortToggleLink<T> extends GbAjaxLink<T>
{
	public GbColumnSortToggleLink(String id)
	{
		super(id);
	}
	
	public GbColumnSortToggleLink(String id, IModel<T> model)
	{
		super(id, model);
	}
	
	@Override
	public void onClick(AjaxRequestTarget target)
	{
		final IGradesPage gradebookPage = (IGradesPage) getPage();
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		
		toggle(settings);
		
		// save settings
		gradebookPage.setUiSettings(settings);

		// reset back to page one
		gradebookPage.resetPaging();
				
		// refresh
		gradebookPage.redrawSpreadsheet(target);
	}
	
	/**
	 * Toggles between ASC/DESC sort directions based on the value returned by getSort().
	 * If you need to do more, override this method
	 * @param settings 
	 */
	protected void toggle(GradebookUiSettings settings)
	{
		SortDirection sort = getSort(settings);
		setSort(settings, sort == null ? SortDirection.getDefault() : sort.toggle());
	}
	
	protected abstract SortDirection getSort(GradebookUiSettings settings);
	
	protected abstract void setSort(GradebookUiSettings settings, SortDirection value);
}
