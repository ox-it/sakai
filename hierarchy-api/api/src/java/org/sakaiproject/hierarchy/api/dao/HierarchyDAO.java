package org.sakaiproject.hierarchy.api.dao;

import java.util.List;

import org.sakaiproject.hierarchy.model.Hierarchy;


//import java.io.Serializable;

public interface HierarchyDAO {
	public org.sakaiproject.hierarchy.model.Hierarchy get(java.lang.String key) throws org.springframework.dao.DataAccessException;

	public org.sakaiproject.hierarchy.model.Hierarchy load(java.lang.String key) throws org.springframework.dao.DataAccessException;

	public java.util.List findAll ();


	/**
	 * Persist the given transient instance, first assigning a generated identifier. (Or using the current value
	 * of the identifier property if the assigned generator is used.) 
	 * @param hierarchy a transient instance of a persistent class 
	 * @return the class identifier
	 */
	public java.lang.String save(org.sakaiproject.hierarchy.model.Hierarchy hierarchy) throws org.springframework.dao.DataAccessException;

	/**
	 * Either save() or update() the given instance, depending upon the value of its identifier property. By default
	 * the instance is always saved. This behaviour may be adjusted by specifying an unsaved-value attribute of the
	 * identifier property mapping. 
	 * @param hierarchy a transient instance containing new or updated state 
	 */
	public void saveOrUpdate(org.sakaiproject.hierarchy.model.Hierarchy hierarchy) throws org.springframework.dao.DataAccessException;

	/**
	 * Update the persistent state associated with the given identifier. An exception is thrown if there is a persistent
	 * instance with the same identifier in the current session.
	 * @param hierarchy a transient instance containing updated state
	 */
	public void update(org.sakaiproject.hierarchy.model.Hierarchy hierarchy) throws org.springframework.dao.DataAccessException;

	/**
	 * A list of Hierachy object by the parent Id
	 * @param parentNodeId
	 * @return
	 */
	public List findHierarchyByParentNodeId(String parentNodeId);

	/**
	 * The node
	 * @param nodeId
	 * @return
	 */
	public org.sakaiproject.hierarchy.model.Hierarchy findHierarchyByNodeId(String nodeId);


	/**
	 * Remove a persistent instance from the datastore. The argument may be an instance associated with the receiving
	 * Session or a transient instance with an identifier associated with existing persistent state. 
	 * @param hierarchy the instance to be removed
	 */
	public void delete(Hierarchy hierachy);


}
