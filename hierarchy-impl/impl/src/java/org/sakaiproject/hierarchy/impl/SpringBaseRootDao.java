/**
 * 
 */
package org.sakaiproject.hierarchy.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * @author ieb
 */
public abstract class SpringBaseRootDao extends HibernateTemplate
{

	public Criteria createCriteria() throws HibernateException
	{
		Session s = getSessionFactory().openSession();
		return s.createCriteria(getReferenceClass());
	}

	protected abstract Class getReferenceClass();

	protected Criteria createCriteria(Session s) throws DataAccessException
	{
		return s.createCriteria(getReferenceClass());
	}

	public String getDefaultOrderProperty()
	{
		return null;
	}

	public String getConfigurationFileName()
	{
		return null;
	}

	public List loadAll() throws DataAccessException
	{
		return loadAll(getReferenceClass());
	}

	public List findAll()
	{
		return super.loadAll(getReferenceClass());
	}

	public List fullList(Query q)
	{
		List l = q.list();
		if (l == null)
		{
			return new ArrayList();
		}
		return l;
	}

	public Object firstItem(Query q)
	{
		List l = q.list();
		if (l.size() > 0)
		{
			return l.get(0);
		}
		return null;
	}

}
