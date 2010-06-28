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
			if(st.execute("SELECT Division.name, Division.id, Department.name, Department.code FROM Division, Department WHERE  Department.code LIKE CONCAT(Division.ID,'%') ORDER BY 1,3")) {
				rs = st.getResultSet();
				String lastDivisionId = null;
				generator.writeStartArray();
				startNode(generator, "root", "University of Oxford");
				while (rs.next()) {
					String divisionName = rs.getString(1);
					String divisionId = rs.getString(2);
					String departmentName = rs.getString(3);
					String departmentCode = rs.getString(4);
					if (!divisionId.equals(lastDivisionId)) {
						if (lastDivisionId != null) {
							endNode(generator);
						}
						startNode(generator,divisionId, divisionName);
						lastDivisionId = divisionId;
					}
					startNode(generator, departmentCode, departmentName);
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
