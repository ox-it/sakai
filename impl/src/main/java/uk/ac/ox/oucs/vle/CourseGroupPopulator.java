package uk.ac.ox.oucs.vle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The idea is this class populates the course group rows.
 * 
 * @author buckett
 * 
 */
public class CourseGroupPopulator {

	private static final Log log = LogFactory.getLog(CourseGroupPopulator.class);
	
	/**
	 * The source to pull the data from.
	 */
	private DataSource ds;
	
	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;

	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}

	public void update() {
		Connection con = null;
		int updated = 0;
		int created = 0;
		try {
			con = ds.getConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("SELECT distinct au.unit_id, au.code, au.title title, au.department_code, au.description FROM Assessment_Unit au INNER JOIN Teaching_Component tc ON ( tc.assessment_unit_code LIKE concat(concat('%',au.code),'%') AND length(tc.assessment_unit_code) > 0)");
			while(rs.next()) {
				String code = rs.getString("code");
				String title = rs.getString("title");
				String departmentCode = rs.getString("department_code");
				String description = rs.getString("description");
				CourseGroupDAO groupDao = dao.findCourseGroupById(code);
				if (groupDao == null) {
					created++;
					groupDao = dao.newCourseGroup(code, title, departmentCode);
				} else {
					updated++;
					groupDao.setDept(departmentCode);
					groupDao.setTitle(title);
				}
				groupDao.getProperties().put("desc", description);
				dao.save(groupDao);
			}
		} catch (SQLException e) {
			log.warn("Problem importing.", e);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
				}
			}
		}

	}
}
