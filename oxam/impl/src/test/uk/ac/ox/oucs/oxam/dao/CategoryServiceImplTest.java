package uk.ac.ox.oucs.oxam.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ox.oucs.oxam.logic.CategoryServiceImpl;
import uk.ac.ox.oucs.oxam.model.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ContextConfiguration("classpath:uk/ac/ox/oucs/oxam/dao/CategoryServiceImplTest-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class CategoryServiceImplTest {
	

    @Autowired
	private CategoryServiceImpl categoryService;
	
	public void setCategoryService(CategoryServiceImpl categoryService) {
		this.categoryService = categoryService;
	}

	@Test
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
