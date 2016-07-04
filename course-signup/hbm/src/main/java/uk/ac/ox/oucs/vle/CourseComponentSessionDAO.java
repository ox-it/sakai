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

import java.util.Date;


public class CourseComponentSessionDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int courseComponentMuid;
	private String sessionId;
	private Date sessionStart;
	private String sessionStartText;
	private Date sessionEnd;
	private String sessionEndText;
	private String location;
    
    public CourseComponentSessionDAO() {
    }
    
    public CourseComponentSessionDAO(String sessionId, Date sessionStart, String sessionStartText, Date sessionEnd,
                                     String sessionEndText, String location) {
    	this.sessionId = sessionId;
    	this.sessionStart = sessionStart;
    	this.sessionStartText = sessionStartText;
    	this.sessionEnd = sessionEnd;
    	this.sessionEndText = sessionEndText;
        this.location = location;
    }
    
    public boolean equals(Object other){  
        if (this == other) return true;  
        if (!(other instanceof CourseComponentSessionDAO)) return false;  
        final CourseComponentSessionDAO that = (CourseComponentSessionDAO) other;  
        if (this.getSessionId().equals(that.getSessionId())) {
        	return true;  
        }
        return false;
    }  
     
    public int hashCode(){  
	   int hash = 1;
	   hash = hash * 31 + getSessionId().hashCode();
	   return hash; 
    } 
    
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
       }
    
    public String getSessionId() {
        return this.sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
       }
    
    public int getCourseComponentMuid() {
        return this.courseComponentMuid;
    }
    
    public void setCourseComponentMuid(int courseComponentMuid) {
        this.courseComponentMuid = courseComponentMuid;
       }
    	
    public Date getSessionStart() {
        return this.sessionStart;
    }
    
    public void setSessionStart(Date sessionStart) {
        this.sessionStart = sessionStart;
    }
    
    
    public String getSessionStartText() {
        return this.sessionStartText;
    }
    
    public void setSessionStartText(String sessionStartText) {
        this.sessionStartText = sessionStartText;
    }
    
    public Date getSessionEnd() {
        return this.sessionEnd;
    }
    
    public void setSessionEnd(Date sessionEnd) {
        this.sessionEnd = sessionEnd;
    }
    
    
    public String getSessionEndText() {
        return this.sessionEndText;
    }
    
    public void setSessionEndText(String sessionEndText) {
        this.sessionEndText = sessionEndText;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
