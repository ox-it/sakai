package org.sakaiproject.hierarchy.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.Tool;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


/**
 * Done in a seperate class to create sites, don't perform this in the main init() as we can't be sure that the 
 * other services have all done init() and can be used (eg siteService).
 * @author buckett
 *
 */
public class PortalHierarchySetup implements ApplicationListener {

	private final Log log = LogFactory.getLog(PortalHierarchySetup.class);

	private int refreshCount = 0;

	private boolean autoDDL;
	private String hierarchySiteId;
	private SecurityService securityService;
	private UsageSessionService usageSessionService;
	private SiteService siteService;
	private SessionManager sessionManager;


	public void onApplicationEvent(ApplicationEvent event) {
		// Typically we will get many of these events during startup.
		// The first one will fire after all the init() methods have been run.
		if (event instanceof ContextRefreshedEvent) {
			if (autoDDL && refreshCount++ == 0) {
				initDefaultContent();
			}
		}
	}

	private void initDefaultContent() {
		
		try {
			// Don't worry about security checks at startup.
			// Still need to change the authz.
			securityService.pushAdvisor(new SecurityAdvisor(){
				public SecurityAdvice isAllowed(String arg0, String arg1, String arg2) { return SecurityAdvice.ALLOWED;};}
			);
			Session session = sessionManager.startSession();
			session.setUserId("admin");
			sessionManager.setCurrentSession(session);
			usageSessionService.startSession("admin", null, null);
			
			siteService.getSite(hierarchySiteId);
		} catch (IdUnusedException e) {
			try {
				Site hierarchySite = siteService.addSite(hierarchySiteId, "hierarchy");
				hierarchySite.setTitle("Hierarchy Site");
				addPage(hierarchySite, "New Site", "sakai.hierarchy-new-site");
				addPage(hierarchySite, "Bring Site", "sakai.hierarchy-bring-site");
				addPage(hierarchySite, "Manage Site", "sakai.hierarchy-manager");
				hierarchySite.setPublished(true);
				Role anonRole = hierarchySite.addRole(".anon");
				anonRole.allowFunction("site.visit");
				siteService.save(hierarchySite);
				log.debug("Created the site: "+ hierarchySiteId);
			} catch (IdUsedException iue) {
				log.info("Site already created. Another node started up at the same time?");
			} catch (IdInvalidException iie) {
				log.warn("Failed to create site with ID: "+ hierarchySiteId);
			} catch (PermissionException pe) {
				log.warn("Failed to create site due to lack of permission.");
			} catch (IdUnusedException iuue) {
				log.warn("Incosistent code, ID isn't used.");
			} catch (RoleAlreadyDefinedException rade) {
				log.warn(".anon roles already on site.");
			}
		} finally {
			Session session = sessionManager.getCurrentSession();
			if (session != null) {
				session.invalidate();
			}
			securityService.popAdvisor();
		}
	}

	private void addPage(Site site, String title, String toolId) {
		SitePage page = site.addPage();
		page.setTitle(title);
		Tool tool = new Tool();
		tool.setId(toolId);	
		tool.setTitle(title);
		page.addTool(tool);
	}

	public String getHierarchySiteId() {
		return hierarchySiteId;
	}

	public void setHierarchySiteId(String hierarchySiteId) {
		this.hierarchySiteId = hierarchySiteId;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public UsageSessionService getUsageSessionService() {
		return usageSessionService;
	}

	public void setUsageSessionService(UsageSessionService usageSessionService) {
		this.usageSessionService = usageSessionService;
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public boolean isAutoDDL() {
		return autoDDL;
	}

	public void setAutoDDL(boolean autoDDL) {
		this.autoDDL = autoDDL;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

}
