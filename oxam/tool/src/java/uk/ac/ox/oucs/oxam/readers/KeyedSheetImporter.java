package uk.ac.ox.oucs.oxam.readers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.validation.ConstraintViolation;

import uk.ac.ox.oucs.oxam.readers.Import.KeyedRow;
import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

/**
 * Class to import rows of a sheet which have a unique key based on some of the values.
 * The idea is that no duplicates of that key should exist in the import.
 * @author buckett
 *
 * @param <T>
 */
public class KeyedSheetImporter<T extends KeyedRow> {
	
	private Importer importer;
	private Class<T> clazz;
	
	// Good data.
	private Map<String, T>rows;
	// Other errors, Use a treemap so it's sorted based on the line number.
	private Map<Integer, ErrorMessages<T>> errors = new TreeMap<Integer, ErrorMessages<T>>();

	/**
	 * @param clazz The class is needed so we still have the type at runtime so we can create new instances.
	 */
	public KeyedSheetImporter(Class<T> clazz, Importer importer) {
		this.importer = importer;
		this.clazz = clazz;
	}

	public Map<String, T> getRows() {
		return rows;
	}

	public Map<Integer, ErrorMessages<T>> getErrors() {
		return errors;
	}
	

	public void read(InputStream source, Format format) {
		this.rows = new HashMap<String, T>();

		SheetImporter sheetImporter = new SheetImporter();
		List<T> paperRows = sheetImporter.importSheet(source, format, clazz);
		
		for (T row: paperRows) {
			Set<ConstraintViolation<T>> violations = importer.validate(row);
			if (violations.isEmpty()) {
				// Check for duplicates based on the key.
				T existing = this.rows.get(row.getKey());
				if (existing != null) {
					addError(row, "Is a duplicate of row "+ existing.getRow());

				} else {
					// Good data.
					this.rows.put(row.getKey(), row);
				}
			} else {
				for (ConstraintViolation<T> violation: violations) {
					addError(row, violation.getPropertyPath()+ " "+ violation.getMessage());
				}
			}
		}
	}
	
	public void addError(T row, String message) {
		if (row != null) {
			ErrorMessages<T> error = errors.get(row.getRow());
			if(error == null) {
				error = new ErrorMessages<T>(row.getRow(), row);
				errors.put(row.getRow(), error);
			}
			error.addMessage(message);
		}
	}


	
}
