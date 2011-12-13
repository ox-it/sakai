package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;
import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

public class Import {

	private static final Log LOG = LogFactory.getLog(Import.class);
	
	private Importer importer;
	
	private KeyedSheetImporter<PaperRow> paperRowImporter;
	private KeyedSheetImporter<ExamRow> examRowImporter;
	private KeyedSheetImporter<ExamPaperRow> examPaperRowImporter;
	
	// This finds the source files.
	private PaperResolver resolver;

	Import(Importer importer) {
		this.importer = importer;
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
			
			int year = parseYear(paperRow.year);
			if (year < 0) {
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
				PaperResolutionResult result = resolver.getPaper(year, paperRow.term, paperRow.code);
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
			int year = parseYear(examPaperRow.year);
			if (year < 0) {
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
				PaperResolutionResult paper = resolver.getPaper(year, examPaperRow.term, examPaperRow.paperCode);
				if(paper.isFound()) {
					ExamPaper examPaper = importer.get(examPaperRow.examCode, examPaperRow.paperCode, year, term);
					if (examPaper == null) {
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
						importer.persist(examPaper, input);
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
	
	public void writeExamError(OutputStream out, Format format) throws IOException {
		SheetExporter exporter = new SheetExporter();
		exporter.writeSheet(out, Format.CSV, getExamRowErrors().values());
	}
	
	public void writePaperError(OutputStream out, Format format) throws IOException {
		SheetExporter exporter = new SheetExporter();
		exporter.writeSheet(out, format, getPaperRowErrors().values());
	}
	
	public void writeExamPaperError(OutputStream out, Format format) throws IOException {
		SheetExporter exporter = new SheetExporter();
		exporter.writeSheet(out, format, getExamPaperRowErrors().values());
	}
	

	private int parseYear(String year) {
		int pos = year.indexOf('-');
		try {
			return Integer.parseInt((pos > 0)?year.substring(0,pos):year);
		} catch (NumberFormatException nfe) {
		}
		return -1;
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
			examRow.year = year;
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
		
		@ColumnMapping("year")
		@NotNull @Pattern(regexp="\\d{4}(-\\d{4})?")
		String year;
		
		@ColumnMapping("cat_code")
		@NotNull @Size(min=1)
		String category;
		
		@Override
		public String getKey() {
			return code+year;
		}

		@Override
		public String toString() {
			return "ExamRow [row=" + row + ", code=" + code + ", title="
					+ title + ", year=" + year + ", category=" + category + "]";
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
