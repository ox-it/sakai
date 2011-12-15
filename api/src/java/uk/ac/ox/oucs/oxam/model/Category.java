package uk.ac.ox.oucs.oxam.model;

import java.io.Serializable;

/**
 * These remain reasonably static but need to be editable.
 * @author buckett
 *
 */
public class Category implements Serializable, Comparable<Category> {

	private static final long serialVersionUID = 1L;
	private final String name;
	private final String code;
	
	public Category(String code, String name) {
		this.name = name;
		this.code = code;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "Category [name=" + name + ", code=" + code + "]";
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code.hashCode();
		result = prime * result + name.hashCode();
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
		Category other = (Category) obj;
		if (!code.equals(other.code))
			return false;
		if (!name.equals(other.name))
			return false;
		return true;
	}

	public int compareTo(Category that) {
		int result = this.getName().compareTo(that.getName());
		if (result == 0) {
			result = this.getCode().compareTo(that.getCode());
		}
		return result;
	}
}
