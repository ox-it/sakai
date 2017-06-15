package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;

import lombok.Setter;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbGradingType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.gradebookng.tool.component.GbHeadersToolbar;
import org.sakaiproject.gradebookng.tool.component.table.GbGradesDataProvider;
import org.sakaiproject.gradebookng.tool.component.table.SakaiDataTable;
import org.sakaiproject.gradebookng.tool.component.table.columns.HandleColumn;
import org.sakaiproject.gradebookng.tool.component.table.columns.StudentNameColumn;
import org.sakaiproject.gradebookng.tool.component.table.columns.StudentNumberColumn;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.panels.AssignmentColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.CategoryColumnCellPanel;
import org.sakaiproject.gradebookng.tool.panels.CategoryColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.CourseGradeItemCellPanel;
import org.sakaiproject.gradebookng.tool.panels.GbGradesDisplayToolbar;
import org.sakaiproject.gradebookng.tool.panels.GradeItemCellPanel;
import org.sakaiproject.gradebookng.tool.panels.ToggleGradeItemsToolbarPanel;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * Grades page. Instructors and TAs see this one. Students see the {@link StudentPage}.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradebookPage extends BasePage implements IGradesPage
{
	@Setter
	private long focusedAssignmentID = 0;

	// flag to indicate a category is uncategorised
	// doubles as a translation key
	public static final String UNCATEGORISED = "gradebookpage.uncategorised";
	
	public static final String COURSE_GRADE_COL_CSS_CLASS = "gb-course-grade";

	GbModalWindow addOrEditGradeItemWindow;
	GbModalWindow studentGradeSummaryWindow;
	GbModalWindow updateUngradedItemsWindow;
	GbModalWindow gradeLogWindow;
	GbModalWindow gradeCommentWindow;
	GbModalWindow deleteItemWindow;
	GbModalWindow gradeStatisticsWindow;
	GbModalWindow updateCourseGradeDisplayWindow;

	Label liveGradingFeedback;
	boolean hasAssignmentsAndGrades;

	Form<Void> form;

	List<PermissionDefinition> permissions = new ArrayList<>();
	boolean showGroupFilter = true;
	
	SakaiDataTable table;
	GbGradesDisplayToolbar toolbar;
	final RadioGroup anonymousToggle;
	ToggleGradeItemsToolbarPanel gradeItemsTogglePanel;
	WebMarkupContainer gradeItemsTogglePanelContainer;
	private transient Map<String, Object> toolbarModelData;
	private transient List<Assignment> assignments;
	private transient GbGradesDataProvider studentGradeMatrix;

	public GradebookPage() {
		disableLink(this.gradebookPageLink);

		// students cannot access this page, they have their own
		if (this.role == GbRole.STUDENT) {
			throw new RestartResponseException(StudentPage.class);
		}

		//TAs with no permissions or in a roleswap situation
		if(this.role == GbRole.TA){

			//roleswapped?
			if(this.businessService.isUserRoleSwapped()) {
				final PageParameters params = new PageParameters();
				params.add("message", getString("ta.roleswapped"));
				throw new RestartResponseException(AccessDeniedPage.class, params);
			}

			// no perms
			this.permissions = this.businessService.getPermissionsForUser(this.currentUserUuid);
			if(this.permissions.isEmpty()) {
				final PageParameters params = new PageParameters();
				params.add("message", getString("ta.nopermission"));
				throw new RestartResponseException(AccessDeniedPage.class, params);
			}
		}

		final GbStopWatch stopwatch = new GbStopWatch();
		stopwatch.start();
		stopwatch.time("GradebookPage init", stopwatch.getTime());

		this.form = new Form<>("form");
		form.setOutputMarkupId(true);
		add(this.form);

		/**
		 * Note that SEMI_TRANSPARENT has a 100% black background and TRANSPARENT is overridden to 10% opacity
		 */
		this.addOrEditGradeItemWindow = new GbModalWindow("addOrEditGradeItemWindow");
		this.addOrEditGradeItemWindow.showUnloadConfirmation(false);
		this.form.add(this.addOrEditGradeItemWindow);

		this.studentGradeSummaryWindow = new GbModalWindow("studentGradeSummaryWindow");
		this.studentGradeSummaryWindow.setWidthUnit("%");
		this.studentGradeSummaryWindow.setInitialWidth(70);
		this.form.add(this.studentGradeSummaryWindow);

		this.updateUngradedItemsWindow = new GbModalWindow("updateUngradedItemsWindow");
		this.form.add(this.updateUngradedItemsWindow);

		this.gradeLogWindow = new GbModalWindow("gradeLogWindow");
		this.form.add(this.gradeLogWindow);

		this.gradeCommentWindow = new GbModalWindow("gradeCommentWindow");
		this.form.add(this.gradeCommentWindow);

		this.deleteItemWindow = new GbModalWindow("deleteItemWindow");
		this.form.add(this.deleteItemWindow);

		this.gradeStatisticsWindow = new GbModalWindow("gradeStatisticsWindow");
		this.gradeStatisticsWindow.setPositionAtTop(true);
		this.form.add(this.gradeStatisticsWindow);

		this.updateCourseGradeDisplayWindow = new GbModalWindow("updateCourseGradeDisplayWindow");
		this.form.add(this.updateCourseGradeDisplayWindow);

		// first get any settings data from the session
		final GradebookUiSettings settings = getUiSettings();

		// Toggles the context between normal / anonymous items. Model is boolean: False->normal, True->anonymous  --bbailla2
		anonymousToggle = new RadioGroup("toggleAnonymous", Model.of(settings.isContextAnonymous()))
		{
			@Override
			public boolean isInputNullable()
			{
				return false;
			}
		};
		Radio anonToggle_normal = new Radio("anonToggle_normal", Model.of(Boolean.FALSE));
		Radio anonToggle_anonymous = new Radio("anonToggle_anonymous", Model.of(Boolean.TRUE));
		anonymousToggle.add(anonToggle_normal.add(new AjaxEventBehavior("onchange")
		{
			protected void onEvent(AjaxRequestTarget target)
			{
				// Flip the context to normal
				settings.setContextAnonymous(false);
				anonymousToggle.setModelObject(Boolean.FALSE);
				// Use default sort order. Maintaining any sort order would violate anonymity constraints
				settings.setNameSortOrder(GbStudentNameSortOrder.LAST_NAME);
				settings.setStudentSortOrder(SortDirection.ASCENDING);
				// Clear the group filter for single user group violation of anonymity constraint
				settings.setGroupFilter(GbGroup.all(getString("groups.all")));
				// repopulate all the data accordingly and redraw it
				addOrReplaceTable(null);
				redrawSpreadsheet(target);
			}
		}));
		anonymousToggle.add(anonToggle_anonymous.add(new AjaxEventBehavior("onchange")
		{
			protected void onEvent(AjaxRequestTarget target)
			{
				// Flip the context to anonymous
				settings.setContextAnonymous(true);
				anonymousToggle.setModelObject(Boolean.TRUE);
				// Clear filters and use the AnonIdSortOrder to eliminate any anonymity constraint violations
				settings.setStudentFilter("");
				settings.setStudentNumberFilter("");
				settings.setAnonIdSortOrder(SortDirection.ASCENDING);
				// Clear the group filter for single user group violation of anonymity constraint
				settings.setGroupFilter(GbGroup.all(getString("groups.all")));
				// repopulate all the data accordingly and redraw it
				addOrReplaceTable(null);
				redrawSpreadsheet(target);
			}
		}));
		// visibility is handled in addOrReplaceTable
		form.add(anonymousToggle);

		final WebMarkupContainer noAssignments = new WebMarkupContainer("noAssignments");
		noAssignments.setVisible(false);
		this.form.add(noAssignments);

		final WebMarkupContainer noStudents = new WebMarkupContainer("noStudents");
		noStudents.setVisible(false);
		this.form.add(noStudents);


		addOrReplaceTable(stopwatch);


		// hide/show components

		// no assignments, hide table, show message
		if (assignments.isEmpty()) {
			//noAssignments.setVisible(true);		OWL-2926: uncomment this to display the message (OWL-2927)
		}
		else if (studentGradeMatrix.size() == 0)
		{
			// no visible students, show table, show message
			// don't want two messages though, hence the else
			noStudents.setVisible(true);
		}

		stopwatch.time("Gradebook page done", stopwatch.getTime());
	}

	/**
	 * Constructs the Gradebook table, then adds it to the form (via form.addOrReplace for Ajax friendly updating).
	 * Class members this method initializes: toolbarModelData, assignments, studentGradeMatrix
	 * @param stopwatch a GbStopWatch instance. Will accept null.
	 */
	public void addOrReplaceTable(GbStopWatch stopwatch)
	{
		if (stopwatch == null)
		{
			stopwatch = new GbStopWatch();
		}

		final GradebookUiSettings settings = getUiSettings();

		final Gradebook gradebook = this.businessService.getGradebook();

		SortType sortBy = SortType.SORT_BY_SORTING;
		if (settings.isCategoriesEnabled()) {
			// Pre-sort assignments by the categorized sort order
			sortBy = SortType.SORT_BY_CATEGORY;
			this.form.add(new AttributeAppender("class", "gb-grouped-by-category"));
		}

		// get list of assignments. this allows us to build the columns and then
		// fetch the grades for each student for each assignment from
		// the map
		assignments = this.businessService.getGradebookAssignments(sortBy);
		stopwatch.time("getGradebookAssignments", stopwatch.getTime());

		// The anonymous toggle should be visible if the site has anonymous IDs, and there is both anonymous and normal content to view.
		boolean anonToggleVisible = false;
		boolean siteHasAnonIds = !businessService.getAnonGradingIDsForCurrentSite().isEmpty();
		if (siteHasAnonIds)
		{
			// Cases:
			// 1) Assignments all normal / empty: invisible & force context=normal
			// 2) Assignments mixed: visible
			// 3) Assignments all anonymous:
			//    a) none count towards course grade?: visible (content is mixed since course grades are normal)
			//    b) some count towards course grade?: invisible & force context = anonymous
			boolean hasNormal = false;
			boolean hasAnon = false;
			boolean hasCountingAnon = false;
			for (Assignment assignment : assignments)
			{
				if (assignment.isAnon())
				{
					hasAnon = true;
					if (assignment.isCounted())
					{
						hasCountingAnon = true;
					}
				}
				else
				{
					hasNormal = true;
				}

				if (hasNormal && hasAnon)
				{
					// Case 2) mixed scenario
					anonToggleVisible = true;
					break;
				}
			}

			if (!hasAnon)
			{
				// Case 1) Assignments are all normal / empty
				// Force this just in case an anonymous item was recently deleted and everything remaining is normal
				settings.setContextAnonymous(false);
			}
			else if (!hasNormal)
			{
				// Case 3) Assignments are all anonymous
				if (!hasCountingAnon)
				{
					// Case 3 a)
					// All assignmnents are anonymous, but none of them count toward the course grade.
					// Without counting items, the course grade is considered normal.
					// Content is mixed; the toggle needs to be visible
					anonToggleVisible = true;
				}
				else
				{
					// case 3 b)
					// only anonymous items exist and the course grade is anonymous; force the context to anonymous
					settings.setContextAnonymous(true);
				}
			}
		}
		anonymousToggle.setVisible(anonToggleVisible);

		final boolean isContextAnonymous = settings.isContextAnonymous();

		// populates settings.getAnonAwareAssignmentIDsForContext() and getCategoryIDsInAnonContext()
		businessService.setupAnonAwareAssignmentIDsAndCategoryIDsForContext(settings, assignments);
		List<Assignment> displayedAssignments = new ArrayList<>(assignments);
		displayedAssignments.removeIf(assignment -> assignment.isAnon() != isContextAnonymous);

		// get the grade matrix. It should be sorted if we have that info
		final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(assignments, settings);

		// mark the current timestamp so we can use this date to check for any changes since now
		final Date gradesTimestamp = new Date();

		stopwatch.time("buildGradeMatrix", stopwatch.getTime());

		// categories enabled?

		// grading type?
		final GbGradingType gradingType = GbGradingType.valueOf(gradebook.getGrade_type());

		// this could potentially be a sortable data provider
		//final ListDataProvider<GbStudentGradeInfo> studentGradeMatrix = new ListDataProvider<GbStudentGradeInfo>(grades);
		studentGradeMatrix = new GbGradesDataProvider(grades, this);
		final List<IColumn> cols = new ArrayList<>();

		cols.add(new HandleColumn());

		cols.add(new StudentNameColumn(this));
		
		// OWL student number column
		if (!isContextAnonymous && businessService.isStudentNumberVisible())
		{
			cols.add(new StudentNumberColumn());
		}

		boolean isCourseGradeColumnDisplayed = isContextAnonymous == businessService.isCourseGradePureAnonForAllAssignments(assignments);
		if (isCourseGradeColumnDisplayed)
		{
			// course grade column
			final boolean courseGradeVisible = this.businessService.isCourseGradeVisible(this.currentUserUuid);
			final AbstractColumn courseGradeColumn = new AbstractColumn(new Model("")) {
				@Override
				public Component getHeader(final String componentId) {
					return new CourseGradeColumnHeaderPanel(componentId, Model.of(settings.getShowPoints()));
				}

				@Override
				public String getCssClass() {
					final String cssClass = COURSE_GRADE_COL_CSS_CLASS;
					if (settings.getShowPoints()) {
						return cssClass + " points";
					} else {
						return cssClass;
					}
				}

				@Override
				public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
					final GbStudentGradeInfo studentGradeInfo = (GbStudentGradeInfo) rowModel.getObject();

					cellItem.add(new AttributeModifier("tabIndex", 0));

					// setup model
					// TODO we may not need to pass everything into this panel since we now use a display string
					// however we do require that the label can receive events and update itself, although this could be recalculated for each
					// event
					final Map<String, Object> modelData = new HashMap<>();
					modelData.put("courseGradeDisplay", studentGradeInfo.getCourseGrade().getDisplayString());
					modelData.put("hasCourseGradeOverride", studentGradeInfo.getCourseGrade().getCourseGrade().getEnteredGrade() != null);
					modelData.put("studentUuid", studentGradeInfo.getStudent().getUserUuid());
					modelData.put("currentUserUuid", GradebookPage.this.currentUserUuid);
					modelData.put("currentUserRole", GradebookPage.this.role);
					modelData.put("gradebook", gradebook);
					modelData.put("showPoints", settings.getShowPoints());
					modelData.put("showOverride", true);
					modelData.put("showLetterGrade", gradebook.isCourseLetterGradeDisplayed());  // OWLTODO: this is always true?
					modelData.put("courseGradeVisible", courseGradeVisible);

					cellItem.add(new CourseGradeItemCellPanel(componentId, Model.ofMap(modelData)));
					cellItem.setOutputMarkupId(true);
				}
			};
			cols.add(courseGradeColumn);
		}

		// build the rest of the columns based on the assignment list
		for (final Assignment assignment : assignments) {

			if (assignment.isAnon() != isContextAnonymous)
			{
				// skip
				continue;
			}

			final AbstractColumn column = new AbstractColumn(new Model(assignment)) {

				@Override
				public Component getHeader(final String componentId) {
					final AssignmentColumnHeaderPanel panel = new AssignmentColumnHeaderPanel(componentId,
							new Model<>(assignment), gradingType);

					panel.add(new AttributeModifier("data-category", assignment.getCategoryName()));
					panel.add(new AttributeModifier("data-category-id", assignment.getCategoryId()));

					if (assignment.getId().equals(focusedAssignmentID)) {
						panel.add(new AttributeModifier("class", "gb-item-focused"));
						focusedAssignmentID = 0;
					}

					return panel;
				}

				@Override
				public String getCssClass() {
					return "gb-grade-item-column-cell";
				}

				@Override
				public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
					final GbStudentGradeInfo studentGrades = (GbStudentGradeInfo) rowModel.getObject();

					final GbGradeInfo gradeInfo = studentGrades.getGrades().get(assignment.getId());
					final Map<String, Object> modelData = new HashMap<>();
					modelData.put("assignmentId", assignment.getId());
					modelData.put("assignmentName", assignment.getName());
					modelData.put("assignmentPoints", assignment.getPoints());
					modelData.put("studentUuid", studentGrades.getStudent().getUserUuid());
					modelData.put("studentName", studentGrades.getStudent().getDisplayName());
					modelData.put("categoryId", assignment.getCategoryId());
					modelData.put("isExternal", assignment.isExternallyMaintained());
					modelData.put("externalAppName", assignment.getExternalAppName());
					modelData.put("gradeInfo", gradeInfo);
					modelData.put("role", GradebookPage.this.role);
					modelData.put("gradingType", gradingType);

					cellItem.add(new GradeItemCellPanel(componentId, Model.ofMap(modelData)));

					cellItem.setOutputMarkupId(true);
				}
			};

			cols.add(column);
		}

		// render the categories
		// Display rules:
		// 1. only show categories if the global setting is enabled
		// 2. only show categories if they have items
		// TODO may be able to pass this list into the matrix to save another
		// lookup in there)

		final boolean categoriesEnabled = this.businessService.categoriesAreEnabled();
		List<CategoryDefinition> categories = new ArrayList<>();

		// Calculate # of fixed columns
		// 1 handle column
		// + 1 studentName column / grading ID column
		// + !isContextAnonymous && isStudentNumberVisible ? 1 : 0
		// + isCourseGradeColumnDisplayed ? 1 : 0
		int fixedColCount = 2 + (!isContextAnonymous && businessService.isStudentNumberVisible() ? 1 : 0) + (isCourseGradeColumnDisplayed ? 1 : 0);

		List<String> mixedCategoryNames = new ArrayList<>();
		if (categoriesEnabled) {

			// only work with categories if enabled
			categories = this.businessService.getGradebookCategories();

			// remove those that have no assignments
			// OWL-2545 also filter if all contained assignments' isAnon() mismatch isContextAnonymous(); this removes pure-anon and pure-normal categories from opposing contexts  --bbailla2
			categories.removeIf(cat -> 
			{
				return cat.getAssignmentList().stream().noneMatch(assignment->assignment.isAnon() == isContextAnonymous);
			});

			Collections.sort(categories, CategoryDefinition.orderComparator);

			int currentColumnIndex = fixedColCount;

			for (final CategoryDefinition category : categories) {
				
				// Get the assignmnet list for this category, but filter them removing assignments whose isAnon doesn't match the context
				List<Assignment> visibleAssignmentList = new ArrayList<>(category.getAssignmentList());
				// Before we filter, note that the anonymous context should not display 'mixed' category columns.
				// Mixed categories: categories containing both normal and anonymous items. Mixed category grades should only be displayed in the normal context
				if (isContextAnonymous)
				{
					// Do not need to seek anonymous items; if no anonymous items exist, the category would have already been filtered. Just seek normal items, then we know it's mixed
					if (visibleAssignmentList.stream().anyMatch(assignment->!assignment.isAnon()))
					{
						mixedCategoryNames.add(category.getName());
						continue;
					}
				}
				// Now filter out assignments that do not match the context
				visibleAssignmentList.removeIf(assignment -> assignment.isAnon() != isContextAnonymous);

				if (visibleAssignmentList.isEmpty()) {
					continue;
				}

				final AbstractColumn column = new AbstractColumn(new Model(category)) {

					@Override
					public Component getHeader(final String componentId) {
						final CategoryColumnHeaderPanel panel = new CategoryColumnHeaderPanel(componentId,
								new Model<>(category));

						panel.add(new AttributeModifier("data-category", category.getName()));

						return panel;
					}

					@Override
					public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
						final GbStudentGradeInfo studentGrades = (GbStudentGradeInfo) rowModel.getObject();

						final Double score = studentGrades.getCategoryAverages().get(category.getId());

						final Map<String, Object> modelData = new HashMap<>();
						modelData.put("score", score);
						modelData.put("studentUuid", studentGrades.getStudent().getUserUuid());
						modelData.put("categoryId", category.getId());

						cellItem.add(new CategoryColumnCellPanel(componentId, Model.ofMap(modelData)));
						cellItem.setOutputMarkupId(true);
					}

					@Override
					public String getCssClass() {
						return "gb-category-item-column-cell";
					}
				};

				if (settings.isCategoriesEnabled()) {
					// insert category column after assignments in that category
					currentColumnIndex = currentColumnIndex + visibleAssignmentList.size();
					cols.add(currentColumnIndex, column);
					currentColumnIndex = currentColumnIndex + 1;
				} else {
					// add to the end of the column list
					cols.add(column);
				}
			}
		}

		stopwatch.time("all Columns added", stopwatch.getTime());

		int pageSize = settings.getGradesPageSize();
		table = new SakaiDataTable("table", cols, studentGradeMatrix, true, pageSize) {
			@Override
			protected Item newCellItem(final String id, final int index, final IModel model) {
				return new Item(id, index, model) {
					@Override
					protected void onComponentTag(final ComponentTag tag) {
						super.onComponentTag(tag);

						final Object modelObject = model.getObject();

						if (modelObject instanceof AbstractColumn && 
							"studentColumn".equals(((AbstractColumn) modelObject).getDisplayModel().getObject())) {
							tag.setName("th");
							tag.getAttributes().put("role", "rowheader");
							tag.getAttributes().put("scope", "row");
						} else {
							tag.getAttributes().put("role", "gridcell");
						}
						tag.getAttributes().put("tabIndex", "0");
					}
				};
			}

			@Override
			protected Item newRowItem(final String id, final int index, final IModel model) {
				return new Item(id, index, model) {
					@Override
					protected void onComponentTag(final ComponentTag tag) {
						super.onComponentTag(tag);

						tag.getAttributes().put("role", "row");
					}
				};
			}

			@Override
			protected IModel<String> getCaptionModel() {
				return Model.of(getString("gradespage.caption"));
			}
		};

		toolbarModelData = new HashMap<>();
		toolbarModelData.put("assignments", displayedAssignments);
		toolbarModelData.put("categories", categories);
		toolbarModelData.put("categoryType", this.businessService.getGradebookCategoryType());
		toolbarModelData.put("categoriesEnabled", categoriesEnabled);
		toolbarModelData.put("fixedColCount", fixedColCount);

		table.addTopToolbar(new GbHeadersToolbar(table, null, Model.ofMap(toolbarModelData)));
		table.add(new AttributeModifier("data-siteid", this.businessService.getCurrentSiteId()));
		table.add(new AttributeModifier("data-gradestimestamp", gradesTimestamp.getTime()));

		// enable drag and drop based on user role (note: entity provider has
		// role checks on exposed API
		table.add(new AttributeModifier("data-sort-enabled", this.businessService.getUserRole() == GbRole.INSTRUCTOR));

		this.form.addOrReplace(table);

		// Populate the toolbar
		this.hasAssignmentsAndGrades = !assignments.isEmpty() && !grades.isEmpty();
		toolbar = new GbGradesDisplayToolbar("toolbar", Model.ofMap(toolbarModelData), table, hasAssignmentsAndGrades);
		form.addOrReplace(toolbar);

		List<Assignment> assignmentsInAnonContext = assignments.stream().filter(assignment-> isContextAnonymous == assignment.isAnon()).collect(Collectors.toList());
		gradeItemsTogglePanel = new ToggleGradeItemsToolbarPanel(
				"gradeItemsTogglePanel", Model.ofList(assignmentsInAnonContext), mixedCategoryNames);
		gradeItemsTogglePanelContainer = new WebMarkupContainer("gradeItemsTogglePanelContainer");
		gradeItemsTogglePanelContainer.setOutputMarkupId(true);
		gradeItemsTogglePanelContainer.add(gradeItemsTogglePanel);
		addOrReplace(gradeItemsTogglePanelContainer);
	}
	
	@Override
	public List<GbStudentGradeInfo> refreshStudentGradeInfo()
	{
		// first get any settings data from the session
		final GradebookUiSettings settings = getUiSettings();

		SortType sortBy = SortType.SORT_BY_SORTING;
		if (settings.isCategoriesEnabled()) {
			// Pre-sort assignments by the categorized sort order
			sortBy = SortType.SORT_BY_CATEGORY;
			this.form.add(new AttributeAppender("class", "gb-grouped-by-category"));
		}

		// get list of assignments. this allows us to build the columns and then
		// fetch the grades for each student for each assignment from
		// the map
		final List<Assignment> assignments = this.businessService.getGradebookAssignments(sortBy);

		// get the grade matrix. It should be sorted if we have that info
		final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(assignments, settings);
		
		// there is a timestamp put on the table that is used to check for concurrent modifications,
		// update it now that we've refreshed the grade matrix
		// See constructor lines around table.add(new AttributeModifier("data-gradestimestamp", gradesTimestamp.getTime()));
		table.add(AttributeModifier.replace("data-gradestimestamp", new Date().getTime()));
		
		return grades;
	}
	
	/**
	 * Getters for panels to get at modal windows
	 *
	 * @return
	 */
	public GbModalWindow getAddOrEditGradeItemWindow() {
		return this.addOrEditGradeItemWindow;
	}

	@Override
	public GbModalWindow getStudentGradeSummaryWindow() {
		return this.studentGradeSummaryWindow;
	}

	@Override
	public GbModalWindow getUpdateUngradedItemsWindow() {
		return this.updateUngradedItemsWindow;
	}

	public GbModalWindow getGradeLogWindow() {
		return this.gradeLogWindow;
	}

	public GbModalWindow getGradeCommentWindow() {
		return this.gradeCommentWindow;
	}

	public GbModalWindow getDeleteItemWindow() {
		return this.deleteItemWindow;
	}

	public GbModalWindow getGradeStatisticsWindow() {
		return this.gradeStatisticsWindow;
	}

	@Override
	public GbModalWindow getUpdateCourseGradeDisplayWindow() {
		return this.updateCourseGradeDisplayWindow;
	}

	/**
	 * Getter for the GradebookUiSettings. Used to store a few UI related settings for the current session only.
	 *
	 * TODO move this to a helper
	 * @return
	 */
	@Override
	public GradebookUiSettings getUiSettings() {

		GradebookUiSettings settings = (GradebookUiSettings) Session.get().getAttribute("GBNG_UI_SETTINGS");

		if (settings == null) {
			settings = new GradebookUiSettings();
			settings.setCategoriesEnabled(this.businessService.categoriesAreEnabled());
			settings.setCategoryColors(this.businessService.getGradebookCategories());
			setUiSettings(settings);
		}

		return settings;
	}

	public void setUiSettings(final GradebookUiSettings settings) {
		Session.get().setAttribute("GBNG_UI_SETTINGS", settings);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		// Drag and Drop/Date Picker (requires jQueryUI)
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/library/webjars/jquery-ui/1.11.3/jquery-ui.min.js?version=%s", version)));

		// Include Sakai Date Picker
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/library/js/lang-datepicker/lang-datepicker.js?version=%s", version)));

		// tablesorted used by student grade summary
		response.render(CssHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/css/theme.bootstrap.min.css?version=%s", version)));
		response.render(JavaScriptHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/js/jquery.tablesorter.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/js/jquery.tablesorter.widgets.min.js?version=%s", version)));

		// GradebookNG Grade specific styles and behaviour
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-grades.css?version=%s", version)));
		response.render(CssHeaderItem
				.forUrl(String.format("/gradebookng-tool/styles/gradebook-print.css?version=%s", version), "print"));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grades.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grade-summary.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
				.forUrl(String.format("/gradebookng-tool/scripts/gradebook-update-ungraded.js?version=%s", version)));
	}

	/**
	 * Helper to generate a RGB CSS color string with values between 180-250 to ensure a lighter color e.g. rgb(181,222,199)
	 * @return
	 */
	public String generateRandomRGBColorString() {
		final Random rand = new Random();
		final int min = 180;
		final int max = 250;

		final int r = rand.nextInt((max - min) + 1) + min;
		final int g = rand.nextInt((max - min) + 1) + min;
		final int b = rand.nextInt((max - min) + 1) + min;

		return String.format("rgb(%d,%d,%d)", r, g, b);
	}

	/**
	 * Comparator class for sorting Assignments in their categorised ordering
	 */
	class CategorizedAssignmentComparator implements Comparator<Assignment> {
		@Override
		public int compare(final Assignment a1, final Assignment a2) {
			// if in the same category, sort by their categorized sort order
			if (Objects.equals(a1.getCategoryId(), a2.getCategoryId())) {
				// handles null orders by putting them at the end of the list
				if (a1.getCategorizedSortOrder() == null) {
					return 1;
				} else if (a2.getCategorizedSortOrder() == null) {
					return -1;
				}
				return Integer.compare(a1.getCategorizedSortOrder(), a2.getCategorizedSortOrder());

				// otherwise, sort by their category order
			} else {
				if (a1.getCategoryOrder() == null && a2.getCategoryOrder() == null) {
					// both orders are null.. so order by A-Z
					if (a1.getCategoryName() == null && a2.getCategoryName() == null) {
						// both names are null so order by id
						return a1.getCategoryId().compareTo(a2.getCategoryId());
					} else if (a1.getCategoryName() == null) {
						return 1;
					} else if (a2.getCategoryName() == null) {
						return -1;
					} else {
						return a1.getCategoryName().compareTo(a2.getCategoryName());
					}
				} else if (a1.getCategoryOrder() == null) {
					return 1;
				} else if (a2.getCategoryOrder() == null) {
					return -1;
				} else {
					return a1.getCategoryOrder().compareTo(a2.getCategoryOrder());
				}
			}
		}
	}

	@Override
	public Component updateLiveGradingMessage(final String message) {
		/*this.liveGradingFeedback.setDefaultModel(Model.of(message));

		return this.liveGradingFeedback;*/
		
		return toolbar.updateLiveGradingMessage(message);
	}
	
	@Override
	public void updatePageSize(int pageSize, AjaxRequestTarget target)
	{
		if (pageSize < 0)
		{
			return;
		}
		
		getUiSettings().setGradesPageSize(pageSize);
		table.setItemsPerPage(pageSize);
		table.setCurrentPage(0);
		
		redrawSpreadsheet(target);
	}
	
	@Override
	public void redrawSpreadsheet(AjaxRequestTarget target)
	{
		if (target != null)
		{
			target.add(form); // OWLTODO: it may be possible to re-render only some components of the form instead of the whole thing
			target.add(gradeItemsTogglePanelContainer);
			
			Component togglePanel = this.get("gradeItemsTogglePanelContainer");
			target.add(togglePanel);
			
			target.appendJavaScript("sakai.gradebookng.spreadsheet.$spreadsheet = $(\"#gradebookGrades\");");
			target.appendJavaScript("sakai.gradebookng.spreadsheet.$table = $(\"#gradebookGradesTable\");");
			target.appendJavaScript("sakai.gradebookng.spreadsheet.initTable();");
		}
	}
	
	@Override
	public void redrawForGroupChange(AjaxRequestTarget target)
	{
		redrawSpreadsheet(target);
	}

	/**
	 * If the group filter is pointing to a group associated with a provider that contains AnonymousIDs  if it's not pointing to an anonymous section
	 * @return
	 */
	public String getGroupFilterProviderId()
	{
		GradebookUiSettings settings = getUiSettings();
		GbGroup group = settings.getGroupFilter();
		return group == null ? "" : StringUtils.trimToEmpty(group.getProviderId());
	}
	
	@Override
	public String getCurrentUserUuid()
	{
		return currentUserUuid;
	}
	
	@Override
	public GbRole getCurrentUserRole()
	{
		return role;
	}
	
	@Override
	public Gradebook getGradebook()
	{
		return gradebook;
	}
}
