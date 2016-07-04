package uk.ac.ox.oucs.vle;

import java.util.List;
import java.util.Map;

/**
 * The external group manager allows for finding external groups of users.
 * Then once an external group of users has been found a mapped group can 
 * be created which assigns a role to that external group of users and creates
 * and ID for the mapped group which can be assigned to a site.
 * 
 * @author buckett
 *
 */
public interface ExternalGroupManager {

	public List<ExternalGroup> search(String query) throws ExternalGroupException;

	public List<ExternalGroup> search(String[] terms) throws ExternalGroupException;

	public List<ExternalGroupNode> findNodes(String path) throws ExternalGroupException;

	public ExternalGroup findExternalGroup(String externalGroupId) throws ExternalGroupException;
	
	public Map<String, String> getGroupRoles(String userId) throws ExternalGroupException;
	
	/**
	 * Find the role for the mapped group.
	 * @param mappedGroupId
	 * @return The role or null if the group couldn't be found.
	 */
	public String findRole(String mappedGroupId);
	
	/**
	 * Add a new mapped group.
	 * @param externalGroupId The external ID of the new group.
	 * @param role The role to assign all participants of the group.
	 * @return The ID of the newly created group, or if it already exists the existing ID.
	 * @throws ExternalGroupException If there was a problem creating the group.
	 */
	public String addMappedGroup(String externalGroupId, String role) throws ExternalGroupException;
	
	/**
	 * Get the external group ID for a mapped group.
	 * @param mappedGroupId The mapped group ID.
	 * @return The external group ID.
	 */
	public String findExternalGroupId(String mappedGroupId);

}
