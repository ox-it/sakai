package uk.ac.ox.oucs.oxam.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import uk.ac.ox.oucs.oxam.logic.AcademicYearService;
import uk.ac.ox.oucs.oxam.logic.Callback;
import uk.ac.ox.oucs.oxam.logic.CategoryService;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;

public class ExamPaperDaoImpl extends BaseDao implements ExamPaperDao {

	private static final Log LOG = LogFactory.getLog(ExamPaperDaoImpl.class);
	
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private TermService termService;
	private CategoryService categoryService;
	private AcademicYearService academicYearService;

	public void setTermService(TermService termService) {
		this.termService = termService;
	}

	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	public void setAcademicYearService(AcademicYearService acadmicYearService) {
		this.academicYearService = acadmicYearService;
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
			examPaper.setYear(academicYearService.getAcademicYear(rs.getInt("academic_year")));
			Term term = termService.getByCode(rs.getString("term"));
			examPaper.setTerm(term);
			return examPaper;
		}

	};
	
	public void init() {
		// Don't need any DDL as the tables are created by other classes.
		// Can't override setDatasource, so do setup of namedparameter here.
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
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
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("start", start);
		params.put("length", length);
		return (List<ExamPaper>) namedParameterJdbcTemplate.query(getStatement("select.range"), params, mapper);
	}
	

	public int count(ExamPaper example) {
		String stmt = getStatement("count");
		
		SQLBuilder sql = new SQLBuilder(getStatement("select.example.begin"), getStatement("select.example.end"), " AND ");
		if (example != null) {
			sql.addParam(getStatement("select.id"), example.getId());
			if (example.getCategory() != null) {
				sql.addParam(getStatement("select.category"), example.getCategory().getCode());
			}
			sql.addParam(getStatement("select.exam_id"), example.getExamId());
			sql.addParam(getStatement("select.exam_title"), example.getExamTitle());
			sql.addParam(getStatement("select.exam_code"), example.getExamCode());
			sql.addParam(getStatement("select.paper_id"), example.getPaperId());
			sql.addParam(getStatement("select.paper_title"), example.getPaperTitle());
			sql.addParam(getStatement("select.paper_code"), example.getPaperCode());
			sql.addParam(getStatement("select.paper_file"), example.getPaperFile());
			if (example.getYear() != null) {
				sql.addParam(getStatement("select.year"), example.getYear().getYear());
			}
			if (example.getTerm() != null) {
				sql.addParam(getStatement("select.term"), example.getTerm().getCode());
			}
		}
		Map<String, Object> params = sql.getParams();
		if (params.size() > 0) {
			// We only use the joined count when we have something to search for.
			stmt = getStatement("count.where")+  sql.getStmt();
			return (Integer) namedParameterJdbcTemplate.queryForInt(stmt, params);
		} else {

			return (Integer) getJdbcTemplate().queryForInt(stmt);
		}
		
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
	
	public List<ExamPaper> findAny(ExamPaper example) {
		return find(example, "OR", -1, -1);
	}
	
	public List<ExamPaper> findAll(ExamPaper example, int start, int length) {
		return find(example, "AND", start, length);
	}
		
	public List<ExamPaper> find(ExamPaper example, String operation, int start, int length) {
		SQLBuilder sql = new SQLBuilder(getStatement("select.example.begin"), getStatement("select.example.end"), " "+ operation+ " ");
		
		sql.addParam(getStatement("select.id"), example.getId());
		if (example.getCategory() != null) {
			sql.addParam(getStatement("select.category"), example.getCategory().getCode());
		}
		sql.addParam(getStatement("select.exam_id"), example.getExamId());
		sql.addParam(getStatement("select.exam_title"), example.getExamTitle());
		sql.addParam(getStatement("select.exam_code"), example.getExamCode());
		sql.addParam(getStatement("select.paper_id"), example.getPaperId());
		sql.addParam(getStatement("select.paper_title"), example.getPaperTitle());
		sql.addParam(getStatement("select.paper_code"), example.getPaperCode());
		sql.addParam(getStatement("select.paper_file"), example.getPaperFile());
		if (example.getYear() != null) {
			sql.addParam(getStatement("select.year"), example.getYear().getYear());
		}
		if (example.getTerm() != null) {
			sql.addParam(getStatement("select.term"), example.getTerm().getCode());
		}
		
		Map params = sql.getParams();
		String statement = getStatement("select");
		if (params.size() > 0) {
			statement += sql.getStmt();
		}
		// Add on the start and limit if required.
		if (start >= 0) {
			params.put("start", start);
			params.put("length", length);
			statement += getStatement("select.range");
		}
		
		List<ExamPaper> examPapers = (List<ExamPaper>)namedParameterJdbcTemplate.query(statement, params, mapper);
		return examPapers;
	}
	

	private static class SQLBuilder {
		StringBuilder stmt;
		String end;
		String join;
		Map<String, Object> params;
		boolean firstParam = true;
		Pattern pattern = Pattern.compile(":(\\S+)");
		
		SQLBuilder(String start, String end, String join) {
			stmt = new StringBuilder(start);
			params = new HashMap<String, Object>();
			this.join = join;
			this.end = end; 
		}
		// This method work reasonably well, except int/long <= 0 are like null. Watch out.		
		void addParam(String sql, Object value) {
			if (value == null) {
				return;
			}
			if (value instanceof Long) {
				Long longValue = (Long)value;
				if (longValue <= 0) {
					return;
				}
			} else if (value instanceof Integer) {
				Integer intValue = (Integer)value;
				if (intValue <= 0) {
					return;
				}
			}
			String param = getKey(sql);
			if (!firstParam) {
				stmt.append(join);
			} else {
				firstParam = false;
			}
			stmt.append(sql);
			params.put(param, value);
		}
		
		String getKey(String sql) {
			Matcher matcher = pattern.matcher(sql);
			if (!matcher.find()) {
				throw new IllegalArgumentException(
						"The supplied string doesn't contain a parameter to subsitute: "+ sql);
			}
			return matcher.group(1); // Trimming the colon.
		}
		
		String getStmt() {
			return stmt.toString()+ end;
		}
		
		Map<String,Object> getParams() {
			return params;
		}
	}


	public List<AcademicYear> getYears() {
		return getJdbcTemplate().query(getStatement("years"),new RowMapper() {
			
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return academicYearService.getAcademicYear(rs.getInt("academic_year"));
			}
		});
	}


}
