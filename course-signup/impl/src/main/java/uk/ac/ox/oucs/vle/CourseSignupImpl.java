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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ox.oucs.vle.CourseSignupService.Status;

public class CourseSignupImpl implements CourseSignup {

	private CourseSignupDAO dao;
	private CourseSignupServiceImpl service;
	
	public CourseSignupImpl(CourseSignupDAO dao, CourseSignupServiceImpl service) {
		this.dao = dao;
		this.service = service;
	}
	
	public String getId() {
		return dao.getId();
	}

	public Person getUser() {
		UserProxy user = service.loadUser(dao.getUserId());
		Person person = null;
		if (user != null) {
			person = new UserProxyPersonImpl(user, service);
		}
		return person;
	}

	public Person getSupervisor() {
		String supervisorId = dao.getSupervisorId();
		if (supervisorId == null) {
			return null;
		}
		UserProxy user = service.loadUser(dao.getSupervisorId());
		Person person = null;
		if (user != null) {
			person = new UserProxyPersonImpl(user, service);
		}
		return person;
	}

	public String getNotes() {
		return dao.getMessage();
	}

	public Status getStatus() {
		return dao.getStatus();
	}

	public Set<CourseComponent> getComponents() {
		Set<CourseComponentDAO> componentDaos = dao.getComponents();
		Set<CourseComponent> components = new HashSet<CourseComponent>(componentDaos.size());
		for(CourseComponentDAO componentDao: componentDaos) {
			components.add(new CourseComponentImpl(componentDao));
		}
		return components;
	}

	public CourseGroup getGroup() {
		return new CourseGroupImpl(dao.getGroup(), service);
	}

	public Date getCreated() {
		return dao.getCreated();
	}
	
	public String getDepartment() {
		return dao.getDepartment();
	}

}
