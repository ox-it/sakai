package org.sakaiproject.gradebookng.tool.panels;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;

/**
 *
 * @author plukasew
 */
public class FinalGradeColumnHeaderPanel extends Panel
{
	public FinalGradeColumnHeaderPanel(final String id)
	{
		super(id);
	}
	
	@Override
	public void onInitialize()
	{
		super.onInitialize();
		
		final IGradesPage gradebookPage = (IGradesPage) getPage();
		
		getParentCellFor(this).setOutputMarkupId(true);
		
		final GbAjaxLink<String> title = new GbAjaxLink<String>("title")
		{
			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				final GradebookUiSettings settings = gradebookPage.getUiSettings();

				if (settings.getFinalGradeSortOrder() == null)
				{
					settings.setFinalGradeSortOrder(SortDirection.getDefault());
				}
				else
				{
					final SortDirection sortOrder = settings.getFinalGradeSortOrder();
					settings.setFinalGradeSortOrder(sortOrder.toggle());
				}

				gradebookPage.setUiSettings(settings);

				gradebookPage.redrawSpreadsheet(target);
			}
		};
		
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		title.add(new AttributeModifier("title", new ResourceModel("column.header.finalgrade")));
		title.add(new Label("label", new ResourceModel("column.header.finalgrade")));
		if (settings != null && settings.getFinalGradeSortOrder() != null) {
			title.add(
				new AttributeModifier("class", "gb-sort-" + settings.getFinalGradeSortOrder().toString().toLowerCase()));
		}
		add(title);
	}
	
	private Component getParentCellFor(final Component component)
	{
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "header")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}
}
