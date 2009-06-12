package org.sakaiproject.portal.charon.handlers;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.SiteService;

public class HierarchyListener implements ServletContextListener {

	private PortalHandler hierarchyHandler;
	private PortalHandler siteHierarchyHandler;
	private PortalHandler hierarchyResetHandler;
	private PortalHandler magicHandler;
	private PortalHandler hierarchyToolHandler;
	private PortalHandler hierarchyToolResetHandler;
	
	public void contextDestroyed(ServletContextEvent arg0) {
		PortalService ps = (PortalService) org.sakaiproject.portal.api.cover.PortalService.getInstance();
		ps.removeHandler("charon", hierarchyHandler.getUrlFragment());
		ps.removeHandler("charon", siteHierarchyHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyResetHandler.getUrlFragment());	
		ps.removeHandler("charon", magicHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyToolHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyToolResetHandler.getUrlFragment());
	}

	public void contextInitialized(ServletContextEvent event) {
		PortalService ps = (PortalService) org.sakaiproject.portal.api.cover.PortalService.getInstance();
		SiteService siteService = (SiteService) org.sakaiproject.site.cover.SiteService.getInstance();
		PortalHierarchyService portalHierarchyService = (PortalHierarchyService)org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
		SecurityService securityService = (SecurityService)org.sakaiproject.authz.cover.SecurityService.getInstance();
		hierarchyHandler = new HierarchyHandler(siteService, portalHierarchyService, securityService, "hierarchy");
		siteHierarchyHandler = new HierarchyHandler(siteService, portalHierarchyService, securityService, "site");
		hierarchyResetHandler = new HierarchyResetHandler();
		magicHandler = new MagicHandler();
		hierarchyToolHandler = new HierarchyToolHandler(portalHierarchyService);
		hierarchyToolResetHandler = new HierarchyToolResetHandler();
		ps.addHandler("charon", hierarchyHandler);
		ps.addHandler("charon", siteHierarchyHandler);
		ps.addHandler("charon", hierarchyResetHandler);
		ps.addHandler("charon", magicHandler);
		ps.addHandler("charon", hierarchyToolHandler);
		ps.addHandler("charon", hierarchyToolResetHandler);
	}

}
