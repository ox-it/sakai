package pom.dao.impl;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import pom.model.Thing;

/**
 * RowMapper to handle Things
 *
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class ThingMapper implements RowMapper{
	
	public Thing mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Thing t = new Thing();
		
		t.setId(rs.getLong("ID"));
		t.setName(rs.getString("TITLE"));
		
		return t;
	}
	
	
	
}
