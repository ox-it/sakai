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

public class CourseComponentSessionImpl implements CourseComponentSession {
	
	private CourseComponentSessionDAO dao;

	public CourseComponentSessionImpl(CourseComponentSessionDAO dao) {
		this.dao = dao;
	}
	
	@Override
	public int compareTo(Object arg0) {
		CourseComponentSession cs = (CourseComponentSession)arg0;
		if (null != this.getSessionStart() && null != cs.getSessionStart()) {
			return this.getSessionStart().compareTo(cs.getSessionStart());
		}
		return this.getSessionId().compareTo(cs.getSessionId());
	}

	public int getCourceComponentMuid() {
		return dao.getCourseComponentMuid();
	}
	
	public String getSessionId() {
		return dao.getSessionId();
	}

	public Date getSessionStart() {
		return dao.getSessionStart();
	}

	public String getSessionStartText() {
		return dao.getSessionStartText();
	}

	public Date getSessionEnd() {
		return dao.getSessionEnd();
	}

	public String getSessionEndText() {
		return dao.getSessionEndText();
	}

}
