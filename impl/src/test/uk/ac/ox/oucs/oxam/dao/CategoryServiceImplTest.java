package uk.ac.ox.oucs.oxam.dao;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import uk.ac.ox.oucs.oxam.logic.CategoryServiceImpl;
import uk.ac.ox.oucs.oxam.model.Category;

public class CategoryServiceImplTest extends AbstractDependencyInjectionSpringContextTests{
	

	private CategoryServiceImpl categoryService;
	
	public void setCategoryService(CategoryServiceImpl categoryService) {
		this.categoryService = categoryService;
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:uk/ac/ox/oucs/oxam/dao/CategoryServiceImplTest-context.xml" };
	}
	
	public void testLoaded() {
		// Simple test which just checks the data got loaded.
		assertEquals("Qualifying Examinations", categoryService.getByCode("T").getName());
		assertNull(categoryService.getByCode("P"));
		assertEquals(28, categoryService.getAll().size());
		// Check first
		assertEquals("A", categoryService.getAll().iterator().next().getCode());
		// Check last
		assertEquals("Z", categoryService.getAll().toArray(new Category[]{})[27].getCode());
	}
	
}
