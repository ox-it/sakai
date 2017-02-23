package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

/**
 * @author plukasew
 * @param <T>
 * @param <S>
 */
public abstract class ClickablePropertyColumn<T, S> extends PropertyColumn<T, S>
{
	public ClickablePropertyColumn(final IModel<String> displayModel, final S sortProperty, final String propertyExpression)
	{
		super(displayModel, sortProperty, propertyExpression);
	}

	public ClickablePropertyColumn(final IModel<String> displayModel, final String propertyExpression)
	{
		super(displayModel, propertyExpression);
	}

	@Override
	public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel)
	{
		item.add(new LabelledLink<T>(componentId, getDataModel(rowModel), rowModel)
		{
			@Override
			protected void onClick()
			{
				ClickablePropertyColumn.this.onClick(rowModel);
			}
		});
	}

	protected abstract void onClick(final IModel<T> model);
}
