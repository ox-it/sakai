package uk.ac.ox.oucs.vle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import junit.framework.TestCase;

public abstract class TestOnSampleData extends TestCase {

	SessionFactory factory;

	public TestOnSampleData() {
		super();
	}

	public TestOnSampleData(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		Configuration cfg = new Configuration()
			.addInputStream(getClass().getResourceAsStream("/uk/ac/ox/oucs/vle/Course.hbm.xml"))
			.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect")
			.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
			.setProperty("hibernate.connection.url", "jdbc:mysql://127.0.0.1:3306/test")
			.setProperty("hibernate.show_sql", "true")
			.setProperty("hibernate.hbm2ddl.auto", "create-drop");
		factory = cfg.buildSessionFactory();
		loadTestData(factory);
	}

	public void tearDown() throws Exception {
		factory.close();
	}

	void loadTestData(SessionFactory factory) throws Exception {
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

}