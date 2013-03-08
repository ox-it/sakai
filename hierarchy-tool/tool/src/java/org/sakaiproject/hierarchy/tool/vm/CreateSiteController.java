package org.sakaiproject.hierarchy.tool.vm;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * Do the work of adding the new node and associating the new site with it.
 * @author buckett
 *
 */
public class CreateSiteController extends AbstractCommandController {
	
	private final static Log log = LogFactory.getLog(CreateSiteController.class);

	private String canceledView;
	
	private String failureView;
	
	private String successView;
	
	public void init() {
		
	}
	
	public CreateSiteController() {
		setCommandClass(CreateSiteCommand.class);
		setValidator(new CreateSiteValidator());
		
	}
	
	/*
	 * Need to pull all the command parameters from session.
	 * @see org.springframework.web.servlet.mvc.BaseCommandController#onBind(javax.servlet.http.HttpServletRequest, java.lang.Object)
	 */
	protected void onBind(HttpServletRequest request, Object object) {
		CreateSiteCommand command = (CreateSiteCommand)object;
		HttpSession session = request.getSession();
		
		command.setCancelled(session.getAttribute(SiteHelper.SITE_CREATE_CANCELLED) != null);
		command.setName((String)session.getAttribute(NewSiteController.ATTRIBUTE_URL));
		command.setSiteId((String)session.getAttribute(SiteHelper.SITE_CREATE_SITE_ID));
		
	}
	
	
	@Override
	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object object, BindException errors) throws Exception {
		CreateSiteCommand command = (CreateSiteCommand)object;
		if (command.isCancelled()) {
			return new ModelAndView(canceledView);
		}
		
		//Pretty serious as is means something went wrong in the flow
		if (errors.hasErrors()) {
			log.warn("Didn't have enough information to finish site creation: "+ command);
			return handleFailure(request, command, errors);
		}

		PortalHierarchyService hs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
		String sitePath = null;
		try {
			PortalNodeSite node = hs.getCurrentPortalNode();
			PortalNode newNode = hs.newSiteNode(node.getId(), command.getName(), command.getSiteId(), node.getManagementSite().getId());
			sitePath = newNode.getPath();
		} catch (Exception e) {
			errors.reject("error.add.hierarchy",new Object[]{e.getMessage()}, "Failed to create node in hierarchy: {0}");
			return handleFailure(request, command,errors);
		}
		// This is a hack as something in the tool session breaks the tool the second time you create a site
		SessionManager.getCurrentToolSession().clearAttributes();
		Map model = VelocityControllerUtils.referenceData(request);
		model.put("siteUrl", ServerConfigurationService.getPortalUrl()+"/hierarchy"+ sitePath);
		
		return new ModelAndView(successView, model);
		
	}

	/**
	 * Handle failure in the creation of the node in the hierarchy. Cleans up the newly created site.
	 * @param command
	 * @param errors
	 * @return
	 */
	protected ModelAndView handleFailure(HttpServletRequest request, CreateSiteCommand command, BindException errors) {
		String siteId = command.getSiteId();
		if (siteId != null && siteId.length() != 0) {
			SiteService siteService = org.sakaiproject.site.cover.SiteService.getInstance();
			try {
				log.debug("Attempting to cleanup site: "+ command.getSiteId());
				Site newSite = siteService.getSite(command.getSiteId());
				siteService.removeSite(newSite);
			} catch (IdUnusedException iue) {
				log.warn("Couldn't find site to remove: "+ siteId, iue);
			} catch (PermissionException pe) {
				log.warn("Current user doesn't have permission to cleanup site.", pe);
			}
		} else {
			log.warn("Not cleaning up site as we don't know the site ID.");
		}
		Map model = VelocityControllerUtils.referenceData(request);
		model.putAll(errors.getModel());
		return new ModelAndView(failureView, model);
	}

	public void setCanceledView(String canceledView) {
		this.canceledView = canceledView;
	}

	public void setFailureView(String failureView) {
		this.failureView = failureView;
	}

	public void setSuccessView(String successView) {
		this.successView = successView;
	}

}
