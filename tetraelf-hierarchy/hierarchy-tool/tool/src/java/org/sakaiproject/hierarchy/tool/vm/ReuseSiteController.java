package org.sakaiproject.hierarchy.tool.vm;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ReuseSiteController extends SimpleFormController {

	private String cancelledView;

	private Integer titleMaxLength;

	public Integer getTitleMaxLength() {
		return titleMaxLength;
	}

	public void setTitleMaxLength(Integer maxLength) {
		this.titleMaxLength = maxLength;
	}

	public ReuseSiteController() {
		setCommandClass(NewSiteCommand.class);
	}

	@Override
	protected void onBindOnNewForm(HttpServletRequest request, Object object) throws Exception {
		NewSiteCommand command = (NewSiteCommand) object;
		HttpSession session = request.getSession();

		Object attribute = session.getAttribute(SiteHelper.SITE_PICKER_SITE_ID);
		if (attribute instanceof String) {
			String siteId = (String) attribute;
			// If throw exception if doesn't exist and bomb out.
			Site site = SiteService.getSite(siteId);
			command.setTitle(site.getTitle());
			command.setSiteId(siteId);
		}

	}

	@Override
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		NewSiteCommand command = (NewSiteCommand) super.formBackingObject(request);
		return command;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		HttpSession session = request.getSession();
		if (session.getAttribute(SiteHelper.SITE_PICKER_CANCELLED) != null) {
			session.removeAttribute(SiteHelper.SITE_PICKER_CANCELLED);
			return new ModelAndView(cancelledView);
		}
		//To add Cancel feature on the Reuse site screen
		String action = request.getParameter("redirectHome");
		if(action != null && action.equals("Cancel")){
			Map model = new VelocityControllerUtils(ServerConfigurationService.getInstance()).referenceData(request);
			PortalHierarchyService portalHierarchyService = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
			PortalNodeSite node = portalHierarchyService.getCurrentPortalNode();
			model.put("siteUrl", node.getSite().getUrl());
			return new ModelAndView(getSuccessView(), model);
		}
		return super.handleRequestInternal(request, response);
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object,
			BindException errors) throws Exception {

		NewSiteCommand command = (NewSiteCommand) object;
		PortalHierarchyService hs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
		PortalNode currentNode = hs.getCurrentPortalNode();
		List<PortalNode> children = hs.getNodeChildren(currentNode.getId());
		for (PortalNode child : children) {
			if (child.getName().equals(command.getName())) {
				errors.rejectValue("name", "error.name.exists");
				return showForm(request, response, errors);
			}
		}

		Map model = errors.getModel();
		try {
			PortalNodeSite node = hs.getCurrentPortalNode();
			PortalNodeSite newNode = hs.newSiteNode(node.getId(), command.getName(), command.getSiteId(), node
					.getManagementSite().getId());
			model.put("siteUrl", newNode.getSite().getUrl());
			model.putAll(new VelocityControllerUtils(ServerConfigurationService.getInstance()).referenceData(request));
		} catch (Exception hse) {
			errors.reject("error.add.hierarchy");
			return showForm(request, errors, getFormView(), errors.getModel());
		}

		return new ModelAndView(getSuccessView(), model);
	}

	public String getCancelledView() {
		return cancelledView;
	}

	public void setCancelledView(String cancelledView) {
		this.cancelledView = cancelledView;
	}

	@Override
	protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
		Map data = new VelocityControllerUtils(ServerConfigurationService.getInstance()).referenceData(request);

		PortalHierarchyService hs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
		PortalNodeSite currentNode = hs.getCurrentPortalNode();
		data.put("siteUrl", currentNode.getSite().getUrl());

		data.put("titleMaxLength", getTitleMaxLength());
		data.put("mode", "reuse");
		data.put("separator", PortalHierarchyService.SEPARATOR);
		return data;
	}

}
