package uk.ac.ox.oucs.vle;

import java.util.Arrays;
import java.util.Comparator;

/**
 * This is for sorting coursegroups with no date by when we think they might happen
 * based on the term.
 *
* @author Matthew Buckett
*/
class NoDateComparator implements Comparator<CourseGroup> {

	public int compare(CourseGroup c1, CourseGroup c2) {
		// This is for when we don't have a good date, but most courses will have
		// a term code.
		// TODO Check that the last component is always the latest.
		String when1 = c1.getComponents().get(c1.getComponents().size() - 1).getWhen();
		String when2 = c2.getComponents().get(c2.getComponents().size() - 1).getWhen();
		if (null == when1) {
			return 1;
		}
		if (null == when2) {
			return -1;
		}
		String[] words1 = when1.split(" ");
		String[] words2 = when2.split(" ");
		if (words1.length < 2) {
			return 1;
		}
		if (words2.length < 2) {
			return -1;
		}

		int i1 = Integer.parseInt(words1[1]);
		int i2 = Integer.parseInt(words2[1]);
		if (i1 > i2) {
			return 1;
		}
		if (i1 < i2) {
			return -1;
		}

		String[] terms = {"Michaelmas", "Hilary", "Trinity"};
		i1 = Arrays.asList(terms).indexOf(words1[0]);
		i2 = Arrays.asList(terms).indexOf(words2[0]);
		if (i1 > i2) {
			return 1;
		}
		if (i1 < i2) {
			return -1;
		}

		return c1.getTitle().compareTo(c2.getTitle());
	}
}
