package org.sakaiproject.hierarchy.impl.model.dao;

// CustomDAOImports

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.hierarchy.model.Hierarchy;

public class HierarchyDAO extends BaseHierarchyDAO implements
		org.sakaiproject.hierarchy.api.dao.HierarchyDAO
{

	private static final String FIND_BY_PARENT_NODE_HQL = null;

	private static final String FIND_BY_NODE_HQL = null;

	// CustomDAOClassConstructor

	public HierarchyDAO()
	{
	}

	public List findHierarchyByParentNodeId(String parentNodeId)
	{
		Session s = getSession();
		Query q = s.createQuery(FIND_BY_PARENT_NODE_HQL);
		q.setString(1, parentNodeId);
		return fullList(q);
	}

	public Hierarchy findHierarchyByNodeId(String nodeId)
	{
		Session s = getSession();
		Query q = s.createQuery(FIND_BY_NODE_HQL);
		q.setString(1, nodeId);
		return (Hierarchy) firstItem(q);
	}

}