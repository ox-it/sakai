package uk.ac.ox.oucs.vle;

import java.util.Comparator;
import java.util.Date;

/**
 * This finds which of the components is newer based on it's base date. If the base date isn't
 * set then it looks to see if there is any start text.
 * @see Date#compareTo(Object)
 * @author Matthew Buckett
 */
public class BaseDateComparator implements Comparator<CourseComponent> {

	@Override
	public int compare(CourseComponent o1, CourseComponent o2) {
		return getBaseDate(o1).compareTo(getBaseDate(o2));
	}

	/**
	 * Extracts the base date from a component.
	 * @param component The course component.
	 * @return A date when the course transitions from previous to current. Never returns <code>null</code>.
	 * @see XcriOxCapPopulator#baseDate
	 */
	public static Date getBaseDate(CourseComponent component) {
		// In the future this should all be in one place and calculated when we import the course.
		// But this needs better test coverage before that sort of refactor happens.
		Date date = component.getBaseDate();
		if (date == null) {
			// If we have some start text then consider it sometime in the future.
			if (null != component.getStartsText() && !component.getStartsText().isEmpty()) {
				date = new Date(Long.MAX_VALUE);
			} else {
				date = new Date(0);
			}
		}
		return date;
	}
}
