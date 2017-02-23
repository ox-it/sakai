// Adapted from https://github.com/apache/wicket/blob/wicket-6.x/wicket-extensions/src/main/java/org/apache/wicket/extensions/markup/html/repeater/data/table/HeadersToolbar.java

package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort.AjaxFallbackOrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class InfinitePagingDataTableHeadersToolbar<S> extends InfinitePagingDataTableToolbar
{
	public <T> InfinitePagingDataTableHeadersToolbar(final InfinitePagingDataTable<T, S> table, final ISortStateLocator<S> stateLocator)
	{
		super(null, table);

		RefreshingView<IColumn<T, S>> headers = new RefreshingView<IColumn<T, S>>("headers")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected Iterator<IModel<IColumn<T, S>>> getItemModels()
			{
				List<IModel<IColumn<T, S>>> columnsModels = new LinkedList<>();

				for (IColumn<T, S> column : table.getColumns())
				{
					columnsModels.add(Model.of(column));
				}

				return columnsModels.iterator();
			}

			@Override
			protected void populateItem(Item<IColumn<T, S>> item)
			{
				final IColumn<T, S> column = item.getModelObject();

				WebMarkupContainer header;
				if (column.isSortable())
				{
					header = newSortableHeader("header", column.getSortProperty(), stateLocator);
				}
				else
				{
					header = new WebMarkupContainer("header");
				}

				if (column instanceof IStyledColumn)
				{
					InfinitePagingDataTable.CssAttributeBehavior cssAttributeBehavior = new InfinitePagingDataTable.CssAttributeBehavior()
					{
						private static final long serialVersionUID = 1L;

						@Override
						protected String getCssClass()
						{
							return ((IStyledColumn<?, S>)column).getCssClass();
						}
					};

					header.add(cssAttributeBehavior);
				}

				item.add(header);
				item.setRenderBodyOnly(true);
				header.add(column.getHeader("label"));
			}
		};

		add(headers);
		table.setOutputMarkupId(true);
	}

	protected WebMarkupContainer newSortableHeader(final String borderId, final S property, final ISortStateLocator<S> locator)
	{
		return new AjaxFallbackOrderByBorder<S>(borderId, property, locator, getAjaxCallListener())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onAjaxClick(final AjaxRequestTarget target)
			{
				target.add(getTable());
			}

			@Override
			protected void onSortChanged()
			{
				super.onSortChanged();
				getTable().setOffset(0);
			}
		};
	}

	protected IAjaxCallListener getAjaxCallListener()
	{
		return null;
	}
}
