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

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class PopulatorTests extends AbstractTransactionalSpringContextTests {

	private CourseDAOImpl courseDao;
	private SessionFactory sessionFactory;
	private PopulatorWrapper populator;
	
	private String prefix = new String("test.populator");

	public void onSetUp() throws Exception {
		super.onSetUp();
		courseDao = (CourseDAOImpl) getApplicationContext().getBean("uk.ac.ox.oucs.vle.CourseDAO");
		sessionFactory = (SessionFactory) getApplicationContext().getBean("org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory");
		populator = (PopulatorWrapper) getApplicationContext().getBean("uk.ac.ox.oucs.vle.OxcapPopulatorWrapper");
	}
	
	public void onTearDown() throws Exception {
		super.onTearDown();
	} 


	protected String[] getConfigPaths() {
		//return new String[]{"/components.xml", "/test-components.xml"};
//		return new String[]{"/course-signup-beans.xml", "/sakai-beans.xml"};
		return new String[]{"/course-dao.xml", "/test-with-h2.xml", "/course-signup-beans.xml"};
	}
	
	public void testPopulator() {
		
//		Map<String, String> contextMap = new HashMap<String, String>();
//		//contextMap.put(prefix+".uri", "file:///home/marc/oxford-sakai-2.8/extras/course-signup/impl/xcri.xml");
//		contextMap.put(prefix+".uri", "https://course.data.ox.ac.uk/catalogues/?uri=https%3A//course.data.ox.ac.uk/id/careers/catalogue&format=xcricap-full");
//		contextMap.put(prefix+".username", "");
//		contextMap.put(prefix+".password", "");
//		contextMap.put(prefix+".name", "test");
//
//		PopulatorContext pContext = new PopulatorContext(prefix, contextMap);
//		populator.update(pContext);
//
//		CourseGroupDAO group = courseDao.findCourseGroupById("assessment-centre-practice");
//		assertNotNull(group);
	}
	
}