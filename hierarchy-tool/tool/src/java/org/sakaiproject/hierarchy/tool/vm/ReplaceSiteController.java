package org.sakaiproject.hierarchy.tool.vm;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.mvc.annotation.TargettedController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
@Controller("replaceSiteController")
public class ReplaceSiteController{
	
	private SessionManager sessionManager;
	private PortalHierarchyService portalHierarchyService;
	private SiteService siteService;
	private VelocityControllerUtils velocityControllerUtils;
	static final String REQUEST_SITE = "_site";
	
		
	@Autowired
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Autowired
	public void setPortalHierarchyService(PortalHierarchyService phs) {
		this.portalHierarchyService = phs;
	}

	@Autowired
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	@Autowired
	public void setVelocityControllerUtils(VelocityControllerUtils velocityControllerUtils) {
		this.velocityControllerUtils = velocityControllerUtils;
	}
	
	@ModelAttribute
	public void referenceData(HttpServletRequest request, Model model) {
		model.addAllAttributes(velocityControllerUtils.referenceData(request));
	}
	
	@RequestMapping("/*")
	protected ModelAndView displaySites(HttpServletRequest request) {
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		toolSession.setAttribute(Tool.HELPER_DONE_URL, velocityControllerUtils.buildUrl(request, "/edit").toString());
		toolSession.setAttribute(SiteHelper.SITE_PICKER_PERMISSION,
				org.sakaiproject.site.api.SiteService.SelectionType.UPDATE);

		// Go to the helper
		RedirectView redirectView = new RedirectView("/sites", true);
		// We don't want to pass through all the model data.
		redirectView.setExposeModelAttributes(false);
		return new ModelAndView(redirectView);
		
	}
	
	@RequestMapping(value = "/edit")
	public ModelAndView editSite(HttpServletRequest request, Model model) {
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		PortalNodeSite node = portalHierarchyService.getCurrentPortalNode();

		model.addAttribute("old", velocityControllerUtils.createSiteMap(node.getSite()));
		// Check to see if the user has come back from helper.
		Object siteAttribute = toolSession.getAttribute(SiteHelper.SITE_PICKER_SITE_ID);
		toolSession.removeAttribute(SiteHelper.SITE_PICKER_SITE_ID);
		if ( siteAttribute instanceof String)
		{
			try
			{
				Site site = siteService.getSite((String)siteAttribute);
				model.addAttribute("new", velocityControllerUtils.createSiteMap(site));
				return new ModelAndView( "replace", model.asMap());
			} catch (IdUnusedException iue) {
				// Bail out.
			}
		}
		// if 'cancel' is clicked display the site browser tool
		return displaySites(request);
	}

	@RequestMapping(value = "/site/save", method = RequestMethod.POST)
	public String saveSite(HttpServletRequest request, @RequestParam(REQUEST_SITE) String siteId) {
		PortalNode node = portalHierarchyService.getCurrentPortalNode();
		if (siteId != null && !siteId.isEmpty()) {
			try {
				portalHierarchyService.changeSite(node.getId(), siteId);
			} catch (PermissionException e) {
				throw new IllegalStateException(
						"You shouldn't have been able to select a site as you don't have permission.", e);
			}
		}
		return "refresh";
	}

}
