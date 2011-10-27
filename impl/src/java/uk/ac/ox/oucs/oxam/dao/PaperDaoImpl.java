package uk.ac.ox.oucs.oxam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import pom.logic.SakaiProxy;
import uk.ac.ox.oucs.oxam.model.Paper;

public class PaperDaoImpl extends JdbcDaoSupport implements PaperDao {

	private static final Log LOG = LogFactory.getLog(PaperDaoImpl.class);

	private RowMapper mapper = new RowMapper() {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Paper paper = new Paper();
			paper.setId(rs.getLong("id"));
			paper.setTitle(rs.getString("title"));
			paper.setCode(rs.getString("code"));
			paper.setFile(rs.getString("file"));
			paper.setActive(rs.getBoolean("active"));
			return paper;
		}

	};

	private String vendor;
	private SakaiProxy proxy;
	private boolean ddl = true;
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
			String createSql = statements.getStatement("paper.create");
			try {
				getJdbcTemplate().execute(createSql);
			} catch (BadSqlGrammarException bsge) {
				throw new RuntimeException("SQL is incorrect: " + createSql,
						bsge);
			} catch (DataAccessException dae) {
				throw dae;
			}
		}
	}

	public Paper getPaper(long id) {
		return (Paper) getJdbcTemplate().queryForObject(
				statements.getStatement("paper.select.one"),
				new Object[] { id }, mapper);
	}

	public List<Paper> getPapers(int start, int length) {
		return (List<Paper>) getJdbcTemplate().query(
				statements.getStatement("paper.select.range"),
				new Object[] { start, length }, mapper);
	}

	public void savePaper(final Paper paper) throws RuntimeException {
		if (paper.getId() != 0) {
			getJdbcTemplate().update(
					statements.getStatement("paper.update"),
					new Object[] { paper.getTitle(), paper.getCode(),
							paper.getFile(), paper.isActive(), paper.getId() });
		} else {
			Long id = (Long) getJdbcTemplate().execute(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(
								Connection con) throws SQLException {
							PreparedStatement stmt = con.prepareStatement(
									statements.getStatement("paper.insert"),
									Statement.RETURN_GENERATED_KEYS);
							stmt.setString(1, paper.getTitle());
							stmt.setString(2, paper.getCode());
							stmt.setString(3, paper.getFile());
							stmt.setBoolean(4, paper.isActive());
							return stmt;
						}
					}, new PreparedStatementCallback() {

						public Object doInPreparedStatement(PreparedStatement ps)
								throws SQLException, DataAccessException {
							ps.execute();
							ResultSet rs = ps.getGeneratedKeys();
							if (rs.next()) {
								return rs.getLong(1);
							}
							return null;
						}
					});
			if (id != null) {
				paper.setId(id);
			} else {
				throw new IllegalStateException("Failed to generate key");
			}
		}
	}

}
