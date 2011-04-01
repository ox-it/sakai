package uk.ac.ox.oucs.vle;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The idea is this class populates the course group rows.
 * 
 * @author buckett
 * 
 */
public class PopulatorImpl implements Populator{

	private static final Log log = LogFactory.getLog(PopulatorImpl.class);
	
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

	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.vle.Populator#update()
	 */
	public void update() {
		Connection con = null;
		int groupSeen = 0, groupUpdated = 0, groupCreated = 0;
		int componentSeen = 0, componentUpdated = 0, componentCreated = 0;
		try {
			con = ds.getConnection();
			Statement st = con.createStatement();
			// Import all course groups (from assessment units)
			ResultSet rs = st.executeQuery(
					"SELECT DISTINCT au.id, au.assessment_unit_code, au.title, " +
					" au.department_code, Department.department_name, " +
					" au.sub_unit_code, SubUnit.sub_unit_name, " +
					" au.description, Employee.webauth_code " +
					" FROM AssessmentUnit au " + 
					" LEFT JOIN Employee ON au.course_administrator_employee_id = Employee.id " + 
					" INNER JOIN TeachingComponent tc ON tc.assessment_unit_id = au.id " + 
					" INNER JOIN Department ON au.department_code = Department.department_code " + 
					" LEFT JOIN SubUnit ON au.sub_unit_id = SubUnit.id;");
			while(rs.next()) {
				groupSeen++;
				String code = rs.getString("assessment_unit_code");
				String title = rs.getString("title");
				String departmentCode = rs.getString("department_code");
				String departmentName = rs.getString("department_name");
				String subunitCode = rs.getString("sub_unit_code");
				String subunitName = rs.getString("sub_unit_name");
				String description = rs.getString("description");
				String administrator = rs.getString("webauth_code");
				if (administrator == null || administrator.trim().length() == 0) {
					logFailure(null, code, "No administrator set");
					continue;
				}
				UserProxy user = proxy.findUserByEid(administrator); 
				if (user == null) {
					logFailure(null, code, "Failed to find administrator");
					continue;
				}
				CourseGroupDAO groupDao = dao.findCourseGroupById(code);
				boolean created = false;
				if (groupDao == null) {
					groupDao = dao.newCourseGroup(code, title, departmentCode, subunitCode);
					created = true;
				} else {
					groupDao.setDept(departmentCode);
					groupDao.setSubunit(subunitCode);
					groupDao.setTitle(title);
				}
				groupDao.setAdministrator(user.getId());
				groupDao.setDescription(description);
				groupDao.setDepartmentName(departmentName);
				groupDao.setSubunitName(subunitName);
				dao.save(groupDao);
				if (created) {
					groupCreated++;
				} else {
					groupUpdated++;
				}
			}
			
			// Now import all the course components ( from teaching (instances/components))
			String sql = "SELECT" + 
					" ti.id, ti.open_date, ti.close_date, ti.expiry_date, ti.start_date, ti.end_date, " +
					" ti.sessions, ti.session_dates, ti.teaching_capacity, ti.bookable, " + 
					" tc.assessment_unit_code, tc.id as teaching_component_id, tc.subject, " + 
					" Location.location_name, " + 
					" Term.term_code, Term.label, " + 
					" c.title, " + 
					" Employee.webauth_code, " +
					" concat_ws(' ', Employee.forename, Employee.surname) as teacher_name, " +
					" Employee.email as teacher_email " + 
					" FROM " + 
					" TeachingInstance ti " + 
					" LEFT JOIN TeachingComponent tc ON tc.id = ti.teaching_component_id " + 
					" LEFT JOIN Term ON ti.term_code = Term.term_code " + 
					" LEFT JOIN Location ON ti.location_id = Location.id " + 
					" LEFT JOIN TeachingComponentType c ON tc.teaching_component_type_id = c.id " + 
					" LEFT JOIN Employee ON ti.employee_id = Employee.id;";
			rs = st.executeQuery(sql);
			while(rs.next()) {
				componentSeen++;
				String bookableString = rs.getString("bookable");
				boolean bookable = bookableString == null || bookableString.trim().length() == 0 || bookableString.equals("TRUE");
				
				String id = rs.getString("id");
				String assessmentUnitCodes = rs.getString("assessment_unit_code");
				Date openDate = getDate(rs, "open_date");
				if (openDate == null) { 
					logFailure(assessmentUnitCodes, id, "No open date set");
					continue;
				}
				Date closeDate = getDate(rs, "close_date");
				if (closeDate == null) {
					logFailure(assessmentUnitCodes, id, "No close date set");
					continue;
				}
				Date expiryDate = getDate(rs, "expiry_date");
				if (expiryDate == null) {
					expiryDate = closeDate;
				}
				Date startDate = getDate(rs, "start_date");
				Date endDate = getDate(rs, "end_date");
				
				if (openDate.after(closeDate)){
					logFailure(assessmentUnitCodes, id, "Open date is after close date");
					continue;
				}
				if(expiryDate.before(closeDate)){
					logFailure(assessmentUnitCodes, id, "Expiry date is before close date");
					continue;
				}
				
				int capacity = rs.getInt("teaching_capacity");
				
				String subject = rs.getString("subject");
				if (subject == null || subject.trim().length() == 0) {
					logFailure(assessmentUnitCodes, id, "Subject isn't set.");
					continue;
				}
				String title = rs.getString("title");
				if (title == null || title.trim().length() == 0) {
					logFailure(assessmentUnitCodes, id, "Title isn't set.");
					continue;
				}
				String termCode = rs.getString("term_code");
				if (termCode == null || termCode.trim().length() == 0) {
					logFailure(assessmentUnitCodes, id, "Term code can't be empty");
					continue;
				}
				String termName = rs.getString("label");
				if (termName == null || termName.trim().length() == 0) {
					logFailure(assessmentUnitCodes, id, "Term name can't be empty");
					continue;
				}
				
				String teachingComponentId = rs.getString("teaching_component_id");
				if (teachingComponentId == null || teachingComponentId.trim().length()==0) {
					logFailure(assessmentUnitCodes, id, "No teaching component ID found.");
					continue;
				}

				//String assessmentUnitCodes = rs.getString("assessment_unit_codes");
				if (assessmentUnitCodes == null || assessmentUnitCodes.trim().length() == 0) {
					logFailure(assessmentUnitCodes, id, "No assessment unit codes set.");
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
				componentDao.setSubject(subject);
				componentDao.setOpens(openDate);
				componentDao.setCloses(closeDate);
				componentDao.setExpires(expiryDate);
				componentDao.setStarts(startDate);
				componentDao.setEnds(endDate);
				componentDao.setBookable(bookable);
				componentDao.setSize(capacity);
				componentDao.setTermcode(termCode);
				componentDao.setComponentId(teachingComponentId+":"+termCode);
				
				// Populate teacher details.
				// Look for details in WebLearn first then fallback to details in DAISY.
				String teacherName = null;
				String teacherEmail = null;
				String teacherId = rs.getString("webauth_code");
				if (teacherId != null && teacherId.length() > 0) {
					UserProxy teacher = proxy.findUserByEid(teacherId);
					if (teacher != null) {
						teacherName = teacher.getName();
						teacherEmail = teacher.getEmail();
					}
				}
				if (teacherName == null) {
					teacherName = rs.getString("teacher_name");
				}
				if (teacherName != null && teacherName.trim().length() > 0) {
					componentDao.setTeacherName(teacherName);
					if (teacherEmail == null) {
						teacherEmail = rs.getString("teacher_email");
					}
					if (teacherEmail != null && teacherName.trim().length() > 0) {
						componentDao.setTeacherEmail(teacherEmail);
					}
				}
				
				// Which term
				componentDao.setWhen(termName);
				
				// When they are happening.
				String sessionDates = rs.getString("session_dates");
				if (sessionDates != null && sessionDates.trim().length() > 0) {
					componentDao.setSlot(sessionDates);
				}
				
				// How many sessions
				String sessions = rs.getString("sessions");
				if (sessions != null && sessions.trim().length() > 0) {
					componentDao.setSessions(sessions);
				}
				
				// Where?
				String location = rs.getString("location_name");
				if (location != null && location.trim().length() > 0) {
					componentDao.setLocation(location);
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
					logFailure(assessmentUnitCodes, id, "Isn't part of any groups.");
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
	
	private void logFailure(String assessmentUnitCodes, String id, String reason) {
		if (null == assessmentUnitCodes) {
			log.warn("Import failed for "+ id+ " because: "+ reason);
		} else {
			log.warn("Import failed for "+ assessmentUnitCodes+":"+id+ " because: "+ reason);
		}
	}
	
	private void logWarning(String id, String reason) {
		log.warn("Import issue for "+ id+ " because: "+ reason);
	}

}
