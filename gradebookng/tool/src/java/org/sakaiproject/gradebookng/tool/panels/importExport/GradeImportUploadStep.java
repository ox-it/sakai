package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.importExport.CommentValidationReport;
import org.sakaiproject.gradebookng.business.importExport.CommentValidator;
import org.sakaiproject.gradebookng.business.importExport.GradeValidationReport;
import org.sakaiproject.gradebookng.business.importExport.GradeValidator;
import org.sakaiproject.gradebookng.business.importExport.HeadingValidationReport;
import org.sakaiproject.gradebookng.business.importExport.UserIdentificationReport;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ImportedColumn;
import org.sakaiproject.gradebookng.business.model.ImportedRow;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
import org.sakaiproject.gradebookng.tool.component.SakaiAjaxButton;
import org.sakaiproject.gradebookng.tool.model.ImportWizardModel;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.pages.ImportExportPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * Upload/Download page
 */
@Slf4j
public class GradeImportUploadStep extends Panel {
	private final String panelId;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public GradeImportUploadStep(final String id) {
		super(id);
		panelId = id;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		add(new ExportPanel("export"));
		add(new UploadForm("form"));
	}

	/*
	 * Upload form
	 */
	private class UploadForm extends Form<Void> {

		FileUploadField fileUploadField;

		public UploadForm(final String id) {
			super(id);

			setMultiPart(true);
			setMaxSize(Bytes.megabytes(2));

			fileUploadField = new FileUploadField("upload");
			add(fileUploadField);

			SakaiAjaxButton continueButton = new SakaiAjaxButton("continuebutton") {
				@Override
				public void onSubmit(AjaxRequestTarget target, Form<?> form) {
					processUploadedFile(target);
				}
			};
			add(continueButton);

			final SakaiAjaxButton cancel = new SakaiAjaxButton("cancelbutton") {
				@Override
				public void onSubmit(AjaxRequestTarget target, Form<?> form) {
					setResponsePage(GradebookPage.class);
				}
			};
			cancel.setDefaultFormProcessing(false);
			add(cancel);
		}

		public void processUploadedFile(AjaxRequestTarget target) {

			final ImportExportPage page = (ImportExportPage) getPage();
			final FileUpload upload = fileUploadField.getFileUpload();
			if (upload != null) {

				log.debug("file upload success");

				// turn file into list
				// TODO would be nice to capture the values from these exceptions
				ImportedSpreadsheetWrapper spreadsheetWrapper;
				try {
					spreadsheetWrapper = ImportGradesHelper.parseImportedGradeFile(upload.getInputStream(), upload.getContentType(), upload.getClientFileName(), businessService);
				} catch (final InvalidFormatException e) {
					error(getString("importExport.error.incorrecttype"));
					return;
				} catch (final IOException e) {
					error(getString("importExport.error.unknown"));
					return;
				}

				if(spreadsheetWrapper == null) {
					error(getString("importExport.error.unknown"));
					return;
				}

				// If there are duplicate headings, tell the user now
				boolean hasValidationErrors = false;
				HeadingValidationReport headingReport = spreadsheetWrapper.getHeadingReport();
				SortedSet<String> duplicateHeadings = headingReport.getDuplicateHeadings();
				if (!duplicateHeadings.isEmpty()) {
					String duplicates = StringUtils.join(duplicateHeadings, ", ");
					error(MessageHelper.getString("importExport.error.duplicateColumns", duplicates));
					hasValidationErrors = true;
				}

				// If there are invalid headings, tell the user now
				SortedSet<String> invalidHeadings = headingReport.getInvalidHeadings();
				if (!invalidHeadings.isEmpty()) {
					String invalids = StringUtils.join(invalidHeadings, ", ");
					error(MessageHelper.getString("importExport.error.invalidColumns", invalids));
					hasValidationErrors = true;
				}

				// If there are blank headings, tell the user now
				int blankHeadings = headingReport.getBlankHeaderTitleCount();
				if (blankHeadings > 0) {
					error(MessageHelper.getString("importExport.error.blankHeadings", blankHeadings));
					hasValidationErrors = true;
				}

				// If there are duplicate student entires, tell the user now (we can't make the decision about which entry takes precedence)
				UserIdentificationReport userReport = spreadsheetWrapper.getUserIdentifier().getReport();
				SortedSet<GbUser> duplicateStudents = userReport.getDuplicateUsers();
				if (!duplicateStudents.isEmpty()) {
					String duplicates = StringUtils.join(duplicateStudents, ", ");
					error(MessageHelper.getString("importExport.error.duplicateStudents", duplicates));
					hasValidationErrors = true;
				}

				// Perform grade validation; present error message with invalid grades on current page
				boolean isSourceDPC = spreadsheetWrapper.isDPC();
				List<ImportedColumn> columns = spreadsheetWrapper.getColumns();
				List<ImportedRow> rows = spreadsheetWrapper.getRows();
				GradeValidationReport gradeReport = new GradeValidator().validate(rows, columns, isSourceDPC);
				SortedMap<String, SortedMap<String, String>> invalidGradesMap = gradeReport.getInvalidNumericGrades();
				if (!invalidGradesMap.isEmpty()) {
					Collection<SortedMap<String, String>> invalidGrades = invalidGradesMap.values();
					List<String> badGradeEntries = new ArrayList<>(invalidGrades.size());
					for (SortedMap<String, String> invalidGradeEntries : invalidGrades) {
						badGradeEntries.add(StringUtils.join(invalidGradeEntries.entrySet(), ", "));
					}

					String badGrades = StringUtils.join(badGradeEntries, ", ");
					error(MessageHelper.getString("importExport.error.invalidGradeData", badGrades));
					hasValidationErrors = true;
				}

				// Perform comment validation if the file is not a DPC; present error message with invalid comments on current page
				if (!isSourceDPC) {
					CommentValidationReport commentReport = new CommentValidator().validate(rows, columns);
					SortedMap<String, SortedMap<String, String>> invalidCommentsMap = commentReport.getInvalidComments();
					if (!invalidCommentsMap.isEmpty()) {
						Collection<SortedMap<String, String>> invalidComments = invalidCommentsMap.values();
						List<String> badCommentEntries = new ArrayList<>(invalidComments.size());
						for (SortedMap<String, String> invalidCommentEntries : invalidComments) {
							badCommentEntries.add(StringUtils.join(invalidCommentEntries.entrySet(), ", "));
						}

						String badComments = StringUtils.join(badCommentEntries, ", ");
						error(MessageHelper.getString("importExport.error.invalidComments", CommentValidator.MAX_COMMENT_LENGTH, badComments));
						hasValidationErrors = true;
					}
				}

				//get existing data
				final List<Assignment> assignments = businessService.getGradebookAssignments();
				final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrixForImportExport(assignments);

				// process file
				List<ProcessedGradeItem> processedGradeItems = ImportGradesHelper.processImportedGrades(spreadsheetWrapper, assignments, grades);

				// If the file has orphaned comment columns, tell the user now
				SortedSet<String> orphanedCommentColumns = headingReport.getOrphanedCommentHeadings();
				if (!orphanedCommentColumns.isEmpty()) {
					String invalids = StringUtils.join(orphanedCommentColumns, ", ");
					error(MessageHelper.getString("importExport.error.orphanedComments", invalids));
					hasValidationErrors = true;
				}

				// if empty there are no users
				if (processedGradeItems.isEmpty()) {
					error(getString("importExport.error.empty"));
					target.add(page.feedbackPanel);
					return;
				}

				// If there are no valid user entries, tell the user now
				boolean noValidUsers = true;
				for (ProcessedGradeItem item : processedGradeItems) {
					if (!item.getProcessedGradeItemDetails().isEmpty()) {
						noValidUsers = false;
					}
				}
				if (noValidUsers) {
					error(getString("importExport.error.noValidStudents"));
					hasValidationErrors = true;
				}

				// Return errors before processing further
				if (hasValidationErrors) {
					target.add(page.feedbackPanel);
					return;
				}

				// No validation errors were encountered; clear out previous errors and continue to the next step in the wizard
				page.clearFeedback();
				target.add(page.feedbackPanel);

				// Create the next panel
				final ImportWizardModel importWizardModel = new ImportWizardModel();
				importWizardModel.setProcessedGradeItems(processedGradeItems);
				importWizardModel.setUserReport(userReport);
				final Component newPanel = new GradeItemImportSelectionStep(GradeImportUploadStep.this.panelId, Model.of(importWizardModel));
				newPanel.setOutputMarkupId(true);

				// AJAX the new panel into place
				WebMarkupContainer container = page.container;
				container.addOrReplace(newPanel);
				target.add(container);
			} else {
				error(getString("importExport.error.noFileSelected"));
				target.add(page.feedbackPanel);
			}
		}
	}
}
