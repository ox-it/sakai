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
import org.springframework.test.AbstractSingleSpringContextTests;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.springframework.test.AbstractTransactionalSpringContextTests;

import uk.ac.ox.oucs.vle.proxy.SakaiProxyTest;

public abstract class TestOnSampleData extends AbstractTransactionalSpringContextTests {

	private SessionFactory factory;
	protected CourseSignupService service;
	protected SakaiProxyTest proxy;
	protected CourseDAOImpl dao;

	protected void onSetUpBeforeTransaction() {
		transactionManager.toString();
	}

	protected String[] getConfigPaths() {
		//return new String[]{"/components.xml", "/test-components.xml"};
		return new String[]{"/course-signup-beans.xml", "/test-sakai-beans.xml"};
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