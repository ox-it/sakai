package uk.ac.ox.oucs.oxam.readers;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

// Tests the paper resolver which looks on the filesystem.
public class FilesystemPaperResolverTest {

	private FilesystemPaperResolver resolver;
	
	@Before
	public void setUp() {
		// Work out 
		String dir = getClass().getResource("/papers").getFile();
		MockTermService termService = new MockTermService();
		resolver = new FilesystemPaperResolver(dir, termService, "pdf");
	}

	@Test
	public void testGood() throws IOException {
		PaperResolutionResult good = resolver.getPaper(2009, "H", "4f52");
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
	public void testMissing() throws IOException {
		PaperResolutionResult missing = resolver.getPaper(2009, "H", "4f51");
		assertFalse(missing.isFound());
		assertNull(missing.getStream());
	}

}
