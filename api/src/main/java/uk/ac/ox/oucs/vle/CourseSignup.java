/*
 * #%L
 * Course Signup API
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
import java.util.Set;

import uk.ac.ox.oucs.vle.CourseSignupService.Status;

public interface CourseSignup extends java.io.Serializable{

	/**
	 * 
	 * @return String unique identifier for signup 
	 */
	public String getId();
	
	/**
	 * 
	 * @return Identity of student making the signup
	 */
	public Person getUser();
	
	/**
	 * 
	 * @return Identity of students supervisor if entered
	 */
	public Person getSupervisor();
	
	/**
	 * 
	 * @return Text supporting signup
	 */
	public String getNotes();
	
	/**
	 * 
	 * @return Status of signup
	 * values may be one of PENDING, WITHDRAWN, APPROVED, ACCEPTED, CONFIRMED, REJECTED, WAITING;
	 */
	public Status getStatus();
	
	/**
	 * 
	 * @return Date the signup was made
	 */
	public Date getCreated();
	
	/**
	 * 
	 * @return Prac code of the students department
	 */
	public String getDepartment();
	
	/**
	 * 
	 * @return Set of course components signed up to
	 */
	public Set<CourseComponent> getComponents();
	
	/**
	 * 
	 * @return course (assessment unit) signed up to 
	 */
	public CourseGroup getGroup();

}
