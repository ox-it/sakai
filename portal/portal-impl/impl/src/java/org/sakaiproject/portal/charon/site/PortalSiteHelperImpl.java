/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.charon.site;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.util.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.portal.api.*;
import org.sakaiproject.portal.charon.site.AllSitesViewImpl;
import org.sakaiproject.portal.charon.site.CurrentSiteViewImpl;
import org.sakaiproject.portal.charon.site.MoreSiteViewImpl;
import org.sakaiproject.portal.charon.site.SubSiteViewImpl;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntitySummary;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.Summary;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.portal.api.SiteView.View;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ArrayUtil;
import org.sakaiproject.util.MapUtil;
import org.sakaiproject.util.Web;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.portal.charon.PortalStringUtil;
import org.sakaiproject.util.FormattedText;

/**
 * This class exists so that portal handlers don't have to get services directly but instead can use
 * this class which gets all the services and has a reference to the portal. We should create a factory
 * which creates the PortalSiteHelper from the portal rather than having the ComponentManager calls.
 *
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */
@SuppressWarnings("deprecation")
public class PortalSiteHelperImpl implements PortalSiteHelper
{
	// Alias prefix for page aliases. Use Entity.SEPARATOR as IDs shouldn't contain it.
	private static final String PAGE_ALIAS = Entity.SEPARATOR+ "pagealias"+ Entity.SEPARATOR;

	private static final Logger log = LoggerFactory.getLogger(PortalSiteHelper.class);

	private final String PROP_PARENT_ID = SiteService.PROP_PARENT_ID;

	private static final String PROP_HTML_INCLUDE = "sakai:htmlInclude";

	private static final String PROP_MENU_CLASS = "sakai:menuClass";

	protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

	private AliasService aliasService;

	public void setAliasService(AliasService aliasService) {
		this.aliasService = aliasService;
	}

	private PageFilter pageFilter;

	public void setPageFilter(PageFilter pageFilter) {
		this.pageFilter = pageFilter;
	}

	private SiteNeighbourhoodService siteNeighbourhoodService;

	public void setSiteNeighbourhoodService(SiteNeighbourhoodService siteNeighbourhoodService) {
		this.siteNeighbourhoodService = siteNeighbourhoodService;
	}

	private boolean lookForPageAliases;

	// 2.3 back port
	// private final String PROP_PARENT_ID = "sakai:parent-id";

	private ToolManager toolManager;

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	/**
	 * @param portal
	 */
	public PortalSiteHelperImpl(Portal portal, boolean lookForPageAliases)
	{
		this.lookForPageAliases = lookForPageAliases;
		aliasService = ComponentManager.get(AliasService.class);
		toolManager = ComponentManager.get(ToolManager.class);
		siteNeighbourhoodService = portal.getSiteNeighbourhoodService();
		pageFilter = portal.getPageFilter();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#doGatewaySiteList()
	 */
	public boolean doGatewaySiteList()
	{
		String gatewaySiteListPref = ServerConfigurationService
		.getString("gatewaySiteList");

		if (gatewaySiteListPref == null) return false;
		return (gatewaySiteListPref.trim().length() > 0);
	}

	// Determine if we are to do multiple tabs for the anonymous view (Gateway)
	/**
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#doGatewaySiteList()
	 */
	public String getGatewaySiteId()
	{
		String gatewaySiteListPref = ServerConfigurationService
				.getString("gatewaySiteList");
		
		if (gatewaySiteListPref == null) return null;					
		
		String[] gatewaySiteIds = getGatewaySiteList();
		if (gatewaySiteIds == null)
		{
			return null; 
		}

		// Loop throught the sites making sure they exist and are visitable
		for (int i = 0; i < gatewaySiteIds.length; i++)
		{
			String siteId = gatewaySiteIds[i];

			Site site = null;
			try
			{
				site = getSiteVisit(siteId);
			}
			catch (IdUnusedException e)
			{
				continue;
			}
			catch (PermissionException e)
			{
				continue;
			}

			if (site != null)
			{
				return siteId;
			}
		}

		log.warn("No suitable gateway sites found, gatewaySiteList preference had "
					+ gatewaySiteIds.length + " sites.");
		return null;
	}

	// Return the list of tabs for the anonymous view (Gateway)
	// If we have a list of sites, return that - if not simply pull in the
	// single
	// Gateway site
	/**
	 * @return
	 */
	private String[] getGatewaySiteList()
	{
		String gatewaySiteListPref = ServerConfigurationService
				.getString("gatewaySiteList");

		if (gatewaySiteListPref == null || gatewaySiteListPref.trim().length() < 1)
		{
			gatewaySiteListPref = ServerConfigurationService.getGatewaySiteId();
		}
		if (gatewaySiteListPref == null || gatewaySiteListPref.trim().length() < 1)
			return null;

		String[] gatewaySites = gatewaySiteListPref.split(",");
		if (gatewaySites.length < 1) return null;

		return gatewaySites;
	}

	public List<Map> getSitesInContext(String context, String userId)
	{

		return null;
	}

	/**
	 * This method takes a list of sites and organizes it into a list of maps of
	 * properties. There is an additional complication that the depth contains
	 * informaiton arround.
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#convertSitesToMaps(javax.servlet.http.HttpServletRequest,
	 *      java.util.List, java.lang.String, java.lang.String,
	 *      java.lang.String, boolean, boolean, boolean, boolean,
	 *      java.lang.String, boolean)
	 */
	public List<Map> convertSitesToMaps(HttpServletRequest req, List mySites,
			String prefix, String currentSiteId, String myWorkspaceSiteId,
			boolean includeSummary, boolean expandSite, boolean resetTools,
			boolean doPages, String toolContextPath, boolean loggedIn)
	{
		List<Map> l = new ArrayList<Map>();
		Map<String, Integer> depthChart = new HashMap<String, Integer>();
		boolean motdDone = false;

		// We only compute the depths if there is no user chosen order
		boolean computeDepth = true;
		Session session = SessionManager.getCurrentSession();

		List favorites = Collections.emptyList();

		if ( session != null )
				{
						Preferences prefs = PreferencesService.getPreferences(session.getUserId());
						ResourceProperties props = prefs.getProperties("sakai:portal:sitenav");

						List propList = props.getPropertyList("order");
						if (propList != null)
						{
								computeDepth = false;
								favorites = propList;
						}
				}

		// Determine the depths of the child sites if needed
		for (Iterator i = mySites.iterator(); i.hasNext();)
		{
			Site s = (Site) i.next();

			// The first site is the current site
			if (currentSiteId == null) currentSiteId = s.getId();

			Integer cDepth =  Integer.valueOf(0);
			if ( computeDepth )
			{
				ResourceProperties rp = s.getProperties();
				String ourParent = rp.getProperty(PROP_PARENT_ID);
				// System.out.println("Depth Site:"+s.getTitle()+
				// "parent="+ourParent);
				if (ourParent != null)
				{
					Integer pDepth = depthChart.get(ourParent);
					if (pDepth != null)
					{
						cDepth = pDepth + 1;
					}
				}
				depthChart.put(s.getId(), cDepth);
				// System.out.println("Depth = "+cDepth);
			}

			Map m = convertSiteToMap(req, s, prefix, currentSiteId, myWorkspaceSiteId,
					includeSummary, expandSite, resetTools, doPages, toolContextPath,
					loggedIn);

			// Add the Depth of the site
			m.put("depth", cDepth);

			// And indicate whether it's a favorite or not
			m.put("favorite", favorites.contains(s.getId()));

			if (includeSummary && m.get("rssDescription") == null)
			{
				if (!motdDone)
				{
					summarizeTool(m, s, "sakai.motd");
					motdDone = true;
				}
				else
				{
					summarizeTool(m, s, "sakai.announcements");
				}

			}
			l.add(m);
		}
		return l;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserSpecificSiteTitle( Site site, boolean escaped )
	{
		return getUserSpecificSiteTitle( site, true, escaped );
	}

	@Override
	public String getUserSpecificSiteTitle(Site site, boolean truncated, boolean escaped)
	{
		String retVal = SiteService.getUserSpecificSiteTitle( site, UserDirectoryService.getCurrentUser().getId() );
		if( truncated )
		{
			retVal = FormattedText.makeShortenedText( retVal, null, null, null );
		}

		if( escaped )
		{
			retVal = Web.escapeHtml( retVal );
		}

		return retVal;
	}

	/**
	 * Explode a site into a map suitable for use in the map
	 * @param prefix The handler used for displaying a site, typically "site" or "site-reset".
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#convertSiteToMap(javax.servlet.http.HttpServletRequest,
	 *      org.sakaiproject.site.api.Site, java.lang.String, java.lang.String,
	 *      java.lang.String, boolean, boolean, boolean, boolean,
	 *      java.lang.String, boolean)
	 */
	public Map convertSiteToMap(HttpServletRequest req, Site s, String prefix,
			String currentSiteId, String myWorkspaceSiteId, boolean includeSummary,
			boolean expandSite, boolean resetTools, boolean doPages,
			String toolContextPath, boolean loggedIn)
	{
		if (s == null) return null;
		Map<String, Object> m = new HashMap<>();

		// In case the effective is different than the actual site
		String effectiveSite = getSiteEffectiveId(s);

		boolean isCurrentSite = currentSiteId != null
				&& (s.getId().equals(currentSiteId) || effectiveSite
						.equals(currentSiteId));
		m.put("isCurrentSite", Boolean.valueOf(isCurrentSite));
		m.put("isPublished", s.isPublished());
		m.put("isMyWorkspace", Boolean.valueOf(myWorkspaceSiteId != null
				&& (s.getId().equals(myWorkspaceSiteId) || effectiveSite
						.equals(myWorkspaceSiteId))));
		
		// SAK-29138
		String siteTitleTruncated = getUserSpecificSiteTitle( s, true, true );
		String siteTitleNotTruncated = getUserSpecificSiteTitle( s, false, true );
		m.put( "siteTitleNotTruncated", siteTitleNotTruncated );
		m.put( "siteTitle", siteTitleTruncated );
		m.put( "fullTitle", siteTitleNotTruncated );
		
		m.put("siteDescription", s.getHtmlDescription());

		if ( s.getShortDescription() !=null && s.getShortDescription().trim().length()>0 ){
			// SAK-23895:  Allow display of site description in the tab instead of site title
			String shortDesc = s.getShortDescription(); 
			String shortDesc_trimmed = FormattedText.makeShortenedText(shortDesc, null, null, null);
			m.put("shortDescription", Web.escapeHtml(shortDesc_trimmed));
		}

		String siteUrlPrefix = Web.serverUrl(req)
				+ ServerConfigurationService.getString("portalPath") + "/";
		if (prefix != null) siteUrlPrefix = siteUrlPrefix + prefix + "/";
		// siteUrl = siteUrl + Web.escapeUrl(siteHelper.getSiteEffectiveId(s));
		m.put("siteUrl", siteUrlPrefix + Web.escapeUrl(getSiteEffectiveId(s)));
		m.put("siteType", s.getType());
		m.put("siteId", s.getId());

		// TODO: This should come from the site neighbourhood.
		ResourceProperties rp = s.getProperties();
		String ourParent = rp.getProperty(PROP_PARENT_ID);
		// We are not really a child unless the parent exists
		// And we have a valid pwd
		boolean isChild = false;

		// Get the current site hierarchy
		if (ourParent != null && isCurrentSite)
		{
			List<Site> pwd = getPwd(s, ourParent);
			if (pwd != null)
			{
				List<Map> l = new ArrayList<>();
				// SAK-30477
				// Skip current site size - 1
				for (int i = 0; i < pwd.size() - 1; i++)
				{
					Site site = pwd.get(i);
					Map<String, Object> pm = new HashMap<>();
					String siteTitleTruncatedBreadCrumb = getUserSpecificSiteTitle( site, true, true );
					String siteTitleNotTruncatedBreadCrumb = getUserSpecificSiteTitle( site, false, true );
					
					pm.put("siteTitleNotTruncated", siteTitleNotTruncatedBreadCrumb );
					pm.put("siteTitle", siteTitleTruncatedBreadCrumb );
					pm.put("fullTitle", siteTitleNotTruncatedBreadCrumb );
					pm.put("siteUrl", siteUrlPrefix + Web.escapeUrl(getSiteEffectiveId(site)));

					l.add(pm);
					isChild = true;
				}
				if ( l.size() > 0 ) m.put("pwd", l);
			}
		}
		
		// If we are a child and have a non-zero length, pwd
		// show breadcrumbs
		if ( isChild ) {
			m.put("isChild", Boolean.valueOf(isChild));
			m.put("parentSite", ourParent);
		}

		if (includeSummary)
		{
			summarizeTool(m, s, "sakai.announce");
		}
		if (expandSite)
		{
			Map pageMap = pageListToMap(req, loggedIn, s, /* SitePage */null,
					toolContextPath, prefix, doPages, resetTools, includeSummary);
			m.put("sitePages", pageMap);
		}

		return m;
	}

	/**
	 * Gets the path of sites back to the root of the tree.
	 * @param s
	 * @param ourParent
	 * @return
	 */
	private List<Site> getPwd(Site s, String ourParent)
	{
		if (ourParent == null) return null;

		// System.out.println("Getting Current Working Directory for
		// "+s.getId()+" "+s.getTitle());

		int depth = 0;
		Vector<Site> pwd = new Vector<Site>();
		Set<String> added = new HashSet<String>();

		// Add us to the list at the top (will become the end)
		pwd.add(s);
		added.add(s.getId());

		// Make sure we don't go on forever
		while (ourParent != null && depth < 8)
		{
			depth++;
			Site site = null;
			try
			{
				site = SiteService.getSiteVisit(ourParent);
			}
			catch (Exception e)
			{
				break;
			}
			// We have no patience with loops
			if (added.contains(site.getId())) break;

			// System.out.println("Adding Parent "+site.getId()+"
			// "+site.getTitle());
			pwd.insertElementAt(site, 0); // Push down stack
			added.add(site.getId());

			ResourceProperties rp = site.getProperties();
			ourParent = rp.getProperty(PROP_PARENT_ID);
		}

		// PWD is only defined for > 1 site
		if (pwd.size() < 2) return null;
		return pwd;
	}

	/**
	 * Produce a page and/or a tool list doPage = true is best for the
	 * tabs-based portal and for RSS - these think in terms of pages doPage =
	 * false is best for the portlet-style - it unrolls all of the tools unless
	 * a page is marked as a popup. If the page is a popup - it is left a page
	 * and marked as such. restTools = true - generate resetting tool URLs.
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#pageListToMap(javax.servlet.http.HttpServletRequest,
	 *      boolean, org.sakaiproject.site.api.Site,
	 *      org.sakaiproject.site.api.SitePage, java.lang.String,
	 *      java.lang.String, boolean, boolean, boolean)
	 */
	public Map pageListToMap(HttpServletRequest req, boolean loggedIn, Site site,
			SitePage page, String toolContextPath, String portalPrefix, boolean doPages,
			boolean resetTools, boolean includeSummary)
	{

		Map<String, Object> theMap = new HashMap<String, Object>();

		String effectiveSiteId = getSiteEffectiveId(site);
		
		// Should be pushed up to the API, similar to server configiuration service, but supporting an Enum(always, never, true, false).
		boolean showHelp = true;
		// Supports true, false, never, always
		String showHelpGlobal = ServerConfigurationService.getString("display.help.menu", "true");
		
		if ("never".equals(showHelp))
		{
			showHelp = false;
		}
		else if ("always".equals(showHelpGlobal))
		{
			showHelp = true;
		}
		else
		{
			showHelp = Boolean.valueOf(showHelpGlobal).booleanValue();
			String showHelpSite = site.getProperties().getProperty("display-help-menu");
			if (showHelpSite != null)
			{
				showHelp = Boolean.valueOf(showHelpSite).booleanValue();
			}
		}
		
		String iconUrl = "";
		try { 
			if (site.getIconUrlFull() != null)
				iconUrl = new URI(site.getIconUrlFull()).toString();
		} catch (URISyntaxException uex) {
			log.debug("Icon URL is invalid: " + site.getIconUrlFull());
		}

		boolean published = site.isPublished();
		String type = site.getType();

		theMap.put("siteId", site.getId());
		theMap.put("pageNavPublished", Boolean.valueOf(published));
		theMap.put("pageNavType", type);
		theMap.put("pageNavIconUrl", iconUrl);
		String htmlInclude = site.getProperties().getProperty(PROP_HTML_INCLUDE);
		if (htmlInclude != null) theMap.put("siteHTMLInclude", htmlInclude);

		// theMap.put("pageNavSitToolsHead",
		// Web.escapeHtml(rb.getString("sit_toolshead")));

		// order the pages based on their tools and the tool order for the
		// site type
		// List pages = site.getOrderedPages();
		List pages = getPermittedPagesInOrder(site);

		List<Map> l = new ArrayList<Map>();

		String addMoreToolsUrl = null;
		for (Iterator i = pages.iterator(); i.hasNext();)
		{

			SitePage p = (SitePage) i.next();
			// check if current user has permission to see page
			// one tool on the page
			List<ToolConfiguration> pTools = p.getTools();
			ToolConfiguration firstTool = null;
			String toolsOnPage = null;

			// Check the tools that indicate the portal is to do the popup
			Iterator<ToolConfiguration> toolz = pTools.iterator();
			String source = null;
			int count = 0;
			ToolConfiguration pageTool = null;
			while(toolz.hasNext()){
				count++;
				pageTool = toolz.next();
				source = ToolUtils.getToolPopupUrl(pageTool);
				if ( "sakai.siteinfo".equals(pageTool.getToolId()) ) {
					addMoreToolsUrl = ToolUtils.getPageUrl(req, site, p, portalPrefix, 
						resetTools, effectiveSiteId, null);
					addMoreToolsUrl += "?sakai_action=doMenu_edit_site_tools&panel=Shortcut";
				}
			}
			if ( count != 1 ) {
				source = null;
				addMoreToolsUrl = null;
				pageTool = null;
			}

			boolean current = (page != null && p.getId().equals(page.getId()) && !p
					.isPopUp());
			String pageAlias = lookupPageToAlias(site.getId(), p);
			String pagerefUrl = ToolUtils.getPageUrl(req, site, p, portalPrefix, 
				resetTools, effectiveSiteId, pageAlias);

			if (doPages || p.isPopUp())
			{
				boolean siteUpdate = SecurityService.unlock("site.upd", site.getReference());
				if ( ! siteUpdate ) addMoreToolsUrl = null;

				if ( ! ServerConfigurationService.getBoolean("portal.experimental.addmoretools", false) ) addMoreToolsUrl = null;

				Map<String, Object> m = pageToMap(req, site, includeSummary, p, pTools, firstTool, source, current, pagerefUrl);
				l.add(m);
				continue;
			}

			String siteUrl = (String) ThreadLocalManager.get("sakai:portal:siteUrl");
			if(siteUrl == null)
			{
				siteUrl = Web.escapeUrl(getSiteEffectiveId(site));
			}

			String toolUrl = Web.returnUrl(req, "/" + portalPrefix
					+ siteUrl);

			if (resetTools) {
				toolUrl = toolUrl + "/tool-reset/";
			} else {
				toolUrl = toolUrl + "/tool/";
			}

			// Loop through the tools again and Unroll the tools
			Iterator iPt = pTools.iterator();

			while (iPt.hasNext())
			{
				ToolConfiguration placement = (ToolConfiguration) iPt.next();

				Tool tool = placement.getTool();
				if (tool != null)
				{
					String toolrefUrl = toolUrl + Web.escapeUrl(placement.getId());
					
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("isPage", Boolean.valueOf(false));
					m.put("toolId", Web.escapeUrl(placement.getId()));
					m.put("jsToolId", Web.escapeJavascript(placement.getId()));
					m.put("toolRegistryId", placement.getToolId());
					m.put("toolTitle", Web.escapeHtml(placement.getTitle()));
					m.put("jsToolTitle", Web.escapeJavascript(placement.getTitle()));
					m.put("toolrefUrl", toolrefUrl);
					m.put("toolpopup", Boolean.valueOf(source!=null));
					m.put("toolpopupurl", source);
					String menuClass = placement.getToolId();
					menuClass = "icon-" + menuClass.replace('.', '-');
					m.put("menuClass", menuClass);
					Properties tmp = placement.getConfig();
					if ( tmp != null ) {
						String mc = tmp.getProperty(PROP_MENU_CLASS);
						if ( mc != null && mc.length() > 0 ) m.put("menuClassOverride", mc);
					}
					// this is here to allow the tool reorder to work if requried.
					m.put("_placement", placement);
					l.add(m);
				}
			}

		}

		if ( addMoreToolsUrl != null ) {
			theMap.put("pageNavAddMoreToolsUrl", addMoreToolsUrl);
			theMap.put("pageNavCanAddMoreTools", true);
		} else {
			theMap.put("pageNavCanAddMoreTools", false);
		}

		theMap.put("pageNavTools", l);
		theMap.put("pageMaxIfSingle", ServerConfigurationService.getBoolean(
				"portal.experimental.maximizesinglepage", false));
		theMap.put("pageNavToolsCount", Integer.valueOf(l.size()));

		String helpUrl = ServerConfigurationService.getHelpUrl(null);
		theMap.put("pageNavShowHelp", Boolean.valueOf(showHelp));
		theMap.put("pageNavHelpUrl", helpUrl);
		theMap.put("helpMenuClass", "icon-sakai-help");

		// theMap.put("pageNavSitContentshead",
		// Web.escapeHtml(rb.getString("sit_contentshead")));

		// Display presence? Global property display.users.present may be always / never / true / false
		// If true or false, the value may be overriden by the site property display-users-present
		// which may be true or false.
				
		boolean showPresence;
		String globalShowPresence = ServerConfigurationService.getString("display.users.present","true");
				
		if ("never".equals(globalShowPresence)) {
			showPresence = false;
		} else if ("always".equals(globalShowPresence)) {
			showPresence = true;
		} else {
			showPresence = Boolean.valueOf(globalShowPresence).booleanValue();
			String showPresenceSite = site.getProperties().getProperty("display-users-present");
			if (showPresenceSite != null)
			{
				showPresence = Boolean.valueOf(showPresenceSite).booleanValue();
			}	
		}
		
		// Check to see if this is a my workspace site, and if so, whether presence is disabled
		if (showPresence && SiteService.isUserSite(site.getId()) && !ServerConfigurationService.getBoolean("display.users.present.myworkspace", false))
			showPresence = false;
		
		String presenceUrl = Web.returnUrl(req, "/presence/"
				+ Web.escapeUrl(site.getId()));

		// theMap.put("pageNavSitPresenceTitle",
		// Web.escapeHtml(rb.getString("sit_presencetitle")));
		// theMap.put("pageNavSitPresenceFrameTitle",
		// Web.escapeHtml(rb.getString("sit_presenceiframetit")));
		theMap.put("pageNavShowPresenceLoggedIn", Boolean.valueOf(showPresence
				&& loggedIn));
		theMap.put("pageNavPresenceUrl", presenceUrl);

		//add softly deleted status and if true, formatted date of planned deletion.
		boolean isSoftlyDeleted = site.isSoftlyDeleted();
		theMap.put("softlyDeleted", isSoftlyDeleted);
		if (isSoftlyDeleted) {
			long milliSinceEpoch = site.getSoftlyDeletedDate().getTime();
			LocalDate siteDeletedDate = LocalDate.ofEpochDay(milliSinceEpoch/(1000*60*60*24)); 
			int graceNumDays = ServerConfigurationService.getInt("site.soft.deletion.gracetime", 30); 
			LocalDate graceDaysRemaining = siteDeletedDate.plusDays(graceNumDays);
			DateTimeFormatter df = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(new ResourceLoader().getLocale());
			theMap.put("graceDaysRemaining", graceDaysRemaining.format(df));
		}

		// Retrieve whether or not we are to put presence in a frame
		theMap.put("pageNavPresenceIframe", Boolean.valueOf(
			ServerConfigurationService.getBoolean("display.users.present.iframe", false)) );
		theMap.put("sakaiPresenceTimeDelay", Integer.valueOf(
			ServerConfigurationService.getInt("display.users.present.time.delay", 3000)) );

		return theMap;
	}

	/**
	 * Collapse a string to only allow characters that can be in variables
		 */
	public String collapseToVariable(String inp)
	{
		if ( inp == null ) return null;
		return inp.replaceAll("[^-_.a-zA-Z0-9]","");
	}

	public Map<String, Object> pageToMap(HttpServletRequest req, Site site, boolean includeSummary, SitePage p, List<ToolConfiguration> pTools, ToolConfiguration firstTool, String source, boolean current, String pagerefUrl) {
		Map<String, Object> m = new HashMap<String, Object>();
		StringBuffer desc = new StringBuffer();

		boolean hidden = false;
		if (pTools != null && pTools.size() > 0) {
			firstTool = pTools.get(0);
			hidden = true; // Only set the page to hidden when we have tools that might un-hide it.
			Iterator<ToolConfiguration> tools = pTools.iterator();
			//get the tool descriptions for this page, typically only one per page, execpt for the Home page
			int tCount = 0;
			while(tools.hasNext()){
				ToolConfiguration t = tools.next();
				if (hidden && !isHidden(t))
				{
					hidden = false;
				}
				if (tCount > 0){
					desc.append(" | ");
				}
				if ( t.getTool() == null ) continue;
				desc.append(t.getTool().getDescription());
				tCount++;
			}
		}

		String pageResetUrl = pagerefUrl;
		if(pagerefUrl != null){
			if(pagerefUrl.contains("/tool/")){
				pageResetUrl = PortalStringUtil.replaceFirst(pagerefUrl, "/tool/", "/tool-reset/");
			}else if(pagerefUrl.contains("/page/")){
				pageResetUrl = PortalStringUtil.replaceFirst(pagerefUrl, "/page/", "/page-reset/");
			}
		}

		String pagePopupUrl = Web.returnUrl(req, "/page/");
		m.put("isPage", Boolean.valueOf(true));
		m.put("current", Boolean.valueOf(current));
		m.put("ispopup", Boolean.valueOf(p.isPopUp()));
		m.put("pagePopupUrl", pagePopupUrl);
		m.put("pageTitle", Web.escapeHtml(p.getTitle()));
		m.put("jsPageTitle", Web.escapeJavascript(p.getTitle()));
		m.put("pageId", Web.escapeUrl(p.getId()));
		m.put("jsPageId", Web.escapeJavascript(p.getId()));
		m.put("pageRefUrl", pagerefUrl);
		m.put("toolpopup", Boolean.valueOf(source!=null));
		m.put("toolpopupurl", source);
		m.put("pageResetUrl", pageResetUrl);

		// TODO: Should have Web.escapeHtmlAttribute()
		String description = desc.toString().replace("\"","&quot;");
		m.put("description",  description);
		m.put("hidden", Boolean.valueOf(hidden));
		// toolsOnPage is always null
		//if (toolsOnPage != null) m.put("toolsOnPage", toolsOnPage);
		if (firstTool != null)
		{
			String menuClass = firstTool.getToolId();
			menuClass = "icon-" + menuClass.replace('.', '-');
			m.put("menuClass", menuClass);
			Properties tmp = firstTool.getConfig();
			if ( tmp != null ) {
				String mc = tmp.getProperty(PROP_MENU_CLASS);
				if ( mc != null && mc.length() > 0 ) m.put("menuClassOverride", mc);
				String fa = tmp.getProperty("imsti.fa_icon");
				if ( fa != null && fa.length() > 0 ) {
					m.put("menuClass", "fa");
					m.put("menuClassOverride", collapseToVariable(fa));
				}
			}
		}
		else
		{
			m.put("menuClass", "icon-default-tool");
		}
		m.put("pageProps", createPageProps(p));
		// this is here to allow the tool reorder to work
		m.put("_sitePage", p);
		return m;
	}

	/**
	 * @param p
	 * @return
	 */
	private Map createPageProps(SitePage p)
	{
		Map properties = new HashMap();
		for (Iterator<String> i = p.getProperties().getPropertyNames(); i.hasNext();)
		{
			String propName = i.next();
			properties.put(propName, p.getProperties().get(propName));
		}

		return properties;
	}


	/**
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#getMyWorkspace(org.sakaiproject.tool.api.Session)
	 */
	public Site getMyWorkspace(Session session)
	{
		String userId = session.getUserId();
		if (userId == null)
		{
			return null;
		}
		String siteId = SiteService.getUserSiteId(session.getUserId());

		// Make sure we can visit
		Site site = null;
		try
		{
			site = getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			site = null;
		}
		catch (PermissionException e)
		{
			site = null;
		}

		return site;
	}

	/*
	 * Temporarily set a placement with the site id as the context - we do not
	 * set a tool ID this will not be a rich enough placement to do *everything*
	 * but for those services which call
	 * ToolManager.getCurrentPlacement().getContext() to contextualize their
	 * information - it wil be sufficient.
	 */

	public boolean setTemporaryPlacement(Site site)
	{
		if (site == null) return false;

		Placement ppp = toolManager.getCurrentPlacement();
		if (ppp != null && site.getId().equals(ppp.getContext()))
		{
			return true;
		}

		// Create a site-only placement
		Placement placement = new org.sakaiproject.util.Placement("portal-temporary", /* toolId */
		null, /* tool */null,
		/* config */null, /* context */site.getId(), /* title */null);

		ThreadLocalManager.set(CURRENT_PLACEMENT, placement);

		// Debugging
		ppp = toolManager.getCurrentPlacement();
		if (ppp == null)
		{
			log.warn("portal-temporary placement not set - null");
		}
		else
		{
			String cont = ppp.getContext();
			if (site.getId().equals(cont))
			{
				return true;
			}
			else
			{
				log.warn("portal-temporary placement mismatch site="
						+ site.getId() + " context=" + cont);
			}
		}
		return false;
	}

	public boolean summarizePage(Map m, Site site, SitePage page)
	{
		List pTools = page.getTools();
		Iterator iPt = pTools.iterator();
		while (iPt.hasNext())
		{
			ToolConfiguration placement = (ToolConfiguration) iPt.next();

			if (summarizeTool(m, site, placement.getToolId()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * There must be a better way of doing this as this hard codes the services
	 * in surely there should be some whay of looking up the serivce and making
	 * the getSummary part of an interface. TODO: Add an interface beside
	 * EntityProducer to generate summaries Make this discoverable
	 * 
	 * @param m
	 * @param site
	 * @param toolIdentifier
	 * @return
	 */
	private boolean summarizeTool(Map m, Site site, String toolIdentifier)
	{
		if (site == null) return false;

		setTemporaryPlacement(site);
		Map newMap = null;

		/*
		 * This is a new, cooler way to do this (I hope) chmaurer... (ieb) Yes:)
		 * All summaries now through this interface
		 */

		// offer to all EntityProducers
		for (Iterator i = EntityManager.getEntityProducers().iterator(); i.hasNext();)
		{
			EntityProducer ep = (EntityProducer) i.next();
			if (ep instanceof EntitySummary)
			{
				try
				{
					EntitySummary es = (EntitySummary) ep;

					// if this producer claims this tool id
					if (ArrayUtil.contains(es.summarizableToolIds(), toolIdentifier))
					{
						String summarizableReference = es.getSummarizableReference(site
								.getId(), toolIdentifier);
						newMap = es.getSummary(summarizableReference, 5, 30);
					}
				}
				catch (Throwable t)
				{
					log.warn(
							"Error encountered while asking EntitySummary to getSummary() for: "
									+ toolIdentifier, t);
				}
			}
		}

		if (newMap != null)
		{
			return (MapUtil.copyHtml(m, "rssDescription", newMap,
					Summary.PROP_DESCRIPTION) && MapUtil.copy(m, "rssPubdate", newMap,
					Summary.PROP_PUBDATE));
		}
		else
		{
			Time modDate = site.getModifiedTime();
			// Yes, some sites have never been modified
			if (modDate != null)
			{
				m.put("rssPubDate", (modDate.toStringRFC822Local()));
			}
			return false;
		}

	}

	/**
	 * If this is a user site, return an id based on the user EID, otherwise
	 * just return the site id.
	 * 
	 * @param site
	 *        The site.
	 * @return The effective site id.
	 */
	public String getSiteEffectiveId(Site site)
	{
		if (SiteService.isUserSite(site.getId()))
		{
			try
			{
				String userId = SiteService.getSiteUserId(site.getId());
				String eid = UserDirectoryService.getUserEid(userId);
				return SiteService.getUserSiteId(eid);
			}
			catch (UserNotDefinedException e)
			{
				log.warn("getSiteEffectiveId: user eid not found for user site: "
						+ site.getId());
			}
		}
		else
		{
			String displayId = siteNeighbourhoodService.lookupSiteAlias(site.getReference(), null);
			if (displayId != null)
			{
				if (displayId.startsWith("/"))
				{
					displayId = displayId.substring(1);
				}
				if (displayId.endsWith("/"))
				{
					displayId = displayId.substring(0, displayId.length()-1);
				}
				return displayId;
			}
		}

		return site.getId();
	}

	/**
	 * Do the getSiteVisit, but if not found and the id is a user site, try
	 * translating from user EID to ID.
	 * 
	 * @param siteId
	 *        The Site Id.
	 * @return The Site.
	 * @throws PermissionException
	 *         If not allowed.
	 * @throws IdUnusedException
	 *         If not found.
	 */
	public Site getSiteVisit(String siteId) throws PermissionException, IdUnusedException
	{
		try
		{
			return SiteService.getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			if (SiteService.isUserSite(siteId))
			{
				try
				{
					String userEid = SiteService.getSiteUserId(siteId);
					String userId = UserDirectoryService.getUserId(userEid);
					String alternateSiteId = SiteService.getUserSiteId(userId);
					return SiteService.getSiteVisit(alternateSiteId);
				}
				catch (UserNotDefinedException ee)
				{
				}
			}
			else
			{
				String reference = siteNeighbourhoodService.parseSiteAlias(siteId);
				if (reference != null) {
					Reference ref = EntityManager.getInstance().newReference(reference);
					try {
						return SiteService.getSiteVisit(ref.getId());
					} catch (IdUnusedException iue) {
					}
				}
			}

			// re-throw if that didn't work
			throw e;
		}
	}



	/**
	 * Retrieve the list of pages in this site, checking to see if the user has
	 * permission to see the page - by checking the permissions of tools on the
	 * page.
	 * 
	 * @param site
	 * @return
	 */
	public List<SitePage> getPermittedPagesInOrder(Site site)
	{
		// Get all of the pages
		List<SitePage> pages = site.getOrderedPages();
		boolean siteUpdate = SecurityService.unlock("site.upd", site.getReference());

		List<SitePage> newPages = new ArrayList<>();
		newPages.addAll(pages);
		if (pageFilter != null)
		{
			newPages = pageFilter.filter(newPages, site);
		}

		for (Iterator<SitePage> i = newPages.iterator(); i.hasNext();)
		{
			// check if current user has permission to see page
			SitePage p = i.next();
			List pTools = p.getTools();
			Iterator<ToolConfiguration> iPt = pTools.iterator();

			boolean allowPage = false;
			while (iPt.hasNext())
			{
				ToolConfiguration placement = iPt.next();

				boolean thisTool = allowTool(site, placement);
				boolean unHidden = siteUpdate || ! isHidden(placement);
				if (thisTool && unHidden) allowPage = true;
			}
			if (!allowPage) i.remove();
		}

		return newPages;
	}

	/**
	 * Make sure that we have a proper page selected in the site pageid is
	 * generally the last page used in the site. pageId must be in the site and
	 * the user must have permission for the page as well.
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#lookupSitePage(java.lang.String,
	 *      org.sakaiproject.site.api.Site)
	 */
	public SitePage lookupSitePage(String pageId, Site site)
	{
		// Make sure we have some permitted pages
		List<SitePage> pages = getPermittedPagesInOrder(site);
		if (pages.isEmpty()) return null;
		// Look in the permitted pages.
		SitePage page = pages.stream().filter(p -> p.getId().equals(pageId)).findFirst().orElse(null);
		if (page == null)
		{
			page = lookupAliasToPage(pageId, site);
			if (page == null)
			{
				page = pages.get(0);
				return page;
			}
		}

		// Make sure that they user has permission for the page.
		// If the page is not in the permitted list go to the first
		// page.
		boolean found = false;
		for (Iterator i = pages.iterator(); i.hasNext();)
		{
			SitePage p = (SitePage) i.next();
			if (p.getId().equals(page.getId())) return page;
		}

		return (SitePage) pages.get(0);
	}

	public SitePage lookupAliasToPage(String alias, Site site)
	{
		SitePage page = null;
		if (alias != null && alias.length() > 0)
		{
			try
			{
				// Use page#{siteId}:{pageAlias} So we can scan for fist colon and alias can contain any character 
				String refString = aliasService.getTarget(buildAlias(alias, site));
				String aliasPageId = EntityManager.newReference(refString).getId();
				page = (SitePage) site.getPage(aliasPageId);
			}
			catch (IdUnusedException e)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Alias does not resolve " + e.getMessage());
				}
			}
		}
		return page;
	}

	public String lookupPageToAlias(String siteId, SitePage page)
	{
		// Shortcut if we aren't using page aliases.
		if (!lookForPageAliases)
		{
			return null;
		}
		String alias = null;
		List<Alias> aliases = aliasService.getAliases(page.getReference());
		if (aliases.size() > 0)
		{	
			if (aliases.size() > 1 && log.isWarnEnabled())
			{
				if (log.isDebugEnabled()) {
					log.debug("More than one alias for: "+siteId+ ":"+ page.getId());
				}
				// Sort on ID so it is consistent in the alias it uses.
				Collections.sort(aliases, getAliasComparator());
			}
			alias = aliases.get(0).getId();
			alias = parseAlias(alias, siteId);
		}
		return alias;
	}

	/**
	 * Find the short alias.
	 * @param alias
	 * @return
	 */
	private String parseAlias(String aliasId, String siteId)
	{
		String prefix = PAGE_ALIAS+ siteId+ Entity.SEPARATOR;
		String alias = null;
		if (aliasId.startsWith(prefix))
		{
			alias = aliasId.substring(prefix.length());
		}
		return alias;
	}

	private String buildAlias(String alias, Site site)
	{
		return PAGE_ALIAS+site.getId()+Entity.SEPARATOR+alias;
	}

	public Comparator<Alias> getAliasComparator()
	{
		return new Comparator<Alias>() {
			public int compare(Alias alias, Alias otherAlias)
			{
				// Sort by date, then by ID to assure consistent order.
				return otherAlias.getCreatedTime().compareTo(alias.getCreatedTime()) * 10 +
					Integer.signum(alias.getId().compareTo(otherAlias.getId()));
			}
			
		};
	}
	
	public boolean allowTool(Site site, Placement placement)
	{
		return toolManager.allowTool(site, placement);
	}

	/**
	 * Check to see if a tool placement is hidden.
	 * Can be used to check is a page should be hidden.
	 */
	public boolean isHidden(Placement placement)
	{
		return toolManager.isHidden(placement);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.portal.api.PortalSiteHelper#getSitesView(org.sakaiproject.portal.api.SiteView.View,
	 *      javax.servlet.http.HttpServletRequest,
	 *      org.sakaiproject.tool.api.Session, java.lang.String)
	 */
	public SiteView getSitesView(View view, HttpServletRequest request, Session session,
			String siteId, String nodeId)
	{
		switch (view)
		{
			case CURRENT_SITE_VIEW:
				return new CurrentSiteViewImpl(this, siteNeighbourhoodService, request, session, siteId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
			case ALL_SITES_VIEW:
				return new AllSitesViewImpl(this, siteNeighbourhoodService, request, session, siteId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
			case DHTML_MORE_VIEW:
				return new MoreSiteViewImpl(this,siteNeighbourhoodService, request, session, siteId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
			case SUB_SITES_VIEW:
				return new SubSiteViewImpl(this, siteNeighbourhoodService, request, session, siteId, nodeId, SiteService
						.getInstance(), ServerConfigurationService.getInstance(),
						PreferencesService.getInstance());
		}
		return null;
	}

	public boolean isJoinable(String siteId, String userId) {
		Site site = null;
		try {
			site = getSite(siteId);
		} catch (IdUnusedException e) {
		}
		return site != null && site.isJoinable() && site.getUserRole(userId) == null;
	}

	public Site getSite(String siteId) throws IdUnusedException
	{
		Site site = null;
		try {
			site = SiteService.getInstance().getSite(siteId);
			return site;
		} catch (IdUnusedException e) {
			// Attempt to lookup by alias.
			String reference = siteNeighbourhoodService.parseSiteAlias(siteId);
			if (reference != null)
			{
				Reference ref = EntityManager.getInstance().newReference(reference);
				try
				{
					site = SiteService.getInstance().getSite(ref.getId());
					return site;
				}
				catch (IdUnusedException e2)
				{
				}
			}
			throw e;
		}
	}

	public String getSiteAlias(String siteId) {
		return SiteService.getInstance().lookupSiteAlias(siteId);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSitePublished(String siteId) {
		boolean siteIsPublished = false;
		try {
			Site site = SiteService.getSite(siteId);
			siteIsPublished = site.isPublished();
		} catch (IdUnusedException e) {
			// not a site after all
		}
		return siteIsPublished;
	}

	@Override
	public Redirect getRedirect(String siteId) {
		return siteNeighbourhoodService.getRedirect(siteId);
	}

}
