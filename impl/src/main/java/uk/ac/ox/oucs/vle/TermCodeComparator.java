package uk.ac.ox.oucs.vle;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is for comparing term codes to put the newest first.
 * Who decided to just use 2 digits for term codes? What happens when someone puts some historic data in.
 * @author Matthew Buckett
 */
public class TermCodeComparator implements Comparator<String> {

	private Pattern pattern = Pattern.compile("(\\w\\w)(\\d\\d)");

	@Override
	public int compare(String o1, String o2) {
		TermCode t1 = new TermCode(o1);
		TermCode t2 = new TermCode(o2);
		return t1.compareTo(t2);
	}

	private class TermCode implements Comparable<TermCode> {
		private String source;
		private CourseComponentImpl.Terms term;
		private Integer year;

		private TermCode(String source) {
			this.source = source;
			if (source != null) {
				Matcher matcher = pattern.matcher(source);
				try {
					if (matcher.matches()) {
						term = CourseComponentImpl.Terms.valueOf(matcher.group(1));
						// Never get exception because of regexp.
						year = Integer.parseInt(matcher.group(2));
					}
				} catch (IllegalArgumentException iae) {
					// Just leave it as invalid.
				}
			}
		}

		private boolean isValid() {
			return term != null;
		}

		@Override
		public int compareTo(TermCode other) {
			if (! (this.isValid() && other.isValid()) ) {
				int ret = 0;
				ret += (this.isValid()?1:0);
				ret -= (other.isValid()?1:0);
				if(ret == 0) {
					// Fallback to the source so we have consistent sorting
					if (this.source != null && other.source != null) {
						ret = this.source.compareTo(other.source);
					} else {
						ret += (this.source != null)?1:0;
						ret -= (other.source != null)?1:0;
					}
				}
				return ret;
			}
			// All fields will be valid
			return
					Integer.signum(this.year.compareTo(other.year)) * 4 +
							Integer.signum(this.term.compareTo(other.term)) * 2 +
							Integer.signum(this.source.compareTo(other.source));
		}
	}
}
