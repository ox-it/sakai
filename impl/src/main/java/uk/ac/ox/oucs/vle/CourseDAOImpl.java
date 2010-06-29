package uk.ac.ox.oucs.vle;

import java.util.Date;
import java.util.List;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class CourseDAOImpl extends HibernateDaoSupport implements CourseDAO {

	public CourseGroupDAO findCourseGroupById(String courseId) {
		// TODO Auto-generated method stub
		return null;
	}

	public CourseGroupDAO findAvailableCourseGroupById(String courseId, Date available) {
		List<CourseGroupDAO> courseGroups = getHibernateTemplate().findByNamedParam(
				"from CourseGroup cg left join cg.sets.components as component where cg.id = :courseId and component.opens < :opens and component.closes > :closes",
				new String[]{"courseId", "opens", "closes"}, new Object[]{courseId, available, available});
		return (courseGroups.size() > 0)? courseGroups.get(0): null;
	}

	public List<CourseComponentDAO> findOpenComponents(String id, Date at) {
		// TODO Auto-generated method stub
		return null;
	}

	public CourseGroupDAO findAvailableCourseGroupById(String courseId) {
		// TODO Auto-generated method stub
		return null;
	}

}
