package uk.ac.ox.oucs.vle;

/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
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
	private String dataFile = "/sakai_test.dump"; //"/test-data.sql";

	public void setSessionFactory(SessionFactory factory) {
		this.factory = factory;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public void init() throws Exception {

		Session session = factory.openSession();
		String sql = "";
		boolean firstStatement = true;
		try {
			log.info("Loading sample data from :"+ dataFile);
			InputStream stream = this.getClass().getResourceAsStream(
					dataFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					stream));
			StringBuilder buffer = new StringBuilder();
			String line = null;
			int semicolon;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("--")) {
					continue;
				}
				buffer.append(line);
				semicolon = buffer.indexOf(";");
				if (semicolon > 0) {
					sql = buffer.substring(0, semicolon+1);
					buffer.delete(0, semicolon + 1);
					if (!sql.startsWith("/*")) {
						session.createSQLQuery(sql).executeUpdate();
						firstStatement=false;
					}
				}
			}
		} catch (HibernateException e) {
			if (firstStatement) {
				log.info("Not doing DDL as already done: "+ dataFile);
			} else {
				log.warn("Problem while executing statement: "+ sql);
				throw e;
			}
		} finally {
			session.close();
		}
	}

}
