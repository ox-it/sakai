package uk.ac.ox.oucs.vle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class TestCourseDAOImpl extends TestCase {

	private CourseDAOImpl courseDao;

	public void setUp() throws Exception {
		Configuration cfg = new Configuration()
			.addInputStream(getClass().getResourceAsStream("/uk/ac/ox/oucs/vle/Course.hbm.xml"))
			.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect")
			.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver")
			.setProperty("hibernate.connection.url", "jdbc:hsqldb:mem:signup")
			.setProperty("hibernate.show_sql", "true")
			.setProperty("hibernate.hbm2ddl.auto", "create");
		SessionFactory factory = cfg.buildSessionFactory();
		loadTestData(factory);
		
		courseDao = new CourseDAOImpl();
		courseDao.setSessionFactory(factory);
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
		//
	}
}
