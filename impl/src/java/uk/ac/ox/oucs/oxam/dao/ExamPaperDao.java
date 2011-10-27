package uk.ac.ox.oucs.oxam.dao;

import java.util.List;

import uk.ac.ox.oucs.oxam.model.ExamPaper;

public interface ExamPaperDao {

	public ExamPaper getExamPaper(long id);
	
	public List<ExamPaper> getExamPapers(int start, int length);
	
	public void saveExamPaper(ExamPaper examPaper);
}
