/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * This loads some sample data into the database.
 *
 * @author buckett
 *
 */
public class SampleDataLoader {
	
	private static final Log log = LogFactory.getLog(SampleDataLoader.class);

	public static Term T2009HILLARY = new Term("2009-HIL", newCalendar(2009, 10, 10));
	public static Term T2010HILLARY = new Term("2010-HIL", newCalendar(2010, 10, 10));
	public static Term T2011HILLARY = new Term("2011-HIL", newCalendar(2011, 10, 10));
	public static Term T2012HILLARY = new Term("2012-HIL", newCalendar(2012, 10, 10));

	protected CourseDAO dao;

	public void setCourseDAO(CourseDAO dao) {
		this.dao = dao;
	}


	public void init() throws Exception {
		log.info("Starting sample data load.");

		// Create the course groups and set administrators.
		CourseGroupDAO course1 = dao.newCourseGroup("course-1", "3C05", "The Politics of Brazil", null);
		course1.setAdministrators(Collections.singleton("admin"));
		course1.setAdministratorApproval(true);
		course1.setSupervisorApproval(true);
		course1.setSource("Test");
		dao.save(course1);
		CourseGroupDAO course2 = dao.newCourseGroup("course-2", "3C05", "The Politics of Mexico", null);
		course2.setAdministrators(Collections.singleton("admin"));
		course2.setSource("Test");
		dao.save(course2);
		CourseGroupDAO course3 = dao.newCourseGroup("course-3", "3C05", "Test of Open", null);
		course3.setAdministrators(new HashSet<String>(Arrays.asList("admin1", "admin2", "admin3")));
		course3.setSource("Test");
		dao.save(course3);
		CourseGroupDAO course4 = dao.newCourseGroup("course-4", "3C05", "In future with signups", null);
		course4.setAdministrators(Collections.singleton("other"));
		course4.setSource("Test");
		dao.save(course4);

		// Create the components.
		CourseComponentDAO comp1 = newComponent("comp-1", "Lecture on Politics of Brazil", T2010HILLARY, 40, "tc-1", course1);
		CourseComponentDAO comp2 = newComponent("comp-2", "Lecture on Politics of Mexico", T2010HILLARY, 15, "tc-2", course2);
		CourseComponentDAO comp3 = newComponent("comp-3", "Seminar on South American Politics", T2010HILLARY, 15, "tc-3", course1, course2);
		CourseComponentDAO comp4 = newComponent("comp-4", "Lecture on Politics of Brazil", T2011HILLARY, 40, "tc-1", course1);
		CourseComponentDAO comp5 = newComponent("comp-5", "Seminar on South American Politics", T2011HILLARY, 45, "tc-3", course1);
		CourseComponentDAO comp6 = newComponent("comp-6", "Lecture on Politics of Brazil", T2009HILLARY, 40, "tc-1", course1);
		CourseComponentDAO comp7 = newComponent("comp-7", "Seminar on South American Politics", T2009HILLARY, 45, "tc-3", course1);
		CourseComponentDAO comp8 = newComponent("comp-8", "Seminar on South American Politics", T2010HILLARY, 5, "tc-3", course1);
		CourseComponentDAO comp9 = newComponent("comp-9", "Component Type", T2012HILLARY, 5, "tc-4", course3);
		CourseComponentDAO comp10 = newComponent("comp-10", "Component with signups", T2012HILLARY, 5, "tc-4", course4);

		// Set the number taken.
		comp6.setTaken(1);
		dao.save(comp6);
		comp7.setTaken(2);
		dao.save(comp7);
		comp8.setTaken(5);
		dao.save(comp8);
		comp9.setTaken(4);
		dao.save(comp9);
		comp10.setTaken(1);
		dao.save(comp10);
		// Create some signups.
		CourseSignupDAO signup1 = dao.newSignup("current", "1", new Date());
		signup1.setStatus(CourseSignupService.Status.ACCEPTED);
		signup1.setGroup(course1);
		dao.save(signup1);
		comp6.getSignups().add(signup1);
		dao.save(comp6);
		comp7.getSignups().add(signup1);
		dao.save(comp7);

		CourseSignupDAO signup2 = dao.newSignup("current", "1", new Date());
		signup2.setStatus(CourseSignupService.Status.ACCEPTED);
		signup2.setGroup(course1);
		dao.save(signup2);
		comp7.getSignups().add(signup2);
		dao.save(comp7);

		CourseSignupDAO signup3 = dao.newSignup("current", "1", new Date());
		signup3.setStatus(CourseSignupService.Status.ACCEPTED);
		signup3.setGroup(course4);
		dao.save(signup3);
		comp10.getSignups().add(signup3);
		dao.save(comp10);

		log.info("Finished sample data load.");
	}

	public static class Term {
		String code;
		Date opens;
		Date closes;
		Date starts;

		public Term (String code, Calendar starts) {
			this(code, addWeeks(starts, -3), addWeeks(starts, -1), starts.getTime());
		}

		public Term(String code, Date opens, Date closes, Date starts) {
			this.code = code;
			this.opens = opens;
			this.closes = closes;
			this.starts = starts;
		}
	}

	public static Date addWeeks(Calendar cal, int i) {
		Calendar opens = (Calendar) cal.clone();
		opens.add(Calendar.WEEK_OF_YEAR, i);
		return opens.getTime();
	}


	public static Calendar newCalendar(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return cal;
	}

	private CourseComponentDAO newComponent(String id, String title, Term term, int size, String componentId, CourseGroupDAO... daos) {
		CourseComponentDAO comp = dao.newCourseComponent(id);
		comp.setBookable(true);
		comp.setTitle(title);
		comp.setTermcode(term.code);
		comp.setOpens(term.opens);
		comp.setCloses(term.closes);
		// We know when teaching starts so that's when we consider this old.
		comp.setBaseDate(term.starts);
		comp.setSize(size);
		comp.setTaken(0);
		comp.setComponentId(componentId);
		comp.setSource("Test");
		for (CourseGroupDAO dao: daos) {
			comp.getGroups().add(dao);
		}
		dao.save(comp);
		return comp;
	}
}
