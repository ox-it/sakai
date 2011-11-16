package uk.ac.ox.oucs.oxam.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ox.oucs.oxam.model.ExamPaper;

// TODO Look at @Parameterized for MySQL/Derby testing.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"/oxam-beans.xml", "/context.xml"})
@Transactional
public class ExamPaperDaoImplTest {

	@Autowired
	private ExamPaperDaoImpl dao;
	
	// We do tests in one method so we don't have the setup dummy data.
	@Test
	public void testAllOperations() {
		// Check it's empty.
		assertEquals(0, dao.getExamPapers(0, 10).size());
	
		ExamPaper examPaper = new ExamPaper();
		examPaper.setCategory("A");
		examPaper.setExamCode("EXAMCODE");
		examPaper.setExamTitle("Exam Title");
		examPaper.setPaperCode("PAPERCODE");
		examPaper.setPaperTitle("Paper Title");
		examPaper.setPaperFile("/some/file/url");
		examPaper.setTerm("T");
		examPaper.setYear(2011);

		dao.saveExamPaper(examPaper);
		
		assertFalse(examPaper.getId() == 0);
		
		assertEquals(1, dao.getExamPapers(0, 10).size());
		
		examPaper.setExamTitle("New Exam Title");
		dao.saveExamPaper(examPaper);
		
		ExamPaper examPaperFromDb = dao.getExamPaper(examPaper.getId());
		assertEquals("New Exam Title", examPaperFromDb.getExamTitle());
	}
	
	@Test
	public void testTransactions() {
		// Check it's empty.
		assertEquals(0, dao.getExamPapers(0, 10).size());
	}
	
	@Test
	public void testResolveExamCodes() {
		// TODO this doesn't handle duplicates in terms and paper codes, need to split papers out into separate table
		newExamPaper("ABC1", "My Exam Title", 2011);
		newExamPaper("ABC1", "Other Exam Title", 2010);
		newExamPaper("ABC1", "Other Exam Title 2", 2009);
		newExamPaper("ABC2", "Other Exam Title 2", 2011);
		assertEquals("My Exam Title", dao.resolveExamCodes(new String[]{"ABC1"}).get("ABC1"));
	}

	private void newExamPaper(String examCode, String examTitle, int year) {
		ExamPaper examPaper = new ExamPaper();
		examPaper.setCategory("A");
		examPaper.setExamCode(examCode);
		examPaper.setExamTitle(examTitle);
		examPaper.setPaperCode("PAPERCODE");
		examPaper.setPaperTitle("Paper Title");
		examPaper.setPaperFile("/some/file/url");
		examPaper.setTerm("T");
		examPaper.setYear(year);
		dao.saveExamPaper(examPaper);
	}

}
