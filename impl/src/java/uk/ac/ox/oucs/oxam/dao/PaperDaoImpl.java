package uk.ac.ox.oucs.oxam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import uk.ac.ox.oucs.oxam.logic.SakaiProxy;
import uk.ac.ox.oucs.oxam.model.Exam;
import uk.ac.ox.oucs.oxam.model.Paper;

public class PaperDaoImpl extends BaseDao implements PaperDao {

	private static final Log LOG = LogFactory.getLog(PaperDaoImpl.class);

	private RowMapper mapper = new RowMapper() {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Paper paper = new Paper(rs.getLong("id"), rs.getString("code"), rs.getInt("academic_year"));
			paper.setTitle(rs.getString("title"));
			return paper;
		}
	};

	public Paper getPaper(long id) {
		return (Paper) getJdbcTemplate().queryForObject(
				getStatement("select.one"),
				new Object[] { id }, mapper);
	}

	public Paper get(String code, int year) {
		// Only one due to unique index.
		return (Paper) getJdbcTemplate().queryForObject(
				getStatement("select.code.year"),
				new Object[] { code, year }, mapper);
	}

	public void savePaper(final Paper paper) throws RuntimeException {
		if (paper.getId() != 0) {
			getJdbcTemplate().update(
					getStatement("update"),
					new Object[] { paper.getTitle(), paper.getCode(), paper.getYear(), paper.getId() });
		} else {
			Long id = (Long) getJdbcTemplate().execute(
					new SimpleStatementCreator() {
						public PreparedStatement createPreparedStatement(
								Connection con) throws SQLException {
							PreparedStatement stmt = con.prepareStatement(
									getSql(),
									Statement.RETURN_GENERATED_KEYS);
							setParam(stmt, 1, paper.getTitle());
							setParam(stmt, 2, paper.getCode());
							setParam(stmt, 3, paper.getYear());
							return stmt;
						}

						public String getSql() {
							return getStatement("insert");
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

	@Override
	protected String getStatementPrefix() {
		return "paper";
	}
	
	public Map<String, Paper> getCodes(String[] codes) {
		String stmt = getStatement("codes.begin");
		stmt = stmt+ StringUtils.repeat("?", ", ", codes.length);
		stmt = stmt + getStatement("codes.end");
		final Map<String, Paper> resolved = new HashMap<String, Paper>();
		List<Paper> papers = getJdbcTemplate().query(stmt, codes, mapper);
		for (Paper paper: papers) {
			resolved.put(paper.getCode(), paper);
		}
		return resolved;	}

}
