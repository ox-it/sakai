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
		// Check it's empty.
		assertEquals(0, dao.getPapers(0, 10).size());
	
		Paper paper = new Paper();
		paper.setTitle("Title");
		paper.setActive(true);
		paper.setCode("CODE");
		paper.setFile("/my/file/name.txt");
		
		dao.savePaper(paper);
		
		assertFalse(paper.getId() == 0);
		
		assertEquals(1, dao.getPapers(0, 10).size());
		
		paper.setTitle("New Title");
		dao.savePaper(paper);
		
		Paper paperFromDb = dao.getPaper(paper.getId());
		assertEquals("New Title", paperFromDb.getTitle());
	}
	
	@Test
	public void testTransactions() {
		// Check it's empty.
		assertEquals(0, dao.getPapers(0, 10).size());
	}

}
