package uk.ac.ox.oucs.oxam.readers;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.oucs.oxam.readers.Import.ExamPaperRow;
import uk.ac.ox.oucs.oxam.readers.SheetImporter.Format;

public class YearImportTest {

	private Import import1;

	@Before
	public void setUp() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		
		Importer importer = new Importer();
		importer.setValidatorFactory(validatorFactory);
		
		import1 = importer.newImport();
	}
	
	@Test
	public void testImporter() {
		InputStream paperInput = getClass().getResourceAsStream("/Papercodes.xlsx");
		import1.readPapers(paperInput, Format.XLSX);
		InputStream examInput = getClass().getResourceAsStream("/Examcodes.xlsx");
		import1.readExams(examInput, Format.XLSX);
		InputStream examPaperInput = getClass().getResourceAsStream("/ExamPapersMerged.xlsx");
		import1.readExamPapers(examPaperInput, Format.XLSX);
		
		
		import1.resolve();
		
		for (ErrorMessages<ExamPaperRow> voilation :import1.getExamPaperRowErrors().values()) {
			System.out.println(voilation);
			
		}
		System.out.println("Count: "+ import1.getExamPaperRows().size());

		
	}

}
