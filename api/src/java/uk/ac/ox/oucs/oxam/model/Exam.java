package uk.ac.ox.oucs.oxam.model;

/**
 * This is exposed so that you can get a list all possible exams.
 * @author buckett
 *
 */
public class Exam {

	private long id;
	private String code;
	private String category;
	private String title;
	private int year;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	
}
