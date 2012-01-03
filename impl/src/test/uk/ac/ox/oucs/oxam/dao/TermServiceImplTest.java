package uk.ac.ox.oucs.oxam.dao;

import org.junit.Test;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import uk.ac.ox.oucs.oxam.logic.TermServiceImpl;
import uk.ac.ox.oucs.oxam.model.Term;

public class TermServiceImplTest extends AbstractDependencyInjectionSpringContextTests {
	
	private TermServiceImpl termService;

	public void setTermService(TermServiceImpl termService) {
		this.termService = termService;
	}

	protected String[] getConfigLocations() {
		return new String[] { "classpath:uk/ac/ox/oucs/oxam/dao/TermServiceImplTest-context.xml" };
	}

	@Test
	public void testLoaded() {
		// Simple test which just checks the data got loaded.
		assertEquals("Trinity", termService.getByCode("T").getName());
		assertNull(termService.getByCode("P"));
		assertEquals(5, termService.getAll().size());
		// Check first
		assertEquals("M", termService.getAll().iterator().next().getCode());
		// Check last
		assertEquals("L", termService.getAll().toArray(new Term[]{})[4].getCode());
	}
	
}
