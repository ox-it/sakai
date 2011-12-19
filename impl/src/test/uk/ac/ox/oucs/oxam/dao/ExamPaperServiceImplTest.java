package uk.ac.ox.oucs.oxam.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ox.oucs.oxam.logic.CategoryService;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.Exam;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Paper;
import uk.ac.ox.oucs.oxam.model.Term;

//TODO Look at @Parameterized for MySQL/Derby testing.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"/context.xml","/oxam-beans.xml"})
@Transactional
public class ExamPaperServiceImplTest {

	@Autowired
	private ApplicationContext ac;
	
	// Autowiring doesn't work as the proxy means it can't wire by type 
	private ExamPaperService service;
	
	@Autowired
	private CategoryService categoryService;
	
	@Autowired
	private TermService termService;
	
	@Before
	public void setUp() {
		service = (ExamPaperService) ac.getBean("examPaperService");
	}
	
	@Test
	public void test() {
		assertEquals(0, service.count());
		ExamPaper examPaper = service.newExamPaper();
		examPaper.setCategory(categoryService.getAll().iterator().next());
		examPaper.setExamCode("EXAM");
		examPaper.setExamTitle("Exam Title");
		examPaper.setPaperCode("PAPER");
		examPaper.setPaperTitle("Paper Title");
		examPaper.setPaperFile("/file");
		examPaper.setYear(new AcademicYear(2000));
		examPaper.setTerm(termService.getAll().iterator().next());
		service.saveExamPaper(examPaper);
		assertNotNull(service.getExamPaper(examPaper.getId()));
		assertEquals(1, service.count());
		examPaper.setPaperFile("/other");
		service.saveExamPaper(examPaper);
		assertTrue(examPaper.getId() != 0);
		examPaper = service.getExamPaper(examPaper.getId());
		assertEquals("/other", examPaper.getPaperFile());
		examPaper.setExamTitle("Other Exam Title");
		service.saveExamPaper(examPaper);
	}
	
	@Test
	@ExpectedException(RuntimeException.class)
	public void testMissing() {
		service.getExamPaper(1);
	}

	@Test
	public void testGetExamCodes() {
		newExam("ABC1", "My Exam Title", 2011);
		newExam("ABC1", "Other Exam Title", 2010);
		newExam("ABC1", "Other Exam Title 2", 2009);
		newExam("ABC2", "Other Exam Title 2", 2011);
		Map<String, Exam> resolveExamCodes = service.getLatestExams(new String[]{"ABC1"});
		assertEquals("My Exam Title", resolveExamCodes.get("ABC1").getTitle());
	}

	private void newExam(String examCode, String examTitle, int year) {
		ExamPaper examPaper = new ExamPaper();
		examPaper.setCategory(new Category("A", "A Name"));
		examPaper.setExamCode(examCode);
		examPaper.setExamTitle(examTitle);
		examPaper.setPaperCode("PAPERCODE");
		examPaper.setPaperTitle("Paper Title");
		examPaper.setPaperFile("/some/file/url");
		examPaper.setTerm(new Term("Y", "Y Term", 1, false));
		examPaper.setYear(new AcademicYear(year));
		service.saveExamPaper(examPaper);
	}
	
	@Test
	public void testGetPaperCodes() {
		newPaper("PAP1", "Paper 1", 2000);
		newPaper("PAP2", "Paper 2", 2000);
		newPaper("PAP1", "Paper 1", 2001);
		newPaper("PAP1", "New Paper 1", 2003);
		Map<String,Paper> papers = service.getLatestPapers(new String[]{"PAP1", "PAP2"});
		assertEquals("New Paper 1", papers.get("PAP1").getTitle());
		assertEquals("Paper 2", papers.get("PAP2").getTitle());
	}
	
	private void newPaper(String paperCode, String paperTitle, int year) {
		ExamPaper examPaper = new ExamPaper();
		examPaper.setCategory(new Category("A", "A Name"));
		examPaper.setExamCode("EXAM");
		examPaper.setExamTitle("Exam Title");
		examPaper.setPaperCode(paperCode);
		examPaper.setPaperTitle(paperTitle);
		examPaper.setPaperFile("/some/file/url");
		examPaper.setTerm(new Term("Y", "Y Term", 1, false));
		examPaper.setYear(new AcademicYear(year));
		service.saveExamPaper(examPaper);
	}

	
}
