package uk.ac.ox.oucs.vle.contentsync;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.time.api.Time;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class ContentSyncDAOImpl extends HibernateDaoSupport implements ContentSyncDAO {

	public void save(ContentSyncTableDAO resourceTrackerDao) {
		getHibernateTemplate().save(resourceTrackerDao).toString();
	}
	
	/**
	 * Find all ResourceTracker since timestamp
	 */
	@SuppressWarnings("unchecked")
	public List<ContentSyncTableDAO> findResourceTrackers(final String context, final Time time) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select * " +
						"from content_sync " +
						"where context = :context and " +
						"timeStamp > :timestamp").addEntity(ContentSyncTableDAO.class);
				
				query.setString("context", context);
				Timestamp timestamp = new Timestamp(time.getTime());
				query.setTimestamp("timestamp", timestamp);
				return query.list();
			}
		});
	}
}


