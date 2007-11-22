package org.sakaiproject.hierarchy.api.dao;

import java.util.List;

import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;



//import java.io.Serializable;

public interface HierarchyDAO {



	/**
	 * The hierarchy node and all its children 
	 * are saved. Depending on its ID, if the ID is null, then the ID is assigned
	 * and a new item is saved, otherwise the item is update by ID.
	 * @throws HierarchyServiceException If saving failed. This maybe because object have been updated
	 * or due to the object being in an inconsistent state.
	 */
	public void saveOrUpdate(Hierarchy hierarchy) throws HierarchyServiceException;
	/**
	 * The hierarchy property node is saved, depending on its Id, if the Id is null, then the id is assigned
	 * and a new item is saved, otherwise the item is update by id.
	 * @throws HierarchyServiceException 
	 */

	public void saveOrUpdate(HierarchyProperty hierarchy) throws HierarchyServiceException;


	/**
	 * Find and locate the hierachy using the SHA1 node ID as the key
	 * @param pathHash The hierarchy to find. 
	 * @return The found hierarchy or <code>null</code> if the path couldn't be found. 
	 */
	public Hierarchy findHierarchyByPathHash(String pathHash);
	
	
	public Hierarchy findHierarchyById(String nodeId);


	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param hierarchy the instance to be removed
	 */
	public void delete(Hierarchy hierachy);
	public void delete(HierarchyProperty hierarchy);
	
	

	/**
	 * Locate root nodes in the datasore, these are nodes that dont have parents.
	 * @return
	 */
	public List findHierarchyRoots();

	/**
	 * Using a hierarchy node find its properties.
	 * @param owner
	 * @return
	 */
	public List findHierarchyProperties(Hierarchy owner);

	/**
	 * Find nodes with the same parent as supplied. Effectively this is a list of children of the 
	 * parent node.
	 * @param parent
	 * @return
	 */
	public List findHierarchyByParent(Hierarchy parent);
	
	public List findHierarchyByProperty(String name, String value);
	
	/**
	 * Begin a hierarchy transaction, a corresponding end() MUST be called.
	 * begin can be called multiple times, but there must be a matching numer of ends.
	 *
	 */
	public void begin();

	/**
	 * End a hierarchy transaction. Must be called after a begin
	 *
	 */
	public void end();
	/**
	 * abort a hierarchy transaction
	 *
	 */
	public void abort();


}
