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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import uk.ac.ox.oucs.oxam.model.Exam;

public class ExamDaoImpl extends BaseDao implements ExamDao {

	private RowMapper mapper = new RowMapper() {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			Exam exam = new Exam(rs.getLong("id"), rs.getString("code"), rs.getInt("academic_year"));
			exam.setCategory(rs.getString("category"));
			exam.setTitle(rs.getString("title"));
			return exam;
		}
	};
	
	public Exam getExam(long id) {
		return (Exam)getJdbcTemplate().queryForObject(getStatement("select.one"), new Object[]{id}, mapper);
	}
	
	public Exam getExam(String code, int year) {
		return (Exam)getJdbcTemplate().queryForObject(getStatement("select.code.year"), new Object[]{code, year}, mapper);
	}

	public void saveExam(final Exam exam) {
		if (exam.getId() != 0) {
			getJdbcTemplate().update(
					getStatement("update"),
					new Object[] { exam.getCategory(), exam.getTitle(), exam.getCode(),
						exam.getYear(), exam.getId()
					});
		} else {
			Long id = (Long) getJdbcTemplate().execute(
					new SimpleStatementCreator() {
						public PreparedStatement createPreparedStatement(
								Connection con) throws SQLException {
							PreparedStatement stmt = con.prepareStatement(
									getSql(),
									Statement.RETURN_GENERATED_KEYS);
							setParam(stmt, 1, exam.getCategory());
							setParam(stmt, 2, exam.getTitle());
							setParam(stmt, 3, exam.getCode());
							setParam(stmt, 4, exam.getYear());
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
				exam.setId(id);
			} else {
				throw new IllegalStateException("Failed to generate key");
			}
		}
	}
	
	public Map<String, Exam> getCodes(String[] codes) {
		String stmt = getStatement("codes.begin");
		stmt = stmt+ StringUtils.repeat("?", ", ", codes.length);
		stmt = stmt + getStatement("codes.end");
		final Map<String, Exam> resolved = new HashMap<String, Exam>();
		List<Exam> exams = getJdbcTemplate().query(stmt, codes, mapper);
		for (Exam exam: exams) {
			resolved.put(exam.getCode(), exam);
		}
		return resolved;
	}

	@Override
	protected String getStatementPrefix() {
		return "exam";
	}

}
