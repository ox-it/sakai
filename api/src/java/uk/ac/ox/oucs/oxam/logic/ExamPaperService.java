package uk.ac.ox.oucs.oxam.logic;

import java.util.List;

import uk.ac.ox.oucs.oxam.model.ExamPaper;

public interface ExamPaperService {

	public ExamPaper getExamPaper(long id);
	
	public List<ExamPaper> getExamPapers(int start, int length);
	
	public void saveExamPaper(ExamPaper paper) throws RuntimeException;

	public void deleteExamPaper(long id);
	
	
}
