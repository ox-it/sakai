package org.sakaiproject.gradebookng.business.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.sakaiproject.service.gradebook.shared.GradeDefinition;

/**
 * Similar to GradeDefinition but serialisable and grader permission aware
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbGradeInfo implements Serializable, Comparable<GbGradeInfo> {

	@Getter
	private final String grade;

	@Getter
	private final String gradeComment;

	/**
	 * Whether or not a user is able to grade this instance of the grade
	 */
	@Getter
	@Setter
	private boolean gradeable;

	/**
	 * Constructor. Takes a GradeDefinition or null. If null, a stub is created.
	 *
	 * @param gd GradeDefinition object. May be null
	 */
	public GbGradeInfo(final GradeDefinition gd) {

		// allows for a stub
		if (gd == null) {
			this.grade = null;
			this.gradeComment = null;
			this.gradeable = false;
		} else {
			this.grade = gd.getGrade();
			this.gradeComment = gd.getGradeComment();
			this.gradeable = false;
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Only compares grades
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(final GbGradeInfo o) {
		return new CompareToBuilder()
				.append(this.grade, o.getGrade())
				.toComparison();
	}
}
