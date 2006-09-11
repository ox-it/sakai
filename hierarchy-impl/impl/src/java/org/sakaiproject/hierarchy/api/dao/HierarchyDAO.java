package org.sakaiproject.hierarchy.api.dao;

import java.util.List;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;



//import java.io.Serializable;

public interface HierarchyDAO {



	/**
	 * The hierarchy node is saved, depending on its Id, if the Id is null, then the id is assigned
	 * and a new item is saved, otherwise the item is update by id.
	 */
	public void saveOrUpdate(Hierarchy hierarchy);
	/**
	 * The hierarchy property node is saved, depending on its Id, if the Id is null, then the id is assigned
	 * and a new item is saved, otherwise the item is update by id.
	 */

	public void saveOrUpdate(HierarchyProperty hierarchy);


	/**
	 * Find and loac hte hierachy using the SHA1 node ID as the key
	 * @param nodeId
	 * @return
	 */
	Hierarchy findHierarchyByPathHash(String pathHash);
	
	
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



}
