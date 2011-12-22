package uk.ac.ox.oucs.oxam.utils;

public class Utils {

	/**
	 * This joins a set of string together with a seperator, but only if they need it.
	 * @param seperator
	 * @param paths
	 * @return
	 */
	public static String joinPaths(String seperator, String... paths) {
		StringBuilder out = new StringBuilder();
		boolean previousEndedWith = paths.length>0 && !paths[0].startsWith(seperator);
		for (String path : paths) {
			if (path.startsWith(seperator)) {
				if (previousEndedWith) {
					out.append(path, seperator.length(), path.length());
				} else {
					out.append(path);
				}
			} else {
				if (!previousEndedWith) {
					out.append(seperator);
				}
				out.append(path);
			}
			previousEndedWith = path.endsWith(seperator);
		}
		return out.toString();
	}
	
	/**
	 * As StringUtils in common-lang 2.4 doesn't have this.
	 * StringUtils in 2.5 does, so then we can switch to that.
	 * 
	 */
	public static String repeat(String element, String join, int count) {
		StringBuilder out = new StringBuilder();
		int i = 0;
		if (i < count) {
			out.append(element);
			for (i = 1;i < count; i++) {
				out.append(join);
				out.append(element);
			}
		}
		return out.toString();
	}

}
