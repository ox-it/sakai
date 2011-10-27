package uk.ac.ox.oucs.oxam.model;

import java.io.Serializable;

public class ExamPaper implements Serializable{

	private static final long serialVersionUID = 1L;

	private long id;

	// Category, closely linked to the exam title.
	private long category;
	
	// From the exam.
	private String examTitle;
	private String examCode;
	
	// From the Paper
	private String paperTitle;
	private String paperCode;
	private String paperFile;
	
	// From terms.
	private Integer year;
	private Integer term;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getCategory() {
		return category;
	}
	public void setCategory(long category) {
		this.category = category;
	}
	public String getExamTitle() {
		return examTitle;
	}
	public void setExamTitle(String examTitle) {
		this.examTitle = examTitle;
	}
	public String getExamCode() {
		return examCode;
	}
	public void setExamCode(String examCode) {
		this.examCode = examCode;
	}
	public String getPaperTitle() {
		return paperTitle;
	}
	public void setPaperTitle(String paperTitle) {
		this.paperTitle = paperTitle;
	}
	public String getPaperCode() {
		return paperCode;
	}
	public void setPaperCode(String paperCode) {
		this.paperCode = paperCode;
	}
	public String getPaperFile() {
		return paperFile;
	}
	public void setPaperFile(String paperFile) {
		this.paperFile = paperFile;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getTerm() {
		return term;
	}
	public void setTerm(Integer term) {
		this.term = term;
	}
}
