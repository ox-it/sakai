package uk.ac.ox.oucs.oxam.dao;

import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import uk.ac.ox.oucs.oxam.model.Exam;

public class ExamDaoImplTest extends AbstractTransactionalDataSourceSpringContextTests {

	private ExamDao dao;

	public void setDao(ExamDao dao) {
		this.dao = dao;
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:/oxam-beans.xml", "classpath:/context.xml" };
	}
	
	public void testAllOpertations() {
		Exam exam = new Exam("CODE", 2000);
		exam.setCategory("AA");
		exam.setTitle("Title");
		dao.saveExam(exam);
		assertFalse(exam.getId() == 0);
		
		long id = exam.getId();
		
		exam.setTitle("New Title");
		dao.saveExam(exam);
		
		Exam loadedExam = dao.getExam(id);
		assertNotNull(loadedExam);
		assertEquals("New Title", loadedExam.getTitle());
	}
}
