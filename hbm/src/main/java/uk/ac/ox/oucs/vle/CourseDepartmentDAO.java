/*
 * #%L
 * Course Signup Hibernate
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

import java.util.HashSet;
import java.util.Set;

public class CourseDepartmentDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String code;
    private String name;
    private boolean approve;
    private Set<String> approvers  = new HashSet<String>(0);
    
    public CourseDepartmentDAO() {
    }
    
    public CourseDepartmentDAO(String code) {
    	this.code = code;
        this.approve = true;
    }
    
    public String getCode() {
        return this.code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean getApprove() {
        return this.approve;
    }
    
    public void setApprove(boolean approve) {
        this.approve = approve;
    }
    
    public Set<String> getApprovers() {
        return this.approvers;
    }
    
    public void setApprovers(Set<String> approvers) {
        this.approvers = approvers;
    }

}
