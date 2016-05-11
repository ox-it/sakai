package uk.ac.ox.oucs.oxam.dao;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;


/**
 * Class which means that SQL gets logged.
 * Use setParam so that params can get logged too.
 * Lots of this is copied form a newer version of org.springframework.jdbc.core.StatementCreatorUtils
 * which is available in spring 2.5.6+
 * @author buckett
 *
 */
public abstract class SimpleStatementCreator implements PreparedStatementCreator, SqlProvider {

	private static Map<Class<?>, Object> javaTypeToSqlTypeMap = new HashMap<Class<?>, Object>();

	static {
		/* JDBC 3.0 only - not compatible with e.g. MySQL at present
		javaTypeToSqlTypeMap.put(boolean.class, new Integer(Types.BOOLEAN));
		javaTypeToSqlTypeMap.put(Boolean.class, new Integer(Types.BOOLEAN));
		*/
		javaTypeToSqlTypeMap .put(byte.class, new Integer(Types.TINYINT));
		javaTypeToSqlTypeMap.put(Byte.class, new Integer(Types.TINYINT));
		javaTypeToSqlTypeMap.put(short.class, new Integer(Types.SMALLINT));
		javaTypeToSqlTypeMap.put(Short.class, new Integer(Types.SMALLINT));
		javaTypeToSqlTypeMap.put(int.class, new Integer(Types.INTEGER));
		javaTypeToSqlTypeMap.put(Integer.class, new Integer(Types.INTEGER));
		javaTypeToSqlTypeMap.put(long.class, new Integer(Types.BIGINT));
		javaTypeToSqlTypeMap.put(Long.class, new Integer(Types.BIGINT));
		javaTypeToSqlTypeMap.put(BigInteger.class, new Integer(Types.BIGINT));
		javaTypeToSqlTypeMap.put(float.class, new Integer(Types.FLOAT));
		javaTypeToSqlTypeMap.put(Float.class, new Integer(Types.FLOAT));
		javaTypeToSqlTypeMap.put(double.class, new Integer(Types.DOUBLE));
		javaTypeToSqlTypeMap.put(Double.class, new Integer(Types.DOUBLE));
		javaTypeToSqlTypeMap.put(BigDecimal.class, new Integer(Types.DECIMAL));
		javaTypeToSqlTypeMap.put(java.sql.Date.class, new Integer(Types.DATE));
		javaTypeToSqlTypeMap.put(java.sql.Time.class, new Integer(Types.TIME));
		javaTypeToSqlTypeMap.put(java.sql.Timestamp.class, new Integer(Types.TIMESTAMP));
		javaTypeToSqlTypeMap.put(Blob.class, new Integer(Types.BLOB));
		javaTypeToSqlTypeMap.put(Clob.class, new Integer(Types.CLOB));
	}
	
	/**
	 * This sets a parameter on a PreparedStatement. It uses the SpringJDBC utils so that logging
	 * of SQL happens consistently.
	 */
	public void setParam(PreparedStatement ps, int pos, Object value) throws SQLException {
		// This isn't in the old version of spring in Sakai 2.6
		//int type = StatementCreatorUtils.javaTypeToSqlParameterType(value.getClass());
		int type = javaTypeToSqlParameterType(value.getClass());
		StatementCreatorUtils.setParameterValue(ps, pos, type, value);
	}
	
	public static int javaTypeToSqlParameterType(Class javaType) {
		Integer sqlType = (Integer) javaTypeToSqlTypeMap.get(javaType);
		if (sqlType != null) {
			return sqlType.intValue();
		}
		if (Number.class.isAssignableFrom(javaType)) {
			return Types.NUMERIC;
		}
		if (isStringValue(javaType)) {
			return Types.VARCHAR;
		}
		if (isDateValue(javaType) || Calendar.class.isAssignableFrom(javaType)) {
			return Types.TIMESTAMP;
		}
		return SqlTypeValue.TYPE_UNKNOWN;
	}
	
	/**
	 * Check whether the given value can be treated as a String value.
	 */
	private static boolean isStringValue(Class inValueType) {
		// Consider any CharSequence (including JDK 1.5's StringBuilder) as String.
		return (CharSequence.class.isAssignableFrom(inValueType) ||
				StringWriter.class.isAssignableFrom(inValueType));
	}

	/**
	 * Check whether the given value is a <code>java.util.Date</code>
	 * (but not one of the JDBC-specific subclasses).
	 */
	private static boolean isDateValue(Class inValueType) {
		return (java.util.Date.class.isAssignableFrom(inValueType) &&
				!(java.sql.Date.class.isAssignableFrom(inValueType) ||
						java.sql.Time.class.isAssignableFrom(inValueType) ||
						java.sql.Timestamp.class.isAssignableFrom(inValueType)));
	}
}
