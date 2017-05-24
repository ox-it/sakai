package org.sakaiproject.authz.impl;

import org.sakaiproject.authz.api.DevolvedAdminDao;
import org.sakaiproject.authz.api.DevolvedSakaiSecurity;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.hbm.DevolvedAdmin;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.user.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Oxford version of Sakai security.
 * 
 * @author buckett
 * 
 */
public abstract class DevolvedSakaiSecurityImpl extends SakaiSecurity implements
		DevolvedSakaiSecurity {
	
	final public static String ADMIN_REALM_CHANGE = "site.admin.change";
	
	private static Logger log = LoggerFactory
			.getLogger(DevolvedSakaiSecurityImpl.class);
	
	private Collection restrictedSiteTypes;

	private String adminSiteType;
	
	private Cache adminCache;

	private Observer siteDeleteObserver;

	private Observer realmChangeObserver;
	
	protected abstract EventTrackingService eventTrackingService();
		
	public void init() {
		super.init();
		FunctionManager.registerFunction(ADMIN_REALM_PERMISSION);
		FunctionManager.registerFunction(ADMIN_REALM_PERMISSION_USE);
		log.debug("Admin site type: "+ adminSiteType);
		log.debug("Restricted site types : "+ String.join(", ", restrictedSiteTypes));

		adminCache = memoryService().newCache(DevolvedSakaiSecurityImpl.class.getName(), SiteService.REFERENCE_ROOT);
		
		siteDeleteObserver = (o, arg) -> {
			if (arg instanceof Event) {
				Event event = (Event)arg;
				String type = event.getEvent();
				if (SiteService.SECURE_REMOVE_SITE.equals(type))  {
					if (event.getResource() != null) {
						dao().delete(event.getResource());
					} else {
						// Should never happen.
						log.warn("Site delete event without a resource.");
					}
				}
			}
		};
		realmChangeObserver = (o, arg) -> {
			if (arg instanceof Event) {
				Event event = (Event) arg;
				if (event.getResource() != null) {
					adminCache.remove(event.getResource());
				}
			}
		};

		eventTrackingService().addLocalObserver(siteDeleteObserver);
		eventTrackingService().addObserver(realmChangeObserver);
	}
	
	public void destroy() {
		super.destroy();
		eventTrackingService().deleteObserver(siteDeleteObserver);
		eventTrackingService().deleteObserver(realmChangeObserver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.impl.DevolvedSakaiSecurity#getAdminRealm(java.lang.String)
	 */
	public String getAdminRealm(String entityRef) {
		// As we only allow admin realms for site this is a performance op.
		if (!entityRef.startsWith(SiteService.REFERENCE_ROOT)) {
			return null;
		}
		String adminRealm = (String) adminCache.get(entityRef);
		// We want to cache nulls to need to look and see of the cache has an entry first.
		// If we check to see if the entry is in the cache first then our hit/miss stats our wrong.
		if (adminRealm == null && !adminCache.containsKey(entityRef) ) {
			DevolvedAdmin admin = dao().findByRealm(entityRef);
			adminRealm = (admin != null) ? admin.getAdminRealm() : null;
			adminCache.put(entityRef, adminRealm);
		}
		return adminRealm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.authz.impl.DevolvedSakaiSecurity#setAdminRealm(java.lang.String,
	 *      java.lang.String)
	 */
	public void setAdminRealm(String entityRef, String adminRealm)
			throws PermissionException {
		getSiteReference(entityRef);
		User user = userDirectoryService().getCurrentUser();
		if (!canSetAdminRealm(entityRef)) {
			throw new PermissionException(user.getId(), SiteService.SECURE_UPDATE_SITE, entityRef);
		}
		if (!canUseAdminRealm(adminRealm)) {
			throw new PermissionException(user.getId(), ADMIN_REALM_PERMISSION_USE, adminRealm);
		}
		DevolvedAdmin admin = dao().findByRealm(entityRef);
		if (admin == null) {
			admin = new DevolvedAdmin();
			admin.setRealm(entityRef);
		}
		admin.setAdminRealm(adminRealm);
		dao().save(admin);
		eventTrackingService().post(eventTrackingService().newEvent(ADMIN_REALM_CHANGE, entityRef, true));
	}

	public boolean canSetAdminRealm(String entityRef) {
		Reference ref = entityManager().newReference(entityRef);
		if (SiteService.APPLICATION_ID.equals(ref.getType())
				&& SiteService.SITE_SUBTYPE.equals(ref.getSubType())) {
			// Only allow admins to change admin site on admin sites.
			String siteType = getSiteType(ref);
			if (siteType != null && restrictedSiteTypes.contains(siteType)) {
				return isSuperUser();
			} else {
				return unlock(SiteService.SECURE_UPDATE_SITE, entityRef);
			}
		}
		return false;
	}
	
	/**
	 * Gets the type of a site.
	 * @param siteRef A reference (hopefully to a site).
	 * @return The String type of the site or <code>null</code> if the site doesn't have one or the reference isn't a site.
	 */
	private String getSiteType(Reference siteRef) {
		Entity entity = siteRef.getEntity();
		if (entity instanceof Site) {
			return ((Site)entity).getType();
		}
		return null;
	}
	
	public boolean canUseAdminRealm(String adminRealm) {
		return unlock(ADMIN_REALM_PERMISSION_USE, adminRealm);
	}
	
	public boolean canRemoveAdminRealm(String adminRealm) {
		return isSuperUser();
	}

	public List<Entity> getAvailableAdminRealms(String entityRef) {
		if (entityRef != null) {
			Reference ref = getSiteReference(entityRef);
		}
		// Must have some sort of filtering as we can't iterate over all the sites.
		List<Site> sites = siteService().getSites(SelectionType.ANY, adminSiteType, null, null, null, null);
		List <Entity> entities = new ArrayList<Entity>();
		for (Site site : sites) {
			if (unlock(ADMIN_REALM_PERMISSION_USE, site.getReference()) && !site.getReference().equals(entityRef)) {
				entities.add(site);
			}
		}
		return entities;
	}
	
	public List<Entity> findUsesOfAdmin(String adminRealm) {
		List<DevolvedAdmin> devolvedAdmins = dao().findByAdminRealm(adminRealm);
		List <Entity> sites = new ArrayList<Entity>(devolvedAdmins.size());
		for (DevolvedAdmin devolvedAdmin: devolvedAdmins)
		{
			Entity entity = getSiteReference(devolvedAdmin.getRealm()).getEntity();
			if (entity != null) {
				sites.add(entity);
			}
		}
		return sites;
	}
	
	public void removeAdminRealm(String entityRef) throws PermissionException {
		Reference ref = getSiteReference(entityRef);
		if (canRemoveAdminRealm(entityRef)) {
			dao().delete(entityRef);
			eventTrackingService().post(eventTrackingService().newEvent(ADMIN_REALM_CHANGE, entityRef, true));
		} else {
			throw new PermissionException(null,null,null);
		}
	}
	
	/**
	 * Get a reference for a site entity.
	 * @throws IllegalArgumentException If the entity supplied isn't a site one.
	 * @param entityRef 
	 * @return The reference.
	 */
	private Reference getSiteReference(String entityRef) {
		Reference ref = entityManager().newReference(entityRef);
		if (SiteService.APPLICATION_ID.equals(ref.getType())
				&& SiteService.SITE_SUBTYPE.equals(ref.getSubType())) {
			return ref;
		} else {
			throw new IllegalArgumentException(
					"Only site entities are supported at the moment. Entity: "
							+ entityRef);
		}
		
	}


	protected boolean checkAuthzGroups(String userId, String function, String entityRef, Collection<String> azgs)
	{

		// get this entity's AuthzGroups if needed
		if (azgs == null)
		{
			// make a reference for the entity
			Reference ref = entityManager().newReference(entityRef);

			azgs = ref.getAuthzGroups(userId);
		}

		String siteRef = null;
		String roleswap = null;

		// Actual code in DbAuthzGroupService will not roleswap if there's a user site ref in the list and
		// it is acceptable. However we can't tell that without doing a database access, so be conservative
		// and cache it separately as a role swap
		// This code does not handle delegated access
		if (azgs != null && userId != null && userId.equals(sessionManager().getCurrentSessionUserId())) {
			// These checks for roleswap assume there is at most one of each type of site in the realms collection,
			// i.e. one ordinary site and one user site
			for (String azg: azgs) {
				if (azg.startsWith(SiteService.REFERENCE_ROOT + Entity.SEPARATOR)) {  // Starts with /site/
					if (userId.equals(siteService().getSiteUserId(azg))) {
						; // reference to a user site
					} else {
						siteRef = azg; // set this variable for potential use later
					}
				}
			}

			Reference ref = entityManager().newReference(siteRef);
			if (SiteService.GROUP_SUBTYPE.equals(ref.getSubType())) {
				String containerSiteRef = siteService().siteReference(ref.getContainer());
				roleswap = getUserEffectiveRole(containerSiteRef);
				if (roleswap != null) {
					siteRef = containerSiteRef;
				}
			} else {
				roleswap = getUserEffectiveRole(siteRef);
			}
		}

		Collection<String> expandedAzgs = new HashSet<>();
		if (roleswap == null ) {
			// We only want to add the admin realms when not in roleswap
			for (String azg : azgs) {
				String adminRealm = getAdminRealm(azg);
				if (adminRealm != null) {
					if (log.isDebugEnabled()) {
						log.debug("Adding admin realm: " + adminRealm + " for: " + azg);
					}
					expandedAzgs.add(adminRealm);
				}
				expandedAzgs.add(azg);
			}
		} else {
			expandedAzgs = azgs;
		}
		// check the cache
		String command = makeCacheKey(userId, roleswap, function, entityRef, false);

		if (m_callCache != null)
		{
			final Boolean value = getFromCache(command, false);
			if(value != null) return value.booleanValue();
		}


		boolean rv = authzGroupService().isAllowed(userId, function, expandedAzgs);

		addToCache(command, rv, false);
		return rv;
	}

	protected abstract SiteService siteService();

	protected abstract DevolvedAdminDao dao();

	protected abstract MemoryService memory();

	public String getAdminSiteType() {
		return adminSiteType;
	}

	public void setAdminSiteType(String adminSiteType) {
		this.adminSiteType = adminSiteType;
	}

	public void setRestrictedSiteTypes(Collection restrictedSiteTypes) {
		this.restrictedSiteTypes = restrictedSiteTypes;
	}
}