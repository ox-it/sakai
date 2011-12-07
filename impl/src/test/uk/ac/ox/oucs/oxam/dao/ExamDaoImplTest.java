package uk.ac.ox.oucs.oxam.dao;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ox.oucs.oxam.model.Exam;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/context.xml", "/oxam-beans.xml"})
@Transactional
public class ExamDaoImplTest {

	@Autowired
	private ExamDaoImpl dao;
	
	@Test
	public void testAllOpertations() {
		Exam exam = new Exam();
		exam.setCategory("AA");
		exam.setTitle("Title");
		exam.setCode("CODE");
		exam.setYear(2000);
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
