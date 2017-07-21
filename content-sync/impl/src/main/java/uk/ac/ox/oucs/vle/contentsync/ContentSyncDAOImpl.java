package uk.ac.ox.oucs.vle.contentsync;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ContentSyncDAOImpl extends HibernateDaoSupport implements ContentSyncDAO {

	public void init() {
	}
	
	public void save(ContentSyncTableDAO resourceTrackerDao) {
		getHibernateTemplate().save(resourceTrackerDao);
	}
	
	/**
	 * Find all ResourceTracker since timestamp
	 */
	@SuppressWarnings("unchecked")
	public List<ContentSyncTableDAO> findResourceTrackers(final String context, final Date time) {
		return (List<ContentSyncTableDAO>) getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select * " +
						"from content_sync " +
						"where context = :context and " +
						"timeStamp > :timestamp").addEntity(ContentSyncTableDAO.class);
				
				query.setString("context", context);
				query.setTimestamp("timestamp", time);
				return query.list();
			}
		});
	}
}


