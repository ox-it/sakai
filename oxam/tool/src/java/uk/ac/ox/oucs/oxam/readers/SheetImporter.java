package uk.ac.ox.oucs.oxam.readers;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a generic service which maps a spreadsheet onto a Java object using
 * annotations. Ignores rows in which no data at all matches. Doesn't do any
 * validation of values returned.
 * 
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

	public <T> List<T> importSheet(InputStream source, Format format,
			Class<T> clazz) {

		List<T> rows = new ArrayList<T>();
		boolean foundHeaders = false;
		SheetReader reader = null;

		if (Format.XLSX.equals(format) || Format.XLS.equals(format)) {
			reader = new SheetReaderExcel(source);
		} else if (Format.CSV.equals(format)) {
			reader = new SheetReaderCSV(source);
		} else {
			throw new IllegalArgumentException("Unsupported format: " + format);
		}
		List<Field> columnUse = new ArrayList<Field>();

		Map<String, Field> columnToField = new HashMap<String, Field>();
		Collection<Field> rowNumberFields = new ArrayList<Field>();

		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(ColumnMapping.class)) {
				ColumnMapping mapping = field
						.getAnnotation(ColumnMapping.class);
				if (String.class.isAssignableFrom(field.getType())) {
					columnToField.put(mapping.value(), field);
				} else {
					throw new IllegalArgumentException(
							"ColumnMapping field is not a java.lang.String. "
									+ field);
				}
			}
			if (field.isAnnotationPresent(RowNumber.class)) {
				if (int.class.isAssignableFrom(field.getType())) {
					rowNumberFields.add(field);
				} else {
					throw new IllegalArgumentException(
							"RowNumber field is not a int. " + field);
				}
			}
		}
		try {
			while (reader.nextRow()) {
				if (foundHeaders) {
					T rowObj = clazz.newInstance();
					// Only include rows with data.
					boolean setSomething = false;
					while (reader.nextColumn()) {
						Field currentField = null;
						if (reader.getColumn() < columnUse.size()) {
							currentField = columnUse.get(reader.getColumn());
						}
						if (currentField != null) {
							String cellValue = reader.getContents();
							if (cellValue != null) {
								currentField.set(rowObj, cellValue);
								setSomething = true;
							}
						}
					}
					if (setSomething) {
						// Set the row number field.
						for (Field currentField : rowNumberFields) {
							currentField.set(rowObj, reader.getRow());
						}
						rows.add(rowObj);
					}
				} else {
					// Look for headers row.
					while (reader.nextColumn()) {
						// Only care about string cells.
						String value = reader.getContents();
						if (value != null) {
							value = value.trim().toLowerCase();
							Field column = columnToField.get(value);
							if (column != null) {
								addWithPadding(columnUse, reader.getColumn(),
										column);
								foundHeaders = true;
							}
						}
					}
					if (foundHeaders) {
						// Validate we have a good set.
					}
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!foundHeaders) {
			throw new IllegalStateException(
					"Failed to find any known headers in the file.");
		}
		return rows;
	}

	/**
	 * Pads the list with nulls if we're not added the item to the end.
	 */
	private static void addWithPadding(List<Field> columnUse, int pos,
			Field column) {
		while (columnUse.size() < pos) {
			columnUse.add(null);
		}
		columnUse.add(column);
	}
}
