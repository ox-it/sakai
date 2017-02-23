package org.sakaiproject.sitestats.tool.wicket.components.paging.infinite;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.sitestats.tool.wicket.components.IndicatingAjaxDropDownChoice;
import org.sakaiproject.sitestats.tool.wicket.pages.BasePage;

/**
 * @author plukasew
 */
public class InfinitePagingNavigator extends Panel
{
	private final InfinitePagingDataTable table;
	private final String pageSizeSelection;

	public InfinitePagingNavigator(final String id, final InfinitePagingDataTable table)
	{
		super(id);
		this.table = table;
		pageSizeSelection = String.valueOf(table.getItemsPerPage());
	}

	public final InfinitePagingDataTable getDataTable()
	{
		return table;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(newPageSizeSelector(table));
		add(newInfinitePagingIncrementLink("prev", table, false));
		add(newInfinitePagingIncrementLink("next", table, true));
	}

	protected DropDownChoice<String> newPageSizeSelector(final InfinitePagingDataTable table)
	{
		final List<String> choices = new ArrayList<>();
		choices.add("5");
		choices.add("10");
		choices.add("20");
		choices.add("50");
		choices.add("100");
		choices.add("200"); // OWLTODO: fix hardcoded page sizes

		DropDownChoice<String> pageSizeSelector = new IndicatingAjaxDropDownChoice<>("pageSize",
				new PropertyModel<String>(this, "pageSizeSelection"), Model.ofList(choices),
				new IChoiceRenderer<String>()
				{
					@Override
					public Object getDisplayValue(String object)
					{
						return new StringResourceModel("pager_textPageSize", getParent(), null, new Object[] { object }).getString();
					}

					@Override
					public String getIdValue(String object, int index)
					{
						return object;
					}
				});
		pageSizeSelector.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				int pageSize = Integer.parseInt(pageSizeSelection);
				table.setItemsPerPage(pageSize);
				if (target != null)
				{
					target.add(table);
					target.appendJavaScript(BasePage.NO_SCROLLBAR);
				}
			}
		});

		return pageSizeSelector;
	}

	protected AbstractLink newInfinitePagingIncrementLink(String id, InfinitePagingDataTable table, boolean increment)
	{
		return new InfinitePagingNavigationIncrementLink(id, table, increment);
	}
}
