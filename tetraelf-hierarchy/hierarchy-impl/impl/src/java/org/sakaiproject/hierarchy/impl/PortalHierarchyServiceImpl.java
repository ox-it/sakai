package org.sakaiproject.hierarchy.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeRedirect;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNodeDao;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * This service joins together 2 services. A Tree service (HierarchyService) and a simple node persistence
 * service which stores the data about each node. We never invalidate parent paths, we just copy and delete things
 * when moving them around and you can't rename a parent path.
 * 
 * @author buckett
 */
public class PortalHierarchyServiceImpl implements PortalHierarchyService {

    private static Log log = LogFactory.getLog(PortalHierarchyServiceImpl.class);
	
	// Used for a threadlocal storage of the current node.
	private static final String CURRENT_NODE = PortalHierarchyServiceImpl.class.getName()+ "#current";
	
	// This is the PREFIX used for our cache entries and our event references.
	private static final String PREFIX = Entity.SEPARATOR+ "portalnode";

	// Our Dependencies that should be injected
	private PortalPersistentNodeDao dao;
	private HierarchyService hierarchyService;
	private SiteService siteService;
	private ThreadLocalManager threadLocalManager;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private EventTrackingService eventTrackingService;
	private FunctionManager functionManager;
	private MemoryService memoryService;

	// Cache hold node ID to PortalPersistentNode. This is the main cache for the hierarchy service which is used
	// whenever any details about a node are looked up. Invalidation is done on events.
	private Cache<String, PortalPersistentNode> idToNodeCache;
	// Cache holding path to node ID. Eg /dept/history/year1 to 12345. This is a derived cache from the idToNodeCache
	// and does it's invalidation using that cache.
	private Cache<String, String> pathToIdCache;
	
	// This cache holds the site ID to default node ID. It has null values when the site isn't in the hierarchy.
	// Cache invalidation is done when new nodes are created or updates to existing nodes happen. This at the 
	// moment is done through DB lookups from events.
	private Cache<String, String> siteToNodeCache;


	// These two caches map node ID to collections of other node IDs. They are invalidated through events.
	private Cache<String, Collection<String>> idChildrenCache;
	private Cache<String, Collection<String>> idParentsCache;

	/**
	 * The ID of the portal hierarchy in the hierarchy service. 
	 */
	private String hierarchyId;

	/**
	 * The Site ID of the site that is displayed when the site at a node can't be found.
	 * This normally happens because someone has deleted a site but there are still nodes in the
	 * hierarchy that reference the now deleted site.
	 */
	private String missingSiteId;

	private boolean autoDDL;

	public void changeSite(String id, String newSiteId) throws PermissionException {
		PortalPersistentNode node = dao.findById(id);
		if (node == null) {
			throw new IllegalArgumentException("Couldn't find node with id: "+ id);
		}
		if (node.getRedirectUrl() != null) {
			throw new IllegalArgumentException("Can't change the site on a redirect node: "+ id);
		}
		try {
			Site site = siteService.getSite(newSiteId);
			if (!canChangeSite(id)) {
				throw new PermissionException(sessionManager.getCurrentSession().getUserEid(), SECURE_MODIFY,
						siteService.siteReference(node.getSiteId()));
			}

			if (!securityService.unlock(SiteService.SECURE_UPDATE_SITE, site.getReference())) {
				throw new PermissionException(sessionManager.getCurrentSession().getUserEid(), SiteService.SECURE_UPDATE_SITE, site.getReference());
			}
			node.setSiteId(newSiteId);
			dao.save(node);
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("Couldn't find site: "+ newSiteId);
		}
		// Do cache invalidation off events
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_MODIFY, toRef(id), true));
	}

	public void deleteNode(String id) throws PermissionException {
		if (!canDeleteNode(id)) {
			throw new PermissionException(sessionManager.getCurrentSession().getUserEid(), SECURE_DELETE, getSiteReference(id));
		}
		// How to invalidate parent children, should we keep them cached 
		List<PortalNode> children = getNodeChildren(id);
		// Remove children.
		for (PortalNode node: children) {
			deleteNode(node.getId());
		}
		HierarchyNode parentNode = hierarchyService.removeNode(id);
		dao.delete(id);
		// Do cache invalidation off events.
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_DELETE, toRef(id), true));
		// This should remove the cache of children for the parent node.
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_MODIFY, toRef(parentNode.id), true));
	}

	public String getCurrentPortalPath() {
		String path = (String) threadLocalManager.get("portal.original.siteId");
		return path;
	}

	public PortalNodeSite getCurrentPortalNode() {
		String path = getCurrentPortalPath();
		if (path == null) {
			return null;
		} else {
			return (PortalNodeSite) getNodeByUrl(path);
		}
	}

	public PortalNode getNodeByUrl(String urlPath) {
		String portalPath = urlPath;
		if (portalPath != null) {
			portalPath = portalPath.replace(SEPARATOR, "/");
		}
		return getNode(portalPath);
	}

	public PortalNode getNode(String portalPath) {
		String lookup = (portalPath==null || portalPath.isEmpty())?"/":portalPath;
		PortalNode portalNode;
		String nodeId = pathToIdCache.get(lookup);
		if (nodeId != null) {
			portalNode = getNodeById(nodeId);
			if(portalNode == null) {
				// We may end up here if invalidation is happening while we're looking up the node as there
				// isn't any cross cache consistency on derived caches. Just clean up and carry on.
				pathToIdCache.remove(lookup);
			}
		} else {
			// Only do the hashing for the DB.
			// Once we've updated all the entries in the DB we can revert to using the single hash lookup
			PortalPersistentNode portalPersistentNode = dao.findByPathHash(hash(lookup), sha1(lookup));
			if (portalPersistentNode != null) {
				// This might already be cached.
				idToNodeCache.put(toRef(portalPersistentNode.getId()), portalPersistentNode);
				pathToIdCache.put(portalPath, portalPersistentNode.getId());
				// We don't need to put it in the pathToIdCache as it will get the data through
				// being a derrived cache.
			}
			portalNode = populatePortalNode(portalPersistentNode);
		}
		return portalNode;
	}

	private PortalNode populatePortalNode(PortalPersistentNode portalPersistentNode) {
		PortalNodeImpl portalNode = null;

		if (portalPersistentNode != null) {
			if (portalPersistentNode.getRedirectUrl() != null) {
				portalNode = populatePortalNodeRedirect(portalPersistentNode);
			} else {
				portalNode = populatePortalNodeSite(portalPersistentNode);
			}
			portalNode.setId(portalPersistentNode.getId());
			portalNode.setName(portalPersistentNode.getName());
			portalNode.setPath(portalPersistentNode.getPath());
		}
		return portalNode;
	}

	private PortalNodeImpl populatePortalNodeRedirect(
			PortalPersistentNode portalPersistentNode) {
		PortalNodeRedirectImpl portalNode = new PortalNodeRedirectImpl(this);
		portalNode.setUrl(portalPersistentNode.getRedirectUrl());
		portalNode.setAppendPath(portalPersistentNode.isAppendPath());
		portalNode.setTitle(portalPersistentNode.getRedirectTitle());
		portalNode.setHidden(portalPersistentNode.isHidden());
		return portalNode;
	}

	private PortalNodeImpl populatePortalNodeSite(PortalPersistentNode portalPersistentNode) {
		PortalNodeSiteImpl portalNode = new PortalNodeSiteImpl(securityService,siteService);
		try {
			Site portalSite = siteService.getSite(portalPersistentNode.getSiteId());
			portalNode.setSite(portalSite);
		} catch (IdUnusedException iue) {
			log.debug("Couldn't find portal site "+ portalPersistentNode.getSiteId()+ " for "+ portalPersistentNode.getPath());
			try {
				Site missingSite = siteService.getSite(missingSiteId);
				portalNode.setSite(missingSite);
			} catch (IdUnusedException iue2 ) {
				log.error("Couldn't find missing site "+ missingSiteId);
				// Just return a node without a site.
			}
		}
		try {
			Site managementSite = siteService.getSite(portalPersistentNode.getManagementSiteId());
			portalNode.setManagementSite(managementSite);
		} catch (IdUnusedException e) {
			log.error("Couldn't find management site "+ portalPersistentNode.getManagementSiteId()+ " for "+ portalPersistentNode.getPath());
		}
		return portalNode;
	}

	public PortalNode getNodeById(String id) {
		PortalPersistentNode node = idToNodeCache.get(toRef(id));
		if (node == null) {
			node = dao.findById(id);
			if (node != null) {
				idToNodeCache.put(toRef(id), node);
			}
		}
		return populatePortalNode(node);
	}

	@SuppressWarnings("unchecked")
	public List<PortalNode> getNodeChildren(String id) {
		Collection<String> nodeIds = idChildrenCache.get(toRef(id));
		if (nodeIds == null) {
			Set<HierarchyNode> nodes = hierarchyService.getChildNodes(id, true);
			nodeIds = new ArrayList<>(nodes.size());
			for(HierarchyNode node : nodes) {
				nodeIds.add(node.id);
			}
			idChildrenCache.put(toRef(id), Collections.unmodifiableCollection(nodeIds));
		}
		
		List<PortalNode> portalNodes = new ArrayList(nodeIds.size());
		for (String nodeId : nodeIds) {
			PortalNode portalNode = getNodeById(nodeId);
			if (portalNode != null) {
				portalNodes.add(portalNode);
			}
		}
		return portalNodes;
	}

	@SuppressWarnings("unchecked")
	public List<PortalNodeSite> getNodesFromRoot(String nodeId) {
		List<String> parentIds = (List<String>) idParentsCache.get(toRef(nodeId));
		if (parentIds == null) {
			Set<HierarchyNode> nodes = hierarchyService.getParentNodes(nodeId, false);
			parentIds = new ArrayList<>(nodes.size());

			for (HierarchyNode node : nodes) {
				// Find the root.
				if (node.directParentNodeIds.isEmpty()){
					parentIds.add(node.id);
					nodes.remove(node);
					String parentId = node.id;
					// Now work through the rest, resetting when we find one.
					for (Iterator<HierarchyNode> it = nodes.iterator(); it.hasNext();) {
						HierarchyNode node2 = it.next();
						if (node2.directParentNodeIds.contains(parentId)) {
							parentIds.add(node2.id);
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
			parentIds = Collections.unmodifiableList(parentIds);
			idParentsCache.put(toRef(nodeId), parentIds);
		}

		return convertParentNodes(parentIds);
	}

	private List<PortalNodeSite> convertParentNodes(List<String> nodeIds) {
		List<PortalNodeSite> portalNodes = new ArrayList<>(nodeIds.size());
		for (String nodeId: nodeIds) {
			PortalNode populatePortalNode = getNodeById(nodeId);
			// We can only have site nodes as parent nodes, so this check should
			// always pass but it doesn't hurt to check.
			if (populatePortalNode instanceof PortalNodeSite) {
				portalNodes.add((PortalNodeSite)populatePortalNode);
			}
		}
		return portalNodes;
	}

	private List<PortalNode> populatePortalNodes(List<PortalPersistentNode> nodes) {
		List<PortalNode> portalNodes = new ArrayList<>(nodes.size());
		for (PortalPersistentNode node : nodes) {
			portalNodes.add(populatePortalNode(node));
		}
		return portalNodes;
	}

	public List<PortalNode> getNodesWithSite(String siteId) {
		// No caching on this.
		List<PortalPersistentNode> nodes =  dao.findBySiteId(siteId);
		return populatePortalNodes(nodes);
	}

	public void moveNode(String id, String newParentId) throws PermissionException {
		if (!canMoveNode(id)) {
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_MODIFY, getSiteReference(id));
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
		PortalNode newNode;
		if (node instanceof PortalNodeRedirect) {
			PortalNodeRedirect redirectNode = (PortalNodeRedirect) node;
			newNode = newRedirectNode(newParentId, redirectNode.getName(), redirectNode.getUrl(),
					redirectNode.getTitle(), redirectNode.isAppendPath(), redirectNode.isHidden());
		} else if (node instanceof PortalNodeSite) {
			PortalNodeSite siteNode = (PortalNodeSite) node;
			newNode = newSiteNode(newParentId, siteNode.getName(), siteNode.getSite().getId(), siteNode.getManagementSite().getId());
		} else {
			throw new IllegalArgumentException("PortalNode must be PortalNodeRedirect or PortalNodeSite");
		}
		for (PortalNode child : getNodeChildren(node.getId())) {
			copyNodes(child, newNode.getId());
		}
	}


	public PortalNodeRedirect newRedirectNode(String parentId, String childName,
			String redirectUrl, String title, boolean appendPath, boolean hidden) throws PermissionException {
		return (PortalNodeRedirect) newNode(parentId, childName, null, null, redirectUrl, title, appendPath, hidden);
	}

	public PortalNodeSite newSiteNode(String parentId, String childName, String siteId, String managementSiteId) throws PermissionException {
		return (PortalNodeSite) newNode(parentId, childName, siteId, managementSiteId, null, null, false, false);
	}

	PortalNode newNode(String parentId, String childName, String siteId, String managementSiteId, String redirectUrl, String title, boolean appendPath, boolean hidden) throws PermissionException {

		if ((siteId == null) == (redirectUrl == null)) {
			throw new IllegalArgumentException("You must specify either a siteId or a redirectUrl");
		}

		PortalNode possibleParent = getNodeById(parentId);
		if (possibleParent == null)
			throw new IllegalArgumentException("Parent site could not be found: "+ parentId);
		if (! (possibleParent instanceof PortalNodeSite))
			throw new IllegalArgumentException("You can only add new nodes to a PortalNodeSite.");

		PortalNodeSite parent= (PortalNodeSite)possibleParent;
		if (!canNewNode(parentId)) {
			throw new PermissionException(sessionManager.getCurrentSessionUserId(), SECURE_NEW, parent.getSite().getReference());
		}

		String childPath = (parent.getPath().equals("/"))?"/" + childName: parent.getPath() + "/" + childName;
		List<PortalNode> children =  getNodeChildren(parentId);
		for (PortalNode child: children) {
			if (child.getName().equals(childName))
				throw new IllegalArgumentException("Child site of this name already exists: "+ childName);
		}

		if (siteId != null) {
			if (!siteService.siteExists(siteId))
				throw new IllegalArgumentException("Site does not exist: "+ siteId);

			List<PortalNodeSite> parents = getNodesFromRoot(parentId);
			for (PortalNodeSite parentNode: parents) {
				if (siteId.equals(parentNode.getSite().getId()))
					throw new IllegalArgumentException("Site: "+ siteId+ " already used in parent: "+ parentNode.getPath());
			}
		}

		HierarchyNode node = hierarchyService.addNode(hierarchyId, parentId);
		PortalPersistentNode portalNode = new PortalPersistentNode();
		portalNode.setId(node.id);
		portalNode.setName(childName);
		portalNode.setPath(childPath);
		portalNode.setPathHash(sha1(childPath));

		if (siteId != null) {
			portalNode.setSiteId(siteId);
			portalNode.setManagementSiteId(managementSiteId);
		}
		if (redirectUrl != null ) {
			portalNode.setRedirectUrl(redirectUrl);
			// Leave the title as null when resource is hidden.
			if(!hidden) {
				portalNode.setRedirectTitle(title);
			}
			portalNode.setAppendPath(appendPath);
			portalNode.setHidden(hidden);
		}


		dao.save(portalNode);
		// We could combine these into one event but then we need custom event processing todo the cache
		// invalidation manually.
		// Need todo cache invalidation off this event for the children.
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_NEW, toRef(node.id), true));
		// This invalidates the children.
		eventTrackingService.post(eventTrackingService.newEvent(EVENT_MODIFY, toRef(parentId), true));
		return populatePortalNode(portalNode);

	}


	public PortalNode getDefaultNode(String siteId) {
		// This cache sites in and out of the hierarchy as it gets called when building a URL for a site.
		String nodeId = (String) siteToNodeCache.get(siteId);
		PortalNode node = null;
		if (nodeId != null) {
			node = getNodeById(nodeId);
			// If the site at this node is no longer correct clear out the cache and lookup from DB.
			if (node instanceof PortalNodeSite && !((PortalNodeSite) node).getSite().getId().equals(siteId)) {
				node = null;
			}
			// No event invalidation is done so cleanup here.
			if (node == null) {
				siteToNodeCache.remove(siteId);
			}
		}
		// If we haven't found the node and we haven't cached a null look in DB.
		if (node == null && !siteToNodeCache.containsKey(siteId)) {
			List<PortalPersistentNode> nodes = dao.findBySiteId(siteId);
			if (!nodes.isEmpty()) {
				Collections.sort(nodes, oldest);
				node = populatePortalNode(nodes.get(0));
				nodeId = node.getId();
			}
			// Will cache nulls.
			siteToNodeCache.put(siteId, nodeId);
		}
		return node;
	}

	public void renameNode(String id, String newPath) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}	

	public void setCurrentPortalNode(PortalNodeSite node) {
		threadLocalManager.set(CURRENT_NODE, node);
	}


	private static char[] encode = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static ThreadLocal<MessageDigest> digest = new ThreadLocal<MessageDigest>();
	private static Comparator<PortalPersistentNode> oldest = new OldestFirstComparator();

	/**
	 * Create a hash of the path.
	 *
	 * @param nodePath The path to hash.
	 * @deprecated This method generates a non-standard SHA1 which makes updates difficult
	 * @return A hash of the path.
	 * @throws RuntimeException If the SHA1 algorith
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
				throw new RuntimeException("Can't find SHA1", e);
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

	/**
	 * This creates a SHA1 hash (hex encoded string) of a string. This produces hashes in the same way as MySQL
	 * so that updates can more easily be done.
	 * @param nodePath  The path to hash
	 * @return The SHA1 hash.
	 * @throws RuntimeException If SHA1 can't be found or UTF-8 can't be found.
     */
	public static String sha1(String nodePath)
	{
		MessageDigest mdigest  = digest.get();
		if ( mdigest == null ) {
			try
			{
				mdigest = MessageDigest.getInstance("SHA1");
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException("Can't find SHA1", e);
			}
			digest.set(mdigest);
		}
		try {
			byte[] b = mdigest.digest(nodePath.getBytes("UTF-8"));
			char[] c = new char[b.length * 2];
			for (int i = 0; i < b.length; i++)
			{
				c[i * 2] = encode[(b[i]>>4)&0x0f];
				c[i * 2 + 1] = encode[b[i]&0x0f];
			}
			String encoded =  new String(c);
			return encoded.toLowerCase();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 is not a valid encoding", e);
		}
	}

	public void setDao(PortalPersistentNodeDao dao) {
		this.dao = dao;
	}

	public void setHierarchyService(HierarchyService hierarchyService) {
		this.hierarchyService = hierarchyService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}

	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}

	public void setFunctionManager(FunctionManager functionManager) {
		this.functionManager = functionManager;
	}

	public void setMemoryService(MemoryService memoryService) {
		this.memoryService = memoryService;
	}

	public void setHierarchyId(String hierarchyId) {
		this.hierarchyId = hierarchyId;
	}

	public void init() {

		if (autoDDL) {
			initDefaultContent();
		}

		functionManager.registerFunction(SECURE_DELETE);
		functionManager.registerFunction(SECURE_MODIFY);
		functionManager.registerFunction(SECURE_NEW);
		
		pathToIdCache = memoryService.newCache(getClass().getName()+"#pathToIdCache");
		idToNodeCache = memoryService.newCache(getClass().getName()+ "#idToNodeCache", PREFIX);
		idChildrenCache = memoryService.newCache(getClass().getName()+ "idChildrenCache", PREFIX);
		idParentsCache = memoryService.newCache(getClass().getName()+ "idParentsCache", PREFIX); 
		siteToNodeCache = memoryService.newCache(getClass().getName()+"#siteToNodeCache");
		
		// This is to invalidate the portalPath to node ID pathToIdCache.
		//idToNodeCache.attachDerivedCache(new DerivedPathCache(pathToIdCache));

		// This is to invalidate the siteToNodeCache.
		eventTrackingService.addObserver(new SiteToNodeObserver(siteToNodeCache, dao));
		eventTrackingService.addObserver((o, arg) -> {
			if (arg instanceof Event) {
				Event event = (Event) arg;
				if (event.getResource().startsWith(PREFIX) && event.getModify()) {
					String resource = event.getResource();
					idToNodeCache.remove(resource);
					idChildrenCache.remove(resource);
					idParentsCache.remove(resource);
				}
			}

        });
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
			portalNode.setPathHash(sha1("/"));
			dao.save(portalNode);
		}
	}

	public void setMissingSiteId(String missingSiteId)
	{
		this.missingSiteId = missingSiteId;
	}

	public void setAutoDDL(boolean autoDDL) {
		this.autoDDL = autoDDL;
	}

	private String getSiteReference(String nodeId) {
		PortalNode node = getNodeById(nodeId);
		if (node instanceof PortalNodeSite) {
			Site site = ((PortalNodeSite)node).getSite();
			return site.getReference();
		}
		return null;
	}

	/**
	 * Check a permission against a site on a node.
	 */
	private boolean unlockCheckNodeSite(String nodeId, String lock) {
		PortalNode node = getNodeById(nodeId);
		if (node instanceof PortalNodeSite) {
			Site site = ((PortalNodeSite)node).getSite();
			return securityService.unlock(lock, site.getReference());
		}
		return false;
	}

	public boolean canChangeSite(String id) {
		return unlockCheckNodeSite(id, SECURE_MODIFY);
	}

	/**
	 * @return <code>true</code> if the node has no children or the user is a sysadmin.
	 */
	public boolean canDeleteNode(String id) {
		PortalNode node = getNodeById(id);
		if (node instanceof PortalNodeRedirect) {
			Set<HierarchyNode> parentNodes = hierarchyService.getParentNodes(id, true);
			HierarchyNode parent = parentNodes.iterator().next();
			// We do the check now as we don't care about the parent having children as
			// this delete won't affect them.
			return unlockCheckNodeSite(parent.id, SECURE_DELETE);
		}
		List<PortalNode> nodes = getNodeChildren(id);
		// Check to see if any of the children are sites, in which case you need to be
		// an admin.
		for (PortalNode child: nodes) {
			if (child instanceof PortalNodeSite) {
				return securityService.isSuperUser();
			}
		}
		return unlockCheckNodeSite(id, SECURE_DELETE);
	}

	public boolean canMoveNode(String id) {
		//first checking if current user is superuser?
		boolean canMoveNode = securityService.isSuperUser();
		if(canMoveNode){
			return canMoveNode;
		}
		//As user is not admin, check if he is maintainer for the current node
		PortalNode parentNode = getNodeById(id);
		if(parentNode instanceof PortalNodeSite){
			canMoveNode = siteService.allowUpdateSite(((PortalNodeSite)parentNode).getSite().getId());
			//if user is not maintainer for the selected site to move, come out of the function
			if(!canMoveNode){
				return canMoveNode;
			}
		}
		//user is maintainer for the current site
		List<PortalNode> nodeChildren = getNodeChildren(id);
		//Check if node has any children subsites?
		for(PortalNode childNode : nodeChildren){
			if(childNode instanceof PortalNodeSite){
				//if there is a subsite for this node maintaner cannot move the node,break and return
				canMoveNode = false;
				break;
			}
		}
		return canMoveNode;
	}

	public boolean canNewNode(String parentId) {
		// Will fail for redirect nodes (correct)
		return unlockCheckNodeSite(parentId, SECURE_NEW);
	}

	public boolean canRenameNode(String id) {
		// Will fail for redirect nodes
		return unlockCheckNodeSite(id, SECURE_MODIFY);
	}
	
	protected static String toRef(String id) {
		return PREFIX+ Entity.SEPARATOR+ id;
	}
	
	protected static String fromRef(String id) {
		int length = (PREFIX+ Entity.SEPARATOR).length();
		return (id.length() > length)?id.substring(length):id;
	}

	/**
	 * This invalidates the site to node cache when the hierarchy changes.
	 */
	public static class SiteToNodeObserver implements Observer {
		// This class is static to make it easily testable.
		private Cache siteToNodeCache;
		private PortalPersistentNodeDao dao;

		public SiteToNodeObserver(Cache siteToNodeCache, PortalPersistentNodeDao dao) {
			this.siteToNodeCache = siteToNodeCache;
			this.dao = dao;
		}

		@Override
		public void update(Observable o, Object arg) {
			if (arg instanceof Event) {
				Event event = (Event)arg;
				// This is to deal with changing a site at a node or adding a new node
				// Our site to node ID pathToIdCache is a summary of multiple obj so needs to
				// be invalidated in this way.
				// The problem is that all the nodes do a DB lookup on the modified node.
				if (event.getResource().startsWith(PREFIX) &&
						EVENT_MODIFY.equals(event.getEvent()) || EVENT_NEW.equals(event.getEvent())) {
					String id = fromRef(event.getResource());
					PortalPersistentNode node = dao.findById(id);
					if (node != null) {
						String siteId = node.getSiteId();
						// Might be a redirect, in which case there won't be a siteId.
						if(siteId != null) {
							siteToNodeCache.remove(siteId);
						}
					} else {
						// This shouldn't happen, but if the user is quick it's possible.
						log.warn("Failed to find node for "+ id+ " to perform cache invalidation");
					}
				}
			}
		}
	}

}
