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
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.internal.runners.TestClassRunner;
import org.junit.runner.RunWith;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import uk.ac.ox.oucs.vle.proxy.SakaiProxyTest;

import java.util.*;

@RunWith(TestClassRunner.class)
public abstract class OnSampleData extends AbstractTransactionalSpringContextTests {

	private static final Log log = LogFactory.getLog(OnSampleData.class);

	protected SessionFactory factory;
	protected CourseSignupService service;
	protected SakaiProxyTest proxy;
	protected CourseDAOImpl dao;
	protected SettableNowService now;


	// pass through to the junit 3 calls, which are not annotated
	@Before
	final public void callSetup() throws Exception {
		super.setUp();
		now.setNow(SampleDataLoader.addWeeks(SampleDataLoader.newCalendar(2010, 10, 10), -2));
	}

	@After
	public void callTearDown() throws Exception {
		super.tearDown();
	}

	protected String[] getConfigPaths() {
		return new String[]{"/sample-data.xml", "/course-signup-beans.xml", "/test-with-h2.xml"};
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
