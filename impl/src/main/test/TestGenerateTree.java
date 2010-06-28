import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.codehaus.jackson.JsonFactory;

import uk.ac.ox.oucs.vle.GenerateTree;

public class TestGenerateTree extends TestCase {

	public void testOutput() {
		DataSource ds = getDataSource();
		JsonFactory factory = new JsonFactory();
		GenerateTree treeGen = new GenerateTree();
		treeGen.setDataSource(ds);
		treeGen.setFactory(factory);
		System.out.println(treeGen.generateDepartmentTree());
	}

	private DataSource getDataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName("com.mysql.jdbc.Driver");
		ds.setUsername("readonlyuser");
		ds.setPassword("readonly");
		ds.setUrl("jdbc:mysql://daisy.nsms.ox.ac.uk:3306/daisydb");
		return ds;
	}

}
