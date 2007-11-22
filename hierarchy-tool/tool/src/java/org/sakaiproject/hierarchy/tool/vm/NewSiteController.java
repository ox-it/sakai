package org.sakaiproject.hierarchy.tool.vm;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.tool.vm.NewSiteCommand.Method;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Tool;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class NewSiteController extends SimpleFormController
{

	public static final String ATTRIBUTE_URL = NewSiteController.class.getName()+ "#URL";
	
	private static final Log log = LogFactory.getLog(NewSiteController.class);
	
	private String returnPath;
	
	private Method defaultMethod;
	
	public String getReturnPath() {
		return returnPath;
	}

	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}

	public NewSiteController() {
		setCommandClass(NewSiteCommand.class);
	}
	
	public void init() {
		
	}
	
	public void setDefaultMethod(Method defaultMethod) {
		this.defaultMethod = defaultMethod;
	}
	
	/**
	 * We override this so that two controllers can share the same command class.
	 */
	protected String getFormSessionAttributeName() {
		return getCommandClass().getName()+"#FORM";
	}
	
	protected Object formBackingObject(HttpServletRequest request) throws Exception {
		NewSiteCommand command = (NewSiteCommand)super.formBackingObject(request);
		// Make sure the default method is set.
		command.setMethod(defaultMethod);
		return command;
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		NewSiteCommand newSite = (NewSiteCommand)command;

		// Check for duplicate node.
		// Not done in validator as it requires DB access.
		PortalHierarchyService hs = org.sakaiproject.hierarchy.cover.PortalHierarchyService.getInstance();
		Hierarchy node = hs.getCurrentPortalNode().getChild(newSite.getName());
		if ( node != null ) {
			errors.rejectValue("name", "error.name.exists");
			newSite.setMethod(Method.CUSTOM);
			return showForm(request, response, errors);
		}
		
		// This will actually be a tool session specific to this tool.
		HttpSession session = request.getSession();
		
		
		// Chuck the URL into the session so another controller can use it.
		session.setAttribute(ATTRIBUTE_URL, newSite.getName());
		
		// TODO Need to change done URL.
		session.setAttribute(Tool.HELPER_DONE_URL, request.getContextPath()+ request.getServletPath()+getReturnPath());
		session.setAttribute(SiteHelper.SITE_CREATE_START, Boolean.TRUE);
		session.setAttribute(SiteHelper.SITE_CREATE_SITE_TYPES, "project,course");
		session.setAttribute(SiteHelper.SITE_CREATE_SITE_TITLE, newSite.getTitle());
		return super.onSubmit(command, errors);
	}


	@Override
	protected Map<String, String> referenceData(HttpServletRequest request, Object command, Errors errors)
	{
		return VelocityControllerUtils.referenceData(request, command, errors);
	}

}
