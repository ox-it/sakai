package uk.ac.ox.oucs.vle;

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

import java.util.Collection;

public class DepartmentImpl implements Department {
	
	private String pracCode;
	private String oucsCode;
	private String name;
	private boolean approve;
	private Collection<String> approvers;
	
	public DepartmentImpl(String pracCode, String oucsCode, String name) {
		this.pracCode = pracCode;
		this.oucsCode = oucsCode;
		this.name = name;
	}
	
	public DepartmentImpl(CourseDepartmentDAO dao) {
		this.pracCode = dao.getCode();
		this.name = dao.getName();
		this.approve = dao.getApprove();
		this.approvers = dao.getApprovers();
	}

	public String getPracCode() {
		return pracCode;
	}
	
	public String getOucsCode() {
		return oucsCode;
	}

	public String getName() {
		return name;
	}
	
	public boolean getApprove() {
		return approve;
	}
	
	public Collection<String> getApprovers() {
		return approvers;
	}

}
