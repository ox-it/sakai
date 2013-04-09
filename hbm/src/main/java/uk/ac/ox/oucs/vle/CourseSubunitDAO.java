package uk.ac.ox.oucs.vle;

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


public class CourseSubunitDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String subunitCode;
    private String subunitName;
    private String departmentCode;
    
    public CourseSubunitDAO() {
    }
    
    public CourseSubunitDAO(String code) {
        this.subunitCode = code;
    }
    
    public String getSubunitCode() {
        return this.subunitCode;
    }
    
    public void setSubunitCode(String code) {
        this.subunitCode = code;
    }
    
    public String getSubunitName() {
        return this.subunitName;
    }
    
    public void setSubunitName(String name) {
        this.subunitName = name;
    }
    
    public String getDepartmentCode() {
        return this.departmentCode;
    }
    
    public void setDepartmentCode(String name) {
        this.departmentCode = name;
    }
}
