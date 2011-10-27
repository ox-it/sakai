package uk.ac.ox.oucs.oxam.readers;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

public class SheetImporterTest {

	@Test
	public void testImportSheet() {
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
