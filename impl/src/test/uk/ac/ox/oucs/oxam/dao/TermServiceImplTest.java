package uk.ac.ox.oucs.oxam.dao;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.ox.oucs.oxam.logic.TermServiceImpl;
import uk.ac.ox.oucs.oxam.model.Term;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TermServiceImplTest {
	
	@Autowired
	private TermServiceImpl termService;

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
