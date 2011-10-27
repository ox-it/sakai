package uk.ac.ox.oucs.oxam.pages;

import java.io.IOException;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;

import pom.tool.pages.BasePage;
import uk.ac.ox.oucs.oxam.readers.Import;
import uk.ac.ox.oucs.oxam.readers.Importer;

public class ImportData extends BasePage {



	// Think this class needs to change so you upload each file one at a time and then process them later.
	private class ImportDataForm extends Form<Void> {

		private static final long serialVersionUID = 1L;
		private FileUploadField examFile;
		private FileUploadField paperFile;
		private FileUploadField examPaperFile;
		private FileUploadField examPaperZip;

		protected ImportDataForm(String id) {
			super(id);

			examFile = new FileUploadField("examFile");
			examFile.setRequired(true);
			paperFile = new FileUploadField("paperFile");
			paperFile.setRequired(true);
			examPaperFile = new FileUploadField("examPaperFile");
			examPaperFile.setRequired(true);
			examPaperZip = new FileUploadField("examPaperZip");
			examPaperZip.setRequired(true);

			add(examFile);
			add(paperFile);
			add(examPaperFile);
			add(examPaperZip);

		}

		@Override
		protected void onSubmit() {
			Import examImporter = new Importer().newImport();
			try {
				examFile.getFileUpload();
				examImporter.readPapers(paperFile.getFileUpload().getInputStream(), paperFile.getFileUpload().getClientFileName());
				examImporter.readExams(examFile.getFileUpload().getInputStream(), examFile.getFileUpload().getClientFileName());
				examImporter.readExamPapers(examPaperFile.getFileUpload().getInputStream(), examFile.getFileUpload().getClientFileName());
			} catch (IOException ioe) {
				
			}
		}
		
		@Override
		protected void onDetach() {
			
		}
	}

	public ImportData() {
		ImportDataForm form = new ImportDataForm("importDataForm");
		add(form);
	}

}
