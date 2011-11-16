package uk.ac.ox.oucs.oxam.pages;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.spring.injection.annot.SpringBean;

import pom.tool.pages.BasePage;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.readers.ErrorMessages;
import uk.ac.ox.oucs.oxam.readers.FlatZipPaperResolver;
import uk.ac.ox.oucs.oxam.readers.Import;
import uk.ac.ox.oucs.oxam.readers.Importer;

public class ImportData extends BasePage {
	
	@SpringBean
	private TermService termService;
	
	@SpringBean
	private Importer importer;
	
	// Think this class needs to change so you upload each file one at a time and then process them later.
	private class ImportDataForm extends Form<Void> {

		private static final long serialVersionUID = 1L;
		private FileUploadField examFile;

		protected ImportDataForm(String id) {
			super(id);

			examFile = new FileUploadField("examFile");
			examFile.setRequired(true);


			add(examFile);

		}

		@Override
		protected void onSubmit() {
			Import examImporter = importer.newImport();
			File file = null;
			try {
				FileUpload fileUpload = examFile.getFileUpload();
				file = fileUpload.writeToTempFile();
				ZipFile zipFile = new ZipFile(file);
				ZipEntry entry;
				entry = zipFile.getEntry("Examcodes.xlsx");
				examImporter.readExams(zipFile.getInputStream(entry), "Examcodes.xlsx");
				
				entry = zipFile.getEntry("Papercodes.xlsx");
				examImporter.readPapers(zipFile.getInputStream(entry), "Papercodes.xlsx");
				
				entry = zipFile.getEntry("ExamPapers.xlsx");
				examImporter.readExamPapers(zipFile.getInputStream(entry), "ExamPapers.xlsx");
				
				// TODO, need to chain resolvers, to look for papers/1234(T).pdf and papers/1234.pdf 
				examImporter.setPaperResolver(new FlatZipPaperResolver(file.getAbsolutePath(), "papers", termService, "pdf"));
				
				examImporter.resolve();
				
				for (Entry<Integer, ErrorMessages<Import.ExamPaperRow>> errorEntry:examImporter.getExamPaperRowErrors().entrySet()) {
					System.out.println(errorEntry);
				}
				
				
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} finally {
				if (file != null) {
					file.delete();
				}
			}
		}
	}

	public ImportData() {
		ImportDataForm form = new ImportDataForm("importDataForm");
		add(form);
	}

}
