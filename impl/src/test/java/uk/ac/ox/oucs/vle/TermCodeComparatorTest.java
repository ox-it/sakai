package uk.ac.ox.oucs.vle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthew Buckett
 */
public class TermCodeComparatorTest {

	private TermCodeComparator comp = new TermCodeComparator();

	@Test
	public void testDiffYear() {
		assertTransative("HT13", "HT12");
	}

	@Test
	public void testDiffTerm() {
		assertTransative("TT13", "MT13");
	}

	@Test
	public void testDiff() {
		assertTransative("MT13", "TT11");
	}

	@Test
	public void testTermOrder() {
		assertTransative("HT10", "MT10");
		assertTransative("TT10", "MT10");
		assertTransative("TT10", "HT10");
	}

	@Test
	public void testNullTerm() {
		assertTransative(null, "HT10");
	}

	@Test
	public void testBothNull() {
		assertMatch(null, null);
	}

	@Test
	public void testBadTerm() {
		assertTransative("II10", "HT10");
	}

	@Test
	public void testBadData() {
		assertTransative("WXYZ", "ABCD");
	}

	@Test
	public void testBadDataNull() {
		assertTransative("ABCD", null);
	}

	private void assertTransative(String tc1, String tc2) {
		assertTrue(tc1+ " should be before "+ tc2, comp.compare(tc1, tc2) > 0);
		assertTrue(tc2+ " should be after "+ tc1, comp.compare(tc2, tc1) < 0);
	}

	private void assertMatch(String tc1, String tc2) {
		assertEquals(0, comp.compare(tc1, tc2));
		assertEquals(0, comp.compare(tc2, tc1));
	}
}
