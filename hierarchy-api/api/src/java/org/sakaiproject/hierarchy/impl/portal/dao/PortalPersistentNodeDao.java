package org.sakaiproject.hierarchy.impl.portal.dao;


import java.util.List;

import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;

public interface PortalPersistentNodeDao {

	public abstract void delete(String id);
	
	public abstract void save(PortalPersistentNode node);

	/**
	 * This is a transitional method that allows path lookups by both the old and new hashes.
	 * @param oldHash The old hash.
	 * @param newHash the new hash.
	 * @return The matched portal node.
	 * @see #findByPathHash(String)
	 */
	PortalPersistentNode findByPathHash(String oldHash, String newHash);

	public abstract List<PortalPersistentNode> findBySiteId(String siteId);

	/**
	 * This finds a node by a hash.
	 * @param pathHash The hash.
	 * @return The matched portal node.
     */
	public abstract PortalPersistentNode findByPathHash(String pathHash);

	public abstract PortalPersistentNode findById(String id);

	
}
