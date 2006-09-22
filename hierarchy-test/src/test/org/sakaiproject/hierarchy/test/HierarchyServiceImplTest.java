package org.sakaiproject.hierarchy.test;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.impl.BasicSqlService;
import org.sakaiproject.db.impl.SqlServiceTest;
import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.hierarchy.impl.HierarchyServiceImpl;
import org.sakaiproject.hierarchy.impl.model.dao.HierarchyDAO;
import org.sakaiproject.id.impl.UuidV4IdComponent;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.thread_local.impl.ThreadLocalComponent;

/**
 * @author Matthew Buckett matthew.buckett at oucs dot ox dot ac dot uk
 */
public class HierarchyServiceImplTest extends HierarchyServiceApiTestBase
{
	private static final Log log = LogFactory
			.getLog(HierarchyServiceImplTest.class);

	private BasicDataSource ds;

	public void setUp()
	{

		ds = new BasicDataSource();
		ds.setDriverClassName("org.hsqldb.jdbcDriver");
		ds.setUsername("sa");
		ds.setPassword("");
		ds.setUrl("jdbc:hsqldb:mem:db");

		BasicSqlService sqlService = new SqlServiceTest();
		sqlService.setDefaultDataSource(ds);
		sqlService.setAutoDdl("true");
		sqlService.init();

		ThreadLocalManager threadLocalManager = new ThreadLocalComponent();

		HierarchyDAO dao = new HierarchyDAO();
		dao.setSqlService(sqlService);
		dao.setThreadLocalManager(threadLocalManager);
		dao.setIdmanager(new UuidV4IdComponent());
		dao.init();

		HierarchyServiceImpl impl = new HierarchyServiceImpl();
		impl.setHierarchyDao(dao);
		impl.init();

		service = impl;
	}

	public void tearDown() throws SQLException
	{
		ds.getConnection().prepareStatement("SHUTDOWN").execute();
		ds.close();
	}

	public void testZBigInjection() throws Exception
	{
		try
		{
			service.begin();
			String testRoot = "/rootTestNode";

			Hierarchy h = service.getNode(testRoot);
			if (h != null)
			{
				service.deleteNode(h);
			}
			h = service.newHierarchy(testRoot);
			addNodes(h, 5);
			service.save(h);

			h = service.getNode(testRoot);
			assertEquals("Root node check ", h.getPath(), testRoot);
			checkNodes(h);
			// hierarchyService.deleteNode(h);
			log.info("Testing Navigation ");
			List l = service.getRootNodes();
			log.info("The following should only generate 1 set of finds ");
			printList("", l.iterator(), 1);
			log.info("The following should only generate 10 set of finds ");
			printList("", l.iterator(), 2);
			log
					.warn("Spring Injected Test Sucessfull..... but plesae remove in production ");
		}
		finally
		{
			service.end();
		}

	}

	private void addNodes(Hierarchy h, int depth)
			throws HierarchyServiceException
	{
		if (depth == 0)
		{
			return;
		}
		for (int i = 0; i < 5; i++)
		{
			Hierarchy child1 = service.newHierarchy(h.getPath()
					+ "/annothernodeinthechain" + i);
			h.addTochildren(child1);

			HierarchyProperty hp = service.newHierachyProperty();
			hp.setName("propertyA" + i);
			hp.setPropvalue("propertyvalueA" + i);
			child1.addToproperties(hp);

			hp = service.newHierachyProperty();
			hp.setName("propertyB" + i);
			hp.setPropvalue("propertyvalueB" + i);
			child1.addToproperties(hp);
			addNodes(child1, depth - 1);
		}
	}

	private void checkNodes(Hierarchy h) throws Exception
	{
		for (int i = 0; i < 5; i++)
		{
			String testPath = h.getPath() + "/annothernodeinthechain" + i;
			Hierarchy child1 = h.getChild(testPath);
			assertNotNull("Missing node path  " + testPath, child1);
			assertEquals("Path name is not correct ", testPath, child1
					.getPath());
			HierarchyProperty hp = child1.getProperty("propertyA" + i);
			assertNotNull("No property " + testPath + "/propertyA" + i
					+ " node found ", hp);
			assertEquals("Property value of " + testPath + "/propertyA" + i
					+ " is ", "propertyvalueA" + i, hp.getPropvalue());
			hp = child1.getProperty("propertyB" + i);
			assertNotNull("No property " + testPath + "/propertyB" + i
					+ " node found ", hp);
			assertEquals("Property value of " + testPath + "/propertyB" + i
					+ " is ", "propertyvalueB" + i, hp.getPropvalue());

		}
	}

}