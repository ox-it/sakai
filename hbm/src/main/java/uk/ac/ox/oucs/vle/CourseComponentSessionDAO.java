package uk.ac.ox.oucs.vle;

import java.util.Date;


public class CourseComponentSessionDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String courseComponentId;
	private String sessionId;
	private Date sessionStart;
	private String sessionStartText;
	private Date sessionEnd;
	private String sessionEndText;
	
    
    public CourseComponentSessionDAO() {
    }
    
    public CourseComponentSessionDAO(String courseComponentId, String sessionId, Date sessionStart, String sessionStartText, Date sessionEnd, String sessionEndText) {
    	this.sessionId = sessionId;
    	this.courseComponentId = courseComponentId;
    	this.sessionStart = sessionStart;
    	this.sessionStartText = sessionStartText;
    	this.sessionEnd = sessionEnd;
    	this.sessionEndText = sessionEndText;
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
    
    public String getCourseComponentId() {
        return this.courseComponentId;
    }
    
    public void setCourseComponentId(String courseComponentId) {
        this.courseComponentId = courseComponentId;
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
    
}
