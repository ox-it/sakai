package org.sakaiproject.hierarchy.impl.portal.dao;


import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PortalPersistentNodeDaoHibernate extends HibernateDaoSupport implements PortalPersistentNodeDao {
	
	public PortalPersistentNode findById(String id) {
		return (PortalPersistentNode)getHibernateTemplate().get(PortalPersistentNode.class, id);
	}
	
	public PortalPersistentNode findByPathHash(String pathHash){
		return (PortalPersistentNode) justFirst(getHibernateTemplate()
				.find(
						"from org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode as node where node.pathHash = ?",
						pathHash));
	}
	
	public List<PortalPersistentNode> findBySiteId(String siteId) {
		return (List<PortalPersistentNode>) getHibernateTemplate()
				.find(
						"from org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode as node where node.siteId = ?",
						siteId);
	}
	
	public void save(PortalPersistentNode node) {
		getHibernateTemplate().saveOrUpdate(node);
	}
	
	private Object justFirst(List<?> list) {
		if (list != null && list.size() > 0) {
			return list.get(0);			
		}
		return null;
	}

	public void delete(String id) {
		PortalPersistentNode node = findById(id);
		getHibernateTemplate().delete(node);
	}


}
