package uk.ac.ox.oucs.oxam.dao;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import uk.ac.ox.oucs.oxam.model.Paper;

public class PaperDaoImplTest extends AbstractTransactionalDataSourceSpringContextTests {

	private PaperDaoImpl dao;

	public void setDao(PaperDaoImpl dao) {
		this.dao = dao;
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:/oxam-beans.xml", "classpath:/context.xml" };
	}

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
