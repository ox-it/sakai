package org.sakaiproject.hierarchy.impl.portal.dao;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Date;
import java.util.List;

public class PortalPersistentNodeDaoHibernate extends HibernateDaoSupport implements PortalPersistentNodeDao {

	public void init() {
	}

	@Override
	public PortalPersistentNode findById(String id) {
		return getHibernateTemplate().get(PortalPersistentNode.class, id);
	}

	@Override
	public PortalPersistentNode findByPathHash(String pathHash){
		return (PortalPersistentNode) justFirst(getHibernateTemplate()
				.find(
						"from org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode as node where node.pathHash = ?",
						pathHash));
	}

	@Override
	public PortalPersistentNode findByPathHash(String oldHash, String newHash){
		return (PortalPersistentNode) justFirst(getHibernateTemplate()
				.find(
						"from org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode as node " +
								"where node.pathHash = ? or node.pathHash = ?",
						oldHash, newHash));
	}


	@Override
	public List<PortalPersistentNode> findBySiteId(String siteId) {
		return getHibernateTemplate().execute((HibernateCallback<List<PortalPersistentNode>>) session -> {
			Query query = session.createQuery("from org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode as node where node.siteId = ?");
			query.setParameter(0, siteId);
			return query.list();
		});
	}

	@Override
	public void save(PortalPersistentNode node) {
		Date now = new Date();
		if (node.getCreated() == null) {
			node.setCreated(now);
		}
		node.setUpdated(now);
		getHibernateTemplate().saveOrUpdate(node);
	}

	private Object justFirst(List<?> list) {
		if (list != null && list.size() > 0) {
			return list.get(0);
		}
		return null;
	}

	@Override
	public void delete(String id) {
		PortalPersistentNode node = findById(id);
		getHibernateTemplate().delete(node);
	}

}
