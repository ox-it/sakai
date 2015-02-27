/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;

import org.hibernate.*;
import org.hibernate.criterion.*;
import org.hibernate.sql.JoinFragment;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import uk.ac.ox.oucs.vle.CourseSignupService.Range;
import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import java.sql.SQLException;
import java.util.*;

public class CourseDAOImpl extends HibernateDaoSupport implements CourseDAO {

	private static final MonthDay FIRST_DAY_OF_ACADEMIC_YEAR = new MonthDay(DateTimeConstants.SEPTEMBER, 1);

	// Set lastYear to 1st September (start of last academic year)
	public static LocalDate getPreviousYearBeginning(LocalDate currentDate) {
		int currentCivilYear = currentDate.getYear();
		int previousAcademicYear;

		// If we've started a new civil year and haven't changed the academic year yet, go back one more year.
		if (currentDate.isBefore(FIRST_DAY_OF_ACADEMIC_YEAR.toLocalDate(currentCivilYear))) {
			previousAcademicYear = currentCivilYear - 2;
		} else {
			previousAcademicYear = currentCivilYear - 1;
		}

		return FIRST_DAY_OF_ACADEMIC_YEAR.toLocalDate(previousAcademicYear);
	}
	
	public CourseGroupDAO findCourseGroupById(final String courseId) {
		return (CourseGroupDAO) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException,	SQLException {
				Criteria criteria = session.createCriteria(CourseGroupDAO.class).add(Restrictions.eq("courseId", courseId));
				return criteria.uniqueResult();
			}
		});
	}

	public List<CourseComponentDAO> findCourseComponents(final String courseId, final Range range, final Date now) {
		return (List<CourseComponentDAO>) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Criteria criteria = session.createCriteria(CourseComponentDAO.class);
				Criteria subCriteria;
				switch (range) {
					case NOTSTARTED:
					case UPCOMING:
						criteria.add(Restrictions.or(
								Restrictions.gt("baseDate", now),
								Restrictions.and(Restrictions.isNull("baseDate"), Restrictions.isNotNull("startsText"))));
						break;
					case PREVIOUS:
						Date startLastYear = getPreviousYearBeginning(new LocalDate(now)).toDate();
						criteria.add(
								Restrictions.or(
										Restrictions.and(Restrictions.le("baseDate", now), Restrictions.gt("baseDate", startLastYear)),
										Restrictions.and(Expression.isNull("baseDate"), Restrictions.isNull("startsText"))
									));
						break;
				}
				subCriteria = criteria.createCriteria("groups", JoinFragment.INNER_JOIN);
				subCriteria.add(Restrictions.eq("courseId", courseId));
				subCriteria.add(Restrictions.eq("hideGroup", false));
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				return criteria.list();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public CourseGroupDAO findUpcomingComponents(String courseId, Date available) {
		List<CourseGroupDAO> courseGroups = getHibernateTemplate().findByNamedParam(
				"select distinct cg from CourseGroupDAO cg left join fetch cg.components as component where cg.courseId = :courseId and component.closes > :closes",
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
	
	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findAllGroups() {
		return getHibernateTemplate().loadAll(CourseGroupDAO.class);
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseComponentDAO> findAllComponents() {
		return getHibernateTemplate().loadAll(CourseComponentDAO.class);
	}

	public CourseGroupDAO findAvailableCourseGroupById(String courseId) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findCourseGroupByDept(final String deptId, final Range range, final Date now, final boolean external) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			/**
			 * Note:
			 * This can't be easily migrated to Hibernate Query API as collections are not supported
			 * org.hibernate.MappingException: collection was not an association: uk.ac.ox.oucs.vle.CourseGroupDAO.otherDepartments
			 */
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				
				Date startLastYear = getPreviousYearBeginning(LocalDate.fromDateFields(now)).toDate();
				StringBuffer querySQL = new StringBuffer();
				querySQL.append("SELECT DISTINCT cg.* ");
				querySQL.append("FROM course_group cg ");
				querySQL.append("LEFT JOIN course_group_otherDepartment cgd on cgd.courseGroupMuid = cg.muid ");
				querySQL.append("LEFT JOIN course_group_component cgc on cgc.courseGroupMuid = cg.muid ");
				querySQL.append("LEFT JOIN course_component cc on cgc.courseComponentMuid = cc.muid ");
				querySQL.append("WHERE ");
				
				querySQL.append("visibility != 'PR' AND ");
				
				if (external) {
					querySQL.append("visibility != 'RS' AND ");
				}

				querySQL.append("hideGroup = false AND ");
				
				switch (range) { 
					case UPCOMING:
						querySQL.append("((cc.baseDate is null AND cc.startsText is not null) OR cc.baseDate > :now) AND ");
						break;
					case PREVIOUS:
						querySQL.append("((cc.baseDate is null AND cc.startsText is null) OR (cc.baseDate <= :now AND cc.baseDate >= :lastYear)) AND ");
						break;
				}
				
				querySQL.append("(otherDepartment = :deptId ");
				querySQL.append("OR (dept = :deptId and (subunit is NULL or subunit = ''))) ");
				querySQL.append("ORDER BY cg.title ");
				
				Query query = session.createSQLQuery(querySQL.toString()).addEntity(CourseGroupDAO.class);
				query.setString("deptId", deptId);
				query.setDate("now", now);
				if (range.equals(range.PREVIOUS)) {
					query.setDate("lastYear", startLastYear);
				}
				
				return query.list();
			}	
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findCourseGroupBySubUnit(final String subunitId, final Range range, final Date now, final boolean external) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				
				Date startLastYear = getPreviousYearBeginning(LocalDate.now()).toDate();
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				criteria.add(Restrictions.eq("subunit", subunitId));
				criteria.add(Restrictions.ne("visibility", "PR"));
				if (external) {
					criteria.add(Restrictions.ne("visibility", "RS"));
				}
				criteria.add(Restrictions.eq("hideGroup", false));
				switch (range) { 
					case UPCOMING:
						criteria = criteria.createCriteria("components", JoinFragment.LEFT_OUTER_JOIN).add(
								Expression.or(Expression.gt("baseDate", now), Expression.and(Expression.isNull("baseDate"), Expression.isNotNull("startsText"))));
						break;
					case PREVIOUS:
						criteria = criteria.createCriteria("components",  JoinFragment.LEFT_OUTER_JOIN).add(
								Expression.or(
										Expression.and(Expression.le("baseDate", now), Expression.gt("baseDate", startLastYear)), 
										Expression.and(Expression.isNull("baseDate"), Expression.isNull("startsText"))
								));
						break;
				}
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				criteria.addOrder(Order.asc("title"));
				return criteria.list();
			}
			
		});
	}
	
	/**
	 * Find all courseGroups that share a component with Id componentId
	 */
	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findCourseGroupByComponent(final String componentId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select * from course_component " +
						"left join course_group_component on course_group_component.courseComponentMuid = course_component.muid " +
						"left join course_group on course_group_component.courseGroupMuid = course_group.muid " +
						"where course_component.presentationId = :componentId").addEntity(CourseGroupDAO.class);
				query.setString("componentId", componentId);
				return query.list();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> findSubUnitByDept(final String deptId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Query query = session.createQuery("select distinct subunit, subunitName " +
						"from CourseGroupDAO cg " +
						"where cg.dept = :deptId and cg.subunit <> '' order by 2");
				query.setString("deptId", deptId);
				return query.list();
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseDepartmentDAO> findAllDepartments() {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			// Need the DISTINCT ROOT ENTITY filter.
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Query query = session.createSQLQuery("select * from course_department").addEntity(CourseDepartmentDAO.class);
				return query.list();
			}
			
		});
	}

	public CourseComponentDAO findCourseComponent(final String id) {
		return (CourseComponentDAO) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Criteria criteria = session.createCriteria(CourseComponentDAO.class);
				criteria.add(Expression.eq("presentationId", id));
				criteria.setResultTransformer(Criteria.ROOT_ENTITY);
				return criteria.uniqueResult();
			}
		});
	}

	public CourseSignupDAO newSignup(String userId, String supervisorId, Date now) {
		CourseSignupDAO signupDao = new CourseSignupDAO();
		signupDao.setUserId(userId);
		signupDao.setSupervisorId(supervisorId);
		signupDao.setCreated(now);
		return signupDao;
	}

	public String save(CourseSignupDAO signupDao) {
		return getHibernateTemplate().save(signupDao).toString();
	}

	public void save(final CourseComponentDAO componentDao) {
		getHibernateTemplate().execute(new HibernateCallback<Object>() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				session.saveOrUpdate(componentDao);
				for (CourseGroupDAO group: componentDao.getGroups()) {
					session.refresh(group);
				}
				return null;
			}
		});
		 getHibernateTemplate().saveOrUpdate(componentDao);
	}

	public CourseSignupDAO findSignupById(String signupId) {
		return (CourseSignupDAO) getHibernateTemplate().get(CourseSignupDAO.class, signupId);
	}
	
	public CourseSignupDAO findSignupByEncryptId(String signupId) {
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
		groupDao.setCourseId(id);
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
		List<CourseGroupDAO> adminGroups = findAdministratorCourseGroups(userId);
		List<CourseGroupDAO> superGroups = findSuperUserCourseGroups(userId);
		
		Set<CourseGroupDAO> allGroups = new HashSet<CourseGroupDAO>(superGroups);
		allGroups.addAll(adminGroups);
		return new ArrayList<CourseGroupDAO>(allGroups);
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findAdministratorCourseGroups(final String userId) {
		// Finds all the coursegroups this user can admin. 
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery("select * from course_group, " +
					"(select courseGroupMuid from course_group_administrator " +
					"where administrator = :userId) admins " +
					"where course_group.muid = admins.courseGroupMuid " +
					"and course_group.hideGroup = false").addEntity(CourseGroupDAO.class);
				query.setString("userId", userId);
				return query.list();
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findSuperUserCourseGroups(final String userId) {
		// Finds all the coursegroups this user can superuser.
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery("select * from course_group, " +
					"(select courseGroupMuid from course_group_superuser " +
					"where superuser = :userId) admins " +
					"where course_group.muid = admins.courseGroupMuid").addEntity(CourseGroupDAO.class);
				query.setString("userId", userId);
				return query.list();
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findLecturingCourseGroups(final String userId) {
		// Finds all the coursegroups this user is teaching. 
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery("select distinct cg.* " +
						"from course_group cg " +
						"LEFT JOIN course_group_component cgc on cgc.courseGroupMuid = cg.muid " +
						"LEFT JOIN course_component cc on cgc.courseComponentMuid = cc.muid " +
						"where cc.teacher = :userId").addEntity(CourseGroupDAO.class);
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
					query = session.createSQLQuery("select * from course_signup " +
							"left join course_group on course_signup.courseGroupMuid = course_group.muid " +
							"where course_group.courseId = :courseId " +
							"and course_signup.status in (:statuses)").addEntity(CourseSignupDAO.class);
					
					Set<String> statusString = new HashSet<String>();
					for (Status status : statuses) {
						statusString.add(status.toString());
					}
					query.setParameterList("statuses", statusString);
				} else {
					query = session.createSQLQuery("select * from course_signup " +
							"left join course_group on course_signup.courseGroupMuid = course_group.muid " +
							"where course_group.courseId = :courseId").addEntity(CourseSignupDAO.class);
				}
				//query.setString("userId", userId);
				query.setString("courseId", courseId);
				return query.list();
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	public Integer countSignupByCourse(final String courseId, final Set<Status> statuses, final Date now) {
		return (Integer)getHibernateTemplate().execute(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery("select count(signup.id) from CourseSignupDAO signup "+
						"left join signup.components component "+
						"left join signup.group grp "+
						"where grp.courseId = :courseId "+
						"and component.starts > :now "+
						"and signup.status in (:statuses)");
				query.setString("courseId", courseId);
				query.setDate("now", now);
				query.setParameterList("statuses", statuses);
				return ((Number)query.uniqueResult()).intValue();
			}

		});
	}
	
	public List<CourseSignupDAO> findSignupByComponent(final String componentId, final Set<Status> statuses) {
		return findSignupByComponent(componentId, statuses, null);
	}
		
	@SuppressWarnings("unchecked")
	public List<CourseSignupDAO> findSignupByComponent(final String componentId, final Set<Status> statuses, final Integer year) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				
				Query query;
				LocalDate startYear = null;
				LocalDate endYear = null;
				
				StringBuffer querySQL = new StringBuffer();
				querySQL.append("select cs from CourseSignupDAO cs " +
								"inner join fetch cs.components cc " +
								"where cc.presentationId = :componentId");
				
				if (null != statuses && !statuses.isEmpty()) {
					querySQL.append(" and cs.status in (:statuses)");
				}
				
				if (null != year) {
					startYear = FIRST_DAY_OF_ACADEMIC_YEAR.toLocalDate(year);
					endYear = FIRST_DAY_OF_ACADEMIC_YEAR.toLocalDate(year+1);
					querySQL.append(" and cc.starts between :starts and :ends");
				}
				
				query = session.createQuery(querySQL.toString());
				query.setString("componentId", componentId);
				if (null != statuses && !statuses.isEmpty()) {
					query.setParameterList("statuses", statuses);
				}
				
				if (null != year) {
					query.setDate("starts", startYear.toDate());
					query.setDate("ends", endYear.toDate());
				}
				return query.list();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseSignupDAO> findSignupPending(final String userId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select distinct cs.id, cs.userId, cs.status, cs.created, cs.amended, cs.message, cs.supervisorId, cs.courseGroupMuid, cs.department " +
						"from course_signup cs " +
						"left join course_group_administrator ca on cs.courseGroupMuid = ca.courseGroupMuid " +
						"inner join course_component_signup cp on cs.id = cp.signup " +
						"inner join course_component cc on cp.courseComponentMuid = cc.muid " +
						"where (ca.administrator = :userId and cs.status = :adminStatus) or (cs.supervisorId = :userId and cs.status = :supervisorStatus)").addEntity(CourseSignupDAO.class);
				query.setString("userId", userId);
				query.setParameter("adminStatus", Status.PENDING.name());
				query.setParameter("supervisorStatus", Status.ACCEPTED.name());
				return query.list();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseSignupDAO> findSignupStillPendingOrAccepted(final Integer period) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select distinct cs.id, cs.userId, cs.status, cs.created, cs.amended, cs.message, cs.supervisorId, cs.courseGroupMuid, cs.department " +
						"from course_signup cs " +
						"left join course_group_administrator ca on cs.courseGroupMuid = ca.courseGroupMuid " +
						"inner join course_component_signup cp on cs.id = cp.signup " +
						"inner join course_component cc on cp.courseComponentMuid = cc.muid " +
						"where (date_sub(curdate(), interval :period day) >= cs.amended " +
						"or date_sub(curdate(), interval :period day) <= cc.starts) " +
						"and (curdate() < cc.starts) " +
						"and ((cs.status = :adminStatus) or (cs.status = :supervisorStatus))").addEntity(CourseSignupDAO.class);
				query.setInteger("period", period);
				query.setParameter("adminStatus", Status.PENDING.name());
				query.setParameter("supervisorStatus", Status.ACCEPTED.name());
				return query.list();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<CourseSignupDAO> findSignupApproval(final String userId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select distinct cs.id, cs.userId, cs.status, cs.created, cs.amended, cs.message, cs.supervisorId, cs.courseGroupMuid, cs.department " +
						"from course_signup cs " +
						"left join course_group_administrator ca on cs.courseGroupMuid = ca.courseGroupMuid " +
						"inner join course_component_signup cp on cs.id = cp.signup " +
						"inner join course_component cc on cp.courseComponentMuid = cc.muid " +
						"inner join course_department_approver da on da.department = cs.department " +
						"where da.approver = :userId and cs.status = :approverStatus").addEntity(CourseSignupDAO.class);
				query.setString("userId", userId);
				query.setParameter("approverStatus", Status.APPROVED.name());
				return query.list();
			}
		});
	}

	public CourseComponentDAO newCourseComponent(String id) {
		CourseComponentDAO componentDao = new CourseComponentDAO();
		componentDao.setPresentationId(id);
		Calendar now = GregorianCalendar.getInstance();
		componentDao.setCreated(now.getTime());
		return componentDao;
	}

	@SuppressWarnings("unchecked")
	public List<CourseGroupDAO> findCourseGroupByWords(final String[] words, 
			final Range range, final Date date, final boolean external) {
		
		return getHibernateTemplate().executeFind(new HibernateCallback() {

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				
				Date startLastYear = getPreviousYearBeginning(LocalDate.now()).toDate();
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				for(String word: words) {
					criteria.add(Expression.ilike("title", word, MatchMode.ANYWHERE));
				}
				criteria.add(Expression.ne("visibility", "PR"));
				if (external) {
					criteria.add(Expression.ne("visibility", "RS"));
				}
				criteria.add(Expression.eq("hideGroup", false));
				
				switch(range) {
					case UPCOMING:
						criteria = criteria.createCriteria("components", JoinFragment.LEFT_OUTER_JOIN).add(
								Expression.or(Expression.gt("baseDate", date), Expression.and(Expression.isNull("baseDate"), Expression.isNotNull("startsText"))));
						break;
					case PREVIOUS:
						criteria = criteria.createCriteria("components",  JoinFragment.LEFT_OUTER_JOIN).add(
								Expression.or(
										Expression.and(Expression.le("baseDate", date), Expression.gt("baseDate", startLastYear)), 
										Expression.and(Expression.isNull("baseDate"), Expression.isNull("startsText"))
									));
						break;
				}
				criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				return criteria.list();
			}
			
		});
		
	}

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<CourseDepartmentDAO> findApproverDepartments(final String userId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select * from course_department_approver " +
						"left join course_department on course_department.code = course_department_approver.department " +
						"where approver = :userId").addEntity(CourseDepartmentDAO.class);
				query.setString("userId", userId);
				return query.list();
			}
		});
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findDepartmentApprovers(final String department) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select approver from course_department_approver " +
						"where department = :deptId");
				query.setString("deptId", department);
				return query.list();
			}
		});
	}
	
	/**
	 * 
	 */
	public CourseDepartmentDAO findDepartmentByCode(String code) {
		return (CourseDepartmentDAO) getHibernateTemplate().get(CourseDepartmentDAO.class, code);
	}
	
	/**
	 * 
	 */
	public void save(CourseDepartmentDAO departmentDao) {
		getHibernateTemplate().save(departmentDao).toString();
	}
	
	/**
	 * 
	 */
	public CourseSubunitDAO findSubunitByCode(String code) {
		return (CourseSubunitDAO) getHibernateTemplate().get(CourseSubunitDAO.class, code);
	}
	
	/**
	 * 
	 */
	public void save(CourseSubunitDAO subunitDao) {
		getHibernateTemplate().save(subunitDao).toString();
	}
	
	/**
	 * 
	 */
	public CourseOucsDepartmentDAO findOucsDeptByCode(String code) {
		return (CourseOucsDepartmentDAO) getHibernateTemplate().get(CourseOucsDepartmentDAO.class, code);
	}
	
	/**
	 * select departmentCode from course_subunit left join course_oucs_department on t2Char = subunitCode where oucsCode = 'histfac'
	 */
	@SuppressWarnings("unchecked")
	public CourseDepartmentDAO findDepartmentByPrimaryOrgUnit(final String primaryOrgUnit) {
		
		List<Object> results = getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select * from course_department " +
						"left join course_subunit on course_subunit.departmentCode = course_department.code " +
						"left join course_oucs_department on t2Char = subunitCode " +
						"where oucsCode = :oucsDept").addEntity(CourseDepartmentDAO.class);
				query.setString("oucsDept", primaryOrgUnit);
				return query.list();
			}
		});
		if (!results.isEmpty()) {
			return (CourseDepartmentDAO)results.get(0);
		}
		return null;
	}
	
	/**
	 * 
	 */
	public void save(CourseOucsDepartmentDAO oucsDao) {
		getHibernateTemplate().save(oucsDao);
	}
	
	public void remove(CourseSignupDAO existingSignup) {
		getHibernateTemplate().delete(existingSignup);
	}

	/**
	 * Used by tests to simulate another request being made.
	 */
	public void flushAndClear() {
		getHibernateTemplate().flush();
		getHibernateTemplate().clear();
	}

	@SuppressWarnings("unchecked")
	public CourseUserPlacementDAO findUserPlacement(final String userId) {
		List<Object> results = getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				Query query = session.createSQLQuery(
						"select * from course_user_placement " +
						"where userId = :userId").addEntity(CourseUserPlacementDAO.class);
				query.setString("userId", userId);
				return query.list();
			}
		});
		if (!results.isEmpty()) {
			return (CourseUserPlacementDAO)results.get(0);
		}
		return null;
	}

	public void save(CourseUserPlacementDAO placementDao) {
		getHibernateTemplate().save(placementDao).toString();
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<CourseComponentDAO> findCourseGroupsByCalendar(final boolean external, final String providerId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				StringBuffer querySQL = new StringBuffer();
				querySQL.append("select distinct * from course_component cc ");
				querySQL.append("left join course_group_component cgc on cgc.courseComponentMuid = cc.muid ");
				querySQL.append("left join course_group cg on cgc.courseGroupMuid = cg.muid ");
				querySQL.append("where cc.starts > NOW() and cg.hideGroup = false ");
				querySQL.append("and cg.visibility != 'PR' ");
				if (external) {
					querySQL.append("and cg.visibility != 'RS' ");
				}
				Query query = session.createSQLQuery(querySQL.toString()).addEntity(CourseComponentDAO.class);
				return query.list();
			}
		});
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public List<CourseComponentDAO> findCourseGroupsByNoDates(final boolean external, String providerId) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				StringBuffer querySQL = new StringBuffer();
				querySQL.append("select distinct * from course_component cc ");
				querySQL.append("left join course_group_component cgc on cgc.courseComponentMuid = cc.muid ");
				querySQL.append("left join course_group cg on cgc.courseGroupMuid = cg.muid ");
				querySQL.append("where cc.starts is NULL and ");
				querySQL.append("(cc.baseDate > NOW() or (cc.baseDate is null and cc.startsText is not null)) and ");
				querySQL.append("cg.hideGroup = false ");
				querySQL.append("and cg.visibility != 'PR' ");
				if (external) {
					querySQL.append("and cg.visibility != 'RS' ");
				}
				Query query = session.createSQLQuery(querySQL.toString()).addEntity(CourseComponentDAO.class);
				return query.list();
			}
		});
	}
	
	/**
	 * 
	 */
	public int flagSelectedCourseGroups(final String source) {
		return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				StringBuffer querySQL = new StringBuffer();
				querySQL.append("update course_group ");
				querySQL.append("set deleted = true ");
				querySQL.append("where source = :source");
				Query query = session.createSQLQuery(querySQL.toString()).setString("source", source);
				return query.executeUpdate();
			}
		});
	}
	
	/**
	 * 
	 */
	public int flagSelectedCourseComponents(final String source) {
		return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				StringBuffer querySQL = new StringBuffer();
				querySQL.append("update course_component ");
				querySQL.append("set deleted = true ");
				querySQL.append("where source = :source");
				Query query = session.createSQLQuery(querySQL.toString()).setString("source", source);
				return query.executeUpdate();
			}
		});
	}

	/**
	 * @{inhertDoc}
	 */
	public int flagSelectedDaisyCourseGroups(final String source, final Date now) {
		return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				// This is done with a subselect so that it's not tied to MySQL.
				// We need the inner select as MySQL doesn't let you select from the table you are updating.
				String sql = "update course_group cg "+
						"set cg.deleted = true "+
						"where cg.source = :source and " +
						"cg.muid in (select muid from ( " +
								"select cg1.muid as muid from course_group cg1 " +
								"left outer join course_signup as cs on cg1.muid = cs.courseGroupMuid "+
								"left outer join course_group_component cgc on cg1.muid = cgc.courseGroupMuid " +
								"left outer join course_component cc on cgc.courseComponentMuid = cc.muid " +
								"where cc.baseDate > :now " +
								"group by cg1.muid " +
								"having count(cs.id) = 0 " +
						") wrapper )";

				Query query = session.createSQLQuery(sql)
						.setString("source", source)
						.setDate("now", now);
				return query.executeUpdate();

			}
		});
	}

	/**
	 * @{inhertDoc}
	 */
	public int flagSelectedDaisyCourseComponents(final String source, final Date now) {
		return (Integer) getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) {
				// This is done with a subselect so that it's not tied to MySQL.
				// We need the inner select as MySQL doesn't let you select from the table you are updating.
				String sql = "update course_component cc "+
						"set cc.deleted = true " +
						"where cc.source = :source and " +
						"cc.muid in (select muid from ( " +
								"select cc1.muid as muid from course_component cc1 " +
								"left outer join course_component_signup ccs on cc1.muid = ccs.courseComponentMuid "+
								"left outer join course_signup cs on ccs.signup = cs.id " +
								"where cc1.baseDate > :now " +
								"group by cc1.muid " +
								"having count(cs.id) = 0 " +
						") wrapper )";
				Query query = session.createSQLQuery(sql)
						.setString("source", source)
						.setDate("now", now);
				return query.executeUpdate();
			}
		});
	}
	
	/**
	 * Hibernate handles the link between groups and components only one direction.
	 * We need to look after removing groups from the component
	 */
	@SuppressWarnings("unchecked")
	public Collection<CourseGroupDAO> deleteSelectedCourseGroups(final String source) {
		return (Collection<CourseGroupDAO>)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) 
					throws HibernateException,	SQLException {
				
				Criteria criteria = session.createCriteria(CourseGroupDAO.class);
				criteria.add(Restrictions.eq("deleted", true));
				criteria.add(Restrictions.eq("source", source));
				List<CourseGroupDAO> groupDaos =  criteria.list();
				for (CourseGroupDAO groupDao : groupDaos) {
					for (CourseComponentDAO component : groupDao.getComponents()) {
						component.getGroups().remove(groupDao);
					}
					session.delete(groupDao);
				}
				return groupDaos;
			}
		});
		
	}

	@SuppressWarnings("unchecked")
	public Collection<CourseComponentDAO> deleteSelectedCourseComponents(final String source) {
		return (Collection<CourseComponentDAO>)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) 
					throws HibernateException,	SQLException {
				
				Criteria criteria = session.createCriteria(CourseComponentDAO.class);
				criteria.add(Restrictions.eq("deleted", true));
				criteria.add(Restrictions.eq("source", source));
				List<CourseComponentDAO> componentDaos =  criteria.list();
				for (CourseComponentDAO componentDao : componentDaos) {
					session.delete(componentDao);
				}
				return componentDaos;
			}
		});
	}

	public CourseCategoryDAO findCourseCategory(final String id) {
		
		return (CourseCategoryDAO)getHibernateTemplate().execute(new HibernateCallback() {
			public CourseCategoryDAO doInHibernate(Session session) {
				return (CourseCategoryDAO)session.get(CourseCategoryDAO.class, id);
			}
		});
	}

	public void save(final CourseCategoryDAO category) {
		/*
		 * Ok so the problem is that there maybe multiple importers running at the same time and they both
		 * want to insert the same category in different transactions. Because you can't do an atomic SELECT
		 * then INSERT in SQL you can end up with 2 transactions trying to do the insert. This is made more
		 * likely by hibernate batching up all the inserts/updates. As the import is run in one big transaction
		 * we don't want to get an exception partway through it.
		 *
		 * We can't alter hibernate to do this insert as using IGNORE means the number of rows affected doesn't
		 * match so hibernate throws an exception and we can't use INSERT ON DUPLICATE UPDATE as that then
		 * returns 2 (or 0) rows affected which again causes hibernate to throw an exception.
		 *
		 * We can't use a REPLACE as when the row exists it doesn't do an update it actually does a DELETE followed
		 * by an INSERT which causes all the foreign key constraints to break and the REPLACE command to fail.
		 *
		 * So we just do the INSERT manually in SQL and ignore the number of rows affected. As INSERT IGNORE
		 * isn't available on H2 we use a JOIN to do an insert if it doesn't already exist only then insert it.
		 * http://www.xaprb.com/blog/2005/09/25/insert-if-not-exists-queries-in-mysql/
		 *
		 * Categories should never be updated and they are marked as such in the hbm file.
		 */
		getHibernateTemplate().execute(new HibernateCallback() {
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createSQLQuery(
						"INSERT INTO course_category (categoryId, categoryName, categoryType) "+
						"SELECT ?, ?, ? " +
						"FROM (SELECT 1 AS i) mutex " +
						"    LEFT OUTER JOIN course_category ON course_category.categoryId = ? " +
						"WHERE mutex.i = 1 AND course_category.categoryId IS NULL");
				query.setString(0, category.getCategoryId());
				query.setString(1, category.getCategoryName());
				query.setString(2, category.getCategoryType());
				query.setString(3, category.getCategoryId());
				query.executeUpdate();
				// This puts the value into the hibernate session.
				findCourseCategory(category.getCategoryId());
				return null;
			}
		});

	}

	public void setFlushMode(int i) {
		getHibernateTemplate().setFlushMode(i);
	}

}


