/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.portal.api.Neighbour;
import org.sakaiproject.portal.api.PortalSiteHelper;
import org.sakaiproject.portal.api.SiteNeighbourhoodService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.Web;

/**
 * @author ieb
 *
 */
public class SubSiteViewImpl extends AbstractSiteViewImpl
{

	private final List<Neighbour> neighbours;
	// The parent sites.
	private List<Site> pSites;
	// Joinable sites
	private List<Site> openSites;

	/**
	 * @param siteHelper
	 * @param request
	 * @param session
	 * @param currentSiteId
	 * @param siteService
	 * @param serverConfigurationService
	 * @param preferencesService
	 */
	public SubSiteViewImpl(PortalSiteHelper siteHelper, SiteNeighbourhoodService siteNeighbourhoodService, HttpServletRequest request,
						   Session session, String currentSiteId, String nodeId, SiteService siteService,
						   ServerConfigurationService serverConfigurationService,
						   PreferencesService preferencesService)
	{
		super(siteHelper, siteNeighbourhoodService, request, session, currentSiteId, nodeId, siteService,
				serverConfigurationService, preferencesService);
		neighbours = siteNeighbourhoodService.getNeighboursAtNode(request, session, nodeId, false);
		pSites = siteNeighbourhoodService.getParentsAtNode(request, session, nodeId, false);
		if(session.getUserId() != null){
			openSites = siteService.getSites(SiteService.SelectionType.JOINABLE,
						null, null, null, SiteService.SortType.TITLE_ASC, null);
		}
	}
	/* (non-Javadoc)
	 * @see org.sakaiproject.portal.api.SiteView#getRenderContextObject()
	 */
	public Object getRenderContextObject()
	{
		Map object = new HashMap();

		if (neighbours != null && !neighbours.isEmpty()) {
			List children = neighbours.stream().map(
					neighbour -> neighbour.getSite().map(site -> convertSite(site, request))
							.orElse(neighbour.getRedirect().map(redirect -> convertRedirect(redirect, request))
									.orElse(new HashMap()))
			).collect(Collectors.toList());
			object.put("children", children);
		}

		if(serverConfigurationService.getBoolean("portal.showJoinableAsSubsites", false)){
			if(openSites != null && !openSites.isEmpty() && !siteService.isGlobalJoinExcludedFromPublicListEnabled()){
				List joinable = openSites.stream().filter(
					open -> siteService.isAllowedToJoin(open.getId())
				).map(
					open -> convertSite(open, request)
				).collect(Collectors.toList());
				object.put("joinable", joinable);
			}
		}

		if(pSites != null && !neighbours.isEmpty()){
			List p = pSites.stream().map(parent -> convertSite(parent, request)).collect(Collectors.toList());
			if (!p.isEmpty()) {
				object.put("parents", p);
			}
		}
		return object;
	}

	@Override
	public boolean isEmpty() {
		return (neighbours == null || neighbours.isEmpty())
				&& (pSites == null || pSites.isEmpty())
				&& (openSites == null || openSites.isEmpty());
	}

	private Map convertSite(Site site, HttpServletRequest req) {
		Map map = new HashMap();
		// SAK-29138
		String siteTitleTruncated = siteHelper.getUserSpecificSiteTitle( site, true, true );
		String siteTitleNotTruncated = siteHelper.getUserSpecificSiteTitle( site, false, true );
		map.put( "titleTruncated", siteTitleTruncated );
		map.put( "title", siteTitleNotTruncated );

		String siteUrlPrefix = Web.serverUrl(req)
				+ org.sakaiproject.component.cover.ServerConfigurationService.getString("portalPath") + "/";
		if (prefix != null) siteUrlPrefix = siteUrlPrefix + prefix + "/";
		map.put("url", siteUrlPrefix + siteHelper.getSiteEffectiveId(site));
		map.put("iconClass", "icon-sakai-sub-site");
		map.put("hidden", !site.isPublished());
		return map;
	}

	private Map convertRedirect(Neighbour.Redirect redirect, HttpServletRequest request) {
		Map map = new HashMap();
		map.put("title", redirect.getTitle());
		map.put("titleTruncated", redirect.getTitle());
		String siteUrlPrefix = Web.serverUrl(request)
				+ org.sakaiproject.component.cover.ServerConfigurationService.getString("portalPath") + "/";
		if (prefix != null) siteUrlPrefix = siteUrlPrefix + prefix + "/";
		map.put("url", siteUrlPrefix + (redirect.getId() ));
		map.put("iconClass", "icon-sakai-redirect");
		map.put("hidden", redirect.isHidden());
		return map;
	}

}
