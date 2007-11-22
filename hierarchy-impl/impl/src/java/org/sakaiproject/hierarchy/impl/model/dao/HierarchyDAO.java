package org.sakaiproject.hierarchy.impl.model.dao;

// CustomDAOImports

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

public class HierarchyDAO implements
		org.sakaiproject.hierarchy.api.dao.HierarchyDAO
{

	private static final String HIERARCHY_SERVICESQL = "hierarchy_service";

	private static final Log log = LogFactory.getLog(HierarchyDAO.class);

	private static final String SAVE_LIST_NAME = HierarchyDAO.class.getName()
			+ "_saveList";

	private SqlService sqlService = null;

	private IdManager idManager = null;

	private ThreadLocalManager threadLocalManager = null;

	public HierarchyDAO()
	{
	}

	/*
	 * Connection and transaction management
	 * ----------------------------------------------------------------------
	 */
	private ThreadLocal connectionHolder = new ThreadLocal();

	protected class ConnectionHolder
	{
		protected Connection connection = null;

		protected int refcount = 0;

	}

	public void begin()
	{
		getConnection();
	}

	public void end()
	{
		releaseConnection();
	}

	public void abort()
	{
		abortConnection();
	}

	private Connection getConnection()
	{
		ConnectionHolder ch = (ConnectionHolder) connectionHolder.get();
		if (ch == null)
		{
			try
			{
				ch = new ConnectionHolder();
				ch.connection = sqlService.borrowConnection();
				connectionHolder.set(ch);
				log.debug("Auto commit: "+ch.connection.getAutoCommit());
				log
						.debug("Getting Connection +++++++++++++++++++++++++++++++++++++");
			}
			catch (Exception ex)
			{
				log.error("Failed to get connection for hierarchy service ");
			}
		}
		ch.refcount++;
		return ch.connection;
	}

	private void releaseConnection()
	{
		ConnectionHolder ch = (ConnectionHolder) connectionHolder.get();
		if (ch != null)
		{
			ch.refcount--;
			if (ch.refcount < 1)
			{
				try
				{
					ch.connection.commit();
				}
				catch (SQLException e)
				{
					log.error("Failed to commit transaction ");
				}
				sqlService.returnConnection(ch.connection);
				connectionHolder.set(null);
				log
						.debug("Release Connection ------------------------------------------");
			}
		}
	}

	private void abortConnection()
	{
		ConnectionHolder ch = (ConnectionHolder) connectionHolder.get();
		if (ch != null)
		{
			try
			{
				ch.connection.rollback();
			}
			catch (SQLException e)
			{
				log.error("Failed to commit transaction ");
			}
			sqlService.returnConnection(ch.connection);
			connectionHolder.set(null);
			log.debug("Abort Connection -------------------------");
		}
	}

	/*
	 * ---------------------------------------------------------------------------
	 */

	public void init()
	{
		try
		{
			sqlService.ddl(this.getClass().getClassLoader(),
					HIERARCHY_SERVICESQL);
		}
		catch (Exception ex)
		{
			log.error("Failed to start " + this.getClass().getName(), ex);
			System.exit(-1);
		}
	}

	public List findHierarchyRoots()
	{
		return findList(HierarchySqlReader.FIND_ROOTS, new Object[] {},
				new HierarchySqlReader(this));
	}

	public List findHierarchyProperties(Hierarchy owner)
	{
		return findList(HierarchyPropertySqlReader.FIND_BY_NODE_ID_SQL,
				new Object[] { owner.getId() }, new HierarchyPropertySqlReader(
						this, owner));
	}

	public Hierarchy findHierarchyById(String nodeId)
	{
		return (Hierarchy) findFirst(HierarchySqlReader.FIND_BY_ID_SQL,
				new Object[] { nodeId }, new HierarchySqlReader(this));
	}

	public Hierarchy findHierarchyByPathHash(String pathHash)
	{
		return (Hierarchy) findFirst(HierarchySqlReader.FIND_BY_PATHHASH_SQL,
				new Object[] { pathHash }, new HierarchySqlReader(this));
	}

	public List findHierarchyByParent(Hierarchy parent)
	{
		return findList(HierarchySqlReader.FIND_BY_PARENT_ID_SQL,
				new Object[] { parent.getId() }, new HierarchySqlReader(this,
						parent));
	}
	
	public List findHierarchyByProperty(String name,String value) 
	{
		return findList(HierarchySqlReader.FIND_BY_PROPERTY,
				new Object[] {name, value}, new HierarchySqlReader(this));
	}

	public void saveOrUpdate(Hierarchy hierarchy)
	{
		if (!isInSaveStack(hierarchy))
		{
			pushSaveStack(hierarchy);
			Connection connection = null;
			try
			{
				connection = getConnection();
				if (hierarchy.getParent() != null)
				{
					saveOrUpdate(hierarchy.getParent());
				}
				if (hierarchy.isModified())
				{
					if (hierarchy.getId() == null)
					{
						Object[] params = HierarchySqlReader.getInsertObjects(
								hierarchy, idManager);
						logSQL("SaveOrUpdate Insert ",
								HierarchySqlReader.INSERT_SQL, params, null);
						if (!sqlService.dbWrite(connection,
								HierarchySqlReader.INSERT_SQL, params))
						{
							log.warn("Failed to save Hieratchy Node at "
									+ hierarchy.getPath());
						}
					}
					else
					{
						Object[] params = HierarchySqlReader
								.getUpdateObjects(hierarchy);
						logSQL("SaveOrUpdate Update ",
								HierarchySqlReader.UPDATE_SQL, params, null);
						if (!sqlService.dbWrite(connection,
								HierarchySqlReader.UPDATE_SQL, params))
						{
							log.warn("Failed to update Hieratchy Node at "
									+ hierarchy.getPath());
						}
					}
					// save properties and nodes
					for (Iterator i = hierarchy.getChildren().values()
							.iterator(); i.hasNext();)
					{
						Hierarchy child = (Hierarchy) i.next();
						if (!hierarchy.equals(child.getParent()))
						{
							child.setParent(hierarchy);
						}
						saveOrUpdate(child);
					}
					for (Iterator i = hierarchy.getProperties().values()
							.iterator(); i.hasNext();)
					{
						HierarchyProperty hp = (HierarchyProperty) i.next();
						saveOrUpdate(hp);
					}
					hierarchy.setModified(false);
				}
			}
			finally
			{
				releaseConnection();
				popSaveStack(hierarchy);
			}
		}

	}

	public void saveOrUpdate(HierarchyProperty hierarchyProperty)
	{
		if (hierarchyProperty.isModified())
		{
			// should this cascade ?, for simplicity probably not.
			if (hierarchyProperty.getNode() == null)
			{
				log.error("Detached Hierarchy Propert, attach it "
						+ "first please before attempting to save " + " name:"
						+ hierarchyProperty.getName() + "; value:"
						+ hierarchyProperty.getPropvalue());
				return;
			}
			else
			{
				Hierarchy h = hierarchyProperty.getNode();
				if (h.getId() == null)
				{
					saveOrUpdate(h); // this could cause recursion
				}
			}
			if (hierarchyProperty.getPropvalue() == null)
			{
				Connection connection = null;
				try
				{
					connection = getConnection();
					Object[] params = HierarchyPropertySqlReader
							.getDeleteObjects(hierarchyProperty);
					logSQL("Insert Hierarchy properties ",
							HierarchyPropertySqlReader.DELETE_SQL, params, null);
					if (!sqlService.dbWrite(connection,
							HierarchyPropertySqlReader.DELETE_SQL, params))
					{
						log.warn("Failed to save Hieratchy Property at "
								+ hierarchyProperty.getNode().getPath() + "/"
								+ hierarchyProperty.getName());

					}
				}
				finally
				{
					releaseConnection();
				}
			}
			else if (hierarchyProperty.getId() == null)
			{
				Connection connection = null;
				try
				{
					connection = getConnection();
					Object[] params = HierarchyPropertySqlReader
							.getInsertObjects(hierarchyProperty, idManager);
					logSQL("Insert Hierarchy properties ",
							HierarchyPropertySqlReader.INSERT_SQL, params, null);
					if (!sqlService.dbWrite(connection,
							HierarchyPropertySqlReader.INSERT_SQL, params))
					{
						log.warn("Failed to save Hieratchy Property at "
								+ hierarchyProperty.getNode().getPath() + "/"
								+ hierarchyProperty.getName());

					}
				}
				finally
				{
					releaseConnection();
				}
			}
			else
			{
				Connection connection = null;
				try
				{
					connection = getConnection();
					Object[] params = HierarchyPropertySqlReader
							.getUpdateObjects(hierarchyProperty);
					logSQL("Update Hierarchy properties ",
							HierarchyPropertySqlReader.UPDATE_SQL, params, null);
					if (!sqlService.dbWrite(connection,
							HierarchyPropertySqlReader.UPDATE_SQL, params))
					{
						log.warn("Failed to update Hieratchy Property at "
								+ hierarchyProperty.getNode().getPath() + "/"
								+ hierarchyProperty.getName());
					}
				}
				finally
				{
					releaseConnection();
				}
			}
			hierarchyProperty.setModified(false);
		}

	}

	public void delete(Hierarchy hierarchy)
	{

		if (hierarchy.getId() != null)
		{
			Connection connection = null;
			try
			{
				connection = getConnection();

				List l = new ArrayList();
				l.add(hierarchy.getId());
				loadChildren(connection, l);
				deleteProperties(connection, l);
				deleteNodes(connection, l);

			}
			finally
			{
				releaseConnection();
			}

		}

	}

	private void deleteNodes(Connection connection, List l)
	{
		StringBuffer sb = null;
		List paramL = new ArrayList();

		for (Iterator i = l.iterator(); i.hasNext();)
		{
			String id = (String) i.next();
			if (sb == null)
			{
				sb = new StringBuffer();
				paramL.clear();
				sb.append(HierarchySqlReader.DELETE_NODE_GROUPS_SQL_1);
			}
			else
			{
				sb.append(",");
			}
			sb.append(" ? ");
			paramL.add(id);
			if (paramL.size() > 500 )
			{
				sb.append(HierarchySqlReader.DELETE_NODE_GROUPS_SQL_2);
				log.debug("Executing " + sb.toString());
				sqlService.dbWrite(connection, sb.toString(), paramL.toArray());
				sb = null;
				paramL.clear();
			}
		}
		if (sb != null)
		{
			sb.append(HierarchySqlReader.DELETE_NODE_GROUPS_SQL_2);
			log.debug("Executing " + sb.toString());
			sqlService.dbWrite(connection, sb.toString(),paramL.toArray());
			sb = null;
			paramL.clear();
		}
	}

	private void deleteProperties(Connection connection, List l)
	{
		StringBuffer sb = null;
		List paramL = new ArrayList();
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			String id = (String) i.next();
			if (sb == null)
			{
				sb = new StringBuffer();
				paramL.clear();
				sb
						.append(HierarchyPropertySqlReader.DELETE_NODE_PROPERTIES_GROUPS_SQL_1);
			}
			else
			{
				sb.append(",");
			}
			sb.append(" ? ");
			paramL.add(id);
			if (paramL.size() > 500 )
			{
				sb
						.append(HierarchyPropertySqlReader.DELETE_NODE_PROPERTIES_GROUPS_SQL_2);
				log.debug("Executing " + sb.toString());
				sqlService.dbWrite(connection, sb.toString(),paramL.toArray());
				sb = null;
				paramL.clear();
			}
		}
		if (sb != null)
		{
			sb
					.append(HierarchyPropertySqlReader.DELETE_NODE_PROPERTIES_GROUPS_SQL_2);
			log.debug("Executing " + sb.toString());
			sqlService.dbWrite(connection, sb.toString(),paramL.toArray());
			sb = null;
			paramL.clear();
		}
	}

	private void loadChildren(Connection connection, List l)
	{
		StringBuffer sb = null;
		List paramL = new ArrayList();
		List found = new ArrayList();
		for (Iterator i = l.iterator(); i.hasNext();)
		{
			String id = (String) i.next();
			if (sb == null)
			{
				sb = new StringBuffer();
				paramL.clear();
				sb
						.append(HierarchySqlReader.FIND_CHILD_ID_BY_PARENT_GROUPS_SQL_1);
			}
			else
			{
				sb.append(",");
			}
			sb.append(" ? ");
			paramL.add(id);
			if (paramL.size() > 500)
			{
				sb
						.append(HierarchySqlReader.FIND_CHILD_ID_BY_PARENT_GROUPS_SQL_2);
				log.debug("Executing " + sb.toString());
				found.addAll(sqlService.dbRead(connection, sb.toString(),
						paramL.toArray(), new SqlReader()
						{

							public Object readSqlResultRecord(ResultSet result)
							{
								try
								{
									return result.getString(1);
								}
								catch (SQLException e)
								{
									return "";
								}
							}

						}));
				sb = null;
				paramL.clear();
			}
		}
		if (sb != null)
		{
			sb.append(HierarchySqlReader.FIND_CHILD_ID_BY_PARENT_GROUPS_SQL_2);
			log.debug("Executing " + sb.toString());
			found.addAll(sqlService.dbRead(connection, sb.toString(),
					paramL.toArray(), new SqlReader()
					{
						public Object readSqlResultRecord(ResultSet result)
						{
							try
							{
								return result.getString(1);
							}
							catch (SQLException e)
							{
								return "";
							}
						}
					}));
			sb = null;
			paramL.clear();
		}
		if (found.size() > 0)
		{
			loadChildren(connection, found);
			l.addAll(found);
			log.debug("Got " + l.size() + " nodes ");
		}
	}

	/*
	 * private void deleteProperties(Hierarchy hierarchy) { if
	 * (hierarchy.getId() != null) { Connection connection = null; try {
	 * connection = getConnection(); Object[] params = new Object[] {
	 * hierarchy.getId() }; logSQL("Delete Hierarchy ",
	 * HierarchyPropertySqlReader.DELETE_NODE_PROPERTIES_SQL, params, null); if
	 * (!sqlService.dbWrite(connection,
	 * HierarchyPropertySqlReader.DELETE_NODE_PROPERTIES_SQL, params)) {
	 * log.warn("Failed to delete Hieratchy Node at " + hierarchy.getPath()); } }
	 * finally { releaseConnection(); } } } private void
	 * deleteChildren(Hierarchy hierarchy) { if (hierarchy.getId() != null) {
	 * Connection connection = null; try { connection = getConnection(); for (
	 * Iterator i = hierarchy.getChildren().values().iterator(); i.hasNext(); ) {
	 * Hierarchy h = ( Hierarchy) i.next(); delete(h); } } finally {
	 * releaseConnection(); } } }
	 */
	public void delete(HierarchyProperty hierarchyProperty)
	{
		if (hierarchyProperty.getId() != null)
		{
			Connection connection = null;
			try
			{
				connection = getConnection();

				Object[] params = HierarchyPropertySqlReader
						.getDeleteObjects(hierarchyProperty);
				logSQL("Delete Hierarchy properties ",
						HierarchyPropertySqlReader.DELETE_SQL, params, null);
				if (!sqlService.dbWrite(connection,
						HierarchyPropertySqlReader.DELETE_SQL, params))
				{
					log.warn("Failed to update Hieratchy Property at "
							+ hierarchyProperty.getNode().getPath() + "/"
							+ hierarchyProperty.getName());
				}
			}
			finally
			{
				releaseConnection();
			}

		}
	}

	// utility methods

	private void popSaveStack(Object o)
	{
		List saveList = getSaveList();
		saveList.remove(o);
	}

	private void pushSaveStack(Object o)
	{
		List saveList = getSaveList();
		if (!saveList.contains(o))
		{
			saveList.add(o);
		}
	}

	private boolean isInSaveStack(Object o)
	{
		List saveList = getSaveList();
		return saveList.contains(o);
	}

	private List getSaveList()
	{
		List l = (List) threadLocalManager.get(SAVE_LIST_NAME);
		if (l == null)
		{
			l = new ArrayList();
			threadLocalManager.set(SAVE_LIST_NAME, l);

		}
		return l;
	}

	private List findList(String sql, Object[] params, SqlReader reader)
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			logSQL("findList ", sql, params, null);
			List l = sqlService.dbRead(connection, sql, params, reader);
			log.debug(" Found " + l);
			if (l == null)
			{
				l = new ArrayList();
			}
			return l;
		}
		catch (Exception ex)
		{
			logSQL("Failed to execute ", sql, params, ex);
		}
		finally
		{
			releaseConnection();
		}
		return null;
	}

	private Object findFirst(String sql, Object[] params, SqlReader reader)
	{
		Connection connection = null;
		try
		{
			connection = getConnection();
			logSQL("findFirst ", sql, params, null);
			List l = sqlService.dbRead(connection, sql, params, reader);
			log.debug(" Found " + l);
			if (l != null && l.size() > 0)
			{
				return l.get(0);
			}
		}
		catch (Exception ex)
		{
			logSQL("Failed ", sql, params, ex);
		}
		finally
		{
			releaseConnection();
		}
		return null;
	}

	private void logSQL(String message, String sql, Object[] params,
			Exception ex)
	{
		StringBuffer psb = new StringBuffer();
		if (params == null)
		{
			psb.append(params);
		}
		else
		{
			psb.append("{");
			for (int j = 0; j < params.length; j++)
			{
				if (j == 0)
				{
					psb.append(params[j]);
				}
				else
				{
					psb.append(",").append(params[j]);
				}
			}
			psb.append("}");
		}
		if (ex == null)
		{
			log.debug(message + sql + "with params " + psb);
		}
		else
		{
			log.error(message + sql + "with params " + psb, ex);
		}
	}

	public SqlService getSqlService()
	{
		return sqlService;
	}

	public void setSqlService(SqlService sqlService)
	{
		this.sqlService = sqlService;
	}

	public ThreadLocalManager getThreadLocalManager()
	{
		return threadLocalManager;
	}

	public void setThreadLocalManager(ThreadLocalManager threadLocalManager)
	{
		this.threadLocalManager = threadLocalManager;
	}

	public IdManager getIdManager()
	{
		return idManager;
	}

	public void setIdManager(IdManager idmanager)
	{
		this.idManager = idmanager;
	}

}