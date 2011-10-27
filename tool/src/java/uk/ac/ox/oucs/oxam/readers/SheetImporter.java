package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * This is a generic service which maps a spreadsheet onto a Java object using annotations.
 * Ignores rows in which no data at all matches.
 * Doesn't do any validation of values returned.
 * @see ColumnMapping 
 * @see RowNumber
 * 
 * @author buckett
 *
 */
public class SheetImporter {
	public static enum Format {
		XLSX, XLS, CSV
	}

	public <T> List<T> importSheet(InputStream source, Format format, Class<T> clazz) {

		List<T> rows = new ArrayList<T>();
		boolean foundHeaders = false;

		if (Format.XLSX.equals(format) || Format.XLS.equals(format)) {
			try {
				// POI needs mark/reset support.
				if (!source.markSupported()) {
					source = new PushbackInputStream(source);
				}
				Workbook wb = WorkbookFactory.create(source);
				Sheet sheet = wb.getSheetAt(0);
				List<Field> columnUse = new ArrayList<Field>();

				Map<String, Field> columnToField = new HashMap<String, Field>();
				Collection<Field> rowNumberFields = new ArrayList<Field>();

				for (Field field: clazz.getDeclaredFields()) {
					if (field.isAnnotationPresent(ColumnMapping.class)) {
						ColumnMapping mapping = field.getAnnotation(ColumnMapping.class);
						if (String.class.isAssignableFrom(field.getType())) {
							columnToField.put(mapping.value(), field);
						} else {
							throw new IllegalArgumentException("ColumnMapping field is not a java.lang.String. "+ field);
						}
					}
					if (field.isAnnotationPresent(RowNumber.class)) {
						if (int.class.isAssignableFrom(field.getType())) {
							rowNumberFields.add(field);
						} else {
							throw new IllegalArgumentException("RowNumber field is not a int. "+ field);
						}
					}
				}

				for (Row row : sheet) {
					if (foundHeaders) {
						T rowObj = clazz.newInstance();
						// Only include rows with data.
						boolean setSomething = false;
						for (Cell cell : row) {
							Field currentField = null;
							if (cell.getColumnIndex() < columnUse.size()) {
								currentField = columnUse.get(cell.getColumnIndex());
							}
							if (currentField != null) {
								String cellValue = null;
								if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
									cellValue = cell.getStringCellValue();
								} else if (Cell.CELL_TYPE_NUMERIC == cell.getCellType()) {
									// http://stackoverflow.com/questions/1072561/how-can-i-read-numeric-strings-in-excel-cells-as-string-not-numbers-with-apache
									cell.setCellType(Cell.CELL_TYPE_STRING);
									cellValue = cell.getStringCellValue();
								}
								if(cellValue != null) {
									currentField.set(rowObj, cellValue);
									setSomething = true;
								}
							}
						}
						if (setSomething) {
							for (Field currentField: rowNumberFields) {
								currentField.set(rowObj, row.getRowNum());
							}
							rows.add(rowObj);
						}
					} else {
						// Look for headers row.
						for (Cell cell : row) {
							// Only care about string cells.
							if (Cell.CELL_TYPE_STRING == cell.getCellType()) {
								String value = cell.getStringCellValue();
								if (value != null) {
									value = value.trim().toLowerCase();
									Field column = columnToField.get(value);
									if (column != null) {
										addWithPadding(columnUse, cell.getColumnIndex(), column);
										foundHeaders = true;
									}
								}
							}
						}
						if (foundHeaders) {
							// Validate we have a good set.
						}
					}
				}
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (Format.CSV.equals(format)) {
			try {
				CSVReader reader = new CSVReader(new InputStreamReader(source));
				
			} catch (E)
		} else {
			throw new IllegalArgumentException("Unsupported format: "+ format);
		}
		if(!foundHeaders) {
			throw new IllegalStateException("Failed to find any known headers in the file.");
		}
		return rows;
	}

	/**
	 * Pads the list with nulls if we're not added the item to the end.
	 */
	private static void addWithPadding(List<Field> columnUse, int pos, Field column) {
		while(columnUse.size() < pos) {
			columnUse.add(null);
		}
		columnUse.add(column);
	}
}
