package uk.ac.ox.oucs.oxam.pages;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;

import pom.tool.pages.BasePage;
import uk.ac.ox.oucs.oxam.components.RawFileUploadField;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.readers.ArchivePaperResolver;
import uk.ac.ox.oucs.oxam.readers.DualPaperResolver;
import uk.ac.ox.oucs.oxam.readers.ExtraZipPaperResolver;
import uk.ac.ox.oucs.oxam.readers.FlatZipPaperResolver;
import uk.ac.ox.oucs.oxam.readers.Import;
import uk.ac.ox.oucs.oxam.readers.Importer;
import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

public class ImportData extends BasePage {
	
	private static final Log LOG = LogFactory.getLog(ImportData.class);
	
	// Things to look for in the zipfile.
	private static final String[] EXAM_FILE_NAMES = new String[]{"Examcodes.xlsx", "Examcodes.xls", "Examcodes.csv"};
	private static final String[] PAPER_FILE_NAMES = new String[]{"Papercodes.xlsx", "Papercodes.xls", "Papercodes.csv"};
	private static final String[] EXAMPAPER_FILE_NAMES = new String[]{"ExamPapers.xlsx", "ExamPapers.xls", "ExamPapers.csv"};
	private static final String[] PAPERS_FOLDER_NAMES = new String[]{"papers"};
	private static final String PAPER_FILE_TYPE = "pdf";

	@SpringBean
	private TermService termService;
	
	@SpringBean
	private Importer importer;

	public ImportData() {
		ImportDataForm form = new ImportDataForm("importDataForm");
		add(form);
	}
	
	// Think this class needs to change so you upload each file one at a time and then process them later.
	private class ImportDataForm extends Form<Void> {

		private static final long serialVersionUID = 1L;
		private transient RawFileUploadField examFile;

		protected ImportDataForm(String id) {
			super(id);
			examFile = new RawFileUploadField("examFile");
			examFile.setRequired(true);
			add(examFile);
		}

		@Override
		protected void onSubmit() {
			final Import examImporter = importer.newImport();
			File file = null;
			try {
				file = examFile.getFile();
				ZipFile zipFile = new ZipFile(file);
				try {
					final ZipEntry examEntry = getEntry(zipFile, EXAM_FILE_NAMES);
					if (examEntry == null) {
						examFile.error(new StringResourceModel("not.in.zipfile", null, new String[] {"exam code file", StringUtils.join(EXAM_FILE_NAMES, ", ")}).getString());
					}
					final ZipEntry paperEntry = getEntry(zipFile, PAPER_FILE_NAMES);
					if (paperEntry == null) {
						examFile.error(new StringResourceModel("not.in.zipfile", null, new String[] {"paper code file", StringUtils.join(PAPER_FILE_NAMES, ", ")}).getString());
					}
					final ZipEntry examPaperEntry = getEntry(zipFile, EXAMPAPER_FILE_NAMES);
					
					if (examPaperEntry == null) {
						examFile.error(new StringResourceModel("not.in.zipfile", null, new String[] {"exam paper file", StringUtils.join(EXAMPAPER_FILE_NAMES, ", ")}).getString());
					}
					ZipEntry papersFolderEntry = getEntry(zipFile,  PAPERS_FOLDER_NAMES);
					if (papersFolderEntry == null) {
						examFile.error(new StringResourceModel("not.in.zipfile", null, new String[] {"papers folder", StringUtils.join(PAPERS_FOLDER_NAMES, ", ")}).getString());
					}
					// Shortcut if we've found errors
					if (hasError()) {
						return;
					}
					
					// Shouldn't ever find files we don't support as we only look for limited set of files.
					final Format examFormat = getFormat(examEntry.getName());
					final Format examPaperFormat = getFormat(examPaperEntry.getName());
					final Format paperFormat = getFormat(paperEntry.getName());

					examImporter.readExams(zipFile.getInputStream(examEntry), examFormat);
					examImporter.readPapers(zipFile.getInputStream(paperEntry), paperFormat);
					examImporter.readExamPapers(zipFile.getInputStream(examPaperEntry), examPaperFormat);

					// This is a chain of resolvers so it looks for 1234.pdf and if that doesn't exist 1234(T).pdf
					// We want to look for the term specific one first and if that doesn't exist fallback to the generic one.
					// TODO Should be injected.
					examImporter.setPaperResolver(new DualPaperResolver(
							new ExtraZipPaperResolver(file.getAbsolutePath(), papersFolderEntry.getName(), termService, PAPER_FILE_TYPE),
							new DualPaperResolver(
									new FlatZipPaperResolver(file.getAbsolutePath(), papersFolderEntry.getName(), termService, PAPER_FILE_TYPE),
									new ArchivePaperResolver(file.getAbsolutePath(), "papers", termService, PAPER_FILE_TYPE)
									)
					));
					
					examImporter.resolve();
					
					// Need to use this so that we don't get exception because response is committed.
					getRequestCycle().setRequestTarget(new IRequestTarget() {
						
						public void respond(RequestCycle requestCycle) {
							try {
								WebResponse response = (WebResponse) requestCycle.getResponse();
								OutputStream out = response.getOutputStream();
								response.setContentType("application/zip");
								ZipOutputStream zip = new ZipOutputStream(out);
								if (!examImporter.getExamRowErrors().isEmpty()) {
									zip.putNextEntry(new ZipEntry(getErrorFile(examEntry.getName())));
									examImporter.writeExamError(zip, examFormat);
									zip.closeEntry();
								}
								if (!examImporter.getPaperRowErrors().isEmpty()) {
									zip.putNextEntry(new ZipEntry(getErrorFile(paperEntry.getName())));
									examImporter.writePaperError(zip, paperFormat);
									zip.closeEntry();
								}
								if (!examImporter.getExamPaperRowErrors().isEmpty()) {
									zip.putNextEntry(new ZipEntry(getErrorFile(examPaperEntry.getName())));
									examImporter.writeExamPaperError(zip, examPaperFormat);
									zip.closeEntry();
								}
								zip.close();
							} catch (IOException ioe) {
								throw new RuntimeException(ioe);
							}
						}
						
						public void detach(RequestCycle requestCycle) {
						}
					});
					
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			} catch (IOException ioe) {
				examFile.error((IValidationError)new ValidationError().addMessageKey("not.zipfile"));
			}
			 finally {
				if (file != null) {
					if (!file.delete()) {
						LOG.warn("Failed to remove temp file: "+ file.getAbsolutePath());
					}
				}
			}
		}

		/**
		 * Looks for any of the supplied names in the zipfile.
		 * @param zipFile The zipfile.
		 * @param names The names to look for.
		 * @return <code>null</code> if none of the names are found.
		 */
		protected ZipEntry getEntry(ZipFile zipFile, String[] names) {
			for (String name: names) {
				ZipEntry entry = zipFile.getEntry(name);
				if (entry != null) {
					return entry;
				}
			}
			return null;
		}
		
		private String getErrorFile(String sourceFilename) {
			return "error"+ sourceFilename;
		}

		private Format getFormat(String filename) {
			Format format = null;
			if (filename != null) {
				if (filename.endsWith(".xls")) {
					format = Format.XLS;
				} else if (filename.endsWith(".xlsx")) {
					format = Format.XLSX;
				} else if (filename.endsWith(".csv")) {
					format = Format.CSV;
				}
			}
			if (format == null) {
				throw new IllegalArgumentException("Unrecognised file extension: "+ filename);
			}
			return format;
		}
	}


}
