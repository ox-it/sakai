package org.sakaiproject.gradebookng.tool.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.table.columns.HandleColumn;
import org.sakaiproject.gradebookng.tool.component.table.columns.StudentNameColumn;
import org.sakaiproject.gradebookng.tool.component.table.columns.StudentNumberColumn;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

public class GbHeadersToolbar<S> extends GbBaseHeadersToolbar<S> {

	private final IModel<Map<String, Object>> model;
	
	private static final String DH_SUFFIX = "-dh";
	private static final String HANDLE_COL_CSS_CLASS_DH = HandleColumn.HANDLE_COL_CSS_CLASS + DH_SUFFIX;
	private static final String STUDENT_COL_CSS_CLASS_DH = StudentNameColumn.STUDENT_COL_CSS_CLASS + DH_SUFFIX;
	private static final String STUDENT_NUM_COL_CSS_CLASS_DH = StudentNumberColumn.STUDENT_NUM_COL_CSS_CLASS + DH_SUFFIX;
	private static final String COURSE_GRADE_COL_CSS_CLASS_DH = GradebookPage.COURSE_GRADE_COL_CSS_CLASS + DH_SUFFIX;

	public <T> GbHeadersToolbar(final DataTable<T, S> table, final ISortStateLocator stateLocator, final IModel<Map<String, Object>> model) {
		super(table, stateLocator);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final IGradesPage page = (IGradesPage) getPage();
		final GradebookUiSettings settings = page.getUiSettings();

		final Map<String, Object> modelData = this.model.getObject();
		final boolean categoriesEnabled = (boolean) modelData.get("categoriesEnabled");
		
		// add dummy headers because table is fixed layout and categories row is using colspan
		// OWLTODO: see if we can remove this if not using categories
		List<? extends IColumn> columns = getTable().getColumns();
		ListView dummies = new ListView<IColumn>("dummyHeaders", columns)
		{
			@Override
			protected void populateItem(ListItem<IColumn> item)
			{
				WebMarkupContainer dummy = new WebMarkupContainer("dummyHeader");
				IColumn<?, S> col = item.getModelObject();
				if (col instanceof IStyledColumn)
				{
					String colCss = StringUtils.trimToEmpty(((IStyledColumn) col).getCssClass());
					String css = "";
					if (HandleColumn.HANDLE_COL_CSS_CLASS.equals(colCss))
					{
						css = HANDLE_COL_CSS_CLASS_DH;
					}
					else if (StudentNameColumn.STUDENT_COL_CSS_CLASS.equals(colCss))
					{
						css = STUDENT_COL_CSS_CLASS_DH;
					}
					else if (StudentNumberColumn.STUDENT_NUM_COL_CSS_CLASS.equals(colCss))
					{
						css = STUDENT_NUM_COL_CSS_CLASS_DH;
					}
					else if (colCss.startsWith(GradebookPage.COURSE_GRADE_COL_CSS_CLASS))
					{
						css = COURSE_GRADE_COL_CSS_CLASS_DH;
					}
					
					if (!css.isEmpty())
					{
						dummy.add(AttributeModifier.append("class", css));
					}
				}

				item.add(dummy);
			}
		};
		add(dummies);

		if (categoriesEnabled && settings.isCategoriesEnabled()) {	
			final WebMarkupContainer categoriesRow = new WebMarkupContainer("categoriesRow");
			
			final Label emptyCat = new Label("empty", Model.of(""));
			emptyCat.add(new AttributeModifier("colspan", (Integer) modelData.get("fixedColCount")));
			categoriesRow.add(emptyCat);

			final List<Assignment> assignments = (List<Assignment>) modelData.get("assignments");
			List<CategoryDefinition> categories = (List<CategoryDefinition>) modelData.get("categories");
			final GbCategoryType categoryType = (GbCategoryType) modelData.get("categoryType");

			Collections.sort(categories, CategoryDefinition.orderComparator);

			final Map<Long, Integer> categoryCounts = new HashMap<Long, Integer>();

			for (final CategoryDefinition category : categories) {
				categoryCounts.put(category.getId(), 0);
			}
			// take into account assignments without a category
			categoryCounts.put(null, 0);

			for (final Assignment assignment : assignments) {
				if (categoryCounts.containsKey(assignment.getCategoryId())) {
					final Integer increment = categoryCounts.get(assignment.getCategoryId()) + 1;
					categoryCounts.put(assignment.getCategoryId(), increment);
				}
			}

			categories = categories.stream().filter(c -> categoryCounts.get(c.getId()) > 0).collect(Collectors.toList());

			final List<String> mixedCategoryNames = (List<String>)modelData.get("mixedCategoryNames");

			categoriesRow.add(new ListView<CategoryDefinition>("categories", categories) {
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(final ListItem<CategoryDefinition> categoryItem) {
					final CategoryDefinition category = categoryItem.getModelObject();
					// add colspan attribute + 1 to account for the category average column (but don't increment if context is anonymous and category is mixed)
					int categoryCellIncrement = mixedCategoryNames.contains(category.getName()) ? 0 : 1;
					categoryItem.add(new AttributeModifier("colspan", categoryCounts.get(category.getId()) + categoryCellIncrement));
					categoryItem.add(new AttributeModifier("data-category-id", category.getId()));
					final String color = settings.getCategoryColor(category.getName());
					categoryItem.add(new AttributeModifier("style",
							String.format("background-color: %s;", color)));
					categoryItem.add(new Label("name", category.getName()));
					categoryItem.add(((BasePage) page).buildFlagWithPopover("extraCreditCategoryFlag",
							getString("label.gradeitem.extracreditcategory")).setVisible(category.isExtraCredit()));

					if (GbCategoryType.WEIGHTED_CATEGORY.equals(categoryType) && category.getWeight() != null) {
						final String weight = FormatHelper.formatDoubleAsPercentage(category.getWeight() * 100);
						categoryItem.add(new Label("weight", weight));
						categoryItem.add(new AttributeModifier("title",
							new StringResourceModel("label.gradeitem.categoryandweightheadertooltip", null,
								new Object[] {category.getName(), weight})));
					} else {
						categoryItem.add(new WebMarkupContainer("weight").setVisible(false));
						categoryItem.add(new AttributeModifier("title",
							new StringResourceModel("label.gradeitem.categoryheadertooltip", null,
								new Object[] {category.getName()})));

					}
				}
			});

			if (categoryCounts.get(null) > 0) {
				final WebMarkupContainer uncategorizedHeader = new WebMarkupContainer("uncategorized");
				uncategorizedHeader.add(new AttributeModifier("colspan", categoryCounts.get(null)));
				uncategorizedHeader.add(new AttributeModifier("title",
					getString("gradebookpage.uncategorised")));
				categoriesRow.add(uncategorizedHeader);
			} else {
				categoriesRow.add(new WebMarkupContainer("uncategorized").setVisible(false));
			}

			add(categoriesRow);
		} else {
			add(new WebMarkupContainer("categoriesRow").setVisible(false));
		}
	}
}
