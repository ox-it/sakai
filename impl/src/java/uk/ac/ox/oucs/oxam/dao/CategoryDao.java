package uk.ac.ox.oucs.oxam.dao;

import java.util.List;

import uk.ac.ox.oucs.oxam.model.Category;

public interface CategoryDao {
	
	public Category getCategory(long id);
	
	public List<Category> getCatagories(int start, int length);
	
	public void saveCategory(Category category);

}
