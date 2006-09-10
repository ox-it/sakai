package org.sakaiproject.hierarchy.impl.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

public class HierarchyPropertySqlReader implements SqlReader
{
	private static final int HIERARCHY_PROP_ID_POS = 0;

	private static final int HIERARCHY_PROP_NAME_POS = 0;

	private static final int HIERARCHY_PROP_NODE_POS = 0;

	private static final int HIERARCHY_PROP_VALUE_POS = 0;

	private static final int HIERARCHY_PROP_VERSION_POS = 0;

	public static final String FIND_BY_NODE_ID_SQL = null;

	public static final String INSERT_SQL = null;

	public static final String UPDATE_SQL = null;

	public static final String DELETE_SQL = null;

	private Hierarchy owner = null;

	public HierarchyPropertySqlReader( Hierarchy owner)
	{
		this.owner = owner;
	}

	public Object readSqlResultRecord(ResultSet result)
	{
		try {
		org.sakaiproject.hierarchy.model.HierarchyProperty h = new org.sakaiproject.hierarchy.model.HierarchyProperty();
		h.setId(result.getString(HIERARCHY_PROP_ID_POS));
		h.setName(result.getString(HIERARCHY_PROP_NAME_POS));
		h.setNode(owner);
		h.setPropvalue(result.getString(HIERARCHY_PROP_VALUE_POS));
		h.setVersion(result.getTimestamp(HIERARCHY_PROP_VERSION_POS));
		return h;
		} catch ( SQLException ex)  {
			throw new RuntimeException("Failed to load record ",ex);
		}
	}

	public static Object[] getInsertObjects(HierarchyProperty hierarchyProperty)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static Object[] getUpdateObjects(HierarchyProperty hierarchyProperty)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static Object[] getDeleteObjects(HierarchyProperty hierarchy)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
