package org.sakaiproject.gradebookng.business.model;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.gradebookng.business.finalgrades.GbStudentCourseGradeInfo;

/**
 * Model for storing the grade info for a student
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbStudentGradeInfo extends GbStudentCourseGradeInfo implements Serializable {

	@Getter
	private Map<Long, GbGradeInfo> grades;

	@Getter
	private Map<Long, Double> categoryAverages;

	private GbStudentGradeInfo() {
	}
	
	public GbStudentGradeInfo(final GbUser u)
	{
		super(u);
		
		grades = new HashMap<>();
		categoryAverages = new HashMap<>();
	}

	/**
	 * Helper to add an assignment grade to the map
	 *
	 * @param assignmentId
	 * @param gradeInfo
	 */
	public void addGrade(final Long assignmentId, final GbGradeInfo gradeInfo) {
		this.grades.put(assignmentId, gradeInfo);
	}

	/**
	 * Helper to add a category average to the map
	 *
	 * @param categoryId
	 * @param score
	 */
	public void addCategoryAverage(final Long categoryId, final Double score) {
		this.categoryAverages.put(categoryId, score);
	}
}
