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
