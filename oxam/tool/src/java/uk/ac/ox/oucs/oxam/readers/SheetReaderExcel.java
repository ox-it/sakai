package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class SheetReaderExcel implements SheetReader {

	private Sheet sheet;
	private Iterator<Row> rowIterator;
	private Iterator<Cell> cellIterator;
	private Cell cell;

	public SheetReaderExcel(InputStream source) {
		// POI needs mark/reset support.
		if (!source.markSupported()) {
			source = new PushbackInputStream(source);
		}
		Workbook wb;
		try {
			wb = WorkbookFactory.create(source);
			sheet = wb.getSheetAt(0);
			rowIterator = sheet.rowIterator();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean nextRow() {
		if (rowIterator.hasNext()) {
			cellIterator = rowIterator.next().cellIterator();
			return true;
		}
		return false;
	}

	public boolean nextColumn() {
		if (cellIterator != null && cellIterator.hasNext()) {
			cell = cellIterator.next();
			return true;
		}
		return false;
	}

	public int getRow() {
		if (cell != null) {
			return cell.getRowIndex();
		}
		return -1;
	}

	public int getColumn() {
		if (cell != null) {
			return cell.getColumnIndex();
		}
		return -1;
	}

	public String getContents() {
		String cellValue = null;
		if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
			cellValue = cell.getStringCellValue();
		} else if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
			// http://stackoverflow.com/questions/1072561/how-can-i-read-numeric-strings-in-excel-cells-as-string-not-numbers-with-apache
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cellValue = cell.getStringCellValue();
		}
		return cellValue;
	}

}
