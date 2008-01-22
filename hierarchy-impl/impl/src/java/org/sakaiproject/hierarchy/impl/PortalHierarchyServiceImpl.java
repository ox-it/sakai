package org.sakaiproject.hierarchy.impl;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNodeDao;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;

public class PortalHierarchyServiceImpl implements PortalHierarchyService {

	private static Log log = LogFactory.getLog(PortalHierarchyServiceImpl.class);
	private static final String CURRENT_PATH = PortalHierarchyServiceImpl.class.getName()+ "#current"; 
	
	private PortalPersistentNodeDao dao;
	private HierarchyService hierarchyService;
	private SiteService siteService;
	private ThreadLocalManager threadLocalManager;
	private SessionManager sessionManager;
	
	
	private String hierarchyId;
	
	public void changeSite(String id, String newSiteId) {
		PortalPersistentNode node = dao.findById(id);
		node.setSiteId(newSiteId);
		dao.save(node);
	}

	public void deleteNode(String id) {
		HierarchyNode node = hierarchyService.getNodeById(id);
		if (!node.childNodeIds.isEmpty()) {
			throw new IllegalStateException("Can't delete a node with children.");
		}
		hierarchyService.removeNode(node.id);
		dao.delete(node.id);
	}

	public PortalNode getCurrentPortalNode() {
		String path = getCurrentPortalPath();
		PortalNode node = null;
		if(path != null) {
			node = getNode(path);
		}
		return node;
	}

	public String getCurrentPortalPath() {
		return (String)sessionManager.getCurrentSession().getAttribute(CURRENT_PATH);
	}

	public PortalNode getNode(String portalPath) {
		String hash = hash((portalPath==null)?"/":portalPath);
		PortalPersistentNode portalPersistentNode = dao.findByPathHash(hash);
		PortalNodeImpl portalNode = populatePortalNode(portalPersistentNode);
		return portalNode;
	}

	protected PortalNodeImpl populatePortalNode(PortalPersistentNode portalPersistentNode) {
		PortalNodeImpl portalNode = null;
		if (portalPersistentNode != null) {
			portalNode = new PortalNodeImpl();
			portalNode.setId(portalPersistentNode.getId());
			portalNode.setName(portalPersistentNode.getName());
			portalNode.setPath(portalPersistentNode.getPath());
			try {
				Site portalSite = siteService.getSite(portalPersistentNode.getSiteId());
				portalNode.setSite(portalSite);
			} catch (IdUnusedException e) {
				log.warn("Couldn't find portal site "+ portalPersistentNode.getSiteId()+ " for "+ portalPersistentNode.getPath());
			}
			try {
				Site managementSite = siteService.getSite(portalPersistentNode.getManagementSiteId());
				portalNode.setManagementSite(managementSite);
			} catch (IdUnusedException e) {
				log.warn("Couldn't find management site "+ portalPersistentNode.getManagementSiteId()+ " for "+ portalPersistentNode.getPath());
			}
			
		}
		return portalNode;
	}

	public PortalNode getNodeById(String id) {
		PortalPersistentNode node = dao.findById(id);
		return populatePortalNode(node);
	}

	public List<PortalNode> getNodeChildren(String nodeId) {
		Set<HierarchyNode> nodes = hierarchyService.getChildNodes(nodeId, true);
		List<PortalNode> portalNodes = new ArrayList<PortalNode>(nodes.size());
		for (HierarchyNode node : nodes) {
			PortalNode portalNode = populatePortalNode(dao.findById(node.id)); 
			if (portalNode != null) {
				portalNodes.add(portalNode);
			}
		}
		return portalNodes;
	}

	public List<PortalNode> getNodesFromRoot(String nodeId) {
		Set<HierarchyNode> nodes = hierarchyService.getParentNodes(nodeId, false);
		List<HierarchyNode> sortedNodes = new ArrayList<HierarchyNode>(nodes.size());
		
		for (HierarchyNode node : nodes) {
			// Find the root.
			if (node.directParentNodeIds.isEmpty()){
				sortedNodes.add(node);
				nodes.remove(node);
				String parentId = node.id;
				// Now work through the rest, resetting when we find one.
				for (Iterator<HierarchyNode> it = nodes.iterator(); it.hasNext();) {
					HierarchyNode node2 = it.next();
					if (node2.directParentNodeIds.contains(parentId)) {
						sortedNodes.add(node2);
						nodes.remove(node2);
						parentId = node2.id;
						it = nodes.iterator();
					}
					
				}
				break;
			}
		}
		
		if (!nodes.isEmpty()) {
			log.warn("We got back more parent nodes than we managed to match to the hierarchy.");
		}
		
		List<PortalNode> portalNodes = convertHierarchyNodes(sortedNodes);
		
		return portalNodes;
	}

	private List<PortalNode> convertHierarchyNodes(List<HierarchyNode> nodes) {
		List<PortalNode> portalNodes = new ArrayList<PortalNode>(nodes.size());
		for (HierarchyNode node: nodes) {
			portalNodes.add(populatePortalNode(dao.findById(node.id)));
		}
		return portalNodes;
	}
	
	protected List<PortalNode> populatePortalNodes(List<PortalPersistentNode> nodes) {
		List<PortalNode> portalNodes = new ArrayList<PortalNode>(nodes.size());
		for (PortalPersistentNode node : nodes) {
			portalNodes.add(populatePortalNode(node));
		}
		return portalNodes;
	}

	public List<PortalNode> getNodesWithSite(String siteId) {
		List<PortalPersistentNode> nodes =  dao.findBySiteId(siteId);
		return populatePortalNodes(nodes);
	}

	public void moveNode(String id, String newParent) {
		// TODO Auto-generated method stub
		
	}

	public PortalNode newNode(String parentId, String childName, String siteId, String managementSiteId) {
		if (!siteService.siteExists(siteId))
			throw new IllegalArgumentException("Site does not exist: "+ siteId);
		
		PortalNode parent = getNodeById(parentId);
		if (parent == null)
			throw new IllegalArgumentException("Parent site could not be found: "+ parentId);
		String childPath = (parent.getPath().equals("/"))?"/" + childName: parent.getPath() + "/" + childName;
		List<PortalNode> children =  getNodeChildren(parentId);
		for (PortalNode child: children) {
			if (child.getName().equals(childName))
				throw new IllegalArgumentException("Child site of this name already exists: "+ childName);
		}
		
		HierarchyNode node = hierarchyService.addNode(hierarchyId, parentId);
		PortalPersistentNode portalNode = new PortalPersistentNode();
		portalNode.setId(node.id);
		portalNode.setName(childName);
		portalNode.setSiteId(siteId);
		portalNode.setManagementSiteId(managementSiteId);
		portalNode.setPath(childPath);
		portalNode.setPathHash(hash(childPath));
		dao.save(portalNode);
		
		return populatePortalNode(portalNode);
		
	}

	public void renameNode(String id, String newPath) {
		// TODO Auto-generated method stub
		
	}

	public void setCurrentPortalPath(String portalPath) {
		sessionManager.getCurrentSession().setAttribute(CURRENT_PATH, portalPath);
	}
	

	private static char[] encode = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static ThreadLocal<MessageDigest> digest = new ThreadLocal<MessageDigest>();

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
		MessageDigest mdigest  = digest.get();
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

	public PortalPersistentNodeDao getDao() {
		return dao;
	}

	public void setDao(PortalPersistentNodeDao dao) {
		this.dao = dao;
	}

	public HierarchyService getHierarchyService() {
		return hierarchyService;
	}

	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public String getHierarchyId() {
		return hierarchyId;
	}

	public void setHierarchyId(String hierarchyId) {
		this.hierarchyId = hierarchyId;
	}

	public void init() {
		HierarchyNode root = hierarchyService.getRootNode(hierarchyId);
		// How to stop race in 
		if (root == null ) {
			hierarchyService.createHierarchy(hierarchyId);
		}
		HierarchyNode node = hierarchyService.getRootNode(hierarchyId);
		if (node == null)
			throw new IllegalStateException("No root node.");
		if (dao.findById(node.id) == null) {
			PortalPersistentNode portalNode = new PortalPersistentNode();
			portalNode.setId(node.id);
			portalNode.setName("");
			portalNode.setSiteId("!gateway");
			portalNode.setManagementSiteId("!hierarchy");
			portalNode.setPath("/");
			portalNode.setPathHash(hash("/"));
			dao.save(portalNode);
		}
		
	}

	public ThreadLocalManager getThreadLocalManager() {
		return threadLocalManager;
	}

	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

}
