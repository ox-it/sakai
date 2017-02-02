package uk.ac.ox.oucs.oxam.dao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import uk.ac.ox.oucs.oxam.model.ExamPaperFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@ContextConfiguration({ "classpath:/oxam-beans.xml", "classpath:/context.xml" })
public class ExamPaperFileDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private ExamPaperFileDao dao;
	
	public void setDao(ExamPaperFileDao dao) {
		this.dao = dao;
	}
	

	@Test
	public void testAllOperations() {
	
		ExamPaperFile examPaperFile = new ExamPaperFile();
		examPaperFile.setExam(1);
		examPaperFile.setPaper(1);
		examPaperFile.setFile("File");
		examPaperFile.setTerm("T");
		examPaperFile.setYear(2011);

		dao.save(examPaperFile);
		
		assertFalse(examPaperFile.getId() == 0);
		
		
		examPaperFile.setFile("New File");
		dao.save(examPaperFile);
		
		ExamPaperFile examPaperFromDb = dao.get(examPaperFile.getId());
		assertEquals("New File", examPaperFromDb.getFile());
	}
	
	public void testTransactions() {
		// Check it's empty.
	}
	
}
