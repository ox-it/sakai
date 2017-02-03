package uk.ac.ox.oucs.oxam.dao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ox.oucs.oxam.model.Paper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Transactional
@ContextConfiguration({ "classpath:/oxam-beans.xml", "classpath:/context.xml" })
public class PaperDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
	private PaperDaoImpl dao;

	public void setDao(PaperDaoImpl dao) {
		this.dao = dao;
	}

	@Test
	public void testAllOperations() {
	
		Paper paper = new Paper("CODE", 2000);
		paper.setTitle("Title");
		
		dao.savePaper(paper);
		
		assertFalse(paper.getId() == 0);
		
		
		paper.setTitle("New Title");
		dao.savePaper(paper);
		
		Paper paperFromDb = dao.getPaper(paper.getId());
		assertEquals("New Title", paperFromDb.getTitle());
	}
}
