package uk.ac.ox.oucs.oxam.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import uk.ac.ox.oucs.oxam.logic.Callback;
import uk.ac.ox.oucs.oxam.logic.CategoryService;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;

public class ExamPaperDaoImpl extends BaseDao implements ExamPaperDao {

	private static final Log LOG = LogFactory.getLog(ExamPaperDaoImpl.class);
	
	private TermService termService;
	private CategoryService categoryService;

	public void setTermService(TermService termService) {
		this.termService = termService;
	}

	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	private RowMapper mapper = new RowMapper() {

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			ExamPaper examPaper = new ExamPaper();
			examPaper.setId(rs.getLong("id"));
			examPaper.setExamId(rs.getLong("exam"));
			Category category = categoryService.getByCode(rs.getString("category"));
			examPaper.setCategory(category);
			examPaper.setExamTitle(rs.getString("exam_title"));
			examPaper.setExamCode(rs.getString("exam_code"));
			examPaper.setPaperId(rs.getLong("paper"));
			examPaper.setPaperTitle(rs.getString("paper_title"));
			examPaper.setPaperCode(rs.getString("paper_code"));
			examPaper.setPaperFile(rs.getString("paper_file"));
			examPaper.setYear(rs.getInt("academic_year"));
			Term term = termService.getByCode(rs.getString("term"));
			examPaper.setTerm(term);
			return examPaper;
		}

	};
	
	public void init() {
		// Don't need any DDL.
	}
	
	protected String getStatementPrefix() {
		return "exampaper";
	}

	public ExamPaper getExamPaper(long id) {
		return (ExamPaper) getJdbcTemplate().queryForObject(
				getStatement("select.one"),
				new Object[] { id }, mapper);
	}

	public List<ExamPaper> getExamPapers(int start, int length) {
		return (List<ExamPaper>) getJdbcTemplate().query(
				getStatement("select.range"),
				new Object[] { start, length }, mapper);
	}
	

	public int count() {
		String stmt = getStatement("count");
		return (Integer) getJdbcTemplate().queryForInt(stmt);
	}

	public void all(final Callback<ExamPaper> callback) {
		String stmt = getStatement("select.all");
		getJdbcTemplate().query(stmt, new RowCallbackHandler() {
			// We could use a preparedstatement and get the resultset to track this,
			// but doing it ourselves is much simpler.
			int row = 1;
			public void processRow(ResultSet rs) throws SQLException {
				ExamPaper examPaper = (ExamPaper) mapper.mapRow(rs, row++);
				callback.callback(examPaper);
			}
		});
	}
	
	public List<ExamPaper> find(ExamPaper example) {
		StringBuilder stmt = new StringBuilder(getStatement("select.example"));
		SQLBuilder sql = new SQLBuilder(getStatement("select.example"), " AND ");
		
		sql.addParam(getStatement("select.id"), example.getId());
		sql.addParam(getStatement("select.category"), example.getCategory());
		sql.addParam(getStatement("select.exam_title"), example.getExamTitle());
		sql.addParam(getStatement("select.exam_code"), example.getExamCode());
		sql.addParam(getStatement("select.paper_title"), example.getPaperTitle());
		sql.addParam(getStatement("select.paper_code"), example.getPaperCode());
		sql.addParam(getStatement("select.paper_file"), example.getPaperFile());
		sql.addParam(getStatement("select.year"), example.getYear());
		sql.addParam(getStatement("select.term"), example.getTerm());
		
		List<ExamPaper> examPapers = (List<ExamPaper>)getJdbcTemplate().query(sql.getStmt(), sql.getParams(), mapper);
		return examPapers;
	}
	
	private class SQLBuilder {
		StringBuilder stmt;
		String join;
		List<Object> params;
		boolean firstParam = true;
		
		SQLBuilder(String start, String join) {
			stmt = new StringBuilder(start);
			params = new ArrayList<Object>();
		}
		// This method work reasonably well, except int/long < 0 are like null. Watch out.		
		void addParam(String sql, Object value) {
			if (value == null) {
				return;
			}
			if (value instanceof Long || value instanceof Integer) {
				Long longValue = (Long)value;
				if (longValue < 0) {
					return;
				}
			}
			if (!firstParam) {
				stmt.append(join);
			}
			stmt.append(sql);
			params.add(value);
		}
		
		String getStmt() {
			return stmt.toString();
		}
		
		Object[] getParams() {
			return params.toArray();
		}
	}


}
