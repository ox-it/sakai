package uk.ac.ox.oucs.vle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Simple data loader that takes a set of SQL statements and runs them against a DB.
 * Uses a hibernate session to connect to the DB.
 * @author buckett
 *
 */
public class SampleDataLoader {
	
	private static final Log log = LogFactory.getLog(SampleDataLoader.class);

	private SessionFactory factory;
	private String dataFile = "/test-data.sql";

	public void setSessionFactory(SessionFactory factory) {
		this.factory = factory;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public void init() throws Exception {

		Session session = factory.openSession();
		String sql = "";
		try {
			InputStream stream = getClass().getResourceAsStream(
					dataFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream));
			StringBuilder buffer = new StringBuilder();
			String line = null;
			int semicolon;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
				semicolon = buffer.indexOf(";");
				if (semicolon > 0) {
					sql = buffer.substring(0, semicolon);
					buffer.delete(0, semicolon + 1);
					session.createSQLQuery(sql).executeUpdate();
				}
			}
		} catch (Exception e) {
			log.warn("Problem while executing statement: "+ sql);
			throw e;
		} finally {
			session.close();
		}
	}

}
