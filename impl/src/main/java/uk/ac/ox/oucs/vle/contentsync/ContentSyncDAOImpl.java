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

	private static final Log log = LogFactory.getLog(ContentSyncDAOImpl.class);
	
	private static final String INDEX_SQL = "CREATE INDEX content_sync_idx ON content_sync (context, timeStamp)";
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void init() {
		if (serverConfigurationService.getBoolean("auto.ddl", true)) {
			
			Session session = getSession();
			Transaction ta = session.beginTransaction();
			try {
				// Hibernate fails to create this index so we do it manually.
				SQLQuery createSQLQuery = session.createSQLQuery(INDEX_SQL);
				createSQLQuery.executeUpdate();
				ta.commit();
				log.info("Created indexes.");
			} catch (HibernateException e ) {
				// This will fail on all subsequent restarts 
				log.debug("Failed to create index: '"+ INDEX_SQL+ "' "+ e.getMessage() );
				ta.rollback();
				session.close();
			}
		}
	}
	
	public void save(ContentSyncTableDAO resourceTrackerDao) {
		getHibernateTemplate().save(resourceTrackerDao);
	}
	
	/**
	 * Find all ResourceTracker since timestamp
	 */
	@SuppressWarnings("unchecked")
	public List<ContentSyncTableDAO> findResourceTrackers(final String context, final Date time) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
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


