package uk.ac.ox.oucs.vle;

import java.util.List;
import java.util.Map;

public interface ExternalGroupManager {

	public List<ExternalGroup> search(String query) throws ExternalGroupException;
	
	public List<ExternalGroup> search(String[] terms) throws ExternalGroupException;
	
	public ExternalGroup findExternalGroup(String externalGroupId);
	
	public String findExternalGroupId(String mappedGroupId);
	
	/**
	 * Find the role for the mapped group.
	 * @param mappedGroupId
	 * @return The role or null if the group couldn't be found.
	 */
	public String findRole(String mappedGroupId);
	
	public String addMappedGroup(String externalGroupId, String role);
	
	public Map<String, String> getGroupRoles(String userId);
	
	public List<ExternalGroupNode> findNodes(String path) throws ExternalGroupException;
	
}
