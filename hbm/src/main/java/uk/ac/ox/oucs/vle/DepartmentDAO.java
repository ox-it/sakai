package uk.ac.ox.oucs.vle;

public class DepartmentDAO implements java.io.Serializable {
	
	private String code;
    private String name;
    
    public DepartmentDAO() {
    }
    
    public DepartmentDAO(String code) {
        this.code = code;
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

}
