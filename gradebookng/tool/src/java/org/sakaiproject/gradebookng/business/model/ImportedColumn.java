package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;

/**
 * Describes the type of column imported
 */
@NoArgsConstructor
@AllArgsConstructor
public class ImportedColumn implements Serializable {

	@Getter
	@Setter
	private String columnTitle;

	@Getter
	@Setter
	private String points;

	@Getter
	@Setter
	private Type type = Type.GB_ITEM_WITHOUT_POINTS;

	public enum Type {
		GB_ITEM_WITH_POINTS,
		GB_ITEM_WITHOUT_POINTS,
		COMMENTS,
		USER_ID,
		USER_NAME,
		STUDENT_NUMBER,
		ANONYMOUS_ID,
		IGNORE;
	}

	/**
	 * Helper to determine if the type of column can be ignored
	 * @return
	 */
	public boolean isIgnorable() {
		return this.type == Type.USER_ID || this.type == Type.USER_NAME || this.type == Type.STUDENT_NUMBER || this.type == Type.ANONYMOUS_ID || this.type == Type.IGNORE;
	}

	/**
	 * Column titles are the only thing we care about for comparisons so that we can filter out duplicates.
	 * Must also match type and exclude IGNORE
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(final Object o) {
		final ImportedColumn other = (ImportedColumn) o;
		if(StringUtils.isBlank(this.columnTitle) || StringUtils.isBlank(other.columnTitle)){
			return false;
		}
		if(this.type == Type.IGNORE || other.type == Type.IGNORE){
			return false;
		}

		//we allow columns names to be the same but of different cases (eg "Assignment 1" and "assignment 1" are both valid and unique)
		return StringUtils.equals(this.columnTitle, other.getColumnTitle()) && this.type == other.getType();
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 47 * hash + Objects.hashCode(this.columnTitle);
		hash = 47 * hash + Objects.hashCode(this.points);
		hash = 47 * hash + Objects.hashCode(this.type);
		return hash;
	}
}
