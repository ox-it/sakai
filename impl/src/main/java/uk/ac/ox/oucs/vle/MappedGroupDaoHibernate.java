package uk.ac.ox.oucs.vle;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class MappedGroupDaoHibernate extends HibernateDaoSupport implements
		MappedGroupDao {
	
	private static Log log = LogFactory.getLog(MappedGroupDaoHibernate.class);

	public MappedGroup findByGroupRole(String group, String role) {
		MappedGroup example = new MappedGroup();
		example.setExternalGroup(group);
		example.setRole(role);
		List<MappedGroup> results = getHibernateTemplate().findByExample(example);
		MappedGroup result = null;
		if (results.size() > 0) {
			result = results.get(0);
			if (results.size() > 1) {
				log.warn("More than one result found for search: "+ example);
			}
		}
		return result;
	}

	public MappedGroup findById(String id) {
		return (MappedGroup) getHibernateTemplate().get(MappedGroup.class, id);
	}

	public String newMappedGroup(String group, String role) {
		final MappedGroup newGroup = new MappedGroup();
		newGroup.setExternalGroup(group);
		newGroup.setRole(role);
		String id = (String)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Serializable id = session.save(newGroup);
				// We look for the group later on in the request so flush.
				session.flush();
				return id;
			}
			
		});
		return id;
	}

	public List<MappedGroup> findByGroup(String group) {
		MappedGroup example = new MappedGroup();
		example.setExternalGroup(group);
		List<MappedGroup> results = getHibernateTemplate().findByExample(example);
		return results;
	}

}
