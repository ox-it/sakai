package uk.ac.ox.oucs.oxam.logic;

import java.util.List;
import java.util.Map;

import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.model.Exam;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Paper;
import uk.ac.ox.oucs.oxam.model.Term;

public interface ExamPaperService {

	public ExamPaper getExamPaper(long id);
	
	public List<ExamPaper> getExamPapers(int start, int length);
	
	public ExamPaper get(String examCode, String paperCode, AcademicYear year, Term term);
	
	public int count();
	
	public void saveExamPaper(ExamPaper paper) throws RuntimeException;

	public void deleteExamPaper(long id);
	
	/**
	 * This return exams details for a set of exam codes. 
	 * @param codes An array of codes to looks for or <code>null</code> for all the latest exams.
	 * @return
	 */
	public Map<String, Exam> getLatestExams(String[] codes);
	
	public Map<String, Paper> getLatestPapers(String[] codes);

	public int reindex();

	public ExamPaper newExamPaper();

	public List<AcademicYear> getYears();
}
