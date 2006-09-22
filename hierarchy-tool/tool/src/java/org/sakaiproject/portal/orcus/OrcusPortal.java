/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.orcus;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.hierarchy.cover.HierarchyService;
import org.sakaiproject.portal.charon.CharonPortal;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * Charon is the Sakai Site based portal.
 * </p>
 */
public class OrcusPortal extends CharonPortal
{
	private static final String TEMPLATE_INCLUDE_HIERARCHY_1 = "<div class=\"floattree\" ><ul class=\"tree\" >";

	private static final String TEMPLATE_INCLUDE_HIERARCHY_2 = "<li class=\"closed\" ><a href=\"#\" >Node:";

	private static final String TEMPLATE_INCLUDE_HIERARCHY_3 = "</a><ul>";

	private static final String TEMPLATE_INCLUDE_HIERARCHY_4 = "</ul></li>";

	private static final String TEMPLATE_INCLUDE_HIERARCHY_5 = "</ul></div>";

	private static final String TEMPLATE_INCLUDE_PROPERTIES_1 = "<li><a href=\"#\" >Property:";

	private static final String TEMPLATE_INCLUDE_PROPERTIES_2 = "=";

	private static final String TEMPLATE_INCLUDE_PROPERTIES_3 = "</a></li>";

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(OrcusPortal.class);

	/** messages. */
	private static ResourceLoader rb = new ResourceLoader("sitenav");






	
	protected void doSite(HttpServletRequest req, HttpServletResponse res, Session session, String siteId, String pageId,
			String toolContextPath) throws ToolException, IOException
	{
		// default site if not set
		if (siteId == null)
		{
			if (session.getUserId() == null)
			{
				siteId = ServerConfigurationService.getGatewaySiteId();
			}
			else
			{
				siteId = SiteService.getUserSiteId(session.getUserId());
			}
		}

		// if no page id, see if there was a last page visited for this site
		if (pageId == null)
		{
			pageId = (String) session.getAttribute(ATTR_SITE_PAGE + siteId);
		}

		// find the site, for visiting
		Site site = null;
		try
		{
			site = getSiteVisit(siteId);
		}
		catch (IdUnusedException e)
		{
			doError(req, res, session, ERROR_SITE);
			return;
		}
		catch (PermissionException e)
		{
			// if not logged in, give them a chance
			if (session.getUserId() == null)
			{
				doLogin(req, res, session, req.getPathInfo(), false);
			}
			else
			{
				doError(req, res, session, ERROR_SITE);
			}
			return;
		}

		// find the page, or use the first page if pageId not found
		SitePage page = site.getPage(pageId);
		if (page == null)
		{
			List pages = site.getOrderedPages();
			if (!pages.isEmpty())
			{
				page = (SitePage) site.getPages().get(0);
			}
		}
		if (page == null)
		{
			doError(req, res, session, ERROR_SITE);
			return;
		}

		// store the last page visited
		session.setAttribute(ATTR_SITE_PAGE + siteId, page.getId());

		// form a context sensitive title
		String title = ServerConfigurationService.getString("ui.service") + " : " + site.getTitle() + " : " + page.getTitle();

		// start the response
		PrintWriter out = startResponse(res, title, site.getSkin(), true);

		// the 'full' top area
		includeSiteNav(out, req, session, siteId);

		String siteType = calcSiteType(siteId);
		
		out.println("<div id=\"container\"");
		out.println(((siteType != null) ? " class=\""+ siteType + "\"" : "") + ">");
		includeHierarchyNav(out, req, session, siteId);
		out.println("<div>");
		
		out.println("<div id=\"container\"");
		out.println(((siteType != null) ? " class=\"" + siteType + "\"" : "") + ">");

		includeWorksite(out, req, session, site, page, toolContextPath, "site");
		out.println("<div>");

		includeBottom(out);

		// end the response
		endResponse(out);
	}

	// Checks to see which form of tool or page placement we have. The normal placement is
	// a GUID. However when the parameter sakai.site is added to the request, the placement
	// can be of the form sakai.resources. This routine determines which form of the
	// placement id, and if this is the second type, performs the lookup and returns the
	// GUID of the placement. If we cannot resolve the pllacement, we simply return
	// the passed in placement ID. If we cannot visit the site, we send the user to login
	// processing and return null to the caller.

	private void includeHierarchyNav(PrintWriter out, HttpServletRequest req, Session session, String siteId)
	{
		
		List rootNodes = HierarchyService.getRootNodes();
		
		out.println(TEMPLATE_INCLUDE_HIERARCHY_1);
		for ( Iterator i = rootNodes.iterator(); i.hasNext(); ) {
			Hierarchy h = (Hierarchy) i.next();
			out.println(TEMPLATE_INCLUDE_HIERARCHY_2);
			out.println(h.getPath());
			out.println(TEMPLATE_INCLUDE_HIERARCHY_3);
			includeChildren(out,h.getChildren());
			includeProperties(out,h.getProperties());
			out.println(TEMPLATE_INCLUDE_HIERARCHY_4);
		}
		out.println(TEMPLATE_INCLUDE_HIERARCHY_5);
		
	}
	
	private void includeProperties(PrintWriter out, Map properties)
	{
		for ( Iterator i = properties.values().iterator(); i.hasNext(); ) {
			HierarchyProperty h = (HierarchyProperty) i.next();
			out.println(TEMPLATE_INCLUDE_PROPERTIES_1);
			out.println(h.getName());
			out.println(TEMPLATE_INCLUDE_PROPERTIES_2);
			out.println(h.getPropvalue());
			out.println(TEMPLATE_INCLUDE_PROPERTIES_3);
		}
	}

	private void includeChildren(PrintWriter out, Map children)
	{
		for ( Iterator i = children.values().iterator(); i.hasNext(); ) {
			Hierarchy h = (Hierarchy) i.next();
			out.println("<li class=\"closed\" ><a href=\"#\" >Node:");
			out.println(h.getPath());
			out.println("</a><ul>");
			includeChildren(out,h.getChildren());
			includeProperties(out,h.getProperties());
			out.println("</ul></li>");
		}
	}

	private void addList(int levels, List l) {
		levels--;
		if ( levels == 0 ) {
			return;
		} else {
			for ( int i = 0; i < 5; i++ ) {
				l.add("Some text at item "+i);
			}
			List l2 = new ArrayList();
			l.add(l2);
			addList(levels,l2);
		}
	}
	private void includeList(PrintWriter out, List l) {
	for ( Iterator i = l.iterator(); i.hasNext(); ) {
		Object o = i.next();
		if ( o instanceof List ) {
			
			out.print("<li class=\"closed\" ><a href=\"#\">Folder/</a><ul>");		
			includeList(out,(List)o);
			out.println("</ul></li>");
		} else {
			out.print("<li><a href=\"#\" >");
			out.print(o);
			out.println("</a></li>");
		}
	}
	}

}
