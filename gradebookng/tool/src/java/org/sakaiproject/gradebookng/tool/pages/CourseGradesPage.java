package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.CachedCMProvider;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.gradebookng.tool.component.GbBaseHeadersToolbar;
import org.sakaiproject.gradebookng.tool.component.table.GbGradesDataProvider;
import org.sakaiproject.gradebookng.tool.component.table.SakaiDataTable;
import org.sakaiproject.gradebookng.tool.component.table.columns.CourseGradeColumn;
import org.sakaiproject.gradebookng.tool.component.table.columns.FinalGradeColumn;
import org.sakaiproject.gradebookng.tool.component.table.columns.HandleColumn;
import org.sakaiproject.gradebookng.tool.component.table.columns.StudentNameColumn;
import org.sakaiproject.gradebookng.tool.component.table.columns.StudentNumberColumn;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.panels.GbBaseGradesDisplayToolbar;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.CourseGradeSubmissionPanel;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.CourseSummaryPanel;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;
import org.sakaiproject.service.gradebook.shared.SortType;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 *
 * @author plukasew
 */
public class CourseGradesPage extends BasePage implements IGradesPage
{
	private static final String SETTINGS_KEY = "GBNG_CG_UI_SETTINGS";
	
	private Label liveGradingFeedback;
	private boolean hasAssignmentsAndGrades;

	private Form<Void> form;

	private List<PermissionDefinition> permissions = new ArrayList<>();
	private boolean showGroupFilter = true;
	
	private SakaiDataTable table;
	
	GbModalWindow updateCourseGradeDisplayWindow;
	GbModalWindow studentGradeSummaryWindow;
	GbModalWindow updateUngradedItemsWindow;
	
	private transient CachedCMProvider cmProvider;
	
	public CourseGradesPage()
	{
		// students and TAs cannot access this page, redirect them to Grades page
		if (role == GbRole.STUDENT || role == GbRole.TA)
		{
			throw new RestartResponseException(GradebookPage.class);
		}
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		disableLink(courseGradesPageLink);
		
		form = new Form<>("form");
		form.setOutputMarkupId(true);
		add(form);

		final WebMarkupContainer noStudents = new WebMarkupContainer("noStudents");
		noStudents.setVisible(false);
		form.add(noStudents);
		
		form.add(new CourseSummaryPanel("courseSummaryPanel"));
		
		form.add(new CourseGradeSubmissionPanel("courseGradeSubmissionPanel"));
		
		updateCourseGradeDisplayWindow = new GbModalWindow("updateCourseGradeDisplayWindow");
		form.add(updateCourseGradeDisplayWindow);
		
		studentGradeSummaryWindow = new GbModalWindow("studentGradeSummaryWindow");
		studentGradeSummaryWindow.setWidthUnit("%");
		studentGradeSummaryWindow.setInitialWidth(70);
		form.add(studentGradeSummaryWindow);
		
		updateUngradedItemsWindow = new GbModalWindow("updateUngradedItemsWindow");
		form.add(updateUngradedItemsWindow);
		
		// first get any settings data from the session
		final GradebookUiSettings settings = getUiSettings();
		
		cmProvider = new CachedCMProvider(businessService, settings);

		SortType sortBy = SortType.SORT_BY_SORTING;
		/*if (settings.isCategoriesEnabled()) {
			// Pre-sort assignments by the categorized sort order
			sortBy = SortType.SORT_BY_CATEGORY;
			this.form.add(new AttributeAppender("class", "gb-grouped-by-category"));
		}*/

		// get list of assignments. this allows us to build the columns and then
		// fetch the grades for each student for each assignment from
		// the map
		// OWLTODO: make this only return the course grade
		final List<Assignment> assignments = this.businessService.getGradebookAssignments(sortBy);

		// get the grade matrix. It should be sorted if we have that info
		// OWLTODO: course grade only
		final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(assignments, settings, cmProvider);

		this.hasAssignmentsAndGrades = !assignments.isEmpty() && !grades.isEmpty();

		// mark the current timestamp so we can use this date to check for any changes since now
		final Date gradesTimestamp = new Date();
		
		final GbGradesDataProvider studentGradeMatrix = new GbGradesDataProvider(grades, this);
		final List<IColumn> cols = new ArrayList<>();

		// add an empty column that we can use as a handle for selecting the row
		cols.add(new HandleColumn());
		
		// student name column
		cols.add(new StudentNameColumn(this));
		
		// OWL student number column
		if (businessService.isStudentNumberVisible())
		{
			cols.add(new StudentNumberColumn());
		}
		
		// course grade column
		cols.add(new CourseGradeColumn(this, businessService.isCourseGradeVisible(currentUserUuid)));
		
		cols.add(new FinalGradeColumn(this));
		
		int pageSize = 100;  // OWLTODO: fix
		table = new SakaiDataTable("table", cols, studentGradeMatrix, true, pageSize)
		{
			@Override
			protected Item newCellItem(final String id, final int index, final IModel model) {
				return new Item(id, index, model) {
					@Override
					protected void onComponentTag(final ComponentTag tag) {
						super.onComponentTag(tag);

						final Object modelObject = model.getObject();

						if (modelObject instanceof AbstractColumn && "studentColumn"
								.equals(((AbstractColumn) modelObject).getDisplayModel().getObject())) {
							tag.setName("th");
							tag.getAttributes().put("role", "rowheader");
							tag.getAttributes().put("scope", "row");
						} else {
							tag.getAttributes().put("role", "gridcell");
						}
						tag.getAttributes().put("tabindex", "0");
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

		final Map<String, Object> modelData = new HashMap<>();
		modelData.put("assignments", assignments);
		modelData.put("categories", Collections.emptyList()); // OWLTODO: remove category stuff
		modelData.put("categoryType", this.businessService.getGradebookCategoryType());
		modelData.put("categoriesEnabled", false);
		modelData.put("fixedColCount", businessService.isStudentNumberVisible() ? 4 : 3);

		table.addTopToolbar(new GbBaseHeadersToolbar(table, null));
		table.add(new AttributeModifier("data-siteid", this.businessService.getCurrentSiteId()));
		table.add(new AttributeModifier("data-gradestimestamp", gradesTimestamp.getTime()));
		
		form.add(table);
		
		final GbBaseGradesDisplayToolbar toolbar = new GbBaseGradesDisplayToolbar("toolbar", table);
		form.add(toolbar);
	}
	
	@Override
	public void renderHead(final IHeaderResponse response)
	{
		super.renderHead(response);

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");

		// OWLTODO: figure out what we need below and remove the rest

		// tablesorted used by student grade summary
		response.render(CssHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/css/theme.bootstrap.min.css?version=%s", version)));
		response.render(JavaScriptHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/js/jquery.tablesorter.min.js?version=%s", version)));
		response.render(JavaScriptHeaderItem
			.forUrl(String.format("/library/js/jquery/tablesorter/2.27.7/js/jquery.tablesorter.widgets.min.js?version=%s", version)));

		// GradebookNG Grade specific styles and behaviour
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-grades.css?version=%s", version)));
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-print.css?version=%s", version), "print"));
		//response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grades.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-coursegrades.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-grade-summary.js?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-update-ungraded.js?version=%s", version)));
	}
	
	@Override
	public void onBeforeRender()
	{
		super.onBeforeRender();

		// add simple feedback nofication to sit above the table
		// which is reset every time the page renders
		liveGradingFeedback = new Label("liveGradingFeedback", getString("feedback.saved"));
		liveGradingFeedback.setVisible(hasAssignmentsAndGrades).setOutputMarkupId(true);

		// add the 'saving...' message to the DOM as the JavaScript will
		// need to be the one that displays this message (Wicket will handle
		// the 'saved' and 'error' messages when a grade is changed
		liveGradingFeedback.add(new AttributeModifier("data-saving-message", getString("feedback.saving")));
		form.addOrReplace(liveGradingFeedback);
	}

	@Override
	public Component updateLiveGradingMessage(final String message)
	{
		liveGradingFeedback.setDefaultModel(Model.of(message));

		return liveGradingFeedback;
	}
	
	/**
	 * Getter for the GradebookUiSettings. Used to store a few UI related settings for the current session only.
	 *
	 * TODO move this to a helper
	 */
	@Override
	public GradebookUiSettings getUiSettings()
	{
		//OWLTODO: revise as needed for this page
		GradebookUiSettings settings = (GradebookUiSettings) Session.get().getAttribute(SETTINGS_KEY);

		if (settings == null) {
			settings = new GradebookUiSettings();
			settings.setCategoriesEnabled(businessService.categoriesAreEnabled());
			settings.setCategoryColors(businessService.getGradebookCategories());
			setUiSettings(settings);
		}

		return settings;
	}
	
	@Override
	public void setUiSettings(final GradebookUiSettings settings)
	{
		Session.get().setAttribute(SETTINGS_KEY, settings);
	}
	
	@Override
	public List<GbStudentGradeInfo> refreshStudentGradeInfo()
	{
		// first get any settings data from the session
		final GradebookUiSettings settings = getUiSettings();

		SortType sortBy = SortType.SORT_BY_SORTING;
		/*if (settings.isCategoriesEnabled()) {
			// Pre-sort assignments by the categorized sort order
			sortBy = SortType.SORT_BY_CATEGORY;
			this.form.add(new AttributeAppender("class", "gb-grouped-by-category"));
		}*/

		// OWLTODO: we don't need the assignments here, just the course grades, build the grade matrix from that instead
		
		// get list of assignments. this allows us to build the columns and then
		// fetch the grades for each student for each assignment from
		// the map
		final List<Assignment> assignments = this.businessService.getGradebookAssignments(sortBy);

		// get the grade matrix. It should be sorted if we have that info
		final List<GbStudentGradeInfo> grades = this.businessService.buildGradeMatrix(assignments, settings, cmProvider);
		
		// there is a timestamp put on the table that is used to check for concurrent modifications,
		// update it now that we've refreshed the grade matrix
		// See constructor lines around table.add(new AttributeModifier("data-gradestimestamp", gradesTimestamp.getTime()));
		table.add(AttributeModifier.replace("data-gradestimestamp", new Date().getTime()));
		
		return grades;
	}
	
	@Override
	public void redrawSpreadsheet(AjaxRequestTarget target)
	{
		if (target != null)
		{
			target.add(form); // OWLTODO: it may be possible to re-render only some components of the form instead of the whole thing
			
			// OWLTODO: constants, maybe in GbJsConstants class or something...
			target.appendJavaScript("sakai.gradebookng.spreadsheet.$spreadsheet = $(\"#gradebookGrades\");");
			target.appendJavaScript("sakai.gradebookng.spreadsheet.$table = $(\"#gradebookGradesTable\");");
			target.appendJavaScript("sakai.gradebookng.spreadsheet.initTable();");
		}
	}
	
	@Override
	public GbModalWindow getUpdateCourseGradeDisplayWindow()
	{
		return updateCourseGradeDisplayWindow;
	}
	
	@Override
	public GbModalWindow getUpdateUngradedItemsWindow()
	{
		return updateUngradedItemsWindow;
	}
	
	@Override
	public GbModalWindow getStudentGradeSummaryWindow()
	{
		return studentGradeSummaryWindow;
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
	public CachedCMProvider getCMProvider()
	{
		return cmProvider;
	}

	@Override
	public void addOrReplaceTable(GbStopWatch stopwatch) {}

	@Override
	public void setFocusedAssignmentID(long assignmentID) {}
}
