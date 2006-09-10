package org.sakaiproject.hierarchy.impl;

import java.security.MessageDigest;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.HierarchyService;
import org.sakaiproject.hierarchy.api.dao.HierarchyDAO;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

public class HierarchyServiceImpl implements HierarchyService
{
	private static final Log log = LogFactory
			.getLog(HierarchyServiceImpl.class);

	private static final String SEPERATOR = "/";

	private HierarchyDAO hierarchyDao = null;

	private MessageDigest digest;

	public void init()
	{
		try
		{
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			log
					.info(" ==================================== init HierarchyServiceImpl ");
			digest = MessageDigest.getInstance("SHA1");
		}
		catch (Exception e)
		{
			log
					.error(
							"Failed to start up the HierarchyService, please investigate ",
							e);
			System.exit(-1);
		}
	}

	/* Dependencies */

	/**
	 * Dependency, the injected DAO
	 * 
	 * @param hierarchyDao
	 */

	public HierarchyDAO getHierarchyDao()
	{
		return hierarchyDao;
	}

	/**
	 * Dependency, the injected DAO
	 * 
	 * @param hierarchyDao
	 */
	public void setHierarchyDao(HierarchyDAO hierarchyDao)
	{
		this.hierarchyDao = hierarchyDao;
	}


	public List getRootNodes()
	{

		return hierarchyDao.findHierarchyRoots();
	}

	private static char[] encode = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * create a hash of the path
	 * 
	 * @param nodePath
	 * @param encode
	 * @return
	 */
	private String hash(String nodePath)
	{
		byte[] b = digest.digest(nodePath.getBytes());
		char[] c = new char[b.length * 2];
		for (int i = 0; i < b.length; i++)
		{
			c[i * 2] = encode[b[i]&0x0f];
			c[i * 2 + 1] = encode[(b[i]>>4)&0x0f];
		}
		return new String(c);
	}




	public Hierarchy getNode(String nodePath)
	{
		String nodeId = hash(nodePath);
		return hierarchyDao.findHierarchyByNodeId(nodeId);
	}


	public void save(Hierarchy hierachy) {
		hierarchyDao.saveOrUpdate((org.sakaiproject.hierarchy.model.Hierarchy) hierachy);
	}


	public void deleteNode(Hierarchy hierachy) 
	{
		hierarchyDao.delete((org.sakaiproject.hierarchy.model.Hierarchy)hierachy);
	}
	
	public Hierarchy newHierarchy(String nodePath) {
		org.sakaiproject.hierarchy.model.Hierarchy h = new org.sakaiproject.hierarchy.model.Hierarchy();
		String nodeId = hash(nodePath);
		h.setNodeid(nodeId);
		h.setName(nodePath);
		return h;
	}
	public HierarchyProperty newHierachyProperty() {
		return new org.sakaiproject.hierarchy.model.HierarchyProperty();
	}

}
