package uk.ac.ox.oucs.vle;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
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
		CourseGroup cg1 = newGroup("HT13", CG1);
		CourseGroup cg2 = newGroup("HT12", CG2);
		assertSymmetric(cg1, cg2);
	}

	@Test
	public void testSame() {
		CourseGroup cg1 = newGroup("HT11", CG1);
		CourseGroup cg2 = newGroup("HT11", CG1);
		assertMatch(cg1, cg2);
	}

	@Test
	public void testDiffTerm() {
		CourseGroup cg1 = newGroup("HT11", CG1);
		CourseGroup cg2 = newGroup("MT11", CG2);
		assertSymmetric(cg1, cg2);
	}

	@Test
	public void testNull() {
		CourseGroup cg1 = newGroup(null, CG1);
		CourseGroup cg2 = newGroup("HT11", CG2);
		assertSymmetric(cg1, cg2);
	}

	@Test
	public void testBadTerm() {
		CourseGroup cg1 = newGroup("NoTerm 11", CG1);
		CourseGroup cg2 = newGroup("HT11", CG2);
		assertSymmetric(cg1, cg2);
	}

	@Test
	public void testBadYear() {
		CourseGroup cg1 = newGroup("HT11/12", CG1);
		CourseGroup cg2 = newGroup("HT11", CG2);
		assertSymmetric(cg1, cg2);
	}

	@Test
	public void testCollectionSort() {

		CourseGroup cg0 = newGroup(null, "0");
		CourseGroup cg1 = newGroup("TT13", "1");
		CourseGroup cg2 = newGroup("TT12", "2");
		CourseGroup cg3 = newGroup("TT11", "3");
		CourseGroup cg4 = newGroup("HT11", "4");
		CourseGroup cg5 = newGroup("MT11", "5");

		List<CourseGroup> list = Arrays.asList(cg0, cg1, cg2, cg3, cg4, cg5);
		Collections.shuffle(list);
		Collections.sort(list, comp);
		assertArrayEquals(new CourseGroup[]{cg5, cg4, cg3, cg2, cg1, cg0}, list.toArray());
	}

	private void assertSymmetric(CourseGroup cg1, CourseGroup cg2) {
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
	private CourseGroup newGroup(String termcode, String title) {
		CourseGroup cg = mock(CourseGroup.class);
		when(cg.getTitle()).thenReturn(title);
		CourseComponent co = mock(CourseComponent.class);
		when(cg.getComponents()).thenReturn(Collections.singletonList(co));
		when(co.getTermCode()).thenReturn(termcode);
		return cg;
	}


}
