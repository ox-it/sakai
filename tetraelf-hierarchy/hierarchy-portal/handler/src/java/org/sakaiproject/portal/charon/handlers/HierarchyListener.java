package org.sakaiproject.portal.charon.handlers;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.portal.api.PortalHandler;
import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.site.api.SiteService;

public class HierarchyListener implements ServletContextListener {

	private HierarchyHandler hierarchyHandler;
	private HierarchyHandler siteHierarchyHandler;
	private PortalHandler hierarchyResetHandler;
	private PortalHandler magicHandler;
	private PortalHandler hierarchyToolHandler;
	private PortalHandler hierarchyToolResetHandler;
	
	public void contextDestroyed(ServletContextEvent arg0) {
		PortalService ps = (PortalService) ComponentManager.get(PortalService.class);
		ps.removeHandler("charon", hierarchyHandler.getUrlFragment());
		ps.removeHandler("charon", siteHierarchyHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyResetHandler.getUrlFragment());	
		ps.removeHandler("charon", magicHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyToolHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyToolResetHandler.getUrlFragment());
	}

	public void contextInitialized(ServletContextEvent event) {
		PortalService ps = (PortalService) ComponentManager.get(PortalService.class);
		SiteService siteService = (SiteService) ComponentManager.get(SiteService.class);
		PortalHierarchyService portalHierarchyService = (PortalHierarchyService) ComponentManager.get(PortalHierarchyService.class);
		SecurityService securityService = (SecurityService) ComponentManager.get(SecurityService.class);

		hierarchyHandler = new HierarchyHandler(siteService, portalHierarchyService, securityService, "hierarchy");
		siteHierarchyHandler = new HierarchyHandler(siteService, portalHierarchyService, securityService, "site");
		// This is to make sure it overrides the default site handler.
		siteHierarchyHandler.setPriority(10);
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
