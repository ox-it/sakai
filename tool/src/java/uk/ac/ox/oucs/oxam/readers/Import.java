package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.oxam.logic.PaperFile;
import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;
import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

/**
 * A new instance of this class is created for each import.
 * @author buckett
 *
 */
public class Import {

	private static final Log LOG = LogFactory.getLog(Import.class);
	
	private Importer importer;
	
	private KeyedSheetImporter<PaperRow> paperRowImporter;
	private KeyedSheetImporter<ExamRow> examRowImporter;
	private KeyedSheetImporter<ExamPaperRow> examPaperRowImporter;
	
	private StringBuilder messages;
	
	private Map<PaperFile, String>cachedMD5s = new HashMap<PaperFile, String>();
	
	// This finds the source files.
	private PaperResolver resolver;

	Import(Importer importer) {
		this.importer = importer;
		this.messages = new StringBuilder();
	}

	public void readPapers(InputStream source, Format format) {
		paperRowImporter = new KeyedSheetImporter<PaperRow>(PaperRow.class, importer);
		paperRowImporter.read(source, format);
	}
	
	public void readExams(InputStream source, Format format) {
		examRowImporter = new KeyedSheetImporter<ExamRow>(ExamRow.class, importer);
		examRowImporter.read(source, format);
	}
	public void readExamPapers(InputStream source, Format format) {
		examPaperRowImporter = new KeyedSheetImporter<ExamPaperRow>(ExamPaperRow.class, importer);
		examPaperRowImporter.read(source, format);
	}
	
	public void setPaperResolver(PaperResolver resolver) {
		this.resolver = resolver;
	}
	
	public Map<String, PaperRow> getPaperRows() {
		return paperRowImporter.getRows();
	}

	public Map<Integer, ErrorMessages<PaperRow>> getPaperRowErrors() {
		return paperRowImporter.getErrors();
	}

	public Map<String, ExamRow> getExamRows() {
		return examRowImporter.getRows();
	}

	public Map<Integer, ErrorMessages<ExamRow>> getExamRowErrors() {
		return examRowImporter.getErrors();
	}

	public Map<String, ExamPaperRow> getExamPaperRows() {
		return examPaperRowImporter.getRows();
	}

	public Map<Integer, ErrorMessages<ExamPaperRow>> getExamPaperRowErrors() {
		return examPaperRowImporter.getErrors();
	}
	
	/**
	 * Log messages from the import.
	 * @return A string of log messages from the import.
	 */
	public String getMessages() {
		return messages.toString();
	}

	// Here we lookup everything to make sure it all matches up.
	public void resolve() {
		Map<String, PaperRow> paperRows = paperRowImporter.getRows();
		Map<String, ExamPaperRow> examPaperRows = examPaperRowImporter.getRows();
		Map<String, ExamRow> examRows = examRowImporter.getRows();
		
		// Should the resolver remove data which isn't good?
		for (PaperRow paperRow: paperRows.values()) {

			if (!importer.checkTerm(paperRow)) {
				paperRowImporter.addError(paperRow, "Not a valid term code.");
			}
			
			AcademicYear year = importer.checkYear(paperRow.year);
			if (year == null) {
				paperRowImporter.addError(paperRow, "Not a valid year.");
			}
			// If the paper is included with another.
			
			// Need to be careful of a circular loop (paper A inc B, paper B inc A)
			if(paperRow.inc != null && paperRow.inc.length() > 0) {
				if (paperRow.code.equals(paperRow.inc)) { 
					paperRowImporter.addError(paperRow, "Paper can't reference itself");
				}
				if (!paperRows.containsKey(paperRow.inc+paperRow.year+paperRow.term)) {
					paperRowImporter.addError(paperRow, "Not found in paperRows: "+ paperRow.inc);
				}
			} else {
				PaperResolutionResult result = resolver.getPaper(year.getYear(), paperRow.term, paperRow.code);
				if (result == null) {
					paperRowImporter.addError(paperRow,  "Not enough good information to locate file.");
				} else {
					if (!result.isFound()) {
						paperRowImporter.addError(paperRow, "Couldn't find file: "+ StringUtils.join(result.getPaths(), " or "));
					}
				}
			}
		}
		for (ExamPaperRow examPaperRow: examPaperRows.values()) {
			boolean error = false;
			ExamRow examRow = examRows.get(examPaperRow.getExamKey());
			if (examRow == null) {
				examPaperRowImporter.addError(examPaperRow, "Failed to find referenced exam");
				error = true;
			}
			PaperRow paperRow = paperRows.get(examPaperRow.getPaperKey());
			if (paperRow == null) {
				examPaperRowImporter.addError(examPaperRow, "Failed to find referenced paper");
				error = true;
			}
			// Parse year.
			AcademicYear year = importer.checkYear(examPaperRow.year);
			if (year == null) {
				examPaperRowImporter.addError(examPaperRow, "Failed to understand date");
				error = true;
			}
			Term term = importer.checkTerm(examPaperRow);
			if (term == null) {
				examPaperRowImporter.addError(examPaperRow, "Not a valid term code.");
				error = true;
			}
			Category category = importer.checkCategory(examRow);
			if (category == null) {
				examRowImporter.addError(examRow, "Category is not valid");
				error = true;
			}
			
			if (!error) {
				String paperCode = (paperRow.inc != null && paperRow.inc.length() > 0)?paperRow.inc:paperRow.code;
				PaperResolutionResult paper = resolver.getPaper(year.getYear(), examPaperRow.term, paperCode);
				if(paper.isFound()) { // It's available to import from the ZIPfile.
					
					ExamPaper examPaper = importer.get(examPaperRow.examCode, examPaperRow.paperCode, year, term);
					boolean isNew = examPaper == null;
					if (isNew) {
						examPaper = new ExamPaper();
						examPaper.setExamCode(examPaperRow.examCode);
						examPaper.setPaperCode(examPaperRow.paperCode);
						examPaper.setYear(year);
						examPaper.setTerm(term);
					}
					examPaper.setPaperTitle(paperRow.title);
					examPaper.setExamTitle(examRow.title);
					examPaper.setCategory(category);
					InputStream input = paper.getStream();
					try {
						String existingMD5 = getPaperMD5(examPaper);
						if (existingMD5 != null) { // May be null if doesn't exist yet.
							String newMD5 = paper.getMD5();
							if (!existingMD5.equals(newMD5)) { // If they don't match, import it.
								importer.persist(examPaper, input);
								LOG.debug("Saved replacement paper for: "+ examPaper.getPaperFile());
							} else {
								LOG.debug("No change for paper: "+ examPaper.getPaperFile());
							}
						} else {
							importer.persist(examPaper, input);
							LOG.debug("Saved new paper for: "+ examPaper.getPaperFile());
						}
						importer.save(examPaper);
						messages.append((isNew?"Added":"Updated"));
						messages.append(" exam paper. ExamCode: "+ examPaper.getExamCode()+ " PaperCode: "+ examPaper.getPaperCode());
						messages.append(" Year: "+ examPaper.getYear()+ " Term: "+ examPaper.getTerm().getName()+ "\n");
					} catch (Exception e) {
						// This shouldn't really happen and indicates a low-level problem.
						examPaperRowImporter.addError(examPaperRow, e.getMessage());
					} finally {
						if (input != null) {
							try {
								input.close();
							} catch (IOException ioe) {
								LOG.info("Failed to close input stream: "+ ioe.getMessage());
							}
						}
					}
				} else {
					examPaperRowImporter.addError(examPaperRow, "File for paper is missing: "+ StringUtils.join(paper.getPaths(), " or "));
				}
			}
		}
	}

	public String getPaperMD5(ExamPaper examPaper) {
		// This needs to be handled here as we want a cache of MD5s per import.
		String md5 = null;
		PaperFile paperFile = importer.getPaperFile(examPaper);
		if (cachedMD5s.containsKey(paperFile)) {
			md5 = cachedMD5s.get(paperFile);
		} else {
			md5 = importer.getMD5(paperFile);
			cachedMD5s.put(paperFile, md5);
		}
		return md5;
	}
	
	public void writeExamError(OutputStream out, Format format) throws IOException {
		SheetExporter exporter = new SheetExporter();
		exporter.writeSheet(out, format, getExamRowErrors().values());
	}
	
	public void writePaperError(OutputStream out, Format format) throws IOException {
		SheetExporter exporter = new SheetExporter();
		exporter.writeSheet(out, format, getPaperRowErrors().values());
	}
	
	public void writeExamPaperError(OutputStream out, Format format) throws IOException {
		SheetExporter exporter = new SheetExporter();
		exporter.writeSheet(out, format, getExamPaperRowErrors().values());
	}
	

	static abstract class KeyedRow {
		abstract int getRow();
		
		abstract String getKey();
	}
	
	public static class ExamPaperRow  extends KeyedRow {

		@RowNumber
		int row;
		
		@Override
		int getRow() {
			return row;
		}
		
		@ColumnMapping("exam code")
		@NotNull @Size(min=1)
		String examCode;
		
		@ColumnMapping("paper code")
		@NotNull @Size(min=1)
		String paperCode;
		
		@ColumnMapping("term")
		@NotNull @Size(min=1)
		String term;
		
		@ColumnMapping("year")
		@NotNull @Pattern(regexp="\\d{4}(-\\d{4})?")
		String year;
		
		String getExamKey() {
			ExamRow examRow = new ExamRow();
			examRow.code = examCode;
			return examRow.getKey();
		}
		
		String getPaperKey() {
			PaperRow paperRow = new PaperRow();
			paperRow.code = paperCode;
			paperRow.year = year;
			paperRow.term = term;
			return paperRow.getKey();
		}
		
		@Override
		public String getKey() {
			return paperCode+ examCode+ year+ term;
		}

		@Override
		public String toString() {
			return "ExamPaperRow [row=" + row + ", examCode=" + examCode
					+ ", paperCode=" + paperCode + ", term=" + term + ", year="
					+ year + "]";
		}
		
	}
	
	
	public static class ExamRow extends KeyedRow {
		
		@RowNumber
		int row;
		
		@Override
		int getRow() {
			return row;
		}
		
		@ColumnMapping("examcode")
		@NotNull @Size(min=1)
		String code;
		
		@ColumnMapping("exam")
		@NotNull @Size(min=2, max=255)
		String title;

		@ColumnMapping("cat_code")
		@NotNull @Size(min=1)
		String category;
		
		@Override
		public String getKey() {
			return code;
		}

		@Override
		public String toString() {
			return "ExamRow [row=" + row + ", code=" + code + ", title="
					+ title + ", category=" + category + "]";
		}
	}
	
	// Hold all the paper information from the sheet.
	// Needs to be static so the SheetImporter can create new instances.
	public static class PaperRow extends KeyedRow {
	
		@RowNumber
		int row;
		
		@Override
		int getRow() {
			return row;
		}
		
		@ColumnMapping("paper code")
		@NotNull @Size(min=1)
		String code;
		
		@ColumnMapping("paper title")
		@NotNull @Size(min=2, max=255)
		String title;
		
		@ColumnMapping("inc with")
		String inc;
		
		@ColumnMapping("term")
		@NotNull @Size(min=1)
		String term;
		
		@ColumnMapping("year")
		@NotNull @Pattern(regexp="\\d{4}(-\\d{4})?")
		String year;
		
		@Override
		public String getKey() {
			return code+year+term;
		}

		@Override
		public String toString() {
			return "PaperRow [row=" + row + ", code=" + code + ", title="
					+ title + ", inc=" + inc + ", term=" + term + ", year="
					+ year + "]";
		}
	}

}
