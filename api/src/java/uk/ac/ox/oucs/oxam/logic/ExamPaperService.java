package uk.ac.ox.oucs.oxam.logic;

import java.util.List;
import java.util.Map;

import uk.ac.ox.oucs.oxam.model.ExamPaper;

public interface ExamPaperService {

	public ExamPaper getExamPaper(long id);
	
	public List<ExamPaper> getExamPapers(int start, int length);
	
	public int count();
	
	public void saveExamPaper(ExamPaper paper) throws RuntimeException;

	public void deleteExamPaper(long id);
	
	public Map<String, String> resolveExamCodes(String[] codes);
	
	public Map<String, String> resolvePaperCodes(String[] codes);

	public int reindex();
	
}
