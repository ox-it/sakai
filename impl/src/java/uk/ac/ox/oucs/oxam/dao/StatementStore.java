package uk.ac.ox.oucs.oxam.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pom.logic.SakaiProxy;

/**
 * Class to load SQL statements, statements can be overridden with vendor specific versions.
 * @author buckett
 *
 */
public class StatementStore {

	private static final Log LOG = LogFactory.getLog(StatementStore.class);

	private Properties statements;
	private String file = "/sql.properties";
	private String vendor;
	private SakaiProxy proxy;
	
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	
	public void setSakaiProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}

	public void init() {

		if (vendor == null && proxy != null) {
			vendor = proxy.getConfigParam(
					"vendor@org.sakaiproject.db.api.SqlService", null);
		}
		
		statements = new Properties();
		loadFile(file);
		if (vendor != null) {
			loadFile("/sql-"+vendor+".properties");
		}
		
	}

	public void loadFile(String file) {
		InputStream stream = null;
		try {
			stream = getClass().getResourceAsStream(file);
			if (stream != null) {
				try {
					statements.load(stream);
				} catch (IOException e) {
					throw new RuntimeException("Failed to load vendor file");
				}
			} else {
				LOG.info("Statement file not found: " + file);
			}
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ignore) {
				}
			}
		}
	}

	public String getStatement(String statement) {
		String sql = statements.getProperty(statement);
		if (sql == null) {
			throw new IllegalArgumentException("Couldn't find '"+statement+"' in statement store.");
		}
		return sql;
	}
}
