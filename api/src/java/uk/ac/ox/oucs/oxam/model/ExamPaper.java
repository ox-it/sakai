package uk.ac.ox.oucs.oxam.model;

import java.io.Serializable;

public class ExamPaper implements Serializable{

	private static final long serialVersionUID = 1L;

	private long id;

	// Category, closely linked to the exam title.
	private Category category;
	
	// From the exam.
	private long examId;
	private String examTitle;
	private String examCode;
	
	// From the Paper
	private long paperId; // Shouldn't be in API.
	private String paperTitle;
	private String paperCode;
	private String paperFile;
	
	// From terms.
	private Integer year;
	private Term term;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
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
	public Term getTerm() {
		return term;
	}
	public void setTerm(Term term) {
		this.term = term;
	}
	public long getExamId() {
		return examId;
	}
	public void setExamId(long examId) {
		this.examId = examId;
	}
	public long getPaperId() {
		return paperId;
	}
	public void setPaperId(long paperId) {
		this.paperId = paperId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExamPaper other = (ExamPaper) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
