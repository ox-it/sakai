package org.sakaiproject.gradebookng.tool.panels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbGradingType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.component.table.columns.GbColumnSortToggleLink;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.BasePage;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;
import org.sakaiproject.gradebookng.tool.util.GbUtils;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GraderPermission;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;

/**
 *
 * Header panel for each assignment column in the UI
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class AssignmentColumnHeaderPanel extends Panel {

	public static final String ICON_SAKAI = "icon-sakai--";
	
	private static final String PARENT_ID = "header";

	private final IModel<Assignment> modelData;
	private final GbGradingType gradingType;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public AssignmentColumnHeaderPanel(final String id, final Model<Assignment> modelData, final GbGradingType gradingType) {
		super(id);
		this.modelData = modelData;
		this.gradingType = gradingType;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		GbUtils.getParentCellFor(this, PARENT_ID).ifPresent(p -> p.setOutputMarkupId(true));

		final Assignment assignment = modelData.getObject();

		// get user's role
		final GbRole role = businessService.getUserRole();

		// do they have permission to edit this assignment?
		final boolean canEditAssignment = canUserEditAssignment(role, assignment);

		final IGradesPage gradebookPage = (IGradesPage) getPage();

		final GbColumnSortToggleLink title = new GbColumnSortToggleLink("title", Model.of(assignment.getName()))
		{	
			@Override
			protected SortDirection getSort(GradebookUiSettings settings)
			{
				GbAssignmentGradeSortOrder sort = settings.getAssignmentSortOrder();
				if (sort == null || !assignment.getId().equals(sort.getAssignmentId()))
				{
					// set to DESC because it will be toggled after
					sort = new GbAssignmentGradeSortOrder(assignment.getId(), SortDirection.DESCENDING);
					settings.setAssignmentSortOrder(sort);
				}
				
				return sort.getDirection();
			}

			@Override
			protected void setSort(GradebookUiSettings settings, SortDirection value)
			{
				settings.getAssignmentSortOrder().setDirection(value);
			}
		};
		title.add(new AttributeModifier("title", assignment.getName()));
		title.add(new Label("label", FormatHelper.abbreviateMiddle(assignment.getName())));

		// set the class based on the sortOrder. May not be set for this assignment so match it
		final GradebookUiSettings settings = gradebookPage.getUiSettings();
		if (settings != null && settings.getAssignmentSortOrder() != null
				&& settings.getAssignmentSortOrder().getAssignmentId() == assignment.getId()) {
			title.add(
					new AttributeModifier("class", "gb-sort-" + settings.getAssignmentSortOrder().getDirection().toString().toLowerCase()));
		}

		add(title);

		// if percentage then we want that as the 'outof' label
		// we also set the attribute here that is used for the cell by the JS
		final Label totalLabel = new Label("totalLabel");
		final Label totalPoints = new Label("totalPoints", Model.of(assignment.getPoints()));
		if (this.gradingType == GbGradingType.PERCENTAGE) {
			totalLabel.setDefaultModel(new ResourceModel("label.relativeweight"));
			totalPoints.add(new AttributeModifier("data-outof-label", new ResourceModel("label.percentage.plain")));
		} else {
			totalLabel.setDefaultModel(new ResourceModel("label.total"));
			totalPoints.add(new AttributeModifier("data-outof-label",
					new StringResourceModel("grade.outof", null, new Object[] { assignment.getPoints() })));
		}
		add(totalLabel);
		add(totalPoints);

		add(new Label("dueDate", Model.of(FormatHelper.formatDate(assignment.getDueDate(), getString("label.noduedate")))));

		final BasePage bp = (BasePage) gradebookPage;
		final WebMarkupContainer externalAppFlag = bp.buildFlagWithPopover("externalAppFlag", "");
		if (assignment.getExternalAppName() == null) {
			externalAppFlag.setVisible(false);
		} else {
			externalAppFlag.setVisible(true);
			externalAppFlag.add(new AttributeModifier("data-content",
					bp.generatePopoverContent(new StringResourceModel("label.gradeitem.externalapplabel",
							null, new Object[] { assignment.getExternalAppName() }).getString())));
			String iconClass = ICON_SAKAI + "default-tool";
			if ("Assignments".equals(assignment.getExternalAppName())) {
				iconClass = ICON_SAKAI + "sakai-assignment-grades";
			} else if ("Tests & Quizzes".equals(assignment.getExternalAppName())) {
				iconClass = ICON_SAKAI + "sakai-samigo";
			} else if ("Lesson Builder".equals(assignment.getExternalAppName())) {
				iconClass = ICON_SAKAI + "sakai-lessonbuildertool";
			}
			externalAppFlag
					.add(new AttributeModifier("class", "gb-external-app-flag " + iconClass));
		}
		add(externalAppFlag);

		add(bp.buildFlagWithPopover("extraCreditFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_EXTRA_CREDIT))
				.setVisible(assignment.isExtraCredit()));
		add(bp.buildFlagWithPopover("isCountedFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_COUNTED))
				.setVisible(assignment.isCounted()));
		boolean showNotCountedFlag = !assignment.isCounted();
		HeaderFlagPopoverPanel.Flag notCountedFlag = HeaderFlagPopoverPanel.Flag.GRADE_ITEM_NOT_COUNTED;
		if (assignment.getCategoryId() == null && businessService.categoriesAreEnabled())
		{
			showNotCountedFlag = true;
			notCountedFlag = HeaderFlagPopoverPanel.Flag.GRADE_ITEM_NOT_COUNTED_UNCATEGORIZED;
		}
		add(bp.buildFlagWithPopover("notCountedFlag", generateFlagPopover(notCountedFlag))
				.setVisible(showNotCountedFlag));
		add(bp.buildFlagWithPopover("isReleasedFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_RELEASED))
				.setVisible(assignment.isReleased()));
		add(bp.buildFlagWithPopover("notReleasedFlag", generateFlagPopover(HeaderFlagPopoverPanel.Flag.GRADE_ITEM_NOT_RELEASED))
				.setVisible(!assignment.isReleased()));

		add(new AttributeModifier("data-assignmentId", assignment.getId()));
		add(new AttributeModifier("data-category", assignment.getCategoryName()));
		if (GbCategoryType.WEIGHTED_CATEGORY.equals(businessService.getGradebookCategoryType()) && assignment.getWeight() != null) {
			add(new AttributeModifier("data-category-weight", String.format("%s%%", Math.round(assignment.getWeight() * 100))));
		}
		add(new AttributeModifier("data-category-extra-credit", assignment.isCategoryExtraCredit()));
		add(new AttributeModifier("data-category-order", assignment.getCategoryOrder()));

		// menu
		final WebMarkupContainer menu = new WebMarkupContainer("menu");

		menu.add(new GbAjaxLink<Long>("editAssignmentDetails", Model.of(assignment.getId())) {

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getAddOrEditGradeItemWindow();
				window.setTitle(getString("heading.editgradeitem"));
				window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(this, PARENT_ID).orElse(null));
				window.setContent(new AddOrEditGradeItemPanel(window.getContentId(), window, getModel()));
				window.showUnloadConfirmation(false);
				window.show(target);
			}

			@Override
			public boolean isVisible() {
				return role == GbRole.INSTRUCTOR;
			}
		});

		menu.add(new GbAjaxLink<Long>("viewAssignmentGradeStatistics", Model.of(assignment.getId())) {

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getGradeStatisticsWindow();
				window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(this, PARENT_ID).orElse(null));
				window.setContent(new GradeStatisticsPanel(window.getContentId(), getModel(), window));
				window.show(target);
			}
		});

		menu.add(new GbAjaxLink<Long>("moveAssignmentLeft", Model.of(assignment.getId())) {

			@Override
			public void onClick(final AjaxRequestTarget target) {
				// given the id, get the assignment, get the sort order, then update and refresh
				// note that we cannot use the passed in assignment sort order in here
				// as we may have had an async reorder on the front end but not had the model data updated,
				// so we just make sure we get it fresh

				final Long assignmentId = (Long) getModelObject();

				GradebookUiSettings settings = gradebookPage.getUiSettings();
				if (settings == null) {
					settings = new GradebookUiSettings();
				}

				if (settings.isCategoriesEnabled()) {
					try {
						final Integer order = calculateCurrentCategorizedSortOrder(assignmentId);
						AssignmentColumnHeaderPanel.this.businessService.updateAssignmentCategorizedOrder(assignmentId, (order - 1));
					} catch (final Exception e) {
						log.debug(e.getMessage());
						error("error reordering within category");
					}
				} else {
					final int order = AssignmentColumnHeaderPanel.this.businessService.getAssignmentSortOrder(assignmentId);
					AssignmentColumnHeaderPanel.this.businessService.updateAssignmentOrder(assignmentId, (order - 1));
				}

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				gradebookPage.addOrReplaceTable(null);
				gradebookPage.redrawSpreadsheet(target);
			}

			@Override
			public boolean isVisible() {
				return role == GbRole.INSTRUCTOR;
			}
		});

		menu.add(new GbAjaxLink<Long>("moveAssignmentRight", Model.of(assignment.getId())) {

			@Override
			public void onClick(final AjaxRequestTarget target) {

				final Long assignmentId = (Long) getModelObject();

				GradebookUiSettings settings = gradebookPage.getUiSettings();
				if (settings == null) {
					settings = new GradebookUiSettings();
				}

				if (settings.isCategoriesEnabled()) {
					try {
						final Integer order = calculateCurrentCategorizedSortOrder(assignmentId);
						AssignmentColumnHeaderPanel.this.businessService.updateAssignmentCategorizedOrder(assignmentId, (order + 1));
					} catch (final Exception e) {
						log.debug(e.getMessage());
						error("error reordering within category");
					}
				} else {
					final int order = AssignmentColumnHeaderPanel.this.businessService.getAssignmentSortOrder(assignmentId);
					AssignmentColumnHeaderPanel.this.businessService.updateAssignmentOrder(assignmentId, (order + 1));
				}

				// save settings
				gradebookPage.setUiSettings(settings);

				// refresh
				gradebookPage.addOrReplaceTable(null);
				gradebookPage.redrawSpreadsheet(target);
			}

			@Override
			public boolean isVisible() {
				return role == GbRole.INSTRUCTOR;
			}
		});

		menu.add(new GbAjaxLink<Long>("hideAssignment", Model.of(assignment.getId())) {

			@Override
			public void onClick(final AjaxRequestTarget target) {
				final Long assignmentId = (Long)getModelObject();
				target.appendJavaScript("sakai.gradebookng.spreadsheet.hideGradeItemAndSyncToolbar('" + assignmentId + "');");
			}
		});

		menu.add(new GbAjaxLink<Long>("setUngraded", Model.of(assignment.getId())) {

			@Override
			public void onClick(final AjaxRequestTarget target) {

				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getUpdateUngradedItemsWindow();
				final UpdateUngradedItemsPanel panel = new UpdateUngradedItemsPanel(window.getContentId(), getModel(), window);
				window.setTitle(getString("heading.updateungradeditems"));
				window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(this, PARENT_ID).orElse(null));
				window.setContent(panel);
				window.showUnloadConfirmation(false);
				window.show(target);

				panel.setOutputMarkupId(true);
				target.appendJavaScript("new GradebookUpdateUngraded($(\"#" + panel.getMarkupId() + "\"));");
			}

			@Override
			public boolean isVisible() {
				if (assignment.isExternallyMaintained()) {
					return false;
				}
				return canEditAssignment;
			}

		});

		// delete item
		menu.add(new GbAjaxLink<Long>("deleteGradeItem", Model.of(assignment.getId())) {

			@Override
			public void onClick(final AjaxRequestTarget target) {

				final GradebookPage gradebookPage = (GradebookPage) getPage();
				final GbModalWindow window = gradebookPage.getDeleteItemWindow();
				final DeleteItemPanel panel = new DeleteItemPanel(window.getContentId(), getModel(), window);
				window.setTitle(getString("delete.label"));
				window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(this, PARENT_ID).orElse(null));
				window.setContent(panel);
				window.showUnloadConfirmation(false);
				window.show(target);
			}

			@Override
			public boolean isVisible() {
				if (assignment.isExternallyMaintained()) {
					return false;
				}
				return role == GbRole.INSTRUCTOR;
			}
		});

		menu.add(new WebMarkupContainer("menuButton").add(
				new AttributeModifier("title", new StringResourceModel("assignment.menulabel", null,
						new Object[] { assignment.getName() }))));

		add(menu);

		// add abbreviation of header content to aid table accessibility
		GbUtils.getParentCellFor(this, PARENT_ID).ifPresent(p -> p.add(new AttributeModifier("abbr", assignment.getName()))
				.add(new AttributeModifier("aria-label", assignment.getName())));
	}

	private String generateFlagPopover(final HeaderFlagPopoverPanel.Flag flag) {
		final Map<String, Object> popoverModel = new HashMap<>();
		popoverModel.put("assignmentId", modelData.getObject().getId());
		popoverModel.put("flag", flag);
		return new HeaderFlagPopoverPanel("popover", Model.ofMap(popoverModel)).toPopoverString();
	}

	/**
	 * Get the assignment's current sort index within it's category. If this value is null in the database, best calculate this index from
	 * the assignments.
	 *
	 * @param assignmentId the id of the assignment
	 * @return the current sort index of the assignment within their category
	 */
	private Integer calculateCurrentCategorizedSortOrder(final Long assignmentId) {
		final Assignment assignment = businessService.getAssignment(assignmentId);
		Integer order = assignment.getCategorizedSortOrder();

		if (order == null) {
			// if no categorized order for assignment, calculate one based on the default sort order
			final List<Assignment> assignments = businessService.getGradebookAssignments();
			final List<Long> assignmentIdsInCategory = assignments.stream()
					.filter(a -> Objects.equals(a.getCategoryId(), assignment.getCategoryId()))
					.map(Assignment::getId)
					.collect(Collectors.toList());

			order = assignmentIdsInCategory.indexOf(assignmentId);
		}

		return order;
	}

	private boolean canUserEditAssignment(final GbRole role, final Assignment assignment) {
		if (role == GbRole.INSTRUCTOR) {
			return true;
		} else {
			final List<PermissionDefinition> permissions = businessService.getPermissionsForUser(businessService.getCurrentUser().getId());
			final boolean categoriesEnabled = businessService.categoriesAreEnabled();
			for (final PermissionDefinition permission : permissions) {
				final boolean gradePermission = permission.getFunction().equals(GraderPermission.GRADE.toString());

				if (gradePermission) {
					if (categoriesEnabled) {
						if ((assignment.getCategoryId() == null && permission.getCategoryId() == null) ||
								(assignment.getCategoryId() != null &&
										assignment.getCategoryId().equals(permission.getCategoryId()))) {
							return true;
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}
}
