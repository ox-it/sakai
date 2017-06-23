package org.sakaiproject.gradebookng.tool.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.gradebookng.business.model.GbGradeInfo;

/**
 * Created by chmaurer on 1/29/15.
 */
// @Data
public class AssignmentStudentGradeInfo implements Serializable {

	@Getter
	@Setter
	private Long assignmemtId;

	@Getter
	private final Map<String, GbGradeInfo> studentGrades;

	public AssignmentStudentGradeInfo() {
		this.studentGrades = new HashMap<>();
	}

	/**
	 * Helper to add a grade to the map
	 *
	 * @param studentId
	 * @param gradeInfo
	 */
	public void addGrade(final String studentId, final GbGradeInfo gradeInfo) {
		this.studentGrades.put(studentId, gradeInfo);
	}
}
