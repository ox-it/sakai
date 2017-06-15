package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Optional;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.service.gradebook.shared.CourseGrade;

import lombok.Getter;
import lombok.Setter;

/**
 * Wraps a {@link CourseGrade} and provides a display string formatted according to the settings from various places in the UI
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbCourseGrade implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private final CourseGrade courseGrade;

	@Getter
	@Setter
	private String displayString;

	/**
	 * Constructor. Takes a {@link CourseGrade}. Display string is set afterwards.
	 *
	 * @param courseGrade CourseGrade object
	 */
	public GbCourseGrade(final CourseGrade courseGrade) {
		this.courseGrade = courseGrade;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	public Optional<String> getOverride()
	{
		if (courseGrade != null)
		{
			return Optional.ofNullable(courseGrade.getEnteredGrade());
		}
		
		return Optional.empty();
	}

	public Optional<Double> getCalculatedGrade()
	{
		try
		{
			if (courseGrade != null)
			{
				return Optional.of(Double.parseDouble(courseGrade.getCalculatedGrade()));
			}
		}
		catch (NullPointerException | NumberFormatException e)
		{
			// do nothing
		}
		
		return Optional.empty();
	}
}
