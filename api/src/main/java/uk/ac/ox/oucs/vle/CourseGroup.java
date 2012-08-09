package uk.ac.ox.oucs.vle;

import java.util.List;

public interface CourseGroup {

	//RM Research Methods
	//RDF Skills
	public static enum Category_Type {RDF, JACS, RM};
	
	public String getId();
	
	public String getTitle();
	
	public String getDescription();
	
	public String getDepartment();
	
	public String getDepartmentCode();
	
	public String getSubUnit();
	
	public String getSubUnitCode();
	
	public boolean getPublicView();
	
	public boolean getSupervisorApproval();
	
	public boolean getAdministratorApproval();
	
	public boolean getHideGroup();
	
	public String getContactEmail();
	
	public List<CourseComponent> getComponents();
	
	public List<CourseCategory> getCategories();
	
	public List<CourseCategory> getCategories(Category_Type categoryType);
	
	public List<Person> getAdministrators();
	
	public List<Person> getSuperusers();
	
	public List<String> getOtherDepartments();
	
	public boolean getIsAdmin();
	
	public boolean getIsSuperuser();
}
