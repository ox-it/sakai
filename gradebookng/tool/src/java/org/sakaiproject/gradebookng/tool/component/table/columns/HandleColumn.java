
package org.sakaiproject.gradebookng.tool.component.table.columns;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * an empty column that we can use as a handle for selecting the row
 * 
 */
public class HandleColumn extends AbstractColumn
{
	public static final String HANDLE_COL_CSS_CLASS = "gb-row-selector";
	
	public HandleColumn()
	{
		super(new Model(""));
	}
	
	@Override
	public void populateItem(final Item cellItem, final String componentId, final IModel rowModel)
	{
		cellItem.add(new EmptyPanel(componentId));
	}

	@Override
	public String getCssClass()
	{
		return HANDLE_COL_CSS_CLASS;
	}
}
