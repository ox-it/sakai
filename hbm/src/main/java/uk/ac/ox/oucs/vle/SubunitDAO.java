package uk.ac.ox.oucs.vle;


public class SubunitDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String subunitCode;
    private String subunitName;
    private String departmentCode;
    
    public SubunitDAO() {
    }
    
    public SubunitDAO(String code) {
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
