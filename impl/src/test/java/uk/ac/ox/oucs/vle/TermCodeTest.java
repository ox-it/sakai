package uk.ac.ox.oucs.vle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @see NoDateComparator For other tests.
 */
public class TermCodeTest {

	@Test
	public void testGetName() {
		TermCode termCode = new TermCode("TT10");
		assertEquals("Trinity 2010", termCode.getName());
	}

	@Test
	public void testTermOrder() {
		// These are academic years.
		TermCode ht10 = new TermCode("HT10");
		TermCode tt10 = new TermCode("TT10");
		TermCode mt10 = new TermCode("MT10");
		// ht before mt
		symmetricCompare(ht10, mt10);
		// tt berfore mt
		symmetricCompare(tt10, mt10);
		// ht before tt
		symmetricCompare(ht10, tt10);
	}

	@Test
	public void testYearOrder() {
		TermCode ht10 = new TermCode("HT10");
		TermCode ht11 = new TermCode("HT11");
		TermCode mt12 = new TermCode("MT12");

		symmetricCompare(ht10, ht11);
		symmetricCompare(ht10, mt12);
		symmetricCompare(ht11, mt12);
	}

	private void symmetricCompare(TermCode first, TermCode second) {
		assertTrue(first.compareTo(second) < 0);
		assertTrue(second.compareTo(first) > 0);
	}

}
