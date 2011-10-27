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
import uk.ac.ox.oucs.oxam.model.ExamPaper;

public class ExamPaperDaoImpl extends JdbcDaoSupport implements ExamPaperDao {

	private static final Log LOG = LogFactory.getLog(ExamPaperDaoImpl.class);

	private RowMapper mapper = new RowMapper() {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ExamPaper examPaper = new ExamPaper();
			examPaper.setId(rs.getLong("id"));
			examPaper.setCategory(rs.getLong("category"));
			examPaper.setExamTitle(rs.getString("exam_title"));
			examPaper.setExamCode(rs.getString("exam_code"));
			examPaper.setPaperTitle(rs.getString("paper_title"));
			examPaper.setPaperCode(rs.getString("paper_code"));
			examPaper.setPaperFile(rs.getString("paper_file"));
			examPaper.setYear(rs.getInt("academic_year"));
			examPaper.setTerm(rs.getInt("term"));
			return examPaper;
		}

	};

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
			String createSql = statements.getStatement("exampaper.create");
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

	public ExamPaper getExamPaper(long id) {
		return (ExamPaper) getJdbcTemplate().queryForObject(
				statements.getStatement("exampaper.select.one"),
				new Object[] { id }, mapper);
	}

	public List<ExamPaper> getExamPapers(int start, int length) {
		return (List<ExamPaper>) getJdbcTemplate().query(
				statements.getStatement("exampaper.select.range"),
				new Object[] { start, length }, mapper);
	}

	public void saveExamPaper(final ExamPaper examPaper) throws RuntimeException {
		if (examPaper.getId() != 0) {
			getJdbcTemplate().update(
					statements.getStatement("exampaper.update"),
					new Object[] { examPaper.getCategory(), examPaper.getExamTitle(), examPaper.getExamCode(),
						examPaper.getPaperTitle(), examPaper.getPaperCode(), examPaper.getPaperFile(),
						examPaper.getYear(), examPaper.getTerm(), examPaper.getId()
					});
		} else {
			Long id = (Long) getJdbcTemplate().execute(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(
								Connection con) throws SQLException {
							PreparedStatement stmt = con.prepareStatement(
									statements.getStatement("exampaper.insert"),
									Statement.RETURN_GENERATED_KEYS);
							stmt.setLong(1, examPaper.getCategory());
							stmt.setString(2, examPaper.getExamTitle());
							stmt.setString(3, examPaper.getExamCode());
							stmt.setString(4, examPaper.getPaperTitle());
							stmt.setString(5, examPaper.getPaperCode());
							stmt.setString(6, examPaper.getPaperFile());;
							stmt.setInt(7, examPaper.getYear());
							stmt.setInt(8, examPaper.getTerm());
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
				examPaper.setId(id);
			} else {
				throw new IllegalStateException("Failed to generate key");
			}
		}
	}

}
