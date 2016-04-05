package org.sakaiproject.authz.impl;

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
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.impl.ToolImpl;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


/**
 * Done in a seperate class to create sites, don't perform this in the main init() as we can't be sure that the 
 * other services have all done init() and can be used (eg siteService).
 * @author buckett
 *
 */
public class DevolvedSakaiSecuritySetup implements ApplicationListener {

	private final Log log = LogFactory.getLog(DevolvedSakaiSecuritySetup.class);

	private int refreshCount = 0;

	private boolean autoDDL;
	private String siteId;
	private SecurityService securityService;
	private UsageSessionService usageSessionService;
	private SiteService siteService;
	private SessionManager sessionManager;
	private ActiveToolManager activeToolManager;


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
			siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			try {
				Site site = siteService.addSite(siteId, "admin");
				site.setTitle("Test Admin Site");
				addPage(site, "Site Info", "sakai.siteinfo");
				addPage(site, "Managed Sites", "sakai.devolvedadmin");
				site.setPublished(true);
				site.removeRoles();
				Role managerRole = site.addRole("manager");
				managerRole.allowFunction("site.visit");
				managerRole.allowFunction("site.upd");
				Role memberRole = site.addRole("member");
				memberRole.allowFunction("site.admin.use");
				site.setMaintainRole("manager");
				site.addMember("admin", "manager", true, false);
				siteService.save(site);
				log.debug("Created the site: "+ siteId);
			} catch (IdUsedException iue) {
				log.info("Site already created. Another node started up at the same time?");
			} catch (IdInvalidException iie) {
				log.warn("Failed to create site with ID: "+ siteId);
			} catch (PermissionException pe) {
				log.warn("Failed to create site due to lack of permission.");
			} catch (IdUnusedException iuue) {
				log.warn("Incosistent code, ID isn't used.");
			} catch (RoleAlreadyDefinedException rade) {
				log.warn(".anon roles already on site.");
			}
		} finally {
			usageSessionService.logout();
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
		ToolImpl tool = new ToolImpl(activeToolManager);
		tool.setId(toolId);	
		tool.setTitle(title);
		page.addTool(tool);
	}

	public String getSiteId() {
		return siteId;
	}

	public void setSiteId(String SiteId) {
		this.siteId = SiteId;
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

	public ActiveToolManager getActiveToolManager() {
		return activeToolManager;
	}

	public void setActiveToolManager(ActiveToolManager activeToolManager) {
		this.activeToolManager = activeToolManager;
	}
}
