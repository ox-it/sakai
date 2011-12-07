package uk.ac.ox.oucs.oxam.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ox.oucs.oxam.model.Paper;

// TODO Look at @Parameterized for MySQL/Derby testing.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"/context.xml","/oxam-beans.xml"})
@Transactional
public class PaperDaoImplTest {

	@Autowired
	private PaperDaoImpl dao;
	
	// We do tests in one method so we don't have the setup dummy data.
	@Test
	public void testAllOperations() {
	
		Paper paper = new Paper();
		paper.setTitle("Title");
		paper.setCode("CODE");
		
		dao.savePaper(paper);
		
		assertFalse(paper.getId() == 0);
		
		
		paper.setTitle("New Title");
		dao.savePaper(paper);
		
		Paper paperFromDb = dao.getPaper(paper.getId());
		assertEquals("New Title", paperFromDb.getTitle());
	}
}
