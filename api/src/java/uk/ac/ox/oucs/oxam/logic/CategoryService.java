package uk.ac.ox.oucs.oxam.logic;

import java.util.List;

import uk.ac.ox.oucs.oxam.model.Category;

public interface CategoryService {
	
	public Category getCategory(long id);
	
	public List<Category> getCatagories(int start, int length);
	
	public void saveCategory(Category category);

	/**
	 * This is used to lookup the category when importing categories.
	 * @param code The shortcode for the category.
	 * @return The Category found or null if there isn't one.
	 */
	public Category getCategoryByCode(String code);

}
