package org.sakaiproject.gradebookng.tool.pages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.finalgrades.CourseGradeStatistics;
import org.sakaiproject.gradebookng.business.finalgrades.CourseGradeSubmissionPresenter;
import org.sakaiproject.gradebookng.business.finalgrades.CourseGradeSubmitter;
import org.sakaiproject.gradebookng.business.finalgrades.CourseGradeSubmitter.GradeChangeReport;
import org.sakaiproject.gradebookng.business.finalgrades.CourseGradeSubmitter.SubmissionHistoryRow;
import org.sakaiproject.gradebookng.business.model.GbGroup;
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
import org.sakaiproject.gradebookng.tool.panels.finalgrades.CourseGradeSubmissionPanel.CourseGradeSubmissionData;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.CourseSummaryPanel;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.SectionStatisticsPanel.SectionStats;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.SubmissionHistoryPanel.SubmissionHistory;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.SubmitAndApprovePanel.SubmitAndApproveStatus;
import org.sakaiproject.service.gradebook.shared.PermissionDefinition;
import org.sakaiproject.service.gradebook.shared.owl.finalgrades.OwlGradeSubmissionGrades;
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
	
	private WebMarkupContainer spreadsheet;

	private List<PermissionDefinition> permissions = new ArrayList<>();
	private boolean showGroupFilter = true;
	
	private SakaiDataTable table;
	private CourseGradeSubmissionPanel submissionPanel;
	
	GbModalWindow updateCourseGradeDisplayWindow;
	GbModalWindow studentGradeSummaryWindow;
	GbModalWindow updateUngradedItemsWindow;
	
	private transient CourseGradeSubmitter submitter;
	private List<GbGroup> sections;

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
		
		sections = businessService.getSiteSections();
		
		CourseGradeSubmissionData data = new CourseGradeSubmissionData();
		updateStatsAndHistoryModel(data);
		form.add(submissionPanel = new CourseGradeSubmissionPanel("courseGradeSubmissionPanel", Model.of(data)));
		submissionPanel.setOutputMarkupId(true);
		
		updateCourseGradeDisplayWindow = new GbModalWindow("updateCourseGradeDisplayWindow");
		form.add(updateCourseGradeDisplayWindow);
		
		studentGradeSummaryWindow = new GbModalWindow("studentGradeSummaryWindow");
		studentGradeSummaryWindow.setWidthUnit("%");
		studentGradeSummaryWindow.setInitialWidth(70);
		form.add(studentGradeSummaryWindow);
		
		updateUngradedItemsWindow = new GbModalWindow("updateUngradedItemsWindow");
		form.add(updateUngradedItemsWindow);
		
		final GradebookUiSettings settings = getUiSettings();
		final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrixForFinalGrades(settings);

		this.hasAssignmentsAndGrades = !grades.isEmpty();

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
		modelData.put("categoryType", this.businessService.getGradebookCategoryType());
		modelData.put("categoriesEnabled", false);
		modelData.put("fixedColCount", businessService.isStudentNumberVisible() ? 4 : 3);

		table.addTopToolbar(new GbBaseHeadersToolbar(table, null));
		table.add(new AttributeModifier("data-siteid", this.businessService.getCurrentSiteId()));
		table.add(new AttributeModifier("data-gradestimestamp", gradesTimestamp.getTime()));
		
		spreadsheet = new WebMarkupContainer("spreadsheet");
		spreadsheet.setOutputMarkupId(true);
		//form.add(table);
		spreadsheet.add(table);
		
		final GbBaseGradesDisplayToolbar toolbar = new GbBaseGradesDisplayToolbar("toolbar", table, sections);
		//form.add(toolbar);
		spreadsheet.add(toolbar);
		
		form.add(spreadsheet);
	}
	
	public List<GbGroup> getSections()
	{
		return sections;
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
		//form.addOrReplace(liveGradingFeedback);
		spreadsheet.addOrReplace(liveGradingFeedback);
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
		final GradebookUiSettings settings = getUiSettings();
		final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrixForFinalGrades(settings);
		
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
			//target.add(form); // OWLTODO: it may be possible to re-render only some components of the form instead of the whole thing
			target.add(spreadsheet);
			
			// OWLTODO: constants, maybe in GbJsConstants class or something...
			//target.appendJavaScript("sakai.gradebookng.spreadsheet.$spreadsheet = $(\"#gradebookGrades\");");
			//target.appendJavaScript("sakai.gradebookng.spreadsheet.$table = $(\"#gradebookGradesTable\");");
			target.appendJavaScript("reinitSpreadsheet();");
			//target.appendJavaScript("sakai.gradebookng.spreadsheet.initTable();");
		}
	}
	
	// OWLTODO: probably move all this logic into CourseGradeSubmissionPanel or into CourseGradeSubmitter itself?
	private void updateStatsAndHistoryModel(CourseGradeSubmissionData data)
	{	
		// 1. refresh the provided members
		// 2. get the course grades
		// 3. get the stats, passing in above
		// 4. get the history, passing in above
		// 5. get the submission/approval status, passing in above
		// 6. update the dependant models
		
		// 1 and 2 (get course grades calls refresh)
		CourseGradeSubmitter submitter = getSubmitter();
		Set<OwlGradeSubmissionGrades> courseGrades = submitter.getCurrentCourseGrades();
		
		// 3
		CourseGradeStatistics stats = new CourseGradeStatistics(courseGrades);
		submitter.setStats(stats);
		int missing = submitter.getMissingGradeCount();  // requires a refresh but this was done above
		data.setStats(new SectionStats(stats, missing));
		
		// 4
		List<SubmissionHistoryRow> history = submitter.getSubmissionHistoryRowsForSelectedSection();
		GradeChangeReport report = submitter.getGradeChangeReport(courseGrades);
		String status = history.isEmpty() ? report.getFirstSubmissionMessage() : report.getCurrentStatusMessage();
		data.setHistory(new SubmissionHistory(history, status));
		
		// 5
		// button visible checks
		boolean isSubmitter = submitter.isCurrentUserAbleToSubmit();
		boolean isApprover = submitter.isCurrentUserAbleToApprove();
		// button enabled checks
		boolean canSubmit = isSubmitter && submitter.hasSubmittableGrades(new StringBuilder()); // OWLTODO: we need this message?
		boolean canApprove = isApprover && submitter.isSectionReadyForApprovalByCurrentUser();
		SubmitAndApproveStatus sas = SubmitAndApproveStatus.builder().canSubmit(isSubmitter).canApprove(isApprover)
				.submitReady(canSubmit).approveReady(canApprove).statusMsg(status).build();
		data.setButtonStatus(sas);
		
		data.setSectionExcluded(submitter.isSectionInExcludePrefixList());
	}
	
	private void redrawStatsAndHistory(AjaxRequestTarget target)
	{
		updateStatsAndHistoryModel(submissionPanel.getData());
		submissionPanel.redrawStats(target);
		submissionPanel.redrawHistory(target);
		submissionPanel.redrawButtons(target);
	}
	
	public void submitGrades(AjaxRequestTarget target)
	{
		// OWLTODO: submit() and updateStatsAndHistoryModel() do a lot of the same expensive work, so refactor the common functionality
		// out to improve performance
		getSubmitter().submit();
		redrawForSubmission(target);
	}
	
	public void approveGrades(AjaxRequestTarget target)
	{
		getSubmitter().approve();
		redrawForSubmission(target);
	}
	
	public void redrawForSubmission(AjaxRequestTarget target)
	{
		updateStatsAndHistoryModel(submissionPanel.getData());
		submissionPanel.redrawHistory(target);
		submissionPanel.redrawButtons(target);
		submissionPanel.redrawFeedback(target);
	}
	
	public void redrawForGradeChange(AjaxRequestTarget target)
	{
		updateStatsAndHistoryModel(submissionPanel.getData());
		submissionPanel.redrawStats(target);
		submissionPanel.redrawButtons(target);
	}
	
	public void submitAndApproveError(String msg)
	{
		submissionPanel.error(msg);
	}
	
	public void submitAndApproveMsg(String msg)
	{
		submissionPanel.success(msg);
	}
	
	@Override
	public void redrawForGroupChange(AjaxRequestTarget target)
	{
		redrawSpreadsheet(target);
		updateStatsAndHistoryModel(submissionPanel.getData());
		submissionPanel.redrawForSectionChange(target);
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
	public void addOrReplaceTable(GbStopWatch stopwatch) {}

	@Override
	public void setFocusedAssignmentID(long assignmentID) {}
	
	public CourseGradeSubmitter getSubmitter()
	{
		if (submitter == null)
		{
			submitter = new CourseGradeSubmitter(businessService, new CourseGradeSubmissionPresenter(this));
		}
		
		return submitter;
	}
}
