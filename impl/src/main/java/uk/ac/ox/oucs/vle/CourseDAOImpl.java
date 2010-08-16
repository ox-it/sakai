package uk.ac.ox.oucs.vle;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.sql.JoinFragment;
import org.hibernate.transform.ResultTransformer;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import uk.ac.ox.oucs.vle.CourseSignupService.Range;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

public class CourseDAOImpl extends HibernateDaoSupport implements CourseDAO {

	
	public CourseGroupDAO findCourseGroupById(String courseId) {
		return (CourseGroupDAO) getHibernateTemplate().get(CourseGroupDAO.class, courseId);
	}
	
	public CourseGroupDAO findCourseGroupById(final String courseId, final Range range, final Date now) {
		return (CourseGroupDAO) getHibernateTemplate().execute(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				criteria.add(Expression.eq("id", courseId));
				switch (range) { 
					case UPCOMING:
						criteria = criteria.createCriteria("components", JoinFragment.LEFT_OUTER_JOIN).add(Expression.gt("closes", now));
						break;
					case PREVIOUS:
						criteria = criteria.createCriteria("components",  JoinFragment.LEFT_OUTER_JOIN).add(Expression.le("closes", now));
						break;
				}
				criteria.setResultTransformer(Criteria.ROOT_ENTITY);
				return criteria.uniqueResult();
			}
		});
	}

	public CourseGroupDAO findUpcomingComponents(String courseId, Date available) {
		List<CourseGroupDAO> courseGroups = getHibernateTemplate().findByNamedParam(
				"select distinct cg from CourseGroupDAO cg left join fetch cg.components as component where cg.id = :courseId and component.closes > :closes",
				new String[]{"courseId", "closes"}, new Object[]{courseId, available});
		int results = courseGroups.size();
		if (results > 0) {
			if (results > 1) {
				throw new IllegalStateException("To many results ("+ results + ") found for "+ courseId );
			}
			return courseGroups.get(0);
		}
		return null;
	}

	public List<CourseComponentDAO> findOpenComponents(String id, Date at) {
		// TODO Auto-generated method stub
		return null;
	}

	public CourseGroupDAO findAvailableCourseGroupById(String courseId) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findCourseGroupByDept(final String deptId, final Range range, final Date now) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				criteria.add(Expression.eq("dept", deptId));
				switch (range) { 
					case UPCOMING:
						criteria = criteria.createCriteria("components", JoinFragment.LEFT_OUTER_JOIN).add(Expression.gt("closes", now));
						break;
					case PREVIOUS:
						criteria = criteria.createCriteria("components",  JoinFragment.LEFT_OUTER_JOIN).add(Expression.le("closes", now));
						break;
				}
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				criteria.addOrder(Order.asc("title"));
				return criteria.list();
			}
			
		});
	}

	public CourseComponentDAO findCourseComponent(String id) {
		return (CourseComponentDAO) getHibernateTemplate().get(CourseComponentDAO.class, id);
	}

	public CourseSignupDAO newSignup(String userId, String supervisorId) {
		CourseSignupDAO signupDao = new CourseSignupDAO();
		signupDao.setUserId(userId);
		signupDao.setSupervisorId(supervisorId);
		return signupDao;
	}

	public void save(CourseSignupDAO signupDao) {
		getHibernateTemplate().save(signupDao);
	}

	public void save(CourseComponentDAO componentDao) {
		getHibernateTemplate().save(componentDao);
	}

	public CourseSignupDAO findSignupById(String signupId) {
		return (CourseSignupDAO) getHibernateTemplate().get(CourseSignupDAO.class, signupId);
	}

	@SuppressWarnings("unchecked")
	public List<CourseSignupDAO> findSignupForUser(final String userId, final Set<Status> statuses) {
		return (List<CourseSignupDAO>)getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(CourseSignupDAO.class);
				criteria.add(Expression.eq("userId", userId));
				if (!statuses.isEmpty()) {
					criteria.add(Expression.in("status", statuses.toArray()));
				}
				criteria.setFetchMode("components", FetchMode.JOIN);
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				return criteria.list();
			}
		});
	}

	public CourseGroupDAO newCourseGroup(String id, String title, String dept) {
		CourseGroupDAO groupDao = new CourseGroupDAO();
		groupDao.setId(id);
		groupDao.setTitle(title);
		groupDao.setDept(dept);
		return groupDao;
	}

	public void save(CourseGroupDAO groupDao) {
		getHibernateTemplate().save(groupDao);
	}

	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findAdminCourseGroups(final String userId) {
		// Finds all the coursegroups this user can admin.
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery("select distinct cg from CourseGroupDAO cg where cg.administrator = :userId");
				query.setString("userId", userId);
				return query.list();
			}
			
		});
	}

	public List<CourseSignupDAO> findSignupByCourse(final String userId, final String courseId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery(" select distinct cs from CourseComponentDAO cc inner join cc.signups cs inner join cs.group cg where cg.administrator = :userId and cg.id = :courseId");
				query.setString("userId", userId);
				query.setString("courseId", courseId);
				return query.list();
			}
			
		});
	}
	
	public List<CourseSignupDAO> findSignupByComponent(final String componentId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery("select cs from CourseSignupDAO cs inner join fetch cs.components cc where cc.id = :componentId");
				query.setString("componentId", componentId);
				return query.list();
			}
		});
	}
	
	public List<CourseSignupDAO> findSignupPending(final String userId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createQuery("select cs from CourseSignupDAO cs inner join fetch cs.components cc inner join fetch cs.group cg where (cg.administrator = :userId and cs.status = :adminStatus) or (cs.supervisorId = :userId and cs.status = :supervisorStatus)");
				query.setString("userId", userId);
				query.setParameter("adminStatus", Status.PENDING);
				query.setParameter("supervisorStatus", Status.ACCEPTED);
				return query.list();
			}
		});
	}

	public CourseComponentDAO newCourseComponent(String id) {
		CourseComponentDAO componentDao = new CourseComponentDAO();
		componentDao.setId(id);
		return componentDao;
	}

	public List<CourseGroupDAO> findCourseGroupByWords(final String[] words, final Range range, final Date date) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				for(String word: words) {
					criteria.add(Expression.ilike("title", word, MatchMode.ANYWHERE));
				}
				switch(range) {
					case UPCOMING:
						criteria = criteria.createCriteria("components", JoinFragment.LEFT_OUTER_JOIN).add(Expression.gt("closes", date));
						break;
					case PREVIOUS:
						criteria = criteria.createCriteria("components",  JoinFragment.LEFT_OUTER_JOIN).add(Expression.le("closes", date));
						break;
				}
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				return criteria.list();
			}
			
		});
		
	}

	public void remove(CourseSignupDAO existingSignup) {
		getHibernateTemplate().delete(existingSignup);
	}

	/**
	 * Used by tests to simulate another request being made.
	 */
	public void flush() {
		getHibernateTemplate().flush();
	}
}


