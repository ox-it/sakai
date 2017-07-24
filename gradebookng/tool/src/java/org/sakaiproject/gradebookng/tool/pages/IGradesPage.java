package org.sakaiproject.gradebookng.tool.pages;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 *
 * @author plukasew
 */
public interface IGradesPage
{
	public String getCurrentUserUuid();

	public GbRole getCurrentUserRole();

	public Gradebook getGradebook();

	public List<GbStudentGradeInfo> refreshStudentGradeInfo();

	public GradebookUiSettings getUiSettings();

	public void setUiSettings(final GradebookUiSettings settings);

	public void redrawSpreadsheet(AjaxRequestTarget target);

	public void addOrReplaceTable(GbStopWatch stopwatch);
	
	public void redrawForGroupChange(AjaxRequestTarget target);
	
	public GbModalWindow getUpdateCourseGradeDisplayWindow();
	
	public GbModalWindow getGradeOverrideLogWindow();

	public GbModalWindow getUpdateUngradedItemsWindow();

	public GbModalWindow getStudentGradeSummaryWindow();

	public Component updateLiveGradingMessage(final String message);

	public void updatePageSize(int pageSize, AjaxRequestTarget target);

	public void setFocusedAssignmentID(long focusedAssignmentID);
}
