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

import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.Paper;

public class CategoryDaoImpl extends JdbcDaoSupport implements CategoryDao {

	private static final Log LOG = LogFactory.getLog(Category.class);

	private StatementStore statements;

	private RowMapper mapper = new RowMapper() {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Category category = new Category();
			category.setId(rs.getLong("id"));
			category.setCode(rs.getString("code"));
			category.setTitle(rs.getString("title"));
			return category;
		}

	};

	private SakaiProxy proxy;
	private boolean ddl = true;

	public void setSakaiProxy(SakaiProxy proxy) {
		this.proxy = proxy;
	}
	
	public void setStatementStore(StatementStore statements) {
		this.statements = statements;
	}

	public void init() {
		if (proxy != null) {
			ddl = proxy.getConfigParam("auto.ddl", true);
		}

		if (ddl) {
			String createSql = statements.getStatement("category.create");
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

	public Category getCategory(long id) {
		return (Category) getJdbcTemplate().queryForObject(
				statements.getStatement("category.select.one"),
				new Object[] { id }, mapper);
	}

	public List<Category> getCatagories(int start, int length) {
		return (List<Category>) getJdbcTemplate().query(
				statements.getStatement("category.select.range"),
				new Object[] { start, length }, mapper);
	}

	public void saveCategory(final Category category) {
		if (category.getId() != 0) {
			getJdbcTemplate().update(
					statements.getStatement("paper.update"),
					new Object[] { category.getTitle(), category.getCode(),
							category.getId() });
		} else {
			Long id = (Long) getJdbcTemplate().execute(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(
								Connection con) throws SQLException {
							PreparedStatement stmt = con.prepareStatement(
									statements.getStatement("paper.insert"),
									Statement.RETURN_GENERATED_KEYS);
							stmt.setString(1, category.getTitle());
							stmt.setString(2, category.getCode());
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
				category.setId(id);
			} else {
				throw new IllegalStateException("Failed to generate key");
			}
		}
	}

}
