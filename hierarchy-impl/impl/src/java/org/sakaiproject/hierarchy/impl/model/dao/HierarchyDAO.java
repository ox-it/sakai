package org.sakaiproject.hierarchy.impl.model.dao;

// CustomDAOImports

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
	
	private IdManager idmanager = null;

	private ThreadLocalManager threadLocalManager = null;

	// CustomDAOClassConstructor

	public HierarchyDAO()
	{
	}

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
				new Object[] { owner.getId() }, new HierarchyPropertySqlReader(this,
						owner));
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

	public void saveOrUpdate(Hierarchy hierarchy)
	{
		if (!isInSaveStack(hierarchy))
		{
			pushSaveStack(hierarchy);
			try
			{
				if (hierarchy.getParent() != null)
				{
					saveOrUpdate(hierarchy.getParent());
				}
				if (hierarchy.isModified())
				{
					if (hierarchy.getId() == null)
					{
						Object[] params = HierarchySqlReader.getInsertObjects(hierarchy, idmanager); 
						logSQL("SaveOrUpdate Insert ",HierarchySqlReader.INSERT_SQL,params,null);
						if ( !sqlService.dbWrite(HierarchySqlReader.INSERT_SQL,params) ) {
							log.warn("Failed to save Hieratchy Node at "+hierarchy.getPath());
						}
					}
					else
					{
						Object[] params = HierarchySqlReader.getUpdateObjects(hierarchy); 
						logSQL("SaveOrUpdate Update ",HierarchySqlReader.UPDATE_SQL,params,null);
						if (!sqlService.dbWrite(HierarchySqlReader.UPDATE_SQL,
								params) ) {
							log.warn("Failed to update Hieratchy Node at "+hierarchy.getPath());
						}
					}
					// save properties and nodes
					for (Iterator i = hierarchy.getChildren().values()
							.iterator(); i.hasNext();)
					{
						Hierarchy child = (Hierarchy) i.next();
						if ( !hierarchy.equals(child.getParent()) ) {
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
				log
						.error("Detached Hierarchy Propert, attach it " +
								"first please before attempting to save " +
								" name:"+hierarchyProperty.getName()+
								"; value:"+hierarchyProperty.getPropvalue());
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
			if (hierarchyProperty.getId() == null)
			{
				Object[] params = HierarchyPropertySqlReader
				.getInsertObjects(hierarchyProperty,idmanager);
				logSQL("Insert Hierarchy properties ",HierarchyPropertySqlReader.INSERT_SQL,params, null);
				if ( !sqlService.dbWrite(HierarchyPropertySqlReader.INSERT_SQL,
						params) ) {
					log.warn("Failed to save Hieratchy Property at "+hierarchyProperty.getNode().getPath()+"/"+hierarchyProperty.getName());

				}
			}
			else
			{
				Object[] params = HierarchyPropertySqlReader
				.getUpdateObjects(hierarchyProperty);
				logSQL("Update Hierarchy properties ",HierarchyPropertySqlReader.UPDATE_SQL,params, null);
				if ( !sqlService.dbWrite(HierarchyPropertySqlReader.UPDATE_SQL,params) ) {
					log.warn("Failed to update Hieratchy Property at "+hierarchyProperty.getNode().getPath()+"/"+hierarchyProperty.getName());
				}
			}
			hierarchyProperty.setModified(false);
		}

	}

	public void delete(Hierarchy hierarchy)
	{
		if (hierarchy.getId() != null)
		{
			Object[] params = HierarchySqlReader
			.getDeleteObjects(hierarchy);
			logSQL("Delete Hierarchy ",HierarchySqlReader.DELETE_SQL,params, null);
			if ( !sqlService.dbWrite(HierarchySqlReader.DELETE_SQL,
					params) ) {
				log.warn("Failed to delete Hieratchy Node at "+hierarchy.getPath());
			}
			for ( Iterator i = hierarchy.getChildren().values().iterator(); i.hasNext(); ) {
				Hierarchy h = (Hierarchy) i.next();
				delete(h);
			}
			for ( Iterator i = hierarchy.getProperties().values().iterator(); i.hasNext(); ) {
				HierarchyProperty hp = (HierarchyProperty) i.next();
				delete(hp);
			}
		}

	}

	public void delete(HierarchyProperty hierarchyProperty)
	{
		if (hierarchyProperty.getId() != null)
		{
			Object[] params = HierarchyPropertySqlReader
			.getDeleteObjects(hierarchyProperty);
			logSQL("Delete Hierarchy properties ",HierarchyPropertySqlReader.DELETE_SQL,params,null);
			if ( !sqlService.dbWrite(HierarchyPropertySqlReader.DELETE_SQL,
					params) ) {
				log.warn("Failed to update Hieratchy Property at "+hierarchyProperty.getNode().getPath()+"/"+hierarchyProperty.getName());				
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
		try
		{
			logSQL("findList ",sql,params,null);
			List l = sqlService.dbRead(sql, params, reader);
			log.info(" Found "+l);
			if (l == null)
			{
				l = new ArrayList();
			}
			return l;
		}
		catch (Exception ex)
		{
			logSQL("Failed to execute ",sql, params, ex);
		}
		return null;
	}


	private Object findFirst(String sql, Object[] params, SqlReader reader)
	{
		try
		{
			logSQL("findFirst ",sql,params,null);
			List l = sqlService.dbRead(sql, params, reader);
			log.info(" Found "+l);
			if (l != null && l.size() > 0)
			{
				return l.get(0);
			}
		}
		catch (Exception ex)
		{
			logSQL("Failed ",sql, params, ex);
		}
		return null;
	}

	private void logSQL(String message, String sql, Object[] params, Exception ex)
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
		if ( ex == null ) {
			log.info(message + sql + "with params " + psb);
		} else {
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

	public IdManager getIdmanager()
	{
		return idmanager;
	}

	public void setIdmanager(IdManager idmanager)
	{
		this.idmanager = idmanager;
	}


}