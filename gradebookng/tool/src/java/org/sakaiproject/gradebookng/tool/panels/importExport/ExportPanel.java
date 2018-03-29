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
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;

import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbSiteType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FinalGradeFormatter;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CourseGrade;
import org.sakaiproject.util.FormattedText;

public class ExportPanel extends Panel {

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	private static final String CUSTOM_EXPORT_COLUMN_PREFIX = "# ";
	private static final char CSV_SEMICOLON_SEPARATOR = ';';

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

	private GbGroup selectedGroup;
	private final List<Assignment> allAssignments = businessService.getGradebookAssignments();

	// Model for file names; gets updated by buildFileName(), which is invoked by buildFile for effiency with determining csv vs zip wrt anonymity
	private final Model<String> fileNameModel = new Model<>();

	public ExportPanel(final String id) {
		super(id);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		final GbCategoryType categoryType = businessService.getGradebookCategoryType();
		final boolean studentNumbersVisible = businessService.isStudentNumberVisible();
		final boolean canUserSeeFinalGradesPage = businessService.currentUserCanSeeFinalGradesPage(businessService.getCurrentSiteId());
		final GbSiteType siteType = businessService.getCurrentSite()
				.map(s -> GbSiteType.COURSE.name().equalsIgnoreCase(s.getType()))
				.orElse(false) ? GbSiteType.COURSE : GbSiteType.PROJECT;

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

			@Override
			public boolean isVisible() {
				return studentNumbersVisible;
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

			@Override
			public boolean isVisible() {
				return siteType == GbSiteType.COURSE && canUserSeeFinalGradesPage;
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

		// OWL-3255 - Group/Section filter
		final List<GbGroup> groups = businessService.getSiteSectionsAndGroups();
		DropDownChoice<GbGroup> groupFilter = new DropDownChoice<>("groupFilter", new Model<>(), groups,
				new ChoiceRenderer<GbGroup>() {

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
				ExportPanel.this.selectedGroup = selected;
			}
		});

		// Determine visibility of group filter based on if Gradebook has any anonymous gradebook items;
		// do this before adding the 'All Students' group so that we don't end up with a drop down populated
		// with only the 'all' selection
		boolean siteHasAnonAssignments = allAssignments.stream().anyMatch(Assignment::isAnon);
		groupFilter.setVisible(!siteHasAnonAssignments && !groups.isEmpty());
		groups.add(0, GbGroup.all(getString("groups.all")));
		groupFilter.setNullValid(false);
		groupFilter.setModelObject(groupFilter.getChoices().get(0));
		add(groupFilter);

		add(new DownloadLink("downloadFullGradebook", new LoadableDetachableModel<File>() {

			@Override
			protected File load() {
				return buildFile(false, false);
			}

		}, fileNameModel).setCacheDuration(Duration.NONE).setDeleteAfterDownload(true));

		boolean isRevealedAllowed = false;
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

				for (Assignment assignment : allAssignments)
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
			isCourseGradePureAnon = businessService.isCourseGradePureAnonForAllAssignments(allAssignments);
		}

		char csvDelimiter = ".".equals(FormattedText.getDecimalSeparator()) ? CSVWriter.DEFAULT_SEPARATOR : CSV_SEMICOLON_SEPARATOR;
		try {
			if (isRevealed || !(hasAnon && hasNormal))
			{
				// Non mixed scenarios (Ie. csv scenarios)
				tempFile = File.createTempFile("gradebookTemplate", ".csv");

				//CSV separator is comma unless the comma is the decimal separator, then is ;
				try (FileWriter fw = new FileWriter(tempFile); final CSVWriter csvWriter = new CSVWriter(fw, csvDelimiter)){
					// Write an appropriate CSV via getCSVContents
					if (isRevealed)
					{
						writeLines(csvWriter, getCSVContents(isCustomExport, true, false, false));
					}
					else if (hasAnon)
					{
						writeLines(csvWriter, getCSVContents(isCustomExport, false, true, isCourseGradePureAnon));
					}
					else
					{
						writeLines(csvWriter, getCSVContents(isCustomExport, false, false, isCourseGradePureAnon));
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
					CSVWriter normalWriter = new CSVWriter(new OutputStreamWriter(zos), csvDelimiter);
					writeLines(normalWriter, getCSVContents(isCustomExport, false, false, isCourseGradePureAnon));
					normalWriter.flush();
					zos.closeEntry();

					ZipEntry anonEntry = new ZipEntry("gradebookExport_anonymous.csv");
					zos.putNextEntry(anonEntry);
					CSVWriter anonWriter = new CSVWriter(new OutputStreamWriter(zos), csvDelimiter);
					writeLines(anonWriter, getCSVContents(isCustomExport, false, true, isCourseGradePureAnon));
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
	 * @param isCustomExport whether this is a custom export
	 * @param isRevealed specifies we're preparing the CSV contents for the revealed export containing both normal and anonymous content
	 * @param isContextAnonymous only considered if isRevealed is false.
	 *    False = only normal content will be added to the CSV
	 *    True = only anonymous content will be added to the CSV
	 * @param isCourseGradePureAnon whether course grades should appear only in the anonymous context
	 */
	private List<List<String>> getCSVContents(boolean isCustomExport, boolean isRevealed, boolean isContextAnonymous, boolean isCourseGradePureAnon)
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
			final String assignmentPoints = FormatHelper.formatGradeForDisplay(assignment.getPoints().toString());
			String externalPrefix = "";
			if (assignment.isExternallyMaintained()) {
				externalPrefix = CUSTOM_EXPORT_COLUMN_PREFIX;
			}
			if (!isCustomExport || includeGradeItemScores) {
				header.add(externalPrefix + assignment.getName() + " [" + StringUtils.removeEnd(assignmentPoints, FormattedText.getDecimalSeparator() + "0") + "]");
			}
			if (!isCustomExport || includeGradeItemComments) {
				header.add(externalPrefix + "* " + assignment.getName());
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
		final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrixForImportExport(assignments, isContextAnonymous, selectedGroup);

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
							String grade = FormatHelper.formatGradeForDisplay(gradeInfo.getGrade());
							line.add(StringUtils.removeEnd(grade, FormattedText.getDecimalSeparator() + "0"));
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
					line.add(FormatHelper.formatGradeForDisplay(FormatHelper.formatDoubleToDecimal(courseGrade.getPointsEarned())));
				}
				if (isCustomExport && includeCourseGrade) {
					line.add(FormatHelper.formatGradeForDisplay(courseGrade.getCalculatedGrade()));
				}
				if (isCustomExport && includeFinalGrade) {
					line.add(FinalGradeFormatter.format(gbCourseGrade));
				}
				if (isCustomExport && includeGradeOverride) {
					line.add(FormatHelper.formatGradeForDisplay(courseGrade.getEnteredGrade()));
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
