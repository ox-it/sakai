package uk.ac.ox.oucs.vle;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This is for sorting coursegroups with no date by when we think they might happen
 * based on the term. Puts oldest first.
 *
* @author Matthew Buckett
*/
class NoDateComparator implements Comparator<CourseGroup> {

	// This does the actual sorting based on term codes.
	private TermCodeComparator comp = new TermCodeComparator();

	public int compare(CourseGroup c1, CourseGroup c2) {
		// This is for when we don't have a good date, but most courses will have
		// a term code.
		// This does return the latest, but only where dates are concerned, otherwise
		// it's based on the presentation ID.
		String tc1 = getTermCode(c1);
		String tc2 = getTermCode(c2);
		int ret = comp.compare(tc1, tc2);
		if (ret == 0 ) {
			ret = c1.getTitle().compareTo(c2.getTitle());
		}
		return ret;
	}

	private String getTermCode(CourseGroup cg) {
		List<CourseComponent> components = cg.getComponents();
		if (!components.isEmpty()) {
			CourseComponent component = components.get(components.size() -1);
			if (component != null) {
				return component.getTermCode();
			}
		}
		return null;
	}
}
