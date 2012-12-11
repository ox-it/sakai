package uk.ac.ox.oucs.vle;


public class CourseCategoryDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int categoryId;
	private int courseGroupMuid;
	private String categoryCode;
    private String categoryName;
    private String categoryType;
    
    public CourseCategoryDAO() {
    }
    
    public CourseCategoryDAO(CourseGroup.Category_Type type, String code, String name) {
    	this.categoryType = type.name();
    	this.categoryCode = code;
    	this.categoryName = name;
    }
    
    public boolean equals(Object other){  
        if (this == other) return true;  
        if (!(other instanceof CourseCategoryDAO)) return false;  
        final CourseCategoryDAO that = (CourseCategoryDAO) other;  
        if (this.getCategoryType().equals(that.getCategoryType()) && 
        	this.getCategoryName().equals(that.getCategoryName())) {
        	return true;  
        }
        return false;
   }  
     
   public int hashCode(){  
	   int hash = 1;
	   hash = hash * 31 + getCategoryType().hashCode();
	   hash = hash * 31 + (getCategoryName() == null ? 0 : getCategoryName().hashCode());
	    return hash; 
   }  
    
    public int getCategoryId() {
        return this.categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
       }
    	
    public int getCourseGroupMuid() {
        return this.courseGroupMuid;
    }
    
    public void setCourseGroupMuid(int courseGroupMuid) {
        this.courseGroupMuid = courseGroupMuid;
    }
    
    public String getCategoryCode() {
        return this.categoryCode;
    }
    
    public void setCategoryCode(String code) {
        this.categoryCode = code;
    }
    
    public String getCategoryName() {
        return this.categoryName;
    }
    
    public void setCategoryName(String name) {
        this.categoryName = name;
    }
    
    public String getCategoryType() {
        return this.categoryType;
    }
    
    public void setCategoryType(String type) {
        this.categoryType = type;
    }
}
