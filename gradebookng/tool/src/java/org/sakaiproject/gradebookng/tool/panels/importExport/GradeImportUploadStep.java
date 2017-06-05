package org.sakaiproject.gradebookng.tool.panels.importExport;

import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.lang.Bytes;

import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.exception.GbImportCommentMissingItemException;
import org.sakaiproject.gradebookng.business.importExport.GradeValidationReport;
import org.sakaiproject.gradebookng.business.importExport.GradeValidator;
import org.sakaiproject.gradebookng.business.importExport.ImportHeadingValidationReport;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.gradebookng.business.util.ImportGradesHelper;
import org.sakaiproject.gradebookng.business.util.MessageHelper;
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

			add(new Button("continuebutton"));

			final Button cancel = new Button("cancelbutton") {
				@Override
				public void onSubmit() {
					setResponsePage(GradebookPage.class);
				}
			};
			cancel.setDefaultFormProcessing(false);
			add(cancel);
		}

		@Override
		public void onSubmit() {

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
				ImportHeadingValidationReport headingReport = spreadsheetWrapper.getHeadingReport();
				SortedSet<String> duplicateHeadings = headingReport.getDuplicateHeadings();
				if (!duplicateHeadings.isEmpty()) {
					String duplicates = StringUtils.join(duplicateHeadings, ", ");
					error(MessageHelper.getString("importExport.error.duplicateColumns", duplicates));
					return;
				}

				// If there are invalid headings, tell the user now
				SortedSet<String> invalidHeadings = headingReport.getInvalidHeadings();
				if (!invalidHeadings.isEmpty()) {
					String invalids = StringUtils.join(invalidHeadings, ", ");
					error(MessageHelper.getString("importExport.error.invalidColumns", invalids));
					return;
				}

				// If there are blank headings, tell the user now
				int blankHeadings = headingReport.getBlankHeaderTitleCount();
				if (blankHeadings > 0) {
					error(MessageHelper.getString("importExport.error.blankHeadings", blankHeadings));
					return;
				}

				// If there are duplicate student entires, tell the user now (we can't make the decision about which entry takes precedence)
				SortedSet<GbUser> duplicateStudents = spreadsheetWrapper.getUserIdentifier().getReport().getDuplicateUsers();
				if (!duplicateStudents.isEmpty()) {
					String duplicates = StringUtils.join(duplicateStudents, ", ");
					error(MessageHelper.getString("importExport.error.duplicateStudents", duplicates));
					return;
				}

				// Perform grade validation for DPC files; present error message with invalid grades on current page
				boolean validateGrades = spreadsheetWrapper.isDPC();
				if (validateGrades) {
					GradeValidationReport report = new GradeValidator().validate(spreadsheetWrapper.getRows(), validateGrades);
					SortedMap<String, String> invalidGrades = report.getInvalidNumericGrades();
					if (!invalidGrades.isEmpty()) {
						String badGrades = StringUtils.join(invalidGrades.entrySet(), ", ");
						error(MessageHelper.getString("importExport.error.invalidGradeData", badGrades));
						return;
					}
				}

				//get existing data
				final List<Assignment> assignments = businessService.getGradebookAssignments();
				final List<GbStudentGradeInfo> grades = businessService.buildGradeMatrix(assignments);

				// process file
				List<ProcessedGradeItem> processedGradeItems;
				try {
					processedGradeItems = ImportGradesHelper.processImportedGrades(spreadsheetWrapper, assignments, grades);
				} catch (final GbImportCommentMissingItemException e) {
					error(MessageHelper.getString("importExport.error.commentnoitem", e.getColumnTitle()));
					return;
				}
				// if empty there are no users
				if (processedGradeItems.isEmpty()) {
					error(getString("importExport.error.empty"));
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
					return;
				}

				// OK, GO TO NEXT PAGE

				// clear any previous errors
				final ImportExportPage page = (ImportExportPage) getPage();
				page.clearFeedback();

				// repaint panel
				final ImportWizardModel importWizardModel = new ImportWizardModel();
				importWizardModel.setProcessedGradeItems(processedGradeItems);
				importWizardModel.setReport(spreadsheetWrapper.getUserIdentifier().getReport());
				final Component newPanel = new GradeItemImportSelectionStep(GradeImportUploadStep.this.panelId, Model.of(importWizardModel));
				newPanel.setOutputMarkupId(true);
				GradeImportUploadStep.this.replaceWith(newPanel);
			} else {
				error(getString("importExport.error.noFileSelected"));
			}
		}
	}
}
