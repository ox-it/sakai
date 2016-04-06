package org.sakaiproject.authz.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.TwoFactorAuthentication;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class TwoFactorAuthenticationImpl implements TwoFactorAuthentication {

	private static final Log log = LogFactory.getLog(TwoFactorAuthenticationImpl.class);

	private boolean enabled;

	private ServerConfigurationService serverConfigurationService;

	private SessionManager sessionManager;

	private EntityManager entityManager;

	private SiteService siteService;

	private ThreadLocalManager threadLocalManager;

	private String siteType;

	private long timeout;

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}

	public void init() {
		enabled = serverConfigurationService.getBoolean("twofactor.enable", false);
		siteType = serverConfigurationService.getString("twofactor.site.type", "secure");
		timeout = serverConfigurationService.getInt("twofactor.timeout", 900000);
	}

	public boolean hasTwoFactor() {
		Session session = sessionManager.getCurrentSession();
		Long timeout = (Long)session.getAttribute(SessionManager.TWOFACTORAUTHENTICATION);
		if (null != timeout) {
			log.debug("hasTwoFactor ["+System.currentTimeMillis()+":"+timeout+"]");
			if (System.currentTimeMillis() < timeout) {
				log.debug("hasTwoFactor [true]");
				return true;
			} else {
				log.debug("hasTwoFactor timeout [false]");
				return false;
			}
		}
		log.debug("hasTwoFactor [false]");
		return false;
	}

	public void markTwoFactor() {
		Session session = sessionManager.getCurrentSession();
		long expire = System.currentTimeMillis() + timeout;
		session.setAttribute(SessionManager.TWOFACTORAUTHENTICATION, expire);
		log.debug("markTwoFactor ["+expire+"]");

	}

	public boolean isTwoFactorRequired(String ref) {
		if (log.isErrorEnabled()) {
			log.debug("isTwoFactorRequired ["+ref+"]");
		}
		// This checks if two factor authentication is required based on the type of the site.
		// For references that don't have a valid context we won't be able to work out the type
		// of the site and so won't know if we should be requiring two factor access.
		if (!enabled) {
			return false;
		}
		if (ref != null) {
			return findSiteId(ref);
		}

		log.debug("isTwoFactorRequired [false]");
		return false;
	}

	private boolean findSiteId(String ref) {
		Reference reference = entityManager.newReference(ref);
		if (SiteService.APPLICATION_ID.equals(reference.getType()) && SiteService.SITE_SUBTYPE.equals(reference.getSubType())) {
			String siteId = reference.getId();
			return isSiteTwoFactor(siteId, ref);
		} else {
			// If this is a reference for something other than a site then check for the
			// context and use the site.
			String siteId = reference.getContext();
			// Some references don't have a context (eg /content/user)
			return isSiteTwoFactor(siteId, ref);
		}
	}


	// This isn't currently used as we can prevent unlocks happening inside the siteservice by
	// checking if a site exists first.
	private boolean checkPreviousRefs(String ref) {
		// Prevent stack overflow, when an unlock happens from inside here:
		List<String> checked = (List<String>) threadLocalManager.get(TwoFactorAuthenticationImpl.class.getName());
		if (checked == null) {
			checked = new ArrayList<String>();
			threadLocalManager.set(TwoFactorAuthenticationImpl.class.getName(), checked);
		}
		if (checked.contains(ref)) {
			return false; // This allows the required entity to load.
		}
		try {
			checked.add(ref);
			return findSiteId(ref);
		} finally {
			if(!checked.isEmpty()) {
				checked.remove(checked.size()-1);
			}
		}
	}

	/**
	 * This checks to see if a siteId requires two factor authentication.
	 * @param siteId The site ID to check.
	 * @param ref The reference which the site ID was in.
	 * @return <code>true</code> if two factor authentication is required.
	 */
	private boolean isSiteTwoFactor(String siteId, String ref) {
		// We have this check because getSite() makes a call to unlock so we only check
		// on sites that already exist.
		if (siteService.siteExists(siteId)) {
			try {
				Site site = siteService.getSite(siteId);
				if (siteType.equals(site.getType())) {
					log.debug("isTwoFactorRequired [true]");
					return true;
				}
			} catch (IdUnusedException iue) {
				// Should only ever happen when someone else deletes the site in anther thread
				// in-between exist check and getting the site.
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("Failed to find site: "+siteId + " for ref: "+ ref);
			}
		}
		return false;
	}

}
