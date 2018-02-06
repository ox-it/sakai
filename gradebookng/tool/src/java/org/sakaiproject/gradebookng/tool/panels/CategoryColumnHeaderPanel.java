package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbCategoryAverageSortOrder;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.table.columns.GbColumnSortToggleLink;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

/**
 *
 * Header panel for each category column in the UI
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class CategoryColumnHeaderPanel extends Panel {

	private final IModel<CategoryDefinition> modelData;

	public CategoryColumnHeaderPanel(final String id, final IModel<CategoryDefinition> modelData) {
		super(id);
		this.modelData = modelData;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final CategoryDefinition category = this.modelData.getObject();

		final GbColumnSortToggleLink title = new GbColumnSortToggleLink("title", Model.of(category.getName()))
		{
			/*@Override
			public void toggleSortOrder(GradebookUiSettings settings)
			{
				// if null, set a default sort, otherwise toggle, save, refresh.
				if (settings.getCategorySortOrder() == null
						|| !category.getId().equals(settings.getCategorySortOrder().getCategoryId())) {
					settings.setCategorySortOrder(new GbCategoryAverageSortOrder(category.getId(), SortDirection.ASCENDING));
				} else {
					final GbCategoryAverageSortOrder sortOrder = settings.getCategorySortOrder();
					SortDirection direction = sortOrder.getDirection();
					direction = direction.toggle();
					sortOrder.setDirection(direction);
					settings.setCategorySortOrder(sortOrder);
				}
			}*/
			
			@Override
			protected SortDirection getSort(GradebookUiSettings settings)
			{
				GbCategoryAverageSortOrder sort = settings.getCategorySortOrder();
				if (sort == null || !category.getId().equals(sort.getCategoryId()))
				{
					// set to DESC because it will be toggled after
					sort = new GbCategoryAverageSortOrder(category.getId(), SortDirection.DESCENDING);
					settings.setCategorySortOrder(sort);
				}
				
				return sort.getDirection();
			}

			@Override
			protected void setSort(GradebookUiSettings settings, SortDirection value)
			{
				settings.getCategorySortOrder().setDirection(value);
			}

		};
		title.add(new AttributeModifier("title", category.getName()));
		title.add(new Label("label", category.getName()));

		// set the class based on the sortOrder. May not be set for this category so match it
		final GradebookPage gradebookPage = (GradebookPage) getPage();
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		if (settings != null && settings.getCategorySortOrder() != null
				&& settings.getCategorySortOrder().getCategoryId() == category.getId()) {
			title.add(
					new AttributeModifier("class", "gb-sort-" + settings.getCategorySortOrder().getDirection().toString().toLowerCase()));
		}

		add(title);

		String categoryColor = settings.getCategoryColor(category.getName(), category.getId());

		final Component colorSwatch = gradebookPage.buildFlagWithPopover("categorySwatch",
				(new StringResourceModel("label.gradeitem.categoryaverage", this, null,
						new Object[] { category.getName() })).getString());
		colorSwatch.add(new AttributeAppender("style", String.format("background-color:%s;", categoryColor)));
		add(colorSwatch);
		
		// display the drop highest/drop lowest/keep highest settings for the column, if any
		// at most two of these settings can be active at the same time
		List<String> dropOptions = FormatHelper.formatCategoryDropInfo(category);
		String dropOption1 = dropOptions.size() > 0 ? dropOptions.get(0) : "";
		String dropOption2 = dropOptions.size() > 1 ? dropOptions.get(1) : "";
		add(new Label("dropOption1", Model.of(dropOption1)).setVisible(!dropOption1.isEmpty()));
		add(new Label("dropOption2", Model.of(dropOption2)).setVisible(!dropOption2.isEmpty()));
	}
}
