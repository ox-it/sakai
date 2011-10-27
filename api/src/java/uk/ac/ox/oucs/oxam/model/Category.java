package uk.ac.ox.oucs.oxam.model;

/**
 * These remain reasonably static, but need to be read from the DB and editable.
 * @author buckett
 *
 */
public class Category {

	private long id;
	private String title;
	private String code;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
}
