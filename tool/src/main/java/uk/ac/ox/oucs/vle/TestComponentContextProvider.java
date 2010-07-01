package uk.ac.ox.oucs.vle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Provider
public class TestComponentContextProvider implements ContextResolver<Object> {

	private CourseSignupServiceImpl service;

	public synchronized void setupService() {

		try {
			Configuration cfg = new Configuration()
					.addInputStream(
							getClass().getResourceAsStream(
									"/uk/ac/ox/oucs/vle/Course.hbm.xml"))
					.setProperty("hibernate.dialect",
							"org.hibernate.dialect.MySQLInnoDBDialect")
					.setProperty("hibernate.connection.driver_class",
							"com.mysql.jdbc.Driver")
					.setProperty("hibernate.connection.url",
							"jdbc:mysql://127.0.0.1:3306/test")
					.setProperty("hibernate.show_sql", "true")
					.setProperty("hibernate.hbm2ddl.auto", "create-drop");
			SessionFactory factory = cfg.buildSessionFactory();

			service = new CourseSignupServiceImpl();
			CourseDAOImpl dao = new CourseDAOImpl();
			dao.setSessionFactory(factory);
			service.setDao(dao);
			loadTestData(factory);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		session.close();
	}
	

	public Object getContext(Class<?> type) {
		if (type.equals(CourseSignupService.class)) {
			if (service == null) {
				setupService();
			}
			return service;
		}
		return null;
	}

}
