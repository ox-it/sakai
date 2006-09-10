package org.sakaiproject.hierarchy.impl.model.dao;

// CustomDAOImports

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

public class HierarchyDAO implements
		org.sakaiproject.hierarchy.api.dao.HierarchyDAO
{

	private static final String HIERARCHY_SERVICESQL = "hierarchy_service";

	private static final Log log = LogFactory.getLog(HierarchyDAO.class);

	
	private SqlService sqlService = null;


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
		return findList(HierarchySqlReader.FIND_ROOTS, new Object[] {}, new HierarchySqlReader(this));
	}

	public List findHierarchyProperties(Hierarchy owner)
	{
		return findList(HierarchyPropertySqlReader.FIND_BY_NODE_ID_SQL, new Object[] { owner
				.getId() }, new HierarchyPropertySqlReader(owner));
	}

	public Hierarchy findHierarchyByNodeId(String nodeId)
	{
		return (Hierarchy) findFirst(HierarchySqlReader.FIND_BY_NODE_ID_SQL,
				new Object[] { nodeId }, new HierarchySqlReader(this));
	}
	public List findHierarchyByParent(Hierarchy parent)
	{
		return findList(HierarchySqlReader.FIND_BY_PARENT_ID_SQL,
				new Object[] { parent.getId() }, new HierarchySqlReader(this,parent));
	}

	
	


	public void saveOrUpdate(Hierarchy hierarchy) 
	{
		// should this cascade ?, for simplicity probably not.
		if ( ! sqlService.dbWrite(HierarchySqlReader.INSERT_SQL,HierarchySqlReader.getInsertObjects(hierarchy)) ) {
			sqlService.dbWrite(HierarchySqlReader.UPDATE_SQL,HierarchySqlReader.getUpdateObjects(hierarchy));
		}
		
	}
	public void saveOrUpdate(HierarchyProperty hierarchyProperty) 
	{
		// should this cascade ?, for simplicity probably not.
		if ( ! sqlService.dbWrite(HierarchyPropertySqlReader.INSERT_SQL,HierarchyPropertySqlReader.getInsertObjects(hierarchyProperty)) ) {
			sqlService.dbWrite(HierarchyPropertySqlReader.UPDATE_SQL,HierarchyPropertySqlReader.getUpdateObjects(hierarchyProperty));
		}
		
	}

	public void delete(Hierarchy hierarchy)
	{
		sqlService.dbWrite(HierarchySqlReader.DELETE_SQL,HierarchySqlReader.getDeleteObjects(hierarchy));

	}
	public void delete(HierarchyProperty hierarchy)
	{
		sqlService.dbWrite(HierarchyPropertySqlReader.DELETE_SQL,HierarchyPropertySqlReader.getDeleteObjects(hierarchy));

	}

	
	
	// utility methods
	
	private List findList(String sql, Object[] params, SqlReader reader)
	{
		try
		{
			List l = sqlService.dbRead(sql, params, reader);
			if (l == null)
			{
				l = new ArrayList();
			}
			return l;
		}
		catch (Exception ex)
		{
			logFail(sql, params, ex);
		}
		return null;
	}

	private Object findFirst(String sql, Object[] params, SqlReader reader)
	{
		try
		{
			List l = sqlService.dbRead(sql, params, reader);
			if (l != null && l.size() > 0)
			{
				return l.get(0);
			}
		}
		catch (Exception ex)
		{
			logFail(sql, params, ex);
		}
		return null;
	}

	private void logFail(String sql, Object[] params, Exception ex)
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
		log.error("Failed to execute " + sql + "with params " + psb, ex);
	}

}