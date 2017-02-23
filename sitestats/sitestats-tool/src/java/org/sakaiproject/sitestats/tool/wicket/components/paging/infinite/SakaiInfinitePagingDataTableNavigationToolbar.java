package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * @author plukasew
 */
public class SakaiInfinitePagingDataTableNavigationToolbar extends InfinitePagingDataTableToolbar
{
	public SakaiInfinitePagingDataTableNavigationToolbar(final InfinitePagingDataTable<?, ?> table)
	{
		super(null, table);

		WebMarkupContainer span = new WebMarkupContainer("span");
		add(span);
		span.add(AttributeModifier.replace("colspan", new AbstractReadOnlyModel<String>()
		{
			@Override
			public String getObject()
			{
				return String.valueOf(table.getColumns().size());
			}
		}));

		span.add(newPagingNavigator("navigator", table).setRenderBodyOnly(true));
		span.add(newNavigatorLabel("navigatorLabel", table).setRenderBodyOnly(true));
	}

	protected InfinitePagingNavigator newPagingNavigator(final String navigatorId, final InfinitePagingDataTable<?, ?> table)
	{
		return new InfinitePagingNavigator(navigatorId, table);
	}

	protected Label newNavigatorLabel(final String id, final InfinitePagingDataTable<?, ?> table)
	{
		return new Label(id, "")
		{

			@Override
			public void onConfigure()
			{
				long startRecord = table.getOffset();
				long rowCount = table.getRowCount();
				long endRecord = startRecord + rowCount;
				if (rowCount > 0)
				{
					++startRecord;
				}

				setDefaultModel(new StringResourceModel("paging_nav_label", table, new Model<>(), startRecord, endRecord));
			}
		};
	}
}
