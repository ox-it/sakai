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
		String encoded =  new String(c);
		log.debug("Encoded "+nodePath+" as "+encoded);
		return encoded;
	}




	public Hierarchy getNode(String nodePath)
	{
		String pathHash = hash(nodePath);
		return hierarchyDao.findHierarchyByPathHash(pathHash);
	}


	public void save(Hierarchy hierachy) {
		hierarchyDao.saveOrUpdate( hierachy);
	}


	public void deleteNode(Hierarchy hierachy) 
	{
		hierarchyDao.delete(hierachy);
	}
	
	public Hierarchy newHierarchy(String nodePath) {
		HierarchyImpl h = new HierarchyImpl();
		String pathhash = hash(nodePath);
		h.setPathHash(pathhash);
		h.setPath(nodePath);
		return h;
	}
	public HierarchyProperty newHierachyProperty() {
		return new HierarchyPropertyImpl();
	}

}
