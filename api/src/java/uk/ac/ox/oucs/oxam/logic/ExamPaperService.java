package uk.ac.ox.oucs.oxam.logic;

import java.util.List;
import java.util.Map;

import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.model.Exam;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Paper;
import uk.ac.ox.oucs.oxam.model.Term;

/**
 * The exam paper API doesn't use examples for searching as although it's more generic it means you don't know 
 * what you need to optimise on. Having the calls only allowing searching on some fields means we know that
 * the API can't be slow.
 * 
 * @author buckett
 *
 */
public interface ExamPaperService {

	public ExamPaper getExamPaper(long id);

	/**
	 * This just gets a list of exampapers.
	 * @see #getExamPapers(String, String, AcademicYear, int, int)
	 */
	public List<ExamPaper> getExamPapers(int start, int length);
	
	/**
	 * This searches for some exam papers.
	 * @param examCode The examCode to look for, this can be null.
	 * @param paperCode The paperCode to look for, this can be null.
	 * @param year The academic year to look in, this can be null.
	 * @param start The first record in the results to return.
	 * @param length The number of records to return.
	 * @return
	 */
	List<ExamPaper> getExamPapers(String examCode, String paperCode,
			AcademicYear year, Term term, int start, int length);
	
	public ExamPaper get(String examCode, String paperCode, AcademicYear year, Term term);
	
	/**
	 * Count the number of exampapers we have in total.
	 * @return
	 */
	public int count(String examCode, String paperCode, AcademicYear year, Term term);

	public void saveExamPaper(ExamPaper paper) throws RuntimeException;

	/**
	 * Remove a single exampaper by ID.
	 * @param id The ID of the exampaper to remove.
	 */
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
