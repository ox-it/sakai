package uk.ac.ox.oucs.oxam.dao;

import java.util.List;

import uk.ac.ox.oucs.oxam.model.Exam;

public interface ExamDao {

	public Exam getExam(long id);
	
	public List<Exam> getExams(int start, int length);
	
	public void saveExam(Exam exam);
}
