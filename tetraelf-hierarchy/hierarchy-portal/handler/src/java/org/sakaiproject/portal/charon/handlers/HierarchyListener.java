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
	private PortalHandler hierarchyResetHandler;
	private PortalHandler magicHandler;
	private PortalHandler hierarchyToolHandler;
	private PortalHandler hierarchyToolResetHandler;
	
	public void contextDestroyed(ServletContextEvent arg0) {
		PortalService ps = ComponentManager.get(PortalService.class);
		ps.removeHandler("charon", hierarchyHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyResetHandler.getUrlFragment());
		ps.removeHandler("charon", magicHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyToolHandler.getUrlFragment());
		ps.removeHandler("charon", hierarchyToolResetHandler.getUrlFragment());
	}

	public void contextInitialized(ServletContextEvent event) {
		PortalService ps = ComponentManager.get(PortalService.class);
		PortalHierarchyService portalHierarchyService = ComponentManager.get(PortalHierarchyService.class);

		hierarchyHandler = new HierarchyHandler(portalHierarchyService, "hierarchy", "/site");
		hierarchyResetHandler = new HierarchyHandler(portalHierarchyService, "hierarchy-reset", "/site-reset");

		magicHandler = new MagicHandler();
		hierarchyToolHandler = new HierarchyToolHandler(portalHierarchyService);
		hierarchyToolResetHandler = new HierarchyToolResetHandler();

		ps.addHandler("charon", hierarchyHandler);
		ps.addHandler("charon", hierarchyResetHandler);
		ps.addHandler("charon", magicHandler);
		ps.addHandler("charon", hierarchyToolHandler);
		ps.addHandler("charon", hierarchyToolResetHandler);
	}

}
