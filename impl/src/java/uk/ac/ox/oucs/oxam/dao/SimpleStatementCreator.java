package uk.ac.ox.oucs.oxam.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCreatorUtils;


/**
 * Class which means that SQL gets logged.
 * Use setParam so that params can get logged too.
 * @author buckett
 *
 */
public abstract class SimpleStatementCreator implements PreparedStatementCreator, SqlProvider {

	/**
	 * This sets a parameter on a PreparedStatement. It uses the SpringJDBC utils so that logging
	 * of SQL happens consistently.
	 */
	public void setParam(PreparedStatement ps, int pos, Object value) throws SQLException {
		int type = StatementCreatorUtils.javaTypeToSqlParameterType(value.getClass());
		StatementCreatorUtils.setParameterValue(ps, pos, type, value);
	}
}
