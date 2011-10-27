package uk.ac.ox.oucs.oxam.readers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;

import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

public class SheetImporterTest {

	@Test
	public void testImportSheetExcel() {
		SheetImporter importer = new SheetImporter();
		InputStream input = getClass().getResourceAsStream("/Papercodes.xlsx");
		List<ExamplePaperRow>rows = importer.importSheet(input, Format.XLSX, ExamplePaperRow.class);
		assertTrue(rows.size() > 0);
		ExamplePaperRow paperRow = rows.get(0);
		assertEquals("0491", paperRow.code);
		assertEquals("Paper I.", paperRow.title);
		assertEquals(null, paperRow.inc);
		assertEquals("E", paperRow.term);
		assertEquals("2010-2011", paperRow.year);
		assertEquals(1, paperRow.row);
	}
	
	@Test
	public void testImportSheetCSV() {
		SheetImporter importer = new SheetImporter();
		InputStream input = getClass().getResourceAsStream("/terms.csv");
		List<TermRow>rows = importer.importSheet(input, Format.CSV, TermRow.class);
		TermRow termRow = rows.get(0);
		assertEquals("T", termRow.code);
		assertEquals("Trinity", termRow.description);
		assertEquals(2, termRow.row);
		assertEquals(5, rows.size());

	}
	
	static class TermRow {
		
		@RowNumber
		int row;
		
		@ColumnMapping("termcode")
		String code;
		
		@ColumnMapping("termdesc")
		String description;
		
	}

	// Example class to be mapped.
	static class ExamplePaperRow {
		
		@RowNumber
		int row;
		
		@ColumnMapping("paper code")
		String code;
		
		@ColumnMapping("paper title")
		String title;
		
		@ColumnMapping("inc with")
		String inc;
		
		@ColumnMapping("term")
		String term;
		
		@ColumnMapping("year")
		String year;
	}
	
}
