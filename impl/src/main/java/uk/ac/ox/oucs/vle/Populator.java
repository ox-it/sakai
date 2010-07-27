package uk.ac.ox.oucs.vle;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.vle.proxy.SakaiProxy;
import uk.ac.ox.oucs.vle.proxy.User;

/**
 * The idea is this class populates the course group rows.
 * 
 * @author buckett
 * 
 */
public class Populator {

	private static final Log log = LogFactory.getLog(Populator.class);
	
	/**
	 * The source to pull the data from.
	 */
	private DataSource ds;
	
	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	
	/**
	 * The proxy for getting users.
	 */
	private SakaiProxy proxy;
	
	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}

	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}

	public void setProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}

	public void update() {
		Connection con = null;
		int groupSeen = 0, groupUpdated = 0, groupCreated = 0;
		int componentSeen = 0, componentUpdated = 0, componentCreated = 0;
		try {
			con = ds.getConnection();
			Statement st = con.createStatement();
			// Import all course groups (from assessment units)
			ResultSet rs = st.executeQuery("SELECT distinct au.unit_id, au.code, au.title title, au.department_code, au.description FROM Assessment_Unit au INNER JOIN Teaching_Component tc ON ( tc.assessment_unit_code LIKE concat(concat('%',au.code),'%') AND length(tc.assessment_unit_code) > 0)");
			while(rs.next()) {
				groupSeen++;
				String code = rs.getString("code");
				String title = rs.getString("title");
				String departmentCode = rs.getString("department_code");
				String description = rs.getString("description");
				CourseGroupDAO groupDao = dao.findCourseGroupById(code);
				if (groupDao == null) {
					groupCreated++;
					groupDao = dao.newCourseGroup(code, title, departmentCode);
				} else {
					groupUpdated++;
					groupDao.setDept(departmentCode);
					groupDao.setTitle(title);
				}
				groupDao.getProperties().put("desc", description);
				dao.save(groupDao);
			}
			// Now import all the course components ( from teaching (instances/components))
			String sql = "SELECT\n" + 
					"  ti.teaching_instance_id, ti.open_date, ti.close_date, ti.sessions, ti.session_dates, ti.capacity,\n" + 
					"  tc.assessment_unit_code as assessment_unit_codes, tc.bookable, tc.teaching_component_id,\n" + 
					"  l.label location,\n" + 
					"  t.code as term_code, t.label as term_name,\n" + 
					"  c.title,\n" + 
					"  admin.webauth_id as admin,\n" + 
					"  teacher.webauth_id as teacher\n" + 
					"\n" + 
					"FROM\n" + 
					"  Teaching_Instance ti\n" + 
					"  LEFT JOIN Teaching_Component tc ON tc.teaching_component_id = ti.teaching_component_id\n" + 
					"  LEFT JOIN Term t ON ti.term_code = t.code\n" + 
					"  LEFT JOIN Location l ON ti.location_id = l.id\n" + 
					"  LEFT JOIN Component_Type c ON tc.component_type = c.component_type_id\n" + 
					"  LEFT JOIN Staff_OpenDoor admin ON tc.course_administrator = admin.employee_number\n" + 
					"  LEFT JOIN Staff_OpenDoor teacher ON ti.employee_number = teacher.employee_number\n";
			rs = st.executeQuery(sql);
			while(rs.next()) {
				componentSeen++;
				String id = rs.getString("teaching_instance_id");
				Date openDate = getDate(rs, "open_date");
				if (openDate == null) { 
					logFailure(id, "No open date set");
					continue;
				}
				Date closeDate = getDate(rs, "close_date");
				if (closeDate == null) {
					logFailure(id, "No close date set");
					continue;
				}
				int capacity = rs.getInt("capacity");
				if (capacity < 1) {
					logFailure(id, "Capacity isn't set or is zero");
					continue;
				}
				String adminEid = rs.getString("admin");
				if (adminEid == null || adminEid.trim().length() == 0) {
					logFailure(id, "No admin user set.");
					continue;
				}
				User admin = proxy.findUserByEid(adminEid);
				if (admin == null) {
					logFailure(id, "Failed to find admin with id: "+ adminEid);
					continue;
				}
				String teacherEid = rs.getString("teacher");
				if (teacherEid == null || teacherEid.trim().length() == 0) {
					logFailure(id, "No teachering user set.");
					continue;
				}
				User teacher = proxy.findUserByEid(teacherEid);
				if(teacher == null) {
					logFailure(id, "Failed to find teacher with id: "+ teacherEid);
				}
				String title = rs.getString("title");
				if (title == null || title.trim().length() == 0) {
					logFailure(id, "Title isn't set.");
					continue;
				}
				String termCode = rs.getString("term_code");
				if (termCode == null || termCode.trim().length() == 0) {
					logFailure(id, "Term code can't be empty");
					continue;
				}
				String termName = rs.getString("term_name");
				if (termName == null || termName.trim().length() == 0) {
					logFailure(id, "Term name can't be empty");
					continue;
				}
				
				String teachingComponentId = rs.getString("teaching_component_id");
				if (teachingComponentId == null || teachingComponentId.trim().length()==0) {
					logFailure(id, "No teaching component ID found.");
					continue;
				}

				String assessmentUnitCodes = rs.getString("assessment_unit_codes");
				if (assessmentUnitCodes == null || assessmentUnitCodes.trim().length() == 0) {
					logFailure(id, "No assessment unit codes set.");
					continue;
				}
				
				// Now find the component.
				boolean created = false;
				CourseComponentDAO componentDao = dao.findCourseComponent(id);
				if (componentDao == null) {
					componentDao = dao.newCourseComponent(id);
					created = true;
				}
				componentDao.setTitle(title);
				componentDao.setAdministrator(admin.getId());
				componentDao.setOpens(openDate);
				componentDao.setCloses(closeDate);
				componentDao.setSize(capacity);
				componentDao.setTermcode(termCode);
				componentDao.setComponentId(teachingComponentId+":"+termCode);
				
				// Cleanout existing ones.
				componentDao.setGroups(new HashSet<CourseGroupDAO>());
				// For each assessment unit we'll need a link.
				for(String assessmentUnitCode: assessmentUnitCodes.split(",")) {
					assessmentUnitCode = assessmentUnitCode.trim();
					CourseGroupDAO groupDao = dao.findCourseGroupById(assessmentUnitCode);
					if (groupDao == null) {
						logWarning(id, "Couldn't find course group of: "+ assessmentUnitCode);
						continue;
					}
					componentDao.getGroups().add(groupDao);
				}
				if (componentDao.getGroups().isEmpty()) {
					logFailure(id, "Isn't part of any groups.");
					continue;
				}
				if (created) {
					componentCreated++;
				} else {
					componentUpdated++;
				}
				
				dao.save(componentDao);
			}
		} catch (SQLException e) {
			log.warn("Problem importing.", e);
		} finally {
			log.info("CourseGroups (seen: "+ groupSeen+ " created: "+ groupCreated+ ", updated: "+ groupUpdated+")");
			log.info("CourseComponents (seen: "+ componentSeen+ " created: "+ componentCreated+ ", updated: "+ componentUpdated+")");
			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqle) {
				}
			}
		}

	}

	/**
	 * Gets a date out of a result set.
	 * This catches dates of the format 0000-00-00.
	 * @param rs The ResultSet.
	 * @param column The column to use.
	 * @return The date or <code>null</code> if the date can't be represented.
	 */
	private Date getDate(ResultSet rs, String column) {
		try {
			return rs.getDate(column);
		} catch (SQLException sqle) {
			return null;
		}
	}
	
	private void logFailure(String id, String reason) {
		log.warn("Import failed for "+ id+ " because: "+ reason);
	}
	
	private void logWarning(String id, String reason) {
		log.warn("Import issue for "+ id+ " because: "+ reason);
	}
}
