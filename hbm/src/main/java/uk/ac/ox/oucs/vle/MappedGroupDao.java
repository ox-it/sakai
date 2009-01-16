package uk.ac.ox.oucs.vle;

public interface MappedGroupDao {

	MappedGroup findById(String id);
	
	MappedGroup findByGroupRole(String group, String role);
	
	String newMappedGroup(String group, String role);
	
}
