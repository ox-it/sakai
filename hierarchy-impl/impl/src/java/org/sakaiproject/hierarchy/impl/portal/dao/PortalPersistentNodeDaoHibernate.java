package org.sakaiproject.hierarchy.impl.portal.dao;


import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PortalPersistentNodeDaoHibernate extends HibernateDaoSupport implements PortalPersistentNodeDao {
	
	private static final Log log = LogFactory.getLog(PortalPersistentNodeDaoHibernate.class);

	private static final String INDEX_SITE_ID = "create index portal_node_site_id_idx on PORTAL_NODE (siteId)";
	private static final String INDEX_PATH_HASH = "create unique index portal_node_path_hash_idx on PORTAL_NODE (pathHash)";

	private boolean ddl = true;

	public void setDdl(boolean ddl) {
		this.ddl = ddl;
	}

	public void init() {
		if (ddl) {
			Session session = getSession();
			Transaction ta = session.beginTransaction();
			SQLQuery createSQLQuery;
			try {
				// Hibernate (3.2.7) fails to create these indexes so we do it manually.
				// With later versions of hibernate this can go away.
				createSQLQuery = session.createSQLQuery(INDEX_SITE_ID);
				createSQLQuery.executeUpdate();
				createSQLQuery = session.createSQLQuery(INDEX_PATH_HASH);
				createSQLQuery.executeUpdate();
				ta.commit();
				log.info("Created indexes.");
			} catch (HibernateException e ) {
				// This will fail on all subsequent restarts
				// On MySQL at least this comes through as a SQL Grammar exception so it isn't easy to
				// see when it's failing due to index already existing.
				log.debug("Failed to create index: "+ e.getMessage() );
				ta.rollback();
			} finally {
				session.close();
			}
		}
	}

	@Override
	public PortalPersistentNode findById(String id) {
		return (PortalPersistentNode)getHibernateTemplate().get(PortalPersistentNode.class, id);
	}

	@Override
	public PortalPersistentNode findByPathHash(String pathHash){
		return (PortalPersistentNode) justFirst(getHibernateTemplate()
				.find(
						"from org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode as node where node.pathHash = ?",
						pathHash));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PortalPersistentNode> findBySiteId(String siteId) {
		return getHibernateTemplate()
				.find(
						"from org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode as node where node.siteId = ?",
						siteId);
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
