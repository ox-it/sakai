package org.sakaiproject.hierarchy.impl.model.dao;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.hierarchy.impl.HierarchyPropertyImpl;
import org.sakaiproject.id.api.IdManager;

public class HierarchyPropertySqlReader implements SqlReader
{
	private static final Log log = LogFactory.getLog(HierarchyPropertySqlReader.class);
	
	private static final int HIERARCHY_PROP_ID_POS = 1;

	private static final int HIERARCHY_PROP_NAME_POS = 3;

	private static final int HIERARCHY_PROP_NODE_POS = 2;

	private static final int HIERARCHY_PROP_VALUE_POS = 4;

	private static final int HIERARCHY_PROP_VERSION_POS = 5;

	public static final String FIND_BY_NODE_ID_SQL = "select id, node_id, name, propvalue, version from hierarchy_property where node_id = ?";

	public static final String INSERT_SQL = "insert into hierarchy_property (id,node_id,name,propvalue,version) values ( ?,?,?,?,? ) ";
	

	public static final String UPDATE_SQL = "update hierarchy_property set node_id = ?,name = ?, propvalue = ?,version = ? where id = ? and version = ?";

	public static final String DELETE_SQL = "delete from hierarchy_property where id = ? and version = ? ";

	public static final String DELETE_NODE_PROPERTIES_SQL = "delete from hierarchy_property where node_id = ?  ";

	public static final String DELETE_NODE_PROPERTIES_GROUPS_SQL_1 = "delete from hierarchy_property where node_id in ( ";

	public static final String DELETE_NODE_PROPERTIES_GROUPS_SQL_2 = " ) ";



	private Hierarchy owner = null;
	private HierarchyDAO hierarchyDao = null;

	public HierarchyPropertySqlReader(HierarchyDAO hierarchyDao, Hierarchy owner)
	{
	
		this.owner = owner;
		this.hierarchyDao = hierarchyDao;
	}

	public Object readSqlResultRecord(ResultSet result)
	{
		try
		{
			HierarchyPropertyImpl h = new HierarchyPropertyImpl();
			h.setId(result.getString(HIERARCHY_PROP_ID_POS));
			h.setName(result.getString(HIERARCHY_PROP_NAME_POS));
			h.setNode(owner);
			h.setPropvalue(result.getString(HIERARCHY_PROP_VALUE_POS));
			h.setVersion(result.getTimestamp(HIERARCHY_PROP_VERSION_POS));
			h.setModified(false);
			return h;
		}
		catch (Throwable ex)
		{
			log.error("Failed to convert record into HierarhyProperty Object ",ex);
			throw new RuntimeException("Failed to load record ", ex);
		}
	}

	public static Object[] getInsertObjects(HierarchyProperty hierarchyProperty, IdManager idManager)
	{
		String nodeId = hierarchyProperty.getId();
		if ( nodeId == null ) {
			hierarchyProperty.setId(idManager.createUuid());
		}
		Object[] params = new Object[5];
		params[0] = hierarchyProperty.getId();
		params[1] = hierarchyProperty.getNode().getId();
		params[2] = hierarchyProperty.getName();
		params[3] = hierarchyProperty.getPropvalue();
		params[4] = new Timestamp(System.currentTimeMillis());
		return params;
	}

	public static Object[] getUpdateObjects(HierarchyProperty hierarchyProperty)
	{
		Object[] params = new Object[6];
		params[0] = hierarchyProperty.getNode().getId();
		params[1] = hierarchyProperty.getName();
		params[2] = hierarchyProperty.getPropvalue();
		params[3] = new Timestamp(System.currentTimeMillis());
		params[4] = hierarchyProperty.getId();
		params[5] = hierarchyProperty.getVersion();
		hierarchyProperty.setVersion((Date)params[3]);
		return params;
	}

	public static Object[] getDeleteObjects(HierarchyProperty hierarchyProperty)
	{
		Object[] params = new Object[2];
		params[0] = hierarchyProperty.getId();
		params[1] = hierarchyProperty.getVersion();
		return params;
	}

}
