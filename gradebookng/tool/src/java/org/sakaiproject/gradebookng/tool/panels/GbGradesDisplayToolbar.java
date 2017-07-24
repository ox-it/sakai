package org.sakaiproject.gradebookng.tool.panels;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.tool.component.GbAjaxButton;
import org.sakaiproject.gradebookng.tool.component.table.SakaiDataTable;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GraderPermission;

/**
 *
 * @author plukasew, bjones86
 */
public class GbGradesDisplayToolbar extends GbBaseGradesDisplayToolbar
{	
	public GbGradesDisplayToolbar(String id, final IModel<Map<String, Object>> model, SakaiDataTable table, final boolean hasAssignmentsAndGrades)
	{
		super(id, table, Collections.emptyList(), hasAssignmentsAndGrades);
		setDefaultModel(model);
	}
	
	@Override
	protected void onInitialize()
	{
		// section and group dropdown
		groups = bus.getSiteSectionsAndGroups();
		
		super.onInitialize();
		
		GradebookPage page = (GradebookPage) getPage();
		
		Map<String, Object> model = (Map<String, Object>) getDefaultModelObject();
		final List<Assignment> assignments = (List<Assignment>) model.get("assignments");
		final List<CategoryDefinition> categories = (List<CategoryDefinition>) model.get("categories");
		
		final Label gradeItemSummary = new Label("gradeItemSummary",
				new StringResourceModel("label.toolbar.gradeitemsummary", null, assignments.size() + categories.size(),
						assignments.size() + categories.size()));
		gradeItemSummary.setEscapeModelStrings(false);
		add(gradeItemSummary);
		
		final GbAjaxButton addGradeItem = new GbAjaxButton("addGradeItem") {
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final GbModalWindow window = page.getAddOrEditGradeItemWindow();
				window.setTitle(getString("heading.addgradeitem"));
				window.setComponentToReturnFocusTo(this);
				window.setContent(new AddOrEditGradeItemPanel(window.getContentId(), window, null));
				window.show(target);
			}

			@Override
			public boolean isVisible() {
				return page.getCurrentUserRole() == GbRole.INSTRUCTOR;
			}
		};
		addGradeItem.setDefaultFormProcessing(false);
		addGradeItem.setOutputMarkupId(true);
		add(addGradeItem);
		
		final WebMarkupContainer toggleGradeItemsToolbarItem = new WebMarkupContainer("toggleGradeItemsToolbarItem");
		toggleGradeItemsToolbarItem.setVisible(hasAssignmentsAndGrades);
		add(toggleGradeItemsToolbarItem);

		// hide/show components

		// no assignments, hide
		if (assignments.isEmpty()) {
			toggleGradeItemsToolbarItem.setVisible(false);
		}
	}
	
	@Override
	protected boolean showGroupFilter()
	{	
		boolean show = !groups.isEmpty();
		
		// otherwise add the 'all groups' option
		// cater for the case where there is only one group visible to TA but they can see everyone.
		if (role == GbRole.TA) {

			//if only one group, hide the filter
			if (groups.size() == 1) {

				// but need to double check permissions to see if we have any permissions with no group reference
				/*permissions.forEach(p -> {
					if (!StringUtils.equalsIgnoreCase(p.getFunction(),GraderPermission.VIEW_COURSE_GRADE.toString()) && StringUtils.isBlank(p.getGroupReference())) {
						showGroupFilter = true;
					}
				});*/
				
				// but need to double check permissions to see if we have any permissions with no group reference
				show = permissions.stream().anyMatch(p -> !StringUtils.equalsIgnoreCase(p.getFunction(),GraderPermission.VIEW_COURSE_GRADE.toString())
						&& StringUtils.isBlank(p.getGroupReference()));
			}
		}
		
		return show;
	}
	
	@Override
	protected void handleShowGroupFilter()
	{
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

		groups.add(0, GbGroup.all(allGroupsTitle));
	}
}
