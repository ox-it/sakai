package org.sakaiproject.hierarchy.api;

import java.util.Collection;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

public interface HierarchyService
{

	/**
	 * Get a list of Root nodes, the list contains Hierachy objects.
	 * Root nodes represent the named hierachies that the hierachy service knows about.
	 * 
	 * @return Collection containing the root nodes.
	 */
	Collection getRootNodes();

	/**
	 * Get the node based on its nodePath.
	 * 
	 * @param nodePath The path of the node. If null or an empty string then get the node
	 * at the base of the service.
	 * @see #getRootNodes() 
	 * @return The found node or null if it couldn't be found.
	 */
	Hierarchy getNode(String nodePath);

	Hierarchy getNodeById(String id);
	
	/**
	 * delete nodes also removes the properties
	 * 
	 * @param nodePath
	 */
	void deleteNode(Hierarchy node);

	/**
	 * Save a node, and all connected nodes/properties that have changed.
	 * 
	 * @param node
	 * @throws HierarchyServiceException 
	 */
	void save(Hierarchy node) throws HierarchyServiceException;

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
