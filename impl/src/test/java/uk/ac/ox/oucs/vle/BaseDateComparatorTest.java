package uk.ac.ox.oucs.vle;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Check the comparator works correctly.
 * @author Matthew Buckett
 */
public class BaseDateComparatorTest {

	private BaseDateComparator comp;
	private Date now;

	@Before
	public void setUp() {
		comp = new BaseDateComparator();
		now = new Date();
	}

	@Test
	public void testSameDates() {
		CourseComponent o1 = mockComponent(now, null);
		CourseComponent o2 = mockComponent(now, null);
		assertEquals(0, comp.compare(o1,o2));
	}

	@Test
	public void testDifferent() {
		CourseComponent current = mockComponent(now, null);
		CourseComponent before = mockComponent(new Date(now.getTime() - 1000), null);
		assertTrue(comp.compare(current, before) > 0);
		assertTrue(comp.compare(before, current) < 0);
	}

	@Test
	public void testNoDates() {
		CourseComponent hasText = mockComponent(null, "Sometime");
		CourseComponent noText = mockComponent(null, null);
		// No text should be before one with text.
		assertTrue(comp.compare(noText, hasText) < 0);
	}

	public CourseComponent mockComponent(Date baseDate, String startText) {
		CourseComponent component = mock(CourseComponent.class);
		when(component.getBaseDate()).thenReturn(baseDate);
		when(component.getStartsText()).thenReturn(startText);
		return component;
	}
}
