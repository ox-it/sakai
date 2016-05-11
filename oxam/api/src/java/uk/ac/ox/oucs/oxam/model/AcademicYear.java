package uk.ac.ox.oucs.oxam.model;

import java.io.Serializable;

public class AcademicYear implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int year;

	public AcademicYear(int year) {
		this.year = year;
	}
	
	public int getYear() {
		return year;
	}

	public String toString() {
		return year+"-"+(year+1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + year;
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
		AcademicYear other = (AcademicYear) obj;
		if (year != other.year)
			return false;
		return true;
	}
}
