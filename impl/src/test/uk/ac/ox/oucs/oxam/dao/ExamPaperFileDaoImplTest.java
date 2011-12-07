package uk.ac.ox.oucs.oxam.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ox.oucs.oxam.model.ExamPaperFile;

// TODO Look at @Parameterized for MySQL/Derby testing.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"/oxam-beans.xml", "/context.xml"})
@Transactional
public class ExamPaperFileDaoImplTest {

	@Autowired
	private ExamPaperFileDao dao;
	
	// We do tests in one method so we don't have the setup dummy data.
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
	
	@Test
	public void testTransactions() {
		// Check it's empty.
	}
	
}
