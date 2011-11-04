package uk.ac.ox.oucs.oxam.readers;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

// Tests the paper resolver which looks on the filesystem.
public class ZipPaperResolverTest {

	private ZipPaperResolver resolver;
	
	@Before
	public void setUp() throws IOException {
		MockTermService termService = new MockTermService();
		String file = getClass().getResource("/papers.zip").getFile();
		resolver = new ZipPaperResolver(file, "papers", termService, "pdf");
	}

	@Test
	public void testGood() {
		PaperResolutionResult good = resolver.getPaper(2009, "H", "7d41");
		
		assertTrue(good.isFound());
		assertNotNull(good.getStream());
	}
	
	@Test
	public void testBad() {
		// Termcode doesn't map.
		PaperResolutionResult bad = resolver.getPaper(2009, "L", "4f52");
		assertNull(bad);
	}
	
	@Test
	public void testMissing() {
		PaperResolutionResult missing = resolver.getPaper(2009, "H", "4f51");
		assertFalse(missing.isFound());
		assertNull(missing.getStream());
	}

}
