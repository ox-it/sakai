package uk.ac.ox.oucs.oxam.logic;

import java.util.Collection;

import uk.ac.ox.oucs.oxam.model.Category;

/**
 * 
 * @author buckett
 *
 */
public interface CategoryService {
	
	public Collection<Category> getAll();

	/**
	 * This is used to lookup the category when importing categories.
	 * @param code The shortcode for the category.
	 * @return The Category found or null if there isn't one.
	 */
	public Category getByCode(String code);

}
