package uk.ac.ox.oucs.vle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class TestCourseDAOImpl extends TestCase {

	private CourseDAOImpl courseDao;
	
	private final Date END_MIC_2010 = createDate(2010, 12, 4); 

	private SessionFactory factory;

	public void setUp() throws Exception {
		Configuration cfg = new Configuration()
			.addInputStream(getClass().getResourceAsStream("/uk/ac/ox/oucs/vle/Course.hbm.xml"))
			.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect")
			.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
			.setProperty("hibernate.connection.url", "jdbc:mysql://127.0.0.1:3306/test")
			.setProperty("hubernate.connection.username", "test")
			.setProperty("hibernate.show_sql", "true")
			.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		SessionFactory factory = cfg.buildSessionFactory();
		loadTestData(factory);
		
		courseDao = new CourseDAOImpl();
		courseDao.setSessionFactory(factory);
	}

	private static Date createDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day);
		return new Date(cal.getTimeInMillis());
	}
	
	private void loadTestData(SessionFactory factory) throws Exception {
		Session session = factory.openSession();
		InputStream stream = getClass().getResourceAsStream("/test-data.sql");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder buffer = new StringBuilder();
		String line = null;
		int semicolon;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
			semicolon = buffer.indexOf(";");
			if (semicolon > 0) {
				String sql = buffer.substring(0, semicolon);
				buffer.delete(0, semicolon+1);
				session.createSQLQuery(sql).executeUpdate();
			}
		}
	}
	
	public void testAvailableCourses() {
		CourseGroupDAO course = courseDao.findUpcomingComponents("course-1", END_MIC_2010);
		assertNotNull(course);
		assertNotNull(course.getComponents());
		assertEquals(2, course.getComponents().size());
	}
}
