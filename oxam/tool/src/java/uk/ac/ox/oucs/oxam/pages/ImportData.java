package uk.ac.ox.oucs.oxam.pages;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;

import uk.ac.ox.oucs.oxam.components.RawFileUploadField;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.readers.ArchivePaperResolver;
import uk.ac.ox.oucs.oxam.readers.DualPaperResolver;
import uk.ac.ox.oucs.oxam.readers.ExtraZipPaperResolver;
import uk.ac.ox.oucs.oxam.readers.FlatZipPaperResolver;
import uk.ac.ox.oucs.oxam.readers.Import;
import uk.ac.ox.oucs.oxam.readers.Importer;
import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

public class ImportData extends AdminPage {
	
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
				final String originalFileName = examFile.getFileUpload().getClientFileName();
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
					String papersFolder = getEntryFolder(zipFile,  PAPERS_FOLDER_NAMES);
					if (papersFolder == null) {
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

					try {
						examImporter.readExams(zipFile.getInputStream(examEntry), examFormat);
					} catch (RuntimeException re) {
						examFile.error(new StringResourceModel("no.format.found", null, new String[]{ examEntry.getName()}).getString());
					}
					try {
						examImporter.readPapers(zipFile.getInputStream(paperEntry), paperFormat);
					} catch (RuntimeException re) {
						examFile.error(new StringResourceModel("no.format.found", null, new String[]{ paperEntry.getName()}).getString());
					}
					try {
						examImporter.readExamPapers(zipFile.getInputStream(examPaperEntry), examPaperFormat);
					} catch (RuntimeException re) {
						examFile.error(new StringResourceModel("no.format.found", null, new String[]{ examPaperEntry.getName()}).getString());
					}
					if (hasError()) {
						return;
					}

					// This is a chain of resolvers so it looks for 1234.pdf and if that doesn't exist 1234(T).pdf
					// We want to look for the term specific one first and if that doesn't exist fallback to the generic one.
					// TODO Should be injected.
					examImporter.setPaperResolver(new DualPaperResolver(
							new ExtraZipPaperResolver(file.getAbsolutePath(), papersFolder, termService, PAPER_FILE_TYPE),
							new DualPaperResolver(
									new FlatZipPaperResolver(file.getAbsolutePath(), papersFolder, termService, PAPER_FILE_TYPE),
									new ArchivePaperResolver(file.getAbsolutePath(), "papers", termService, PAPER_FILE_TYPE)
									)
					));
					
					examImporter.resolve();
					// 
					// Need to use this so that we don't get exception because response is committed.
					getRequestCycle().setRequestTarget(new IRequestTarget() {
						
						public void respond(RequestCycle requestCycle) {
							try {
								WebResponse response = (WebResponse) requestCycle.getResponse();
								OutputStream out = response.getOutputStream();
								response.setContentType("application/zip");
								response.setHeader("Content-Disposition", "attachment; filename="+getResultFile(originalFileName));
								ZipOutputStream zip = new ZipOutputStream(out);
								zip.putNextEntry(new ZipEntry("messages.txt"));
								Writer messagesWriter = new OutputStreamWriter(zip);
								messagesWriter.write("Messages from Import\n\n");
								messagesWriter.write(examImporter.getMessages());
								messagesWriter.flush(); // Don't close.
								zip.closeEntry();
								
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
								
								// This doesn't get rendered as we are returning a zip.
								info(getString("action.import.ok"));
							} catch (IOException ioe) {
								throw new RuntimeException(ioe);
							}
						}
						
						public void detach(RequestCycle requestCycle) {
						}
					});
				} catch (DuplicateFoundException e) {
					examFile.error(new StringResourceModel("duplicate.in.zipfile", null, new String[] {e.getOriginal(), e.getDuplicate()}).getString());
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			} catch (IOException ioe) {
				examFile.error(new StringResourceModel("not.zipfile", null).getString());
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
		protected ZipEntry getEntry(ZipFile zipFile, String[] names) throws DuplicateFoundException {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			ZipEntry found = null;
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				for (String name: names) {
					if (entry.getName().equalsIgnoreCase(name)) {
						if (found != null) {
							// Throw an error saying the which files clash.
							throw new DuplicateFoundException(found.getName(), entry.getName());
						} else {
							found = entry;
						}
					}
				}
			}
			return found;
		}
		
		/**
		 * This looks through the entries in the zip to see if any start with any of the names.
		 * We use this method because Windows when it creates zipfiles doesn't create entries for
		 * folders so we just have to look to see if any of the files have the prefix in their path 
		 * that we are interested in.
		 * 
		 * @param zipFile The zipfile to search in.
		 * @param names The directories to look for.
		 * @return The found name.
		 */
		protected String getEntryFolder(ZipFile zipFile, String[] names) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				for (String name: names) {
					if (entry.getName().startsWith(name+"/")) {
						return name;
					}
				}
			}
			return null;
		}
		
		private String getErrorFile(String sourceFilename) {
			return "error"+ sourceFilename;
		}
		
		/**
		 * Gets the filename to send the results back as.
		 * @param sourceFilename The original filename supplied by the user.
		 * @return A filename to set in the HTTP headers for the results.
		 */
		private String getResultFile(String sourceFilename) {
			String filename = "details";
			if (sourceFilename != null && sourceFilename.length() > 0) {
				int lastDot = sourceFilename.lastIndexOf('.');
				if (lastDot != -1) {
					filename = sourceFilename.substring(0, lastDot);
				} else {
					filename = sourceFilename;
				}
			}
			return "results-"+ filename+ ".zip";
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
