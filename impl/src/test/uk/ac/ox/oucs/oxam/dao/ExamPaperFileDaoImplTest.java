package uk.ac.ox.oucs.oxam.dao;

import org.junit.Test;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import uk.ac.ox.oucs.oxam.model.ExamPaperFile;

public class ExamPaperFileDaoImplTest extends AbstractTransactionalDataSourceSpringContextTests {
	
	private ExamPaperFileDao dao;
	
	public void setDao(ExamPaperFileDao dao) {
		this.dao = dao;
	}
	

	protected String[] getConfigLocations() {
		return new String[] { "classpath:/oxam-beans.xml", "classpath:/context.xml" };
	}

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
