package org.sakaiproject.hierarchy.tool.vm;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Tool;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class BringSiteController extends SimpleFormController {

	private String returnPath;
	private SecurityService securityService;
	
	public BringSiteController() {
		setCommandClass(Object.class);
	}
	
	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		HttpSession session = request.getSession();
		
		session.setAttribute(Tool.HELPER_DONE_URL, request.getContextPath()+ request.getServletPath()+getReturnPath());
		session.setAttribute(SiteHelper.SITE_CREATE_START, Boolean.TRUE);
		if (securityService.isSuperUser()) {
			session.setAttribute(SiteHelper.SITE_PICKER_PERMISSION, org.sakaiproject.site.api.SiteService.SelectionType.ANY);
		} else {
			session.setAttribute(SiteHelper.SITE_PICKER_PERMISSION, org.sakaiproject.site.api.SiteService.SelectionType.UPDATE);
		}
		
		return super.onSubmit(request, response, command, errors);
	}

	public String getReturnPath() {
		return returnPath;
	}

	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}

	
	public Map<String, Object> referenceData(HttpServletRequest request, Object command, Errors errors)
	{
		return VelocityControllerUtils.referenceData(request, command, errors);
	}

	public SecurityService getSecurityService()
	{
		return securityService;
	}

	public void setSecurityService(SecurityService securityService)
	{
		this.securityService = securityService;
	}
	
}
