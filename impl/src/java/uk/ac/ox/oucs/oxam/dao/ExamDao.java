package uk.ac.ox.oucs.oxam.dao;

import java.util.Map;

import uk.ac.ox.oucs.oxam.model.Exam;

public interface ExamDao {

	public Exam getExam(long id);
	
	public void saveExam(Exam exam);

	public Exam getExam(String examCode, int year);
	
	public Map<String, Exam> getCodes(String[] codes);

}
