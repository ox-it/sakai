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
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinFragment;
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

	@SuppressWarnings("unchecked")
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
	public List<CourseGroupDAO> findCourseGroupByDept(final String deptId, final Range range, final Date now, final boolean external) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				
				StringBuffer querySQL = new StringBuffer();
				querySQL.append("SELECT DISTINCT ");
				querySQL.append("cg.id, cg.title, cg.dept, cg.departmentName, ");
				querySQL.append("cg.subunit, cg.subunitName, cg.description, cg.publicView, ");
				querySQL.append("cg.supervisorApproval, cg.administratorApproval, cg.homeApproval, cg.contactEmail ");
				querySQL.append("FROM course_group cg ");
				querySQL.append("LEFT JOIN course_group_otherDepartment cgd on cgd.course_group = cg.id ");
				querySQL.append("LEFT JOIN course_group_component cgc on cgc.course_group = cg.id ");
				querySQL.append("LEFT JOIN course_component cc on cgc.component = cc.id ");
				querySQL.append("WHERE ");
				
				if (external) {
					querySQL.append("publicView = true AND ");
				}
				
				switch (range) { 
					case UPCOMING:
						querySQL.append("closes > NOW() AND ");
						break;
					case PREVIOUS:
						querySQL.append("closes < NOW() AND ");
						break;
				}
				
				querySQL.append("(otherDepartment = :deptId ");
				querySQL.append("OR (dept = :deptId and (subunit is NULL or subunit = ''))) ");
				querySQL.append("ORDER BY cg.title ");
				
				Query query = session.createSQLQuery(querySQL.toString()).addEntity(CourseGroupDAO.class);
				query.setString("deptId", deptId);
				return query.list();
			
				//you can't use Criteria to query against a collection of value types
			/*
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				
				criteria.add(
					Restrictions.or(
							Restrictions.and(
									Restrictions.eq("dept", deptId), 
									Restrictions.or(Restrictions.isNull("subunit"),Restrictions.eq("subunit", ""))),
							Restrictions.eq("otherDepartment", deptId)));
				
				if (external) {
					criteria.add(Restrictions.eq("publicView", true));
				}
				switch (range) { 
					case UPCOMING:
						criteria = criteria.createCriteria("components", JoinFragment.LEFT_OUTER_JOIN).add(Restrictions.gt("closes", now));
						break;
					case PREVIOUS:
						criteria = criteria.createCriteria("components",  JoinFragment.LEFT_OUTER_JOIN).add(Restrictions.le("closes", now));
						break;
				}
				
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				criteria.addOrder(Order.asc("title"));
				return criteria.list();
			*/
			}	
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findCourseGroupBySubUnit(final String subunitId, final Range range, final Date now, final boolean external) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				criteria.add(Restrictions.eq("subunit", subunitId));
				if (external) {
					criteria.add(Restrictions.eq("publicView", true));
				}
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
	
	@SuppressWarnings("unchecked")
	public List<Object[]> findSubUnitByDept(final String deptId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Query query = session.createQuery("select distinct subunit, subunitName from CourseGroupDAO cg where cg.dept = :deptId and cg.subunit <> '' order by 2");
				query.setString("deptId", deptId);
				return query.list();
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<DepartmentDAO> findAllDepartments() {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Query query = session.createSQLQuery("select * from department").addEntity(DepartmentDAO.class);
				return query.list();
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

	public String save(CourseSignupDAO signupDao) {
		return getHibernateTemplate().save(signupDao).toString();
	}

	public String save(CourseComponentDAO componentDao) {
		return getHibernateTemplate().save(componentDao).toString();
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

	public CourseGroupDAO newCourseGroup(String id, String title, String dept, String subunit) {
		CourseGroupDAO groupDao = new CourseGroupDAO();
		groupDao.setId(id);
		groupDao.setTitle(title);
		groupDao.setDept(dept);
		groupDao.setSubunit(subunit);
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
				Query query = session.createSQLQuery("select * from course_group, (select course_group from course_group_administrator where administrator = :userId union select course_group from course_group_superuser where superuser = :userId) admins where course_group.id = admins.course_group").addEntity(CourseGroupDAO.class);
				query.setString("userId", userId);
				return query.list();
			}
			
		});
	}

	@SuppressWarnings("unchecked")
	public List<CourseSignupDAO> findSignupByCourse(final String userId, final String courseId, final Set<Status> statuses) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query;
				if (null != statuses && !statuses.isEmpty()) {
					query = session.createSQLQuery("select * from course_signup, (select course_group from course_group_administrator where administrator = :userId union select course_group from course_group_superuser where superuser = :userId) admins where course_signup.groupId = admins.course_group and course_signup.groupId = :courseId and cs.status in (:statuses)").addEntity(CourseSignupDAO.class);
					
					query.setParameterList("statuses", statuses);
				} else {
					query = session.createSQLQuery("select * from course_signup, (select course_group from course_group_administrator where administrator = :userId union select course_group from course_group_superuser where superuser = :userId) admins where course_signup.groupId = admins.course_group and course_signup.groupId = :courseId").addEntity(CourseSignupDAO.class);
				}
				query.setString("userId", userId);
				query.setString("courseId", courseId);
				return query.list();
			}
			
		});
	}
	
	public Integer countSignupByCourse(final String courseId, final Set<Status> statuses) {
		List results = getHibernateTemplate().findByNamedParam(
				"select count(*) from CourseSignupDAO where groupId = :courseId and status in (:statuses)",
				new String[]{"courseId", "statuses"}, new Object[]{courseId, statuses});
		int count = results.size();
		if (count > 0) {
			if (count > 1) {
				throw new IllegalStateException("To many results ("+ results + ") found for "+ courseId );
			}
			return (Integer)results.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseSignupDAO> findSignupByComponent(final String componentId, final Set<Status> statuses) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query;
				if (null != statuses && !statuses.isEmpty()) {
					query = session.createQuery("select cs from CourseSignupDAO cs inner join fetch cs.components cc where cc.id = :componentId and cs.status in (:statuses)");
					query.setParameterList("statuses", statuses);
				} else {
					query = session.createQuery("select cs from CourseSignupDAO cs inner join fetch cs.components cc where cc.id = :componentId");
				}
				
				query.setString("componentId", componentId);
				return query.list();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseSignupDAO> findSignupPending(final String userId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery("select * from course_signup cs left join course_group_administrator ca on cs.groupId = ca.course_group inner join course_component_signup cp on cs.id = cp.signup inner join course_component cc on cp.component = cc.id where (ca.administrator = :userId and cs.status = :adminStatus) or (cs.supervisorId = :userId and cs.status = :supervisorStatus)").addEntity(CourseSignupDAO.class);
				query.setString("userId", userId);
				query.setParameter("adminStatus", Status.PENDING.name());
				query.setParameter("supervisorStatus", Status.ACCEPTED.name());
				return query.list();
			}
		});
	}

	public CourseComponentDAO newCourseComponent(String id) {
		CourseComponentDAO componentDao = new CourseComponentDAO();
		componentDao.setId(id);
		return componentDao;
	}

	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findCourseGroupByWords(final String[] words, 
			final Range range, final Date date, final boolean external) {
		
		return getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				for(String word: words) {
					criteria.add(Expression.ilike("title", word, MatchMode.ANYWHERE));
				}
				if (external) {
					criteria.add(Expression.eq("publicView", true));
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
	
	public DepartmentDAO findDepartmentByCode(String code) {
		return (DepartmentDAO) getHibernateTemplate().get(DepartmentDAO.class, code);
	}
	
	public void save(DepartmentDAO departmentDao) {
		getHibernateTemplate().save(departmentDao).toString();
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


