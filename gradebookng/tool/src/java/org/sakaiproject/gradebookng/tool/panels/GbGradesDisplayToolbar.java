/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sakaiproject.gradebookng.tool.panels;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.tool.component.table.GbSakaiPagerContainer;
import org.sakaiproject.gradebookng.tool.component.table.SakaiDataTable;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.GraderPermission;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;

/**
 *
 * @author plukasew
 */
public class GbGradesDisplayToolbar extends Panel
{
	private final SakaiDataTable table;
	boolean showGroupFilter;
	
	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
	
	public GbGradesDisplayToolbar(String id, final IModel<Map<String, Object>> model, SakaiDataTable table)
	{
		super(id);
		this.setDefaultModel(model);
		this.table = table;
		showGroupFilter = true;
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		GradebookPage page = (GradebookPage) getPage();
		
		Map<String, Object> model = (Map<String, Object>) getDefaultModelObject();
		final List<Assignment> assignments = (List<Assignment>) model.get("assignments");
		final List<CategoryDefinition> categories = (List<CategoryDefinition>) model.get("categories");
		final boolean categoriesEnabled = (Boolean) model.get("categoriesEnabled");
		
		final Label gradeItemSummary = new Label("gradeItemSummary",
				new StringResourceModel("label.toolbar.gradeitemsummary", null, assignments.size() + categories.size(),
						assignments.size() + categories.size()));
		gradeItemSummary.setEscapeModelStrings(false);
		add(gradeItemSummary);
		
		final WebMarkupContainer toggleGradeItemsToolbarItem = new WebMarkupContainer("toggleGradeItemsToolbarItem");
		add(toggleGradeItemsToolbarItem);
		
		final Button toggleCategoriesToolbarItem = new Button("toggleCategoriesToolbarItem") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				if (categoriesEnabled) {
					add(new AttributeAppender("class", " on"));
				}
				add(new AttributeModifier("aria-pressed", categoriesEnabled));
			}

			@Override
			public void onSubmit() {
				GradebookUiSettings settings = page.getUiSettings();
				settings.setCategoriesEnabled(!settings.isCategoriesEnabled());
				page.setUiSettings(settings);

				// refresh
				setResponsePage(GradebookPage.class);
			}

			@Override
			public boolean isVisible() {
				return categoriesEnabled && !assignments.isEmpty();
			}
		};
		add(toggleCategoriesToolbarItem);
		
		// section and group dropdown
		GbRole role = businessService.getUserRole();
		String currentUserUuid = businessService.getCurrentUser().getId();
		final List<GbGroup> groups = businessService.getSiteSectionsAndGroups();
		List<PermissionDefinition> permissions = businessService.getPermissionsForUser(currentUserUuid);
		

		// if only one group, just show the title
		// otherwise add the 'all groups' option
		// cater for the case where there is only one group visible to TA but they can see everyone.
		if (role == GbRole.TA) {

			//if only one group, hide the filter
			if (groups.size() == 1) {
				showGroupFilter = false;

				// but need to double check permissions to see if we have any permissions with no group reference
				permissions.forEach(p -> {
					if (!StringUtils.equalsIgnoreCase(p.getFunction(),GraderPermission.VIEW_COURSE_GRADE.toString()) && StringUtils.isBlank(p.getGroupReference())) {
						showGroupFilter = true;
					}
				});
			}
		}

		if(!showGroupFilter) {
			add(new Label("groupFilterOnlyOne", Model.of(groups.get(0).getTitle())));
		} else {
			add(new EmptyPanel("groupFilterOnlyOne").setVisible(false));

			// add the default ALL group to the list
			String allGroupsTitle = getString("groups.all");
			if (role == GbRole.TA) {

				// does the TA have any permissions set?
				// we can assume that if they have any then there is probably some sort of group restriction so we can change the label
				if (!permissions.isEmpty()) {
					allGroupsTitle = getString("groups.available");
				}
			}
			groups.add(0, new GbGroup(null, allGroupsTitle, null, GbGroup.Type.ALL, null));
		}

		final DropDownChoice<GbGroup> groupFilter = new DropDownChoice<>("groupFilter", new Model<GbGroup>(),
				groups, new ChoiceRenderer<GbGroup>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final GbGroup g) {
						return g.getTitle();
					}

					@Override
					public String getIdValue(final GbGroup g, final int index) {
						return g.getId();
					}

				});

		groupFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				final GbGroup selected = (GbGroup) groupFilter.getDefaultModelObject();

				// store selected group (null ok)
				final GradebookUiSettings settings = page.getUiSettings();
				settings.setGroupFilter(selected);
				page.setUiSettings(settings);

				// refresh
				setResponsePage(GradebookPage.class);
			}

		});
		// Hide group filter in anonymous contexts (prevents single user group exploit of anonymous constraint)
		groupFilter.setVisible(!page.getUiSettings().isContextAnonymous());

		// set selected group, or first item in list
		final GradebookUiSettings settings = page.getUiSettings();
		groupFilter.setModelObject((settings.getGroupFilter() != null) ? settings.getGroupFilter() : groups.get(0));
		groupFilter.setNullValid(false);

		// if only one item, hide the dropdown
		if (groups.size() == 1) {
			groupFilter.setVisible(false);
		}

		add(groupFilter);

		// hide/show components

		// no assignments, hide
		if (assignments.isEmpty()) {
			toggleGradeItemsToolbarItem.setVisible(false);
		}
		
		add(new GbSakaiPagerContainer("gradebookPager", table));
	}
}
