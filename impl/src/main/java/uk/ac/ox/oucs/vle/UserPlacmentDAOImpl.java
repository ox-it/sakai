package uk.ac.ox.oucs.vle;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

/**
 * Created by buckett on 24/09/15.
 */
public class UserPlacmentDAOImpl extends HibernateDaoSupport implements UserPlacementDAO {

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

}
