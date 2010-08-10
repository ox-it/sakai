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
import uk.ac.ox.oucs.vle.proxy.UserProxy;

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
			ResultSet rs = st.executeQuery(
					"SELECT distinct au.unit_id, au.code, au.title, au.department_code, d.name as department_name, au.description,\n" +
					"  admin.webauth_id as admin\n" +
					"FROM\n" + 
					"  Assessment_Unit au\n" + 
					"  LEFT JOIN Staff_OpenDoor admin ON au.course_administrator = admin.employee_number\n" + 
					"  INNER JOIN Teaching_Component tc ON\n" + 
					"  ( tc.assessment_unit_code LIKE concat(concat('%',au.code),'%') AND length(tc.assessment_unit_code) > 0)\n" + 
					"  INNER JOIN Department d ON au.department_code = d.code\n" + 
					";");
			while(rs.next()) {
				groupSeen++;
				String code = rs.getString("code");
				String title = rs.getString("title");
				String departmentCode = rs.getString("department_code");
				String description = rs.getString("description");
				String departmentName = rs.getString("department_name");
				String administrator = rs.getString("admin");
				if (administrator == null || administrator.trim().length() == 0) {
					logFailure(code, "No administrator set");
					continue;
				}
				UserProxy user = proxy.findUserByEid(administrator);
				if (user == null) {
					logFailure(code, "Failed to find administrator");
					continue;
				}
				CourseGroupDAO groupDao = dao.findCourseGroupById(code);
				boolean created = false;
				if (groupDao == null) {
					groupDao = dao.newCourseGroup(code, title, departmentCode);
					created = true;
				} else {
					groupDao.setDept(departmentCode);
					groupDao.setTitle(title);
				}
				groupDao.setAdministrator(user.getId());
				groupDao.getProperties().put("desc", description);
				groupDao.getProperties().put("department", departmentName);
				dao.save(groupDao);
				if (created) {
					groupCreated++;
				} else {
					groupUpdated++;
				}
			}
			// Now import all the course components ( from teaching (instances/components))
			String sql = "SELECT\n" + 
					"  ti.teaching_instance_id, ti.open_date, ti.close_date, ti.expiry_date, ti.sessions, ti.session_dates, ti.capacity, ti.bookable,\n" + 
					"  tc.assessment_unit_code as assessment_unit_codes, tc.teaching_component_id,\n" + 
					"  l.label location,\n" + 
					"  t.code as term_code, t.label as term_name,\n" + 
					"  c.title,\n" + 
					"  concat_ws(' ', teacher.forename, teacher.surname) as teacher_name, teacher.email as teacher_email\n" + 
					"\n" + 
					"FROM\n" + 
					"  Teaching_Instance ti\n" + 
					"  LEFT JOIN Teaching_Component tc ON tc.teaching_component_id = ti.teaching_component_id\n" + 
					"  LEFT JOIN Term t ON ti.term_code = t.code\n" + 
					"  LEFT JOIN Location l ON ti.location_id = l.id\n" + 
					"  LEFT JOIN Component_Type c ON tc.component_type = c.component_type_id\n" + 
					"  LEFT JOIN Staff_OpenDoor teacher ON ti.employee_number = teacher.employee_number\n";
			rs = st.executeQuery(sql);
			while(rs.next()) {
				componentSeen++;
				String bookableString = rs.getString("bookable");
				boolean bookable = bookableString == null || bookableString.trim().length() == 0 || bookableString.equals("TRUE");
				
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
				Date expiryDate = getDate(rs, "expiry_date");
				if (expiryDate == null) {
					expiryDate = closeDate;
				}
				if (openDate.after(closeDate)){
					logFailure(id, "Open date is after close date");
					continue;
				}
				if(expiryDate.before(closeDate)){
					logFailure(id, "Expiry date is before close date");
					continue;
				}
				int capacity = rs.getInt("capacity");
				if (bookable && capacity < 1) {
					logFailure(id, "Capacity isn't set or is zero");
					continue;
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
				componentDao.setOpens(openDate);
				componentDao.setCloses(closeDate);
				componentDao.setExpires(expiryDate);
				componentDao.setBookable(bookable);
				componentDao.setSize(capacity);
				componentDao.setTermcode(termCode);
				componentDao.setComponentId(teachingComponentId+":"+termCode);
				
				// Populate teacher details.
				String teacherName = rs.getString("teacher_name");
				if (teacherName != null && teacherName.trim().length() > 0) {
					componentDao.getProperties().put("teacher.name", teacherName);
					String teacherEmail = rs.getString("teacher_email");
					if (teacherEmail != null && teacherName.trim().length() > 0) {
						componentDao.getProperties().put("teacher.email", teacherEmail);
					}
				}
				
				// Which term
				componentDao.getProperties().put("when", termName);
				
				// When they are happening.
				String sessionDates = rs.getString("session_dates");
				if (sessionDates != null && sessionDates.trim().length() > 0) {
					componentDao.getProperties().put("slot", sessionDates);
				}
				
				// How many sessions
				String sessions = rs.getString("sessions");
				if (sessions != null && sessions.trim().length() > 0) {
					componentDao.getProperties().put("sessions", sessions);
				}
				
				// Where?
				String location = rs.getString("location");
				if (location != null && location.trim().length() > 0) {
					componentDao.getProperties().put("location", location);
				}
				
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
