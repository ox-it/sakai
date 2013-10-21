package uk.ac.ox.oucs.vle;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Matthew Buckett
 */
public class NoDateComparatorTest {

	public static final String CG1 = "CG1";
	public static final String CG2 = "CG2";
	private NoDateComparator comp = new NoDateComparator();

	@Test
	public void testDiffYear() {
		CourseGroup cg1 = newGroup("Hilary 2013", CG1);
		CourseGroup cg2 = newGroup("Hilary 2012", CG2);
		assertTransative(cg1, cg2);
	}

	@Test
	public void testSame() {
		CourseGroup cg1 = newGroup("Hilary 2011", CG1);
		CourseGroup cg2 = newGroup("Hilary 2011", CG1);
		assertMatch(cg1, cg2);
	}

	@Test
	public void testDiffTerm() {
		CourseGroup cg1 = newGroup("Hilary 2011", CG1);
		CourseGroup cg2 = newGroup("Michaelmas 2011", CG2);
		assertTransative(cg1, cg2);
	}

	@Test
	public void testNull() {
		CourseGroup cg1 = newGroup("Hilary 2011", CG1);
		CourseGroup cg2 = newGroup(null, CG2);
		assertTransative(cg1, cg2);
	}

	@Test
	public void testBadTerm() {
		CourseGroup cg1 = newGroup("Hilary 2011", CG1);
		CourseGroup cg2 = newGroup("NoTerm 2011", CG2);
		assertTransative(cg1, cg2);
	}

	@Test
	public void testBadYear() {
		CourseGroup cg1 = newGroup("Hilary 2011", CG1);
		CourseGroup cg2 = newGroup("Hilary 2011/12", CG2);
		assertTransative(cg1, cg2);
	}

	private void assertTransative(CourseGroup cg1, CourseGroup cg2) {
		assertTrue(cg1+ " should be before "+ cg2, comp.compare(cg1, cg2) > 0);
		assertTrue(cg2+ " should be after "+ cg1, comp.compare(cg2, cg1) < 0);
	}

	private void assertMatch(CourseGroup cg1, CourseGroup cg2) {
		assertEquals(0, comp.compare(cg1, cg2));
		assertEquals(0, comp.compare(cg2, cg1));
	}

	/**
	 * Create a course group with a single component.
	 */
	private CourseGroup newGroup(String when, String title) {
		CourseGroup cg = mock(CourseGroup.class);
		when(cg.getTitle()).thenReturn(title);
		CourseComponent co = mock(CourseComponent.class);
		when(cg.getComponents()).thenReturn(Collections.singletonList(co));
		when(co.getWhen()).thenReturn(when);
		return cg;
	}


}
