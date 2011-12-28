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
	private AcademicYear year;
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
	public AcademicYear getYear() {
		return year;
	}
	public void setYear(AcademicYear year) {
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
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result
				+ ((examCode == null) ? 0 : examCode.hashCode());
		result = prime * result + (int) (examId ^ (examId >>> 32));
		result = prime * result
				+ ((examTitle == null) ? 0 : examTitle.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result
				+ ((paperCode == null) ? 0 : paperCode.hashCode());
		result = prime * result
				+ ((paperFile == null) ? 0 : paperFile.hashCode());
		result = prime * result + (int) (paperId ^ (paperId >>> 32));
		result = prime * result
				+ ((paperTitle == null) ? 0 : paperTitle.hashCode());
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
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
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (examCode == null) {
			if (other.examCode != null)
				return false;
		} else if (!examCode.equals(other.examCode))
			return false;
		if (examId != other.examId)
			return false;
		if (examTitle == null) {
			if (other.examTitle != null)
				return false;
		} else if (!examTitle.equals(other.examTitle))
			return false;
		if (id != other.id)
			return false;
		if (paperCode == null) {
			if (other.paperCode != null)
				return false;
		} else if (!paperCode.equals(other.paperCode))
			return false;
		if (paperFile == null) {
			if (other.paperFile != null)
				return false;
		} else if (!paperFile.equals(other.paperFile))
			return false;
		if (paperId != other.paperId)
			return false;
		if (paperTitle == null) {
			if (other.paperTitle != null)
				return false;
		} else if (!paperTitle.equals(other.paperTitle))
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		if (year == null) {
			if (other.year != null)
				return false;
		} else if (!year.equals(other.year))
			return false;
		return true;
	}

	
}
