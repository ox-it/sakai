package org.sakaiproject.hierarchy.impl.portal.dao;


import java.util.List;

import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;

public interface PortalPersistentNodeDao {

	public abstract void delete(String id);
	
	public abstract void save(PortalPersistentNode node);

	public abstract List<PortalPersistentNode> findBySiteId(String siteId);

	public abstract PortalPersistentNode findByPathHash(String pathHash);

	public abstract PortalPersistentNode findById(String id);

	
}
