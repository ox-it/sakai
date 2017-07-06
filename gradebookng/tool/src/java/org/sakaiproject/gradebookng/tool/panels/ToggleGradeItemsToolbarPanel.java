package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * Panel that renders the list of assignments and categories and allows the user to toggle each one on and off from the display.
 */
public class ToggleGradeItemsToolbarPanel extends Panel {

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<List<? extends Assignment>> model;
	List<String> mixedCategoryNames;
	boolean categoriesEnabled = false;

	public ToggleGradeItemsToolbarPanel(final String id, final IModel<List<? extends Assignment>> model, List<String> mixedCategoryNames) {
		super(id, model);
		this.model = model;
		this.mixedCategoryNames = mixedCategoryNames;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// setup
		final Map<String, Long> categoryNameToIdMap = new HashMap<>();
		final Map<String, List<Assignment>> categoryNamesToAssignments = new HashMap<>();

		final List<Assignment> assignments = (List<Assignment>) model.getObject();

		// only deal with categories if categories are enabled
		categoriesEnabled = businessService.categoriesAreEnabled();

		// iterate over assignments and build map of categoryname to list of assignments
		for (final Assignment assignment : assignments) {

			final String categoryName = getCategoryName(assignment);
			final Long categoryID = assignment.getCategoryId();

			if (!categoryNamesToAssignments.containsKey(categoryName)) {
				categoryNameToIdMap.put(categoryName, categoryID);
				categoryNamesToAssignments.put(categoryName, new ArrayList<>());
			}

			categoryNamesToAssignments.get(categoryName).add(assignment);
		}

		List<String> categoryNames = new ArrayList<>(categoryNameToIdMap.keySet());
		add(new ListView<String>("categoriesList", categoryNames) {

			@Override
			protected void populateItem(final ListItem<String> categoryItem) {
				final String categoryName = categoryItem.getModelObject();
				final Long categoryID = categoryNameToIdMap.get(categoryName);

				WebMarkupContainer categoryFilter = new WebMarkupContainer("categoryFilter");
				if (!ToggleGradeItemsToolbarPanel.this.categoriesEnabled) {
					categoryFilter.add(new AttributeAppender("class", " hide"));
					categoryItem.add(new AttributeAppender("class", " gb-no-categories"));
				}
				categoryItem.add(categoryFilter);

				final GradebookPage gradebookPage = (GradebookPage) getPage();

				GradebookUiSettings settings = gradebookPage.getUiSettings();

				final Label categoryLabel = new Label("category", categoryName);
				categoryLabel.add(new AttributeModifier("data-category-color", settings.getCategoryColor(categoryName, categoryID)));
				categoryFilter.add(categoryLabel);

				final CheckBox categoryCheckbox = new CheckBox("categoryCheckbox");
				categoryCheckbox.add(new AttributeModifier("value", categoryName));
				categoryCheckbox.add(new AttributeModifier("checked", "checked"));
				categoryFilter.add(categoryCheckbox);

				categoryItem.add(new ListView<Assignment>("assignmentsForCategory", categoryNamesToAssignments.get(categoryName)) {

					@Override
					protected void populateItem(final ListItem<Assignment> assignmentItem) {
						final Assignment assignment = assignmentItem.getModelObject();

						GradebookUiSettings settings = gradebookPage.getUiSettings();
						if (settings == null) {
							settings = new GradebookUiSettings();
							gradebookPage.setUiSettings(settings);
						}

						assignmentItem.add(new Label("assignmentTitle", FormatHelper.abbreviateMiddle(assignment.getName())));

						final CheckBox assignmentCheckbox = new AjaxCheckBox("assignmentCheckbox", Model.of(settings.isAssignmentVisible(assignment.getId()))) {
							@Override
							protected void onUpdate(final AjaxRequestTarget target) {
								GradebookUiSettings settings = gradebookPage.getUiSettings();
								if (settings == null) {
									settings = new GradebookUiSettings();
								}

								final Boolean value = settings.isAssignmentVisible(assignment.getId());
								settings.setAssignmentVisibility(assignment.getId(), !value);

								gradebookPage.setUiSettings(settings);
							}
						};
						assignmentCheckbox.add(new AttributeModifier("value", assignment.getId().toString()));
						assignmentCheckbox.add(new AttributeModifier("data-colidx", assignments.indexOf(assignment)));
						assignmentItem.add(assignmentCheckbox);
					}
				});

				final WebMarkupContainer categoryScoreFilter = new WebMarkupContainer("categoryScore");
				categoryScoreFilter.setVisible(!categoryName.equals( getString(GradebookPage.UNCATEGORISED) ));
				categoryScoreFilter.add(new Label("categoryScoreLabel",
						new StringResourceModel("label.toolbar.categoryscorelabel", null, new Object[] { categoryName })));

				final CheckBox categoryScoreCheckbox = new AjaxCheckBox("categoryScoreCheckbox", new Model<>(settings.isCategoryScoreVisible(categoryName))) {
					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						GradebookUiSettings settings = gradebookPage.getUiSettings();
						if (settings == null) {
							settings = new GradebookUiSettings();
						}

						final Boolean value = settings.isCategoryScoreVisible(categoryName);
						settings.setCategoryScoreVisibility(categoryName, !value);

						gradebookPage.setUiSettings(settings);
					}
				};
				categoryScoreCheckbox.add(new AttributeModifier("value", categoryName));
				// If the context is anonymous, we have to filter out category scores for mixed categories (scores for mixed categories should display in normal view only)
				boolean hideCategoryScores = settings.isContextAnonymous() && mixedCategoryNames.contains(categoryName);
				categoryScoreFilter.add(categoryScoreCheckbox).setVisible(!hideCategoryScores);

				categoryItem.add(categoryScoreFilter);
			}
		});
	}

	/**
	 * Helper to get the category name. Looks at settings as well.
	 *
	 * @param assignment
	 * @return
	 */
	private String getCategoryName(final Assignment assignment) {

		if (!this.categoriesEnabled) {
			return getString(GradebookPage.UNCATEGORISED);
		}

		return StringUtils.isBlank(assignment.getCategoryName()) ? getString(GradebookPage.UNCATEGORISED) : assignment.getCategoryName();
	}
}
