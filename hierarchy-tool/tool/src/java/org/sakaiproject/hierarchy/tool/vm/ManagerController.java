package org.sakaiproject.hierarchy.tool.vm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.hierarchy.api.model.PortalNode;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class ManagerController extends AbstractController
{

	private static final Log log = LogFactory.getLog(ManagerController.class);
	
	static final String ACT_EDIT_SITE = "act_editsite";
	
	static final String ACT_SAVE_SITE = "act_savesite";
	
	static final String ACT_SETPROPERTY = "act_setproperty";
	
	static final String ACT_SELECT_SITE = "act_selectsite";
	
	static final String ACT_CUT = "act_cut";
	
	static final String ACT_PASTE = "act_paste";

	static final String ACT_CANCEL = "act_cancel";

	static final String REQUEST_ACTION = "_action";

	static final String REQUEST_SITES = "_sites";
	
	static final String REQUEST_SITE = "_site";

	private static final String CUT_ID = ManagerController.class.getName() + "#CUT_ID";

	private PortalHierarchyService phs;

	public ManagerController()
	{
		super();
	}
	
	public void setPortalHierarchyService(PortalHierarchyService phs)
	{
		this.phs = phs;
	}

	public void init()
	{
	}

	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception
			{

		PortalNode node = null;
		String currentPath = request.getPathInfo();
		if (currentPath != null && currentPath.length() > 0)
		{
			node = phs.getNode(currentPath);
		}
		if (node == null)
		{
			node = phs.getCurrentPortalNode();
			if ( node == null ) {
				node = phs.getNode(null);
			}
		}
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		Session session = SessionManager.getCurrentSession();
		String cutId = (String) session.getAttribute(ManagerController.CUT_ID);


		Map<String, Object> model = new HashMap<String, Object>();
		populateModel(model,request);


		boolean topRefresh = false;

		String action = request.getParameter(REQUEST_ACTION);
		if (ACT_SELECT_SITE.equals(action))
		{
			toolSession.setAttribute(Tool.HELPER_DONE_URL, buildUrl(request) +"?"+REQUEST_ACTION+"="+ACT_EDIT_SITE);
			toolSession.setAttribute(SiteHelper.SITE_PICKER_PERMISSION, org.sakaiproject.site.api.SiteService.SelectionType.UPDATE);

			return new ModelAndView(new RedirectView("/sites", true), null);
		}
		else if (ACT_EDIT_SITE.equals(action))
		{
			Map<String, Object> editModel = new HashMap<String, Object>();
			populateModel(editModel, request);
			editModel.put("old", createSiteMap(node.getSite()));
			// Check to see if the user has come back from helper.
			Object siteAttribute = toolSession.getAttribute(SiteHelper.SITE_PICKER_SITE_ID);
			toolSession.removeAttribute(SiteHelper.SITE_PICKER_SITE_ID);
			if ( siteAttribute instanceof String) 
			{
				try
				{
					Site site = SiteService.getSite((String)siteAttribute);
					editModel.put("new", createSiteMap(site));
					return new ModelAndView( "replace", editModel );
				} catch (IdUnusedException iue) {
					log.warn("Couldn't find site returned by helper: "+ siteAttribute);
					// TODO Display message to user.
				}
			}
		}
		else if (ACT_SAVE_SITE.equals(action))
		{
			String siteId = request.getParameter(REQUEST_SITE);

			if (siteId != null && siteId.length() > 0)
			{
				phs.changeSite(node.getId(), siteId);
				topRefresh = true;
			}
		}
		else if (ACT_CUT.equals(action))
		{
			cutId = node.getId();
			session.setAttribute(ManagerController.CUT_ID, cutId);
		}
		else if (ACT_PASTE.equals(action))
		{
			if (cutId != null)
			{
				phs.moveNode(cutId, node.getId());
				session.removeAttribute(ManagerController.CUT_ID);
				cutId = null;
				topRefresh = true;
			}
		} else if (ACT_CANCEL.equals(action))
		{
			cutId = null;
			session.removeAttribute(ManagerController.CUT_ID);
		}
		
		Map<String, Object> showModel = new HashMap<String, Object>();
		populateModel(showModel, request);
		populateSite(showModel, node);
		showModel.put("topRefresh", topRefresh);
		if (cutId != null) {
			showModel.put("cutId", cutId);
			PortalNode cutNode = phs.getNodeById(cutId);
			showModel.put("cutChild", node.getPath().startsWith(cutNode.getPath()));
			return new ModelAndView( "cut", showModel);
		} else {
			showModel.put("canDelete", phs.canDeleteNode(node.getId()));
			showModel.put("canMove", phs.canMoveNode(node.getId()));
			showModel.put("canReplace", phs.canChangeSite(node.getId()));
			return new ModelAndView("show", showModel);
		}



			}

	private StringBuilder buildUrl(HttpServletRequest request) {
		StringBuilder doneUrl = new StringBuilder();
		if (request.getContextPath() != null) doneUrl.append(request.getContextPath());
		if (request.getServletPath() != null) doneUrl.append(request.getServletPath());
		if (request.getPathInfo() != null) doneUrl.append(request.getPathInfo());
		return doneUrl;
	}

	private void populateSite(Map<String, Object> editModel, PortalNode node) {
			Map<String,Object> site = createSiteMap(node.getSite());
			site.putAll(createNodeMap(node));
			
			List<PortalNode> nodes = phs.getNodesWithSite(node.getSite().getId());
			List<Map<String, Object>> paths = new ArrayList<Map<String, Object>>(nodes.size());
			for (PortalNode currentNode: nodes) {
				if (!node.getPath().equals(currentNode.getPath()))
					paths.add(createNodeMap(currentNode));
			}
			editModel.put("other", paths);
			editModel.put("current", site);
	}
	
	private Map<String, Object> createSiteMap(Site site) {
		Map<String, Object> siteMap = new HashMap<String, Object>();
		siteMap.put("siteTitle", site.getTitle());
		siteMap.put("siteId", site.getId());
		siteMap.put("siteShortDescription", site.getShortDescription());
		// TODO Should also add contact.
		return siteMap;
	}

	private Map<String, Object> createNodeMap(PortalNode node) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("nodePath", node.getPath());
		map.put("nodeId", node.getId());
		map.put("nodeUrl",ServerConfigurationService.getPortalUrl()+"/hierarchy"+ node.getPath());
		return map;
	}

	private void populateModel(Map<String, Object> model, HttpServletRequest request)
	{
		model.put("sakai_fragment","false");
		model.put("sakai_head", (String) request
				.getAttribute("sakai.html.head"));
		model.put("sakai_onload", (String) request
				.getAttribute("sakai.html.body.onload"));

		model.put("toolTitle", "Hierarchy Manager");
		String editor = ServerConfigurationService.getString("wysiwyg.editor");
		model.put("sakai_editor", editor);
		model.put("sakai_library_path", "/library/");

		model.put("rootUrl", request.getContextPath()+request.getServletPath());
	}

}
