package uk.ac.ox.oucs.vle;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModuleImplTest {

	private ModuleImpl moduleImpl;
	private CourseComponentDAO dao;

	@Before
	public void setUp() {
		moduleImpl = new ModuleImpl();
		dao = new CourseComponentDAO();

		// Essentials
		dao.setTitle("*TITLE*");
		dao.setStartsText("*STARTS*");
	}

	@Test
	public void testFormatComponent() {
		assertEquals(
				"*TITLE* starts on *STARTS*",
				moduleImpl.formatComponent(dao)
		);
	}

	@Test
	public void testFormatComponentInvalidTerm() {
		// Add invalid term
		dao.setTermcode("INVALID");
		assertEquals(
				"*TITLE* starts on *STARTS*",
				moduleImpl.formatComponent(dao)
		);
	}

	@Test
	public void testFormatComponentValid() {
		dao.setTermcode("TT10");
		assertEquals(
				"*TITLE* starts on *STARTS* Trinity 2010/11",
				moduleImpl.formatComponent(dao)
		);
	}

	@Test
	public void testFormatComponentSessions() {
		dao.setSessions("10");
		assertEquals(
				"*TITLE* for 10 sessions starts on *STARTS*",
				moduleImpl.formatComponent(dao)
		);
	}

	@Test
	public void testFormatComponentTeacher() {
		dao.setTeacherName("*TEACHER*");
		assertEquals(
				"*TITLE* starts on *STARTS* with *TEACHER*",
				moduleImpl.formatComponent(dao)
		);
	}
}
