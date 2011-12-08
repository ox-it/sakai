package uk.ac.ox.oucs.oxam.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.oxam.logic.Callback;
import uk.ac.ox.oucs.oxam.logic.CategoryService;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.logic.PaperFile;
import uk.ac.ox.oucs.oxam.logic.PaperFileServiceImpl;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;
import uk.ac.ox.oucs.oxam.readers.Import.ExamPaperRow;
import uk.ac.ox.oucs.oxam.readers.Import.ExamRow;
import uk.ac.ox.oucs.oxam.readers.Import.PaperRow;

public class Importer {
	
	public static final Log LOG = LogFactory.getLog(Importer.class);
	
	private Validator validator;
	private ExamPaperService examPaperService;
	private PaperFileServiceImpl paperFileService;
	private TermService termService;
	private CategoryService categoryService;

	public void setValidatorFactory(ValidatorFactory validator) {
		this.validator = validator.getValidator();
	}

	public void setExamPaperService(ExamPaperService service) {
		this.examPaperService = service;
	}
	
	public void setTermService(TermService termService) {
		this.termService = termService;
	}

	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	public void setPaperFileService(PaperFileServiceImpl paperFileService) {
		this.paperFileService = paperFileService;
	}

	public Import newImport() {
		return new Import(this);
	}
	
	public <T> Set<ConstraintViolation<T>> validate(T object) {
		return validator.validate(object);
	}
	
	public void persist(ExamPaper examPaper, final InputStream inputStream) {
		// Need to map it to a URL, then upload it.
		// Must then set the URL on the examPaper.
		PaperFile file = paperFileService.get(examPaper.getYear().toString(), examPaper.getTerm().getCode(), examPaper.getPaperCode(), "pdf");
		
		paperFileService.deposit(file, new Callback<OutputStream>() {
			public void callback(OutputStream output) {
				try {
					IOUtils.copy(inputStream, output);
				} catch (IOException e) {
					// TODO Need to deal with this error;
					// Maybe should be in the import so we can collect errors.
				} finally {
					// We don't close the streams here as we didn't create them here.
				}
			}
		});
		examPaper.setPaperFile(file.getURL());
		examPaperService.saveExamPaper(examPaper);
	}
	
	public boolean paperExists(ExamPaper examPaper) {
		PaperFile file = paperFileService.get(examPaper.getYear().toString(), examPaper.getTerm().getCode(), examPaper.getPaperCode(), "pdf");
		return paperFileService.exists(file);
	}

	public boolean checkTerm(PaperRow row) {
		return row != null && termService.getByCode(row.term)!= null; 
	}

	public Term checkTerm(ExamPaperRow row) {
		return (row != null)?termService.getByCode(row.term):null;
	}
	
	public Category checkCategory(ExamRow row) {
		return (row != null)?categoryService.getByCode(row.category):null;
	}

	public ExamPaper get(String examCode, String paperCode, int year, Term term) {
		return examPaperService.get(examCode, paperCode, year, term);
	}

}
