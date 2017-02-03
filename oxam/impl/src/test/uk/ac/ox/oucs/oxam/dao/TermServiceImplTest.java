package uk.ac.ox.oucs.oxam.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ox.oucs.oxam.logic.TermServiceImpl;
import uk.ac.ox.oucs.oxam.model.Term;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:uk/ac/ox/oucs/oxam/dao/TermServiceImplTest-context.xml")
public class TermServiceImplTest {

    @Autowired
	private TermServiceImpl termService;

	public void setTermService(TermServiceImpl termService) {
		this.termService = termService;
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
