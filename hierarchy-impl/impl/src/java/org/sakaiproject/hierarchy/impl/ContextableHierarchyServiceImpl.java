package org.sakaiproject.hierarchy.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.HierarchyService;
import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * Extension of the HierarchyService which allows a HierarchyService to be
 * limited to a particular context (prefix). This allows the client of the API
 * not have to continually worry about performing all operation in a particular
 * part of the HierarchyService as using this API you shouldn't be able to
 * escape outside the context.
 * 
 * @author buckett
 * 
 */
public class ContextableHierarchyServiceImpl implements HierarchyService {

	private static final Log log = LogFactory
			.getLog(ContextableHierarchyServiceImpl.class);

	private HierarchyService hierarchyService = null;

	private String context;

	// Root node is cached for a transaction.
	private ThreadLocalManager threadLocalManager = org.sakaiproject.thread_local.cover.ThreadLocalManager.getInstance();

	public void init() {
		if (context == null) {
			throw new IllegalStateException("Context is not set.");
		}

		if (hierarchyService.getNode(context) == null) {
			try {
				hierarchyService.begin();
				hierarchyService.save(hierarchyService.newHierarchy(context));
				hierarchyService.end();
				log.debug("Created context node: " + context);
			} catch (HierarchyServiceException hse) {
				log.error("Failed to create context node: " + context, hse);
			}
		} else {
			log.debug("Context node already exists: " + context);
		}
	}

	public void abort() {
		threadLocalManager.set("root", null);
		hierarchyService.abort();
	}

	public void begin() {
		threadLocalManager.set("root", null);
		hierarchyService.begin();
	}

	public void deleteNode(Hierarchy node) {
		hierarchyService.deleteNode(node);
	}

	public void end() {
		threadLocalManager.set("root", null);
		hierarchyService.end();
	}

	public Hierarchy getNode(String nodePath) {
		Hierarchy node;
		if (nodePath == null || nodePath.length() == 0) {
			node = getRootNode();
		} else { 
			node = hierarchyService.getNode(context + nodePath);
			if (node != null) {
				node = new ContextableHierarchyImpl(node, context);
			}
		}
		return node;
	}
	
	public Hierarchy getNodeById(String id) {
		return hierarchyService.getNode(id);
	}

	public Hierarchy getRootNode() {
		Hierarchy node = (Hierarchy) threadLocalManager.get("root");
		if (node == null) {
			node = new ContextableHierarchyImpl(hierarchyService
					.getNode(context), context);
			threadLocalManager.set("root", node);
		}
		return node;
	}

	public Collection getRootNodes() {
		return getRootNode().getChildren().values();
	}

	public HierarchyProperty newHierachyProperty() {
		return hierarchyService.newHierachyProperty();
	}

	public Hierarchy newHierarchy(String nodePath)
			throws HierarchyServiceException {
		if ( nodePath == null ) {
			throw new HierarchyServiceException("Node Path cannot be null");
		}
		if ( !nodePath.startsWith("/") ) {
			throw new HierarchyServiceException("Node Path must start with a / ");
		}
		if ( nodePath.length() <= 1 ) {
			throw new HierarchyServiceException("Cant create the / node, it already exists ");
		}
		ContextableHierarchyImpl node = new ContextableHierarchyImpl(
				hierarchyService.newHierarchy(context + nodePath), context);
		node.setParent(getRootNode());
		return node;
	}

	public void save(Hierarchy node) throws HierarchyServiceException {
		if (node instanceof ContextableHierarchyImpl) {
			ContextableHierarchyImpl contextable = (ContextableHierarchyImpl) node;
			hierarchyService.save(contextable.getDelegate());
		} else {
			throw new IllegalArgumentException(
					"This service can only save objects from itself.");
		}

	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public HierarchyService getHierarchyService() {
		return hierarchyService;
	}

	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	public List getNodesByProperty(String name, String value) {
		// Piss poor performance.
		List<Hierarchy> allNodes = hierarchyService.getNodesByProperty(name, value);
		List<Hierarchy> ourNodes = new ArrayList(allNodes.size());
		for (Hierarchy node: allNodes) {
			if (node.getPath().startsWith(getContext())) {
				ourNodes.add(new ContextableHierarchyImpl(node, context));
			}
		}
		return ourNodes;
	}

}
