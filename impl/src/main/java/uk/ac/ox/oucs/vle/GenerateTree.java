package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;


/**
 * Simple class to generate a static JSON file for the jsTree.
 * @author buckett
 *
 */
public class GenerateTree {

	private DataSource ds;
	private JsonFactory factory;
	
	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}
	
	public void setFactory(JsonFactory factory) {
		this.factory = factory;
	}

	
	public String generateDepartmentTree() {
		StringWriter writer = new StringWriter();

		Connection connection = null;
		ResultSet rs = null;
		Statement st = null;
		
		try {
			JsonGenerator generator = factory.createJsonGenerator(writer);
			connection = ds.getConnection();
			st = connection.createStatement();
			
			if(st.execute("SELECT Division.division_name, Division.division_code, "+
					  "Department.department_name, Department.department_code, "+
					  "SubUnit.sub_unit_code, SubUnit.sub_unit_name "+
					  "FROM Division "+
					  "INNER JOIN Department ON Department.division_code = Division.division_code "+
					  "INNER JOIN SubUnit ON SubUnit.department_code = Department.department_code "+
					  "ORDER BY 1,3,5")) {
			
				rs = st.getResultSet();
				String lastDivisionId = null;
				String lastDepartmentCode = null;
				generator.writeStartArray();
				startNode(generator, "root", "University of Oxford");
				while (rs.next()) {
					String divisionName = rs.getString(1);
					String divisionId = rs.getString(2);
					String departmentName = rs.getString(3);
					String departmentCode = rs.getString(4);
					
					//String subUnitName = rs.getString(5);
					//String subUnitCode = rs.getString(6);
					
					if (!departmentCode.equals(lastDepartmentCode)) {
						if (lastDepartmentCode != null) {
							endNode(generator);
						}
						
						if (!divisionId.equals(lastDivisionId)) {
							if (lastDivisionId != null) {
								endNode(generator);
							}
							startNode(generator,divisionId, divisionName);
							lastDivisionId = divisionId;
						}
						
						startNode(generator,departmentCode, departmentName);
						lastDepartmentCode = departmentCode;
					}
					//startNode(generator, subUnitCode, subUnitName);
					//endNode(generator);
					
				}
				if (lastDepartmentCode != null) {
					endNode(generator);
				}
				if (lastDivisionId != null) {
					endNode(generator);
				}
				endNode(generator);
				generator.writeEndArray();
				generator.close();
				return writer.toString();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {}
			}
		}
		
		return null;
	}
	
	private void startNode(JsonGenerator generator, String id, String name) throws JsonGenerationException, IOException
	{
		generator.writeStartObject();
		generator.writeObjectFieldStart("attr");
		generator.writeStringField("id", id);
		generator.writeEndObject();
		generator.writeStringField("data", name);
		generator.writeStringField("state", "closed");
		generator.writeArrayFieldStart("children");
	}
	
	private void endNode(JsonGenerator generator) throws JsonGenerationException, IOException
	{
		generator.writeEndArray();
		generator.writeEndObject();
	}
}
