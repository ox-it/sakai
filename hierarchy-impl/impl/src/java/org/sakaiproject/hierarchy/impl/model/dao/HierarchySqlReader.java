package org.sakaiproject.hierarchy.impl.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.hierarchy.api.model.Hierarchy;

public class HierarchySqlReader implements SqlReader
{

	private static final int HIERARCHY_ID_POS = 0;

	private static final int HIERARCHY_NAME_POS = 0;

	private static final int HIERARCHY_NODE_POS = 0;

	private static final int HIERARCHY_PARENT_ID = 0;

	private static final int HIERARCHY_REALM_POS = 0;

	private static final int HIERARCHY_VERSION_POS = 0;

	public static final String FIND_ROOTS = null;

	public static final String FIND_BY_NODE_ID_SQL = null;

	public static final String FIND_BY_PARENT_ID_SQL = null;

	public static final String INSERT_SQL = null;

	public static final String UPDATE_SQL = null;

	public static final String DELETE_SQL = null;

	private Hierarchy owner = null;
	
	private HierarchyDAO dao = null;
	public HierarchySqlReader(HierarchyDAO dao, Hierarchy owner) {
		this.owner = owner;
		this.dao = dao;
	}
	public HierarchySqlReader(HierarchyDAO dao) {
		this.dao = dao;
	}

	public Object readSqlResultRecord(ResultSet result)
	{
		try
		{
			org.sakaiproject.hierarchy.model.Hierarchy h = new org.sakaiproject.hierarchy.model.Hierarchy();
			h.setId(result.getString(HIERARCHY_ID_POS));
			h.setName(result.getString(HIERARCHY_NAME_POS));
			h.setNodeid(result.getString(HIERARCHY_NODE_POS));
			if ( owner == null ) {
				h.setParent(new LazyHierarchyParent(dao,result
					.getString(HIERARCHY_PARENT_ID)));
			} else {
				h.setParent(owner);
			}
			h.setRealm(result.getString(HIERARCHY_REALM_POS));
			h.setVersion(result.getTimestamp(HIERARCHY_VERSION_POS));
			h.setChildren(new LazyHierarchyChildren(dao,h));
			h.setProperties(new LazyHierarchyProperties(dao,h));
			return h;
		}
		catch (SQLException ex)
		{
			throw new RuntimeException("Failed to load record ",ex);

		}
	}
	public static Object[] getUpdateObjects(Hierarchy hierarchy)
	{
		// TODO Auto-generated method stub
		return null;
	}
	public static Object[] getInsertObjects(Hierarchy hierarchy)
	{
		// TODO Auto-generated method stub
		return null;
	}
	public static Object[] getDeleteObjects(Hierarchy hierarchy)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
