package uk.ac.ox.oucs.oxam.dao;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import uk.ac.ox.oucs.oxam.model.Exam;

import static org.junit.Assert.*;

@ContextConfiguration({ "classpath:/oxam-beans.xml", "classpath:/context.xml" })
public class ExamDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private ExamDao dao;

	public void setDao(ExamDao dao) {
		this.dao = dao;
	}

	@Test
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
