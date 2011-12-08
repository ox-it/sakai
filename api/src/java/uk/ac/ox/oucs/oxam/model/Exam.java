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
	// Tracks if this pojo has changed.
	boolean changed = false;
	
	public Exam(String code, int year) {
		this(0, code, year);
	}
	
	public Exam(long id, String code, int year) {
		this.id = id;
		this.code = code;
		this.year = year;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		if (!category.equals(this.category)) {
			if (this.category != null) {
				changed = true;
			}
			this.category = category;
			
		}
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		if(!title.equals(this.title)){
			if (this.title != null) {
				changed = true;
			}
			this.title = title;
		}
	}
	public int getYear() {
		return year;
	}
	public boolean hasChanged() {
		return changed;
	}
	
}
