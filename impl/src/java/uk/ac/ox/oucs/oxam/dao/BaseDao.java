package uk.ac.ox.oucs.oxam.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import uk.ac.ox.oucs.oxam.logic.SakaiProxy;

public abstract class BaseDao extends JdbcDaoSupport {

	private static final Log LOG = LogFactory.getLog(BaseDao.class);
	
	protected SakaiProxy proxy;
	protected boolean ddl = true;
	private StatementStore statements;

	public void setSakaiProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}

	public void setStatementStore(StatementStore statementStore) {
		this.statements = statementStore;
	}

	public void init() {
		if (proxy != null) {
			ddl = proxy.getConfigParam("auto.ddl", true);
		}
	
		if (ddl) {
			createTable();
			createIndex("index");

		}
	}

	protected void createTable() {
		String createSql = getStatement("create");
		try {
			getJdbcTemplate().execute(createSql);
		} catch (BadSqlGrammarException bsge) {
			// MySQL Table exists.
			if (1050 != bsge.getSQLException().getErrorCode()) { 
				throw new RuntimeException("SQL is incorrect.",
						bsge);
			}
		} catch (DataAccessException dae) {
			throw dae;
		}
	}

	protected void createIndex(String index) {
		try {
			String indexSql = getStatement(index);
			getJdbcTemplate().execute(indexSql);
		} catch (BadSqlGrammarException bsge) {
			// MySQL duplicate index name.
			if (1061 != bsge.getSQLException().getErrorCode()) {
				throw new RuntimeException("SQL is incorrect.",
						bsge);
			}
		} catch (DataAccessException dae) {
			throw dae;
		} catch (IllegalArgumentException iae) {
			LOG.debug("No index creation statement: "+ iae.getMessage());
		}
	}

	protected String getStatement(String stmt) {
		return statements.getStatement(getStatementPrefix()+ "."+ stmt);
	}
	
	/**
	 * This is used by multiple DAOs so it's in the base class.
	 * @param codes
	 * @param stmt
	 * @return
	 */
	protected Map<String, String> resolveCodes(String[] codes, String stmt) {
		final Map<String, String> resolved = new HashMap<String, String>();
		getJdbcTemplate().query(stmt, codes, new RowCallbackHandler() {
			
			public void processRow(ResultSet rs) throws SQLException {
				String code = rs.getString(1);
				String title = rs.getString(2);
				resolved.put(code, title);
			}

		});
		return resolved;
	}

	abstract protected String getStatementPrefix();

}
