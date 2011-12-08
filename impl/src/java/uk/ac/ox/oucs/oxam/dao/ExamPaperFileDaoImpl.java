package uk.ac.ox.oucs.oxam.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;

import uk.ac.ox.oucs.oxam.model.ExamPaperFile;

public class ExamPaperFileDaoImpl extends BaseDao implements ExamPaperFileDao {

	private RowMapper mapper = new RowMapper() {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ExamPaperFile examPaperFile = new ExamPaperFile();
			examPaperFile.setId(rs.getLong("id"));
			examPaperFile.setExam(rs.getLong("exam"));
			examPaperFile.setPaper(rs.getLong("paper"));
			examPaperFile.setTerm(rs.getString("term"));
			examPaperFile.setYear(rs.getInt("academic_year"));
			examPaperFile.setFile(rs.getString("paper_file"));
			return examPaperFile;
		}
	};

	@Override
	protected String getStatementPrefix() {
		return "exampaperfile";
	}
	
	@Override
	public void init() {
		super.init();
	}

	public ExamPaperFile get(long id) {
		return (ExamPaperFile) getJdbcTemplate().queryForObject(
				getStatement("select.one"), new Object[] { id }, mapper);
	}
	
	public void delete(long id) {
		getJdbcTemplate().update(getStatement("delete"), new Object[]{id});
	}
	
	public void save(final ExamPaperFile examPaperFile) throws RuntimeException {
		if (examPaperFile.getId() != 0) {
			getJdbcTemplate().update(
					getStatement("update"),
					new Object[] { examPaperFile.getFile(), examPaperFile.getTerm(), examPaperFile.getId()});
		} else {
			Long id = (Long) getJdbcTemplate().execute(
					new SimpleStatementCreator() {
						public PreparedStatement createPreparedStatement(
								Connection con) throws SQLException {
							PreparedStatement stmt = con.prepareStatement(
									getSql(),
									Statement.RETURN_GENERATED_KEYS);
							setParam(stmt, 1, examPaperFile.getExam());
							setParam(stmt, 2, examPaperFile.getPaper());
							setParam(stmt, 3, examPaperFile.getFile());
							setParam(stmt, 4, examPaperFile.getYear());
							setParam(stmt, 5, examPaperFile.getTerm());
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
				examPaperFile.setId(id);
			} else {
				throw new IllegalStateException("Failed to generate key");
			}
		}
	}


}
