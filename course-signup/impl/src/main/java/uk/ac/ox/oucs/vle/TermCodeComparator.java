package uk.ac.ox.oucs.vle;

import java.util.Comparator;

/**
 * This is for comparing term codes to put the newest first.
 * Who decided to just use 2 digits for term codes? What happens when someone puts some historic data in.
 * @author Matthew Buckett
 */
public class TermCodeComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		TermCode t1 = new TermCode(o1);
		TermCode t2 = new TermCode(o2);
		return t1.compareTo(t2);
	}

}
