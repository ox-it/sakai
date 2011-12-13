package uk.ac.ox.oucs.oxam.readers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.readers.Import.ExamRow;
import uk.ac.ox.oucs.oxam.readers.Import.PaperRow;
import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations= {"/standalone-beans.xml","/oxam-beans.xml","/import-beans.xml"})
@Transactional
public class BulkImportTest {

	
	@Autowired
	private Importer importer;
	
	@Autowired
	private TermService termService;

	@Test
	public void testImporter() throws FileNotFoundException {
		Import import1 = importer.newImport();
		String dir = "/Users/buckett/Documents/oxam/data/";
		InputStream paperInput = new FileInputStream(new File(dir+ "papercodes.csv"));
		import1.readPapers(paperInput, Format.CSV);
		System.out.println("Papers: "+import1.getPaperRows().size());
		InputStream examInput = new FileInputStream(new File(dir+ "examcodes.csv"));
		import1.readExams(examInput, Format.CSV);
		System.out.println("Exams: "+import1.getExamRows().size());
		InputStream examPaperInput = new FileInputStream(new File(dir+ "exampapers.csv"));
		import1.readExamPapers(examPaperInput, Format.CSV);
		System.out.println("ExamsPapers: "+import1.getExamPaperRows().size());

		import1.setPaperResolver(new FilesystemPaperResolver(dir, termService, "pdf"));

		import1.resolve();
		
		for (ErrorMessages<PaperRow> voilation :import1.getPaperRowErrors().values()) {
			System.out.println(voilation);
		}
		for (ErrorMessages<ExamRow> voilation :import1.getExamRowErrors().values()) {
			System.out.println(voilation);
		}
		
		System.out.println("PaperRowErrors: "+ import1.getPaperRowErrors().size());
		System.out.println("ExamRowErrors: "+ import1.getExamRowErrors().size());
		System.out.println("ExamPaperRowErrors: "+ import1.getExamPaperRowErrors().size());
		
	}

}
