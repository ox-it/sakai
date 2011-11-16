package uk.ac.ox.oucs.oxam.dao;

import java.util.List;
import java.util.Map;

import uk.ac.ox.oucs.oxam.logic.Callback;
import uk.ac.ox.oucs.oxam.model.ExamPaper;

public interface ExamPaperDao {

	public ExamPaper getExamPaper(long id);
	
	public List<ExamPaper> getExamPapers(int start, int length);
	
	public void saveExamPaper(ExamPaper examPaper);

	public void deleteExamPaper(long id);

	public Map<String, String> resolvePaperCodes(String[] codes);

	public Map<String, String> resolveExamCodes(String[] codes);

	public int count();

	/**
	 * For all the items execute this callback.
	 * This is here so that transactionally we can do something to all the items, without exposing the upper layers to the transaction.
	 * The alternative would have been to have exposed an iterator, but then you need to leave the transaction open while the iterator is in use
	 * and rely on the upper layers to behave well.
	 * @param callback
	 */
	public void all(Callback<ExamPaper> callback);
}
