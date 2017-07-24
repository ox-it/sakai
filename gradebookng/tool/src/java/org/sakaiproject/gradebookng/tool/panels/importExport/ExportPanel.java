package org.sakaiproject.gradebookng.tool.panels.importExport;

import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.Session;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;

import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FinalGradeFormatter;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

public class ExportPanel extends Panel {

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private static final String CUSTOM_EXPORT_COLUMN_PREFIX = "# ";

	enum ExportFormat {
		CSV
	}

	// default export options
	ExportFormat exportFormat = ExportFormat.CSV;
	boolean includeStudentName = true;
	boolean includeStudentId = true;
	boolean includeStudentNumber = true;
	boolean includeGradeItemScores = true;
	boolean includeGradeItemComments = true;
	boolean includeFinalGrade = false;
	boolean includePoints = false;
	boolean includeLastLogDate = false;
	boolean includeCourseGrade = false;
	boolean includeGradeOverride = false;

	// Model for file names; gets updated by buildFileName(), which is invoked by buildFile for effiency with determining csv vs zip wrt anonymity
	private Model<String> fileNameModel = new Model<>();

	public ExportPanel(final String id) {
		super(id);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new AjaxCheckBox("includeStudentName", Model.of(includeStudentName)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentName = !ExportPanel.this.includeStudentName;
				setDefaultModelObject(ExportPanel.this.includeStudentName);
			}
		});
		add(new AjaxCheckBox("includeStudentId", Model.of(includeStudentId)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentId = !ExportPanel.this.includeStudentId;
				setDefaultModelObject(ExportPanel.this.includeStudentId);
			}
		});
		add(new AjaxCheckBox("includeStudentNumber", Model.of(includeStudentNumber)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeStudentNumber = !ExportPanel.this.includeStudentNumber;
				setDefaultModelObject(ExportPanel.this.includeStudentNumber);
			}
		});
		add(new AjaxCheckBox("includeGradeItemScores", Model.of(includeGradeItemScores)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeGradeItemScores = !ExportPanel.this.includeGradeItemScores;
				setDefaultModelObject(ExportPanel.this.includeGradeItemScores);
			}
		});
		add(new AjaxCheckBox("includeGradeItemComments", Model.of(includeGradeItemComments)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeGradeItemComments = !ExportPanel.this.includeGradeItemComments;
				setDefaultModelObject(ExportPanel.this.includeGradeItemComments);
			}
		});
		add(new AjaxCheckBox("includePoints", Model.of(includePoints)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includePoints = !ExportPanel.this.includePoints;
				setDefaultModelObject(ExportPanel.this.includePoints);
			}

			@Override
			public boolean isVisible() {
				// only allow option if categories are not weighted
				final GbCategoryType categoryType = ExportPanel.this.businessService.getGradebookCategoryType();
				return categoryType != GbCategoryType.WEIGHTED_CATEGORY;
			}
		});
		add(new AjaxCheckBox("includeLastLogDate", Model.of(includeLastLogDate)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeLastLogDate = !ExportPanel.this.includeLastLogDate;
				setDefaultModelObject(ExportPanel.this.includeLastLogDate);
			}
		});
		add(new AjaxCheckBox("includeFinalGrade", Model.of(includeFinalGrade)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeFinalGrade = !ExportPanel.this.includeFinalGrade;
				setDefaultModelObject(ExportPanel.this.includeFinalGrade);
			}
		});
		add(new AjaxCheckBox("includeCourseGrade", Model.of(includeCourseGrade)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeCourseGrade = !ExportPanel.this.includeCourseGrade;
				setDefaultModelObject(ExportPanel.this.includeCourseGrade);
			}
		});
		add(new AjaxCheckBox("includeGradeOverride", Model.of(includeGradeOverride)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget ajaxRequestTarget) {
				ExportPanel.this.includeGradeOverride = !ExportPanel.this.includeGradeOverride;
				setDefaultModelObject(ExportPanel.this.includeGradeOverride);
			}
		});

		add(new DownloadLink("downloadFullGradebook", new LoadableDetachableModel<File>() {

			@Override
			protected File load() {
				return buildFile(false, false);
			}

		}, fileNameModel).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

		boolean isRevealedAllowed = false;
		boolean siteHasAnonAssignments = businessService.getGradebookAssignments().stream().anyMatch(Assignment::isAnon);
		if (siteHasAnonAssignments)
		{
			// is gradebook in approved state?
			isRevealedAllowed = businessService.areAllSectionsApproved(businessService.getViewableSectionEids());
		}

		add(new DownloadLink("downloadFullGradebookRevealed", new LoadableDetachableModel<File>() {

			@Override
			protected File load() {
				return buildFile(false, true);
			}
		}, fileNameModel)
			.setCacheDuration(Duration.NONE)
			.setDeleteAfterDownload(true)
			.setVisible(isRevealedAllowed));

		add(new DownloadLink("downloadCustomGradebook", new LoadableDetachableModel<File>() {

			@Override
			protected File load() {
				return buildFile(true, false);
			}

		}, fileNameModel).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

		add(new DownloadLink("downloadCustomGradebookRevealed", new LoadableDetachableModel<File>() {

			@Override
			protected File load() {
				return buildFile(true, true);
			}
		}, fileNameModel)
			.setCacheDuration(Duration.NONE)
			.setDeleteAfterDownload(true)
			.setVisible(isRevealedAllowed));
	}

	/**
	 * Builds the export file. Will produce a zip if the anonymity is mixed; preduces a csv otherwise
	 * @param isCustomExport - whether the custom
	 * @param isRevealed - for anonymous exports
	 */
	private File buildFile(final boolean isCustomExport, final boolean isRevealed) {
		// The temporary file that will be returned. May be .csv, or .zip in the case of mixed anonymous content
		File tempFile;

		// get list of assignments. this allows us to build the columns and then fetch the grades for each student for each assignment from the map
		final List<Assignment> assignments = businessService.getGradebookAssignments();

		// Determine what kind of content we're working with - all normal / all anonymous / mixed, and build the file name with the appropriate extension
		boolean hasNormal = false;
		boolean hasAnon = false;
		if (isRevealed)
		{
			buildFileName("csv");
		}
		else
		{
			// Determine if the site is pure normal / mixed / pure anonymous
			boolean siteHasAnonIds = !businessService.getAnonGradingIDsForCurrentSite().isEmpty();
			if (siteHasAnonIds)
			{
				// cases:
				// 1) Assignments all normal: single CSV
				// 2) Assignments mixed: zip with normal & anon CSVs
				// 3) Assignments all anonymous:
				//    a) none count towards course grade?: zip (normal CSV with course grades; anon CSV with everything else)
				//    b) some count towards course grade?: anon CSV only

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
						// Prepare a zip
						buildFileName("zip");
						break;
					}
				}

				if (!hasAnon)
				{
					// Case 1) pure normal
					buildFileName("csv");
				}
				else if (!hasNormal)
				{
					// Case 3) all anonymous
					if (!hasCountingAnon)
					{
						// Case 3 a) - mixed scenario: all anon, but course grade is normal
						hasNormal = true;
						// Prepare a zip
						buildFileName("zip");
					}
					else
					{
						// Case 3 b) - pure anon
						buildFileName("csv");
					}
				}
			}
			else
			{
				// !siteHasAnonIds: all normal
				buildFileName("csv");
			}
		}


		// Are course grades anonymous? (only matters for non-revealed)
		boolean isCourseGradePureAnon = false;
		if (!isRevealed)
		{
			isCourseGradePureAnon = businessService.isCourseGradePureAnonForAllAssignments(assignments);
		}


		try {
			if (isRevealed || !(hasAnon && hasNormal))
			{
				// Non mixed scenarios (Ie. csv scenarios)
				tempFile = File.createTempFile("gradebookTemplate", ".csv");
				try (FileWriter fw = new FileWriter(tempFile); CSVWriter csvWriter = new CSVWriter(fw)){
					// Write an appropriate CSV via getCSVContents
					if (isRevealed)
					{
						writeLines(csvWriter, getCSVContents(assignments, isCustomExport, true, false, false));
					}
					else if (hasAnon)
					{
						writeLines(csvWriter, getCSVContents(assignments, isCustomExport, false, true, isCourseGradePureAnon));
					}
					else
					{
						writeLines(csvWriter, getCSVContents(assignments, isCustomExport, false, false, isCourseGradePureAnon));
					}

				}
			}
			else
			{
				// !isRevealed && hasAnon && hasNormal: zip file
				tempFile = File.createTempFile("gradebookTemplate", ".zip");
				FileOutputStream fos = new FileOutputStream(tempFile);
				try (ZipOutputStream zos = new ZipOutputStream(fos))
				{
					ZipEntry normalEntry = new ZipEntry("gradebookExport_normal.csv");
					zos.putNextEntry(normalEntry);
					CSVWriter normalWriter = new CSVWriter(new OutputStreamWriter(zos));
					writeLines(normalWriter, getCSVContents(assignments, isCustomExport, false, false, isCourseGradePureAnon));
					normalWriter.flush();
					zos.closeEntry();

					ZipEntry anonEntry = new ZipEntry("gradebookExport_anonymous.csv");
					zos.putNextEntry(anonEntry);
					CSVWriter anonWriter = new CSVWriter(new OutputStreamWriter(zos));
					writeLines(anonWriter, getCSVContents(assignments, isCustomExport, false, true, isCourseGradePureAnon));
					anonWriter.flush();
					zos.closeEntry();
				}
			}

		} catch (final IOException e) {
			Session.get().error("Unable to create export file");
			throw new RuntimeException(e);
		}

		return tempFile;
	}

	private void writeLines(final CSVWriter writer, List<List<String>> lines)
	{
		lines.stream().forEach(line ->
		{
			writer.writeNext(line.toArray(new String[] {}));
		});
	}

	/**
	 * Prepares a 2D List of CSV contents; outer list represents lines, inner list represents cells.
	 * @param allAssignments all the assignments in the course
	 * @param isCustomExport whether this is a custom export
	 * @param isRevealed specifies we're preparing the CSV contents for the revealed export containing both normal and anonymous content
	 * @param isContextAnonymous only considered if isRevealed is false.
	 *    False = only normal content will be added to the CSV
	 *    True = only anonymous content will be added to the CSV
	 * @param isCourseGradePureAnon whether course grades should appear only in the anonymous context
	 */
	private List<List<String>> getCSVContents(List<Assignment> allAssignments, boolean isCustomExport, boolean isRevealed, boolean isContextAnonymous,
												boolean isCourseGradePureAnon)
	{
		List<List<String>> csvContents = new ArrayList<>();

		// get a filtered list of the assignmnets for this CSV file
		List<Assignment> assignments;
		if (isRevealed)
		{
			assignments = allAssignments;
		}
		else if (isContextAnonymous)
		{
			assignments = allAssignments.stream().filter(Assignment::isAnon).collect(Collectors.toList());
		}
		else
		{
			assignments = allAssignments.stream().filter(assignment -> !assignment.isAnon()).collect(Collectors.toList());
		}

		final List<String> header = new ArrayList<>();
		if (isRevealed || !isContextAnonymous)
		{
			if (!isCustomExport || includeStudentId) {
				header.add(getString("importExport.export.csv.headers.studentId"));
			}
			if (!isCustomExport || includeStudentName) {
				header.add(getString("importExport.export.csv.headers.studentName"));
			}
			if (!isCustomExport || includeStudentNumber) {
				header.add(String.format("%s%s", CUSTOM_EXPORT_COLUMN_PREFIX, getString("importExport.export.csv.headers.studentNumber")));
			}
		}
		else
		{
			header.add(getString("importExport.export.csv.headers.anonId"));
		}

		// build column header
		assignments.forEach(assignment -> {
			final String assignmentPoints = assignment.getPoints().toString();
			if (!isCustomExport || includeGradeItemScores) {
				header.add(assignment.getName() + " [" + StringUtils.removeEnd(assignmentPoints, ".0") + "]");
			}
			if (!isCustomExport || includeGradeItemComments) {
				header.add("* " + assignment.getName());
			}
		});

		// Course grade related headers
		if (isRevealed || isContextAnonymous == isCourseGradePureAnon)
		{
			if (isCustomExport && includePoints) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.points")));
			}
			if (isCustomExport && includeCourseGrade) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.courseGrade")));
			}
			if (isCustomExport && includeFinalGrade) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.finalGrade")));
			}
			if (isCustomExport && includeGradeOverride) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.gradeOverride")));
			}
			if (isCustomExport && includeLastLogDate) {
				header.add(String.format("%s%s",
					CUSTOM_EXPORT_COLUMN_PREFIX,
					getString("importExport.export.csv.headers.lastLogDate")));
			}
		}

		csvContents.add(header);

		// get the grade matrix
		// OWLTODO: add param to eliminate duplicate course grade retrieval
		final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrixForImportExport(assignments, isContextAnonymous);

		//add grades
		grades.forEach(studentGradeInfo -> {
			final List<String> line = new ArrayList<>();
			if (isRevealed || !isContextAnonymous)
			{
				if (!isCustomExport || includeStudentId) {
					line.add(studentGradeInfo.getStudent().getEid());
				}
				if (!isCustomExport || includeStudentName) {
					line.add(studentGradeInfo.getStudent().getLastName() + ", " + studentGradeInfo.getStudent().getFirstName());
				}
				if (!isCustomExport || includeStudentNumber) {
					line.add(studentGradeInfo.getStudent().getStudentNumber());
				}
			}
			else
			{
				// !isRevealed && isContextAnonymous: get Anon ID
				line.add(studentGradeInfo.getStudent().getAnonId());
			}
			if (!isCustomExport || includeGradeItemScores || includeGradeItemComments) {
				assignments.forEach(assignment -> {
					final GbGradeInfo gradeInfo = studentGradeInfo.getGrades().get(assignment.getId());
					if (gradeInfo != null)
					{
						if (!isCustomExport || includeGradeItemScores) {
							line.add(StringUtils.removeEnd(gradeInfo.getGrade(), ".0"));
						}
						if (!isCustomExport || includeGradeItemComments) {
							line.add(gradeInfo.getGradeComment());
						}
					} else {
						// Need to account for no grades
						if (!isCustomExport || includeGradeItemScores) {
							line.add(null);
						}
						if (!isCustomExport || includeGradeItemComments) {
							line.add(null);
						}
					}
				});
			}

			if (isRevealed || isContextAnonymous == isCourseGradePureAnon)
			{
				final GbCourseGrade gbCourseGrade = studentGradeInfo.getCourseGrade();
				final CourseGrade courseGrade = gbCourseGrade.getCourseGrade();

				if (isCustomExport && includePoints) {
					line.add(FormatHelper.formatDoubleToDecimal(courseGrade.getPointsEarned()));
				}
				if (isCustomExport && includeCourseGrade) {
					line.add(courseGrade.getCalculatedGrade());
				}
				if (isCustomExport && includeFinalGrade) {
					line.add(FinalGradeFormatter.format(gbCourseGrade));
				}
				if (isCustomExport && includeGradeOverride) {
					line.add(courseGrade.getEnteredGrade());
				}
				if (isCustomExport && includeLastLogDate) {
					if (courseGrade.getDateRecorded() == null) {
						line.add(null);
					} else {
						line.add(FormatHelper.formatDateTime(courseGrade.getDateRecorded()));
					}
				}
			}

			csvContents.add(line);
		});

		return csvContents;
	}

	private void buildFileName(String extension) {
		final String prefix = "gradebook_export";
		String gradebookName = businessService.getGradebook().getName();

		if (StringUtils.trimToNull(gradebookName) == null) {
			fileNameModel.setObject(String.format("%s.%s", gradebookName, extension));
		} else {
			gradebookName = gradebookName.replaceAll("\\s", "_");
			fileNameModel.setObject(String.format("%s-%s.%s", prefix, gradebookName, extension));
		}
	}
}
