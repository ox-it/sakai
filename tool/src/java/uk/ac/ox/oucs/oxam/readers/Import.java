package uk.ac.ox.oucs.oxam.readers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

public class Import {

	private Map<String, PaperRow>paperRows;
	private Map<Integer, Set<ConstraintViolation<PaperRow>>>paperRowViolations = new HashMap<Integer, Set<ConstraintViolation<PaperRow>>>();
	
	private Map<String, ExamRow>examRows;
	private Map<Integer, Set<ConstraintViolation<ExamRow>>>examRowViolations = new HashMap<Integer, Set<ConstraintViolation<ExamRow>>>();
	
	private List<ExamPaperRow>examPaperRows;
	private Map<Integer, Set<ConstraintViolation<ExamPaperRow>>>examPaperRowViolations = new HashMap<Integer, Set<ConstraintViolation<ExamPaperRow>>>();
	
	private Importer importer;

	Import(Importer importer) {
		this.importer = importer;
	}

	public void readPapers(InputStream source, String filename) {
		this.paperRows = new HashMap<String,PaperRow>();
		
		Format format = getFormat(filename);
		
		SheetImporter sheetImporter = new SheetImporter();
		List<PaperRow> paperRows = sheetImporter.importSheet(source, format, PaperRow.class);
		
		for (PaperRow paperRow: paperRows) {
			Set<ConstraintViolation<PaperRow>> violations = importer.validate(paperRow);
			if (violations.isEmpty()) {
				this.paperRows.put(paperRow.code, paperRow);
			} else {
				paperRowViolations.put(paperRow.row, violations);
			}
		}
	}
	
	public void readExams(InputStream source, String filename) {
		// WorkbookFactory needs support for mark/reset.
		this.examRows = new HashMap<String,ExamRow>();
		
		Format format = getFormat(filename);
		
		SheetImporter sheetImporter = new SheetImporter();
		List<ExamRow> examRows = sheetImporter.importSheet(source, format, ExamRow.class);
		
		for (ExamRow examRow: examRows) {
			Set<ConstraintViolation<ExamRow>> violations = importer.validate(examRow);
			if (violations.isEmpty()) {
				this.examRows.put(examRow.code, examRow);
			} else {
				this.examRowViolations.put(examRow.row, violations);
			}
		}
	}
	public void readExamPapers(InputStream source, String filename) {
		// WorkbookFactory needs support for mark/reset.
		this.examPaperRows = new ArrayList<ExamPaperRow>();
		
		Format format = getFormat(filename);
		
		SheetImporter sheetImporter = new SheetImporter();
		List<ExamPaperRow> examPaperRows = sheetImporter.importSheet(source, format, ExamPaperRow.class);
		
		for (ExamPaperRow examPaperRow: examPaperRows) {
			Set<ConstraintViolation<ExamPaperRow>> violations = importer.validate(examPaperRow);
			if (violations.isEmpty()) {
				this.examPaperRows.add(examPaperRow);
			} else {
				this.examPaperRowViolations.put(examPaperRow.row, violations);
			}
		}
	}
	
	public Map<String, PaperRow> getPaperRows() {
		return paperRows;
	}

	public Map<Integer, Set<ConstraintViolation<PaperRow>>> getPaperRowViolations() {
		return paperRowViolations;
	}

	public Map<String, ExamRow> getExamRows() {
		return examRows;
	}

	public Map<Integer, Set<ConstraintViolation<ExamRow>>> getExamRowViolations() {
		return examRowViolations;
	}

	public List<ExamPaperRow> getExamPaperRows() {
		return examPaperRows;
	}

	public Map<Integer, Set<ConstraintViolation<ExamPaperRow>>> getExamPaperRowViolations() {
		return examPaperRowViolations;
	}

	// Here we lookup everything to make sure it all matches up.
	public void resolve() {
		for (PaperRow paperRow: paperRows.values()) {
			// If the paper is included with another.
			if(paperRow.inc != null && paperRow.inc.length() > 0) {
				if (paperRow.code.equals(paperRow.inc)) {
					System.out.println("Paper can't reference itself.");
				}
				if (!paperRows.containsKey(paperRow.inc)) {
					System.out.println("Not found in paperRows: "+ paperRow.inc);
					//paperRowViolations.put(paperRow.row, null);
				}
			} else {
				// Todo need to validate PDF links.
			}
		}
		for (ExamPaperRow examPaperRow: examPaperRows) {
			if (!examRows.containsKey(examPaperRow.examCode)) {
				System.out.println("Not found in examRows: "+ examPaperRow.examCode);
				examPaperRowViolations.put(examPaperRow.row, null);
			}
			if (!paperRows.containsKey(examPaperRow.paperCode)) {
				System.out.println("Not found in paperRows: "+ examPaperRow.paperCode);
				examPaperRowViolations.put(examPaperRow.row, null);
			}
		}
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
	
	static class ExamPaperRow {
		@RowNumber
		int row;
		
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
	}
	
	
	static class ExamRow {
		@RowNumber
		int row;
		
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
	}
	
	// Hold all the paper information from the sheet.
	// Needs to be static so the SheetImporter can create new instances.
	static class PaperRow {
		@RowNumber
		int row;
		
		@ColumnMapping("paper code")
		@NotNull @Size(min=1)
		String code;
		
		@ColumnMapping("paper title")
		@NotNull @Size(min=2, max=255)
		String title;
		
		@ColumnMapping("inc with")
		@Size(min=1)
		String inc;
		
		@ColumnMapping("term")
		@NotNull @Size(min=1)
		String term;
		
		@ColumnMapping("year")
		@NotNull @Pattern(regexp="\\d{4}(-\\d{4})?")
		String year;
	}

}
