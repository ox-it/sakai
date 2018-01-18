package uk.ac.ox.oucs.vle;


import static org.junit.Assert.*;
import org.junit.Test;

public class XcriOxCapPopulatorImplTest {

	@Test
	public void testParse() {
		String original = "http://www.lsidtc.ox.ac.uk/the-course/core-modules\r\n\r\nFormal Assessment:";
		String output = XcriOxCapPopulator.parse(original);
		assertTrue(output.contains("href=\"http://www.lsidtc.ox.ac.uk/the-course/core-modules\""));
	}
}
