package uk.ac.ox.oucs.vle;


public class CourseCategoryDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String categoryId;
	private String groupId;
	private String categoryCode;
    private String categoryName;
    private String categoryType;
    
    public CourseCategoryDAO() {
    }
    
    public CourseCategoryDAO(String groupId, CourseGroup.Category_Type type, String code, String name) {
    	this.groupId = groupId;
    	this.categoryType = type.name();
    	this.categoryCode = code;
    	this.categoryName = name;
    }
    
    public String getCategoryId() {
        return this.categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
       }
    	
    public String getGroupId() {
        return this.groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
