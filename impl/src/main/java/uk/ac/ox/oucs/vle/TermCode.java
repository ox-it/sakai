package uk.ac.ox.oucs.vle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a parsed term code.
 *
 * @author Matthew Buckett
 */
class TermCode implements Comparable<TermCode> {

	// Patterns are threadsafe
	private static final Pattern pattern = Pattern.compile("(\\w\\w)(\\d\\d)");

	/**
	 * Enum for the standard Oxford term.
	 */
	public static enum Terms {
		// Order matters as it's used for sorting.
		MT("Michaelmas"), HT("Hilary"),TT("Trinity");

		private String title;

		Terms(String title) {
			this.title = title;
		}
		public String title() {
			return this.title;
		}
	}

	private String source;
	private Terms term;
	private Integer year;

	/**
	 * Create a new termcode by parsing a string.
	 * @param source The source to parse, eg TT10 or MT11
	 */
	public TermCode(String source) {
		this.source = source;
		if (source != null) {
			Matcher matcher = pattern.matcher(source);
			try {
				if (matcher.matches()) {
					term = Terms.valueOf(matcher.group(1));
					// Never get exception because of regexp.
					year = Integer.parseInt(matcher.group(2));
				}
			} catch (IllegalArgumentException iae) {
				// Just leave it as invalid.
			}
		}
	}

	/**
	 * Gets a nice display name for a term.
	 * @return User readable name for term or <code>null</code> if it's not a good term.
	 */
	public String getName() {
		if (isValid()) {
			return String.format("%s %d/%d", term.title(), 2000+year, year+1);
		}
		return null;
	}

	public boolean isValid() {
		return term != null;
	}

	@Override
	public int compareTo(TermCode other) {
		if (! (this.isValid() && other.isValid()) ) {
			int ret = 0;
			ret -= (this.isValid()?1:0);
			ret += (other.isValid()?1:0);
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
