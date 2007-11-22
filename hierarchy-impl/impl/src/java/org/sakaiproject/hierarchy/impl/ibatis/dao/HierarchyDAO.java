package org.sakaiproject.hierarchy.impl.ibatis.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

import com.ibatis.sqlmap.client.SqlMapClient;

public abstract class HierarchyDAO implements
		org.sakaiproject.hierarchy.api.dao.HierarchyDAO {

	private static Log log = LogFactoryImpl.getLog(HierarchyDAO.class);

	private SqlMapClient sqlMapClient;
	
	private SqlService sqlService;

	private IdManager idManager;
	
	private ThreadLocalManager threadLocal;
	
	private static final String SAVE_LIST_NAME = HierarchyDAO.class.getName()
	+ "_saveList";

	public void init() {
		sqlService.ddl(getClass().getClassLoader(), "hierarchy_service");
	}

	public void abort() {
		try {
			sqlMapClient.endTransaction();
		} catch (SQLException e) {
			log.warn("Failed to start transaction.", e);
		}
	}

	public void begin() {

		try {
			sqlMapClient.startTransaction();
		} catch (SQLException e) {
			log.warn("Failed to start transaction.", e);
		}
	}

	public void delete(Hierarchy hierarchy) {

		if (hierarchy.getId() != null)
		{
			try {
			List list = new ArrayList();
			list.add(hierarchy.getId());
			findAllChildren(hierarchy.getId(), list);
			deleteProperties(list);
			deleteNodes(list);
			} catch (SQLException sqle) {
				log.error("bobo", sqle);
			}

		}


	}

	private void deleteNodes(List list) throws SQLException {
		sqlMapClient.delete("Hierarchy.deleteWithIds", list);
	}

	private void deleteProperties(List list) throws SQLException {
		sqlMapClient.delete("HierarchyProperty.deleteWithIds", list);
	}

	private void findAllChildren(String hierarchyId, List list) throws SQLException {
		List children;
		children = (sqlMapClient.queryForList("Hierarchy.getChildIds", hierarchyId));
		list.addAll(children);
		for( Iterator it = children.iterator(); it.hasNext();) {
			findAllChildren((String)it.next(), list);
		}

	}

	public void delete(HierarchyProperty hierarchy) {
		// TODO Auto-generated method stub

	}

	public void end() {
		try {
			sqlMapClient.commitTransaction();
		} catch (SQLException e) {
			log.warn("Failed to start transaction.", e);
		}
	}

	public Hierarchy findHierarchyById(String nodeId) {
		try {
			FirstRowHandler rowHandler = new FirstRowHandler(new HierarchyRowHandler(this,null));
			sqlMapClient.queryWithRowHandler("Hierarchy.getById", nodeId, rowHandler);
			return (Hierarchy)rowHandler.getObject();
		} catch (SQLException e) {
			log.warn("Failed to load hierarchy: " + nodeId, e);
		}
		return null;
	}

	public List findHierarchyByParent(Hierarchy parent) {
		try {
			ListRowHandler rowHandler = new ListRowHandler(new HierarchyRowHandler(this, parent));
			sqlMapClient.queryWithRowHandler("Hierarchy.getByParentId", parent.getId(), rowHandler);
			return rowHandler.getList();
		} catch (SQLException e) {
			log.warn("Failed to load children for: " + parent, e);
		}
		return null;
	}

	public Hierarchy findHierarchyByPathHash(String pathHash) {
		try {
			FirstRowHandler rowHandler = new FirstRowHandler(new HierarchyRowHandler(this, null)); 
			sqlMapClient.queryWithRowHandler("Hierarchy.getByHash", pathHash, rowHandler);
			return (Hierarchy)rowHandler.getObject();
		} catch (SQLException e) {
			log.warn("Failed to load hierarchy: " + pathHash, e);
		}
		return null;
	}

	public List findHierarchyProperties(Hierarchy owner) {
		try {
			ListRowHandler rowHandler = new ListRowHandler(new HierarchyPropertyRowHandler(owner));
			sqlMapClient.queryWithRowHandler("HierarchyProperty.getByNodeId", owner.getId(), rowHandler);
			return rowHandler.getList();
		} catch (SQLException e) {
			log.error("Failed to load properties for: "+ owner, e);
		}
		return Collections.EMPTY_LIST;
	}

	public List findHierarchyRoots() {
		try {
			ListRowHandler rowHandler = new ListRowHandler(new HierarchyRowHandler(this, null));
			sqlMapClient.queryWithRowHandler("Hierarchy.getRoots", rowHandler);
			return rowHandler.getList();
		} catch (SQLException e) {
			log.warn("Failed to load root hierarchies.", e);
		}
		return null;
	}

	public void saveOrUpdate(Hierarchy hierarchy) throws HierarchyServiceException {
		if (!isInSaveStack(hierarchy)) {
			pushSaveStack(hierarchy);
			try {
				// First walk to the root of the tree.
				if (hierarchy.getParent() != null) {
					saveOrUpdate(hierarchy.getParent());
				}
				if (hierarchy.isModified()) {
					if (hierarchy.getId() == null) {
						HierarchyWrapper wrapped = new HierarchyWrapper(hierarchy);
						wrapped.setId(idManager.createUuid());
						wrapped.setOldVersion(wrapped.getVersion());
						wrapped.setVersion(new Date());
						wrapped.setParentId(hierarchy.getParent()==null?null:hierarchy.getParent().getId());
						try {
							sqlMapClient.insert("Hierarchy.insert", wrapped);
						} catch (SQLException e) {
							log.error("Failed to insert hierarchy: "
									+ hierarchy, e);
						}
					} else {
						try {
							HierarchyWrapper wrapped = new HierarchyWrapper(hierarchy);
							wrapped.setOldVersion(wrapped.getVersion());
							wrapped.setVersion(new Date());
							wrapped.setParentId(hierarchy.getParent()==null?null:hierarchy.getParent().getId());
							if (sqlMapClient.update("Hierarchy.update", wrapped) != 1) {
								throw new HierarchyServiceException("Failed to save hierarchy, locking failed on :"+ hierarchy);
							}
						} catch (SQLException e) {
							log.error("Failed to insert hierarchy: "
									+ hierarchy, e);
						}
					}
					// save properties and nodes
					// TODO would be nice to know if the children were loaded.
					for (Iterator i = hierarchy.getChildren().values()
							.iterator(); i.hasNext();) {
						Hierarchy child = (Hierarchy) i.next();
						if (!hierarchy.equals(child.getParent())) {
							child.setParent(hierarchy);
						}
						saveOrUpdate(child);
					}
					for (Iterator i = hierarchy.getProperties().values()
							.iterator(); i.hasNext();) {
						HierarchyProperty hp = (HierarchyProperty) i.next();
						saveOrUpdate(hp);
					}
					hierarchy.setModified(false);
				}
			} finally {
				popSaveStack(hierarchy);
			}
		}

	}

	public void saveOrUpdate(HierarchyProperty hierarchyProperty) throws HierarchyServiceException {
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
			if (hierarchyProperty.getId() == null)
			{
				hierarchyProperty.setId(idManager.createUuid());
				hierarchyProperty.setVersion(new Date());
				HierarchyPropertyWrapper wrapper = new HierarchyPropertyWrapper(hierarchyProperty);
				try {
					sqlMapClient.insert("HierarchyProperty.insert", wrapper);
				} catch (SQLException e) {
					log.error("Failed to insert property: "+ hierarchyProperty, e);
				}
			}
			else
			{
				try {
					HierarchyPropertyWrapper wrapper = new HierarchyPropertyWrapper(hierarchyProperty);
					wrapper.setOldVersion(wrapper.getVersion());
					wrapper.setVersion(new Date());
					if (sqlMapClient.update("HierarchyProperty.update", hierarchyProperty) != 1) {
						throw new HierarchyServiceException("Failed to save hierarchy property, is has been modified: "+ hierarchyProperty);
					}
				} catch (SQLException e) {
					log.error("Failed to update property: "+ hierarchyProperty, e);
				}
			}
			hierarchyProperty.setModified(false);
		}

	}
	

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
		List l = (List) threadLocal.get(SAVE_LIST_NAME);
		if (l == null)
		{
			l = new ArrayList();
			threadLocal.set(SAVE_LIST_NAME, l);

		}
		return l;
	}

	public IdManager getIdManager() {
		return idManager;
	}

	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}

	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public ThreadLocalManager getThreadLocal() {
		return threadLocal;
	}

	public void setThreadLocal(ThreadLocalManager threadLocal) {
		this.threadLocal = threadLocal;
	}

	public SqlService getSqlService() {
		return sqlService;
	}

	public void setSqlService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

}
