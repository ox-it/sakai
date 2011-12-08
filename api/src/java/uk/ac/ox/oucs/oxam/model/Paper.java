package uk.ac.ox.oucs.oxam.model;

public class Paper {
	
	private long id;
	private String code;
	private String title;
	private int year;
	boolean changed = false;
	
	public Paper(String code, int year) {
		this(0, code, year);
	}
	
	public Paper(long id, String code, int year) {
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		if (!title.equals(this.title)) {
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
		Paper other = (Paper) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
