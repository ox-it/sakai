package uk.ac.ox.oucs.oxam.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.oxam.model.Category;

/**
 * This is a very simple implementation of the category service which just stores the categorys in a flat file and loads them at startup.
 * @author buckett
 *
 */
public class CategoryServiceImpl implements CategoryService, Reloadable {

	private final static Log LOG = LogFactory.getLog(CategoryServiceImpl.class);

	private Map<String, Category> categorys;

	private ValueSource valueSource;

	public void setValueSource(ValueSource valueSource) {
		this.valueSource = valueSource;
	}
	
	public void init() {
		LOG.info("init()");
		reload();
	}

	public void reload() {
		LOG.info("Reloading data.");
		InputStream source = valueSource.getInputStream();
		if (source == null) {
			LOG.error("Failed to get a stream to read from.");
			return;
		}
		// We use a LinkedHashMap to preserve insertion order.
		Map<String, Category> newCategorys = new LinkedHashMap<String, Category>();
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(
				source));
		try {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				String[] parts = line.split(",");
				if (parts.length != 2) {
					LOG.warn("When reading line " + reader.getLineNumber()
							+ " which contains '" + line + "'");
				} else {
					String code = parts[0];
					String name = parts[1];
					Category newCategory = new Category(code, name);
					Category oldCategory = newCategorys.put(code, newCategory);
					if (oldCategory != null) {
						LOG.warn("Replaced old category value of " + oldCategory
								+ " with " + newCategory);
					} else {
						LOG.debug("Found new category: "+ newCategory);
					}
				}
			}
			if (newCategorys.size() > 0) {
				categorys = newCategorys;
				LOG.info("Successfully load new category data.");
			} else {
				LOG.warn("Failed to find any values in data, not replacing existing data.");
			}
		} catch (IOException ioe) {
			LOG.warn("Problem reading data.", ioe);
		} finally {
			if (source != null) {
				try {
					source.close();
				} catch (IOException ioe) {
					LOG.debug("Failed to close source.", ioe);
				}
			}
		}
	}

	public Category getByCode(String code) {
		return categorys.get(code);
	}

	public Collection<Category> getAll() {
		// This will be ordered as is't a LinkedHashMap
		return categorys.values();
	}

}
