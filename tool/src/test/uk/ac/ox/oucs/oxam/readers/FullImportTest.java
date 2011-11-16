package uk.ac.ox.oucs.oxam.readers;

import java.io.InputStream;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ox.oucs.oxam.logic.CategoryService;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.readers.Import.ExamPaperRow;

// TODO Look at @Parameterized for MySQL/Derby testing.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"/context.xml","/oxam-beans.xml"})
@Transactional
public class FullImportTest {

	@Autowired
	private ExamPaperService service;
	
	@Autowired
	private TermService termService;
	
	@Autowired
	private CategoryService categoryService;
	
	private Import import1;

	@Before
	public void setUp() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		
		Importer importer = new Importer();
		importer.setValidatorFactory(validatorFactory);
		importer.setExamPaperService(service);
		importer.setCategoryService(categoryService);
		importer.setTermService(termService);
		
		import1 = importer.newImport();
	}
	
	@Test
	public void testImporter() {
		InputStream paperInput = getClass().getResourceAsStream("/Papercodes.xlsx");
		import1.readPapers(paperInput, "/Papercodes.xlsx");
		InputStream examInput = getClass().getResourceAsStream("/Examcodes.xlsx");
		import1.readExams(examInput, "/Examcodes.xlsx");
		InputStream examPaperInput = getClass().getResourceAsStream("/ExamPapersMerged.xlsx");
		import1.readExamPapers(examPaperInput, "/ExamPapersMerged.xlsx");
		
		
		import1.resolve();
		
		for (ErrorMessages<ExamPaperRow> voilation :import1.getExamPaperRowErrors().values()) {
			System.out.println(voilation);
			
		}
		System.out.println("Count: "+ import1.getExamPaperRows().size());

		
	}

}
