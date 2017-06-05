package org.sakaiproject.gradebookng.business.finalgrades;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.gradebookng.business.model.GbCourseGrade;
import org.sakaiproject.gradebookng.business.model.GbUser;

/**
 *
 * @author plukasew
 */
public class GbStudentCourseGradeInfo implements Serializable
{
	@Getter
	private final GbUser student;
	
	@Getter
	@Setter
	private GbCourseGrade courseGrade;

	protected GbStudentCourseGradeInfo()
	{
		student = null;
	}
	
	public GbStudentCourseGradeInfo(final GbUser user)
	{
		student = user;
	}
}
