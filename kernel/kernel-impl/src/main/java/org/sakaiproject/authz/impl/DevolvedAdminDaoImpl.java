package org.sakaiproject.authz.impl;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.sakaiproject.authz.api.DevolvedAdminDao;
import org.sakaiproject.authz.hbm.DevolvedAdmin;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class DevolvedAdminDaoImpl extends HibernateDaoSupport implements DevolvedAdminDao {
	
	public DevolvedAdmin findByRealm(String realm) {
		return (DevolvedAdmin)getHibernateTemplate().get(DevolvedAdmin.class, realm);
	}

	public void save(DevolvedAdmin devolvedAdmin) {
		getHibernateTemplate().saveOrUpdate(devolvedAdmin);
	}
	
	public void delete(final String realm) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Object toDelete = session.get(DevolvedAdmin.class, realm);
				if (toDelete != null) {
					session.delete(toDelete);
				}
				return null;
			}
			
		});
	}

	@SuppressWarnings("unchecked")
	public List<DevolvedAdmin> findByAdminRealm(String adminRealm) {
		DevolvedAdmin example = new DevolvedAdmin();
		example.setAdminRealm(adminRealm);
		return (List<DevolvedAdmin>)getHibernateTemplate().findByExample(example);
	}

}
