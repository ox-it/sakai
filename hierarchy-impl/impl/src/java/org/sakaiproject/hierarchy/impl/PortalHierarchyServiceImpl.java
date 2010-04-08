package org.sakaiproject.hierarchy.impl;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
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
	private static final String CURRENT_NODE = PortalHierarchyServiceImpl.class.getName()+ "#current"; 
	
	private PortalPersistentNodeDao dao;
	private HierarchyService hierarchyService;
	private SiteService siteService;
	private ThreadLocalManager threadLocalManager;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private EventTrackingService eventTrackingService;

	private String hierarchyId;
	
	private String missingSiteId;
	
	private boolean autoDDL;
	
	public void changeSite(String id, String newSiteId) throws PermissionException {
		unlockNodeSite(id);
		PortalPersistentNode node = dao.findById(id);
		try {
			Site site = siteService.getSite(newSiteId);
			if (!securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
				throw new PermissionException(sessionManager.getCurrentSession().getUserEid(), SiteService.SECURE_UPDATE_SITE, site.getReference());
			}
			node.setSiteId(newSiteId);
			dao.save(node);
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("Couldn't find site: "+ newSiteId);
		}
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_MODIFY, id, true));
	}

	public void deleteNode(String id) {
		if (!canDeleteNode(id)) {
			throw new IllegalStateException("Can't delete this node.");
		}
		List<PortalNode> children = getNodeChildren(id);
		// Remove children.
		for (PortalNode node: children) {
			deleteNode(node.getId());
		}
		hierarchyService.removeNode(id);
		dao.delete(id);
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_DELETE, id, true));
	}

	public String getCurrentPortalPath() {
		PortalNode node = getCurrentPortalNode();
		return (node==null)?null:node.getPath();
	}

	public PortalNode getCurrentPortalNode() {
		return (PortalNode)threadLocalManager.get(CURRENT_NODE);
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
			portalNode = new PortalNodeImpl(securityService, siteService);
			portalNode.setId(portalPersistentNode.getId());
			portalNode.setName(portalPersistentNode.getName());
			portalNode.setPath(portalPersistentNode.getPath());
			try {
				Site portalSite = siteService.getSite(portalPersistentNode.getSiteId());
				portalNode.setSite(portalSite);
			} catch (IdUnusedException iue) {
				log.debug("Couldn't find portal site "+ portalPersistentNode.getSiteId()+ " for "+ portalPersistentNode.getPath());
				try {
					Site missingSite = siteService.getSite(missingSiteId);
					portalNode.setSite(missingSite);
				} catch (IdUnusedException iue2 ) {
					log.warn("Couldn't find missing site "+ missingSiteId);
					return null;
				}
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

	public void moveNode(String id, String newParentId) throws PermissionException {
		if (!canMoveNode(id)) {
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), null, id);
		}
		PortalNode toMove = getNodeById(id);
		PortalNode newParent = getNodeById(newParentId);
		if (toMove == null) {
			throw new IllegalArgumentException("Couldn't find node to move: "+ id);
		}
		if (newParent == null) {
			throw new IllegalArgumentException("Couldn't find node to move to: "+ newParentId);
		}
		copyNodes(toMove, newParentId);
		deleteNode(id);
	}
	
	private void copyNodes(PortalNode node, String newParentId) throws PermissionException {
		PortalNode newNode = newNode(newParentId, node.getName(), node.getSite().getId(), node.getManagementSite().getId());
		for (PortalNode child: getNodeChildren(node.getId())) {
			copyNodes(child, newNode.getId());
		}
	}

	public PortalNode newNode(String parentId, String childName, String siteId, String managementSiteId) throws PermissionException {
		unlockNodeSite(parentId);
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
		
		List<PortalNode> parents = getNodesFromRoot(parentId);
		for (PortalNode parentNode: parents) {
			if (siteId.equals(parentNode.getSite().getId()))
				throw new IllegalArgumentException("Site: "+ siteId+ " already used in parent: "+ parentNode.getPath());
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
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_NEW, node.id, true));
		return populatePortalNode(portalNode);
		
	}
	

	public PortalNode getDefaultNode(String siteId) {
		List<PortalPersistentNode> nodes =  dao.findBySiteId(siteId);
		if (nodes.size() == 0) {
			return null;
		}
		if (nodes.size() > 1) {
			Comparator<PortalPersistentNode> comp = new Comparator<PortalPersistentNode>() {

				public int compare(PortalPersistentNode o1, PortalPersistentNode o2)
				{
					Date o1Created = o1.getCreated();
					Date o2Created = o2.getCreated();
					if (o1Created == null) {
						o1Created = new Date(0);
					}
					if (o2Created == null) {
						o2Created = new Date(0);
					}
					
					return o1Created.compareTo(o2Created);
				}
			};
			Collections.sort(nodes, comp);
		}
		return populatePortalNode(nodes.get(0));
	}

	public void renameNode(String id, String newPath) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	public void setCurrentPortalNode(PortalNode node) {
		threadLocalManager.set(CURRENT_NODE, node);
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

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public EventTrackingService getEventTrackingService() {
		return eventTrackingService;
	}

	public String getHierarchyId() {
		return hierarchyId;
	}

	public void setHierarchyId(String hierarchyId) {
		this.hierarchyId = hierarchyId;
	}

	public void init() {
		
		if (autoDDL) {
			initDefaultContent();
		}
		
	}

	private void initDefaultContent() {
		HierarchyNode root = hierarchyService.getRootNode(hierarchyId);
		
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

	public String getMissingSiteId()
	{
		return missingSiteId;
	}

	public void setMissingSiteId(String missingSiteId)
	{
		this.missingSiteId = missingSiteId;
	}

	private void unlockNodeSite(String id) throws PermissionException {
		PortalNode node = getNodeById(id);
		if (node != null) {
			Site site = node.getSite();
			if (securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
				return;
			} else {
				throw new PermissionException(sessionManager.getCurrentSession().getUserEid(), SiteService.SECURE_UPDATE_SITE, site.getReference());
			}
		}
		throw new PermissionException(sessionManager.getCurrentSession().getUserEid(), SiteService.SECURE_UPDATE_SITE, null);
	}
	
	private boolean unlockCheckNodeSite(String id) {
		PortalNode node = getNodeById(id);
		if (node != null) {
			Site site = node.getSite();
			return securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference());
		}
		return false;
	}

	public boolean canChangeSite(String id) {
		return unlockCheckNodeSite(id);
	}

	/**
	 * @return <code>true</code> if the node has no children or the user is a sysadmin.
	 */
	public boolean canDeleteNode(String id) {
		List<PortalNode> nodes = getNodeChildren(id);
		if (nodes.size() > 0) {
			return securityService.isSuperUser();
		}
		return unlockCheckNodeSite(id);
	}

	public boolean canMoveNode(String id) {
		return securityService.isSuperUser();
	}

	public boolean canNewNode(String parentId) {
		return unlockCheckNodeSite(parentId);
	}

	public boolean canRenameNode(String id) {
		return unlockCheckNodeSite(id);
	}

	public boolean isAutoDDL() {
		return autoDDL;
	}

	public void setAutoDDL(boolean autoDDL) {
		this.autoDDL = autoDDL;
	}

}
