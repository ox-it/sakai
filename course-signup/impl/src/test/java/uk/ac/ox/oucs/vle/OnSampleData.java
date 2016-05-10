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

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.ox.oucs.vle.proxy.SakaiProxyTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/sample-data.xml", "/course-signup-beans.xml", "/test-with-h2.xml"})
public abstract class OnSampleData extends Assert {

	@Autowired
	protected SessionFactory factory;
	@Qualifier("uk.ac.ox.oucs.vle.CourseSignupService")
	@Autowired
	protected CourseSignupService service;
	@Autowired
	protected SakaiProxyTest proxy;
	@Autowired
	protected CourseDAOImpl dao;
	@Autowired
	protected SettableNowService now;


	// pass through to the junit 3 calls, which are not annotated
	@Before
	final public void callSetup() throws Exception {
		now.setNow(SampleDataLoader.addWeeks(SampleDataLoader.newCalendar(2010, 10, 10), -2));
	}

	@After
	public void callTearDown() throws Exception {
	}

	public void setFactory(SessionFactory factory) {
		this.factory = factory;
	}
	
	public SessionFactory getFactory() {
		return this.factory;
	}

	public CourseSignupService getService() {
		return service;
	}

	public void setService(CourseSignupService service) {
		this.service = service;
	}

	public void setNowService(SettableNowService now) {
		this.now = now;
	}

	public SakaiProxyTest getProxy() {
		return proxy;
	}

	public void setProxy(SakaiProxyTest proxy) {
		this.proxy = proxy;
	}

	public void setDao(CourseDAOImpl dao) {
		this.dao = dao;
	}
	



}
