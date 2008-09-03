/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
 * licensed under the Educational Community License, Version 2.0
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.SecurityService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * SecurityServiceImpl implements Mneme's SecurityService
 */
public class SecurityServiceImpl implements SecurityService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(SecurityServiceImpl.class);

	/** Dependency: AuthzGroupService */
	protected AuthzGroupService authzGroupService = null;

	/** Dependency: SecurityService */
	protected org.sakaiproject.authz.api.SecurityService securityService = null;

	/** Dependency: SiteService */
	protected SiteService siteService = null;

	/** Dependency: ThreadLocalManager */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * {@inheritDoc}
	 */
	public boolean checkSecurity(String userId, String function, String context)
	{
		// check for super user
		if (securityService.isSuperUser(userId)) return true;

		// check for the user / function / context-as-site-authz
		// use the site ref for the security service (used to cache the security calls in the security service)
		String siteRef = siteService.siteReference(context);

		// form the azGroups for a context-as-implemented-by-site
		Collection azGroups = new Vector(2);
		azGroups.add(siteRef);
		azGroups.add("!site.helper");

		boolean rv = securityService.unlock(userId, function, siteRef, azGroups);
		return rv;
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getUsersIsAllowed(String function, String context)
	{
		// check the cache
		String key = function + "@" + context;
		Set<String> cached = (Set<String>) this.threadLocalManager.get(key);

		// if found, return a copy
		if (cached != null) return new HashSet(cached);

		// form the azGroups for a context-as-implemented-by-site
		Collection azGroups = new Vector(2);
		azGroups.add(siteService.siteReference(context));
		azGroups.add("!site.helper");

		// get the user ids who can
		Set userIds = authzGroupService.getUsersIsAllowed(function, azGroups);

		// cache
		this.threadLocalManager.set(key, new HashSet(userIds));

		return userIds;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void secure(String userId, String function, String context) throws AssessmentPermissionException
	{
		if (!checkSecurity(userId, function, context))
		{
			throw new AssessmentPermissionException(userId, function, context);
		}
	}

	/**
	 * Dependency: AuthzGroupService.
	 * 
	 * @param service
	 *        The AuthzGroupService.
	 */
	public void setAuthzGroupService(AuthzGroupService service)
	{
		authzGroupService = service;
	}

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(org.sakaiproject.authz.api.SecurityService service)
	{
		securityService = service;
	}

	/**
	 * Dependency: SiteService.
	 * 
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService service)
	{
		siteService = service;
	}

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The ThreadLocalManager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		this.threadLocalManager = service;
	}
}
