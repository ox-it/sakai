package uk.ac.ox.oucs.vle;

import java.util.List;

public interface MappedGroupDao {

	MappedGroup findById(String id);
	
	List<MappedGroup> findByGroup(String group);
	
	MappedGroup findByGroupRole(String group, String role);
	
	String newMappedGroup(String group, String role);
	
}
