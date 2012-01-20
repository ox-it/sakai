package uk.ac.ox.oucs.vle;

import java.util.HashSet;
import java.util.Set;

public class CourseUserPlacementDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userId;
    private String placementId;
    
    public CourseUserPlacementDAO() {
    }
    
    public CourseUserPlacementDAO(String userId) {
    	this.userId = userId;
    }
    
    public String getUserId() {
        return this.userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPlacementId() {
        return this.placementId;
    }
    
    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }
}
