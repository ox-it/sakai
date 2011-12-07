package uk.ac.ox.oucs.oxam.model;

public class ExamPaperFile {

	private long id;
	private long exam;
	private long paper;
	private int year;
	private String file;
	private String term;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getExam() {
		return exam;
	}
	public void setExam(long exam) {
		this.exam = exam;
	}
	public long getPaper() {
		return paper;
	}
	public void setPaper(long paper) {
		this.paper = paper;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
}
