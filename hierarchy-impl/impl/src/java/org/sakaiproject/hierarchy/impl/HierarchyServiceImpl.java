package org.sakaiproject.hierarchy.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.hierarchy.api.HierarchyService;
import org.sakaiproject.hierarchy.api.HierarchyServiceException;
import org.sakaiproject.hierarchy.api.dao.HierarchyDAO;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

public class HierarchyServiceImpl implements HierarchyService
{
	private static final Log log = LogFactory
			.getLog(HierarchyServiceImpl.class);

	private static final String SEPERATOR = "/";

	private HierarchyDAO hierarchyDao = null;


	public void init()
	{
		try
		{
			log
					.info(" ==================================== init HierarchyServiceImpl ");
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

	private static ThreadLocal digest = new ThreadLocal();
	/**
	 * create a hash of the path
	 * 
	 * @param nodePath
	 * @param encode
	 * @return
	 * @throws NoSuchAlgorithmException 
	 */
	public static String hash(String nodePath) 
	{
		MessageDigest mdigest  = (MessageDigest) digest.get();
		if ( mdigest == null ) {
			try
			{
				mdigest = MessageDigest.getInstance("SHA1");
			}
			catch (NoSuchAlgorithmException e)
			{
				log.error("Cant find Hash Algorithm ",e);
			}
			digest.set(mdigest);
		}
		byte[] b = mdigest.digest(nodePath.getBytes());
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
	
	public Hierarchy newHierarchy(String nodePath) throws HierarchyServiceException {
		if ( nodePath == null ) {
			throw new HierarchyServiceException("Node Path cannot be null");
		}
		if ( !nodePath.startsWith("/") ) {
			throw new HierarchyServiceException("Node Path must start with a / ");
		}
		if ( nodePath.length() <= 1 ) {
			throw new HierarchyServiceException("Cant create the / node, it already exists ");
		}
		HierarchyImpl h = new HierarchyImpl();
		h.setPath(nodePath);
		return h;
	}
	public HierarchyProperty newHierachyProperty() {
		return new HierarchyPropertyImpl();
	}
	
	public void begin() {
		hierarchyDao.begin();
	}
	public void end() {
		hierarchyDao.end();
	}
	
	public void abort() {
		hierarchyDao.abort();
	}

	public String getCurrentPortalPath()
	{
		Session session = SessionManager.getCurrentSession();
		return (String) session.getAttribute("portal-hierarchy-path");
	}

	public void setCurrentPortalPath(String portalPath)
	{
		Session session = SessionManager.getCurrentSession();
		session.setAttribute("portal-hierarchy-path", portalPath);
	}

	public Hierarchy getCurrentPortalNode()
	{
		String portalPath = getCurrentPortalPath();
		if (portalPath == null)
		{
			portalPath = "/";
		}
		return getNode(portalPath);
	}

}
