package uk.ac.ox.oucs.oxam.readers;

import java.io.InputStream;
import java.util.Arrays;

public class DualPaperResolver implements PaperResolver {

	private PaperResolver one;
	private PaperResolver two;
	
	public DualPaperResolver(PaperResolver one, PaperResolver two) {
		this.one = one;
		this.two = two;
	}

	public PaperResolutionResult getPaper(int year, String termCode, String paperCode) {
		PaperResolutionResult resultOne = one.getPaper(year, termCode, paperCode);
		if (!resultOne.isFound()) {
			PaperResolutionResult resultTwo = two.getPaper(year, termCode, paperCode);
			if (!resultTwo.isFound()) {
				// Only if both are not found do we need todo this.
				return new DualPaperResolutionResult(resultOne, resultTwo);
			}
			return resultTwo;
		}
		return resultOne;
	}
	
	/**
	 * Holds all the paths when we fail to find anything.
	 * @author buckett
	 */
	public class DualPaperResolutionResult implements PaperResolutionResult {
		
		private String[] allPaths;

		public DualPaperResolutionResult(PaperResolutionResult one, PaperResolutionResult two) {
			String[] onePaths = one.getPaths();
			String[] twoPaths = two.getPaths();
			allPaths = Arrays.copyOf(onePaths, twoPaths.length+onePaths.length); // Grow an array
			System.arraycopy(twoPaths, 0, allPaths, onePaths.length, twoPaths.length); // put the extra data in.
		}

		public boolean isFound() {
			return false;
		}

		public InputStream getStream() {
			return null;
		}

		public String[] getPaths() {
			return allPaths;
		}
		
	}

}
