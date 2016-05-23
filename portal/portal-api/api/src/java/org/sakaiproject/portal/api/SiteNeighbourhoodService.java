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

package org.sakaiproject.portal.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;

/**
 * The SiteNeighbourhoodService provides a list of sites in the neighbourhood of the current context.
 * This might be all sites, or it might be just the children, siblings and drect parents of a site.
 * @author ieb
 *
 */
public interface SiteNeighbourhoodService
{

	/**
	 * Get a list of sites at the current node as defined by the request
	 * @param request
	 * @param session
	 * @param includeMyWorksite
	 * @return
	 */
	List<Site> getSitesAtNode(HttpServletRequest request, Session session, String nodeId, boolean includeMyWorksite);

	/**
	 * Get a list of neighbours at the current node. This is needed so that more than just sites can be returned.
	 * In most cases it will be mainly sites.
     */
	List<Neighbour> getNeighboursAtNode(HttpServletRequest request, Session session, String nodeId, boolean includeMyWorksite);

	List<Site> getParentsAtNode(HttpServletRequest request, Session session, String nodeId, boolean includeMyWorksite);
	
	/**
	 * Convert an ID that is normally displayed in the URL into something more readable.
	 * @param siteId The ID to convert.
	 * @param content The context that this site is presented in.
	 * @return 
	 */
	String lookupSiteAlias(String reference, String context);
	
	
	/**
	 * Attempt to convert an alias back into a site reference.
	 * @param alias
	 * @return <code>null</code> if we couldn't find an reference that this maps to.
	 * TODO Shouldn't throw permission exception
	 */
	Site parseSiteAlias(String alias) throws PermissionException;

	/**
	 * Looks up a redirect for a site ID contained in a URL.
	 * @param siteId The string found in the URL that wasn't a site ID.
	 * @return A URL to redirect to or <code>null</code> if none is found.
     */
	String getRedirect(String siteId);
}
