package uk.ac.ox.oucs.vle;

import java.util.List;

public interface ExternalGroupManager {

	public List<ExternalGroup> search(String query);
	
	public ExternalGroup findExternalGroup(String externalGroupId);
	
	public String findExternalGroupId(String mappedGroupId);
	
	public String addMappedGroup(String externalGroupId, String role);
	
}
