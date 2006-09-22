package org.sakaiproject.hierarchy.api;

import java.util.List;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

public interface HierarchyService
{

	/**
	 * Get a list of Root nodes, the list contains Hierachy objects.
	 * Root nodes represent the named hierachies that the hierachy service knos about
	 * 
	 * @return
	 */
	List getRootNodes();

	/**
	 * Get the node based on its nodePath
	 * 
	 * @param nodePath
	 * @return
	 */
	Hierarchy getNode(String nodePath);

	/**
	 * delete nodes also removes the properties
	 * 
	 * @param nodePath
	 */
	void deleteNode(Hierarchy node);

	/**
	 * Save a node, and all connected nodes/properties
	 * 
	 * @param node
	 */
	void save(Hierarchy node);

	/**
	 * Create a new hierachy with a nodePath. The node path should represent the
	 * nodes position withing the hierachy. It should also be unique within the
	 * hierachy service. Once populated and connected, the node or an ancestor
	 * will need to be saved.
	 * 
	 * @param nodePath
	 * @return
	 * @throws HierarchyServiceException 
	 */
	Hierarchy newHierarchy(String nodePath) throws HierarchyServiceException;

	/**
	 * create a new detached HierarchyPropery. This must be added to a node, and
	 * the node saved
	 * 
	 * @return
	 */
	HierarchyProperty newHierachyProperty();

	/**
	 * Begin a transaction
	 *
	 */
	void begin();

	/**
	 * end a hierarchy transaction 
	 *
	 */
	void end();

	/**
	 * abort a hierarchy transaction and prepare to begin a new one
	 *
	 */
	void abort();
	
	

}
