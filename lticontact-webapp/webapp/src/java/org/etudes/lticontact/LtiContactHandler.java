/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/ltiContact/trunk/lticontact-webapp/webapp/src/java/org/etudes/lticontact/LtiContactHandler.java $
 * $Id: LtiContactHandler.java 8794 2014-09-18 17:33:54Z rashmim $
 ***********************************************************************************
 *
 * Copyright (c) 2013, 2014 Etudes, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.lticontact;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.basicltiContact.SakaiBLTIUtil;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 */
public class LtiContactHandler extends HttpServlet implements EntityProducer
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(LtiContactHandler.class);
	
	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = "/lticontact/";
	
	/**
	 * constructor registers with entity producer
	 */
	public LtiContactHandler()
	{
		EntityManager.registerEntityProducer(this, "/lticontact");
	}

	/**
	 * 
	 */
	public String getPrefix()
	{
		return "lticontact";
	}

	// entityProducer
	/**
	 * entityProducer {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * entityProducer {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return "nothing to archive \n";
	}

	/**
	 * entityProducer {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		return "nothing to merge \n";
	}

	/**
	 * entityProducer {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith("/lticontact/"))
		{
			String id = null;
			String context = null;
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);
			
			if (parts.length > 3)
			{
				context = parts[3];
				id = "/" + StringUtil.unsplit(parts, 2, parts.length - 2, "/");
			}

			ref.set("lticontact", null, id, null, context);

			return true;
		}

		return false;
	}

	/**
	 * entityProducer {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		// isolate the ContentHosting reference
		Reference contentHostingRef = EntityManager.newReference(ref.getId());

		// setup a security advisor
		pushAdvisor();
		try
		{
			// make sure we have a valid reference with an entity producer we can talk to
			EntityProducer service = ref.getEntityProducer();
			if (service == null) return null;

			// pass on the request
			return service.getEntityDescription(contentHostingRef);
		}
		finally
		{
			// clear the security advisor
			popAdvisor();
		}
	}

	/**
	 * entityProducer {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// isolate the reference
		Reference hostingRef = EntityManager.newReference(ref.getId());

		// setup a security advisor
		pushAdvisor();
		try
		{
			// make sure we have a valid reference with an entity producer we can talk to
			EntityProducer service = ref.getEntityProducer();
			if (service == null) return null;

			// pass on the request
			return service.getEntityResourceProperties(hostingRef);
		}
		finally
		{
			// clear the security advisor
			popAdvisor();
		}
	}

	/**
	 * entityProducer {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		// isolate the hosting reference
		Reference hostingRef = EntityManager.newReference(ref.getId());

		// setup a security advisor
		pushAdvisor();
		try
		{
			// make sure we have a valid reference with an entity producer we can talk to
			EntityProducer service = ref.getEntityProducer();
			if (service == null) return null;

			// pass on the request
			return service.getEntity(hostingRef);
		}
		finally
		{
			// clear the security advisor
			popAdvisor();
		}
	}

	/**
	 * entityProducer {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return ServerConfigurationService.getAccessUrl() + ref.getReference();
	}

	/**
	 * entityProducer {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		return null;
	}

	/**
	 * entityProducer {@inheritDoc}
	 * The url is in format /access/lticontact/provider/siteId/targetframe/launchAdddress
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
					throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
			{				
				boolean handled = false;
				// Find the site we are coming from
				String contextId = ref.getContext();
				String fullAddress = ref.getId();
				String whichProvider = "";
				String launchAddress = "";
				boolean secureAddress = false;
				String targetFrame = "frame";
				String resource_link_id = ref.getId();
				String[] parts = StringUtil.split(fullAddress, Entity.SEPARATOR);
				
				if (parts.length >= 5)
				{
					whichProvider = parts[1];
					targetFrame = ("true".equalsIgnoreCase(parts[3])) ? "window" : "iframe";
					resource_link_id= parts[4];
				}
				
				if (parts.length >= 6)
				{
					secureAddress = ("https:".equals(parts[5])) ? true : false;
					launchAddress = StringUtil.unsplit(parts, 6, parts.length - 6, "/");			
				}
						
				try
				{
					String postData = null;
					Placement placement = SakaiBLTIUtil.findProviderPlacement(contextId, whichProvider);

					Properties launch = new Properties();
					Properties info = new Properties();
					
					if (launchAddress != null && launchAddress.length() > 0)
					{
						if (secureAddress)
							info.setProperty("secure_launch_url", "https://" + launchAddress);
						else
							info.setProperty("launch_url", "http://" + launchAddress);
					}
					// if launchAddress is not specified then load default from placement alomg with key, secret and custom
					SakaiBLTIUtil.loadFromPlacement(info, null, placement);
					info.setProperty("autoSubmit", "true");
					
			     	String userId = SessionManager.getCurrentSessionUserId();      	 
			      	if  (SecurityService.unlock(userId, "site.upd", "/site/" + contextId)) launch.setProperty("roles", "Instructor");
			      	else launch.setProperty("roles", "Learner");
			      	
					String[] ltidata = SakaiBLTIUtil.postLaunchHTML(placement.getId(), null, contextId, resource_link_id, info, launch, true, null, null, targetFrame);
					postData = ltidata[0];
				

					if (postData == null)
					{
						postData = "<p>" + "Not configured." + "</p>";
					}
					else
					{
						res.setContentType("text/html");
						res.setCharacterEncoding("UTF-8");
						ServletOutputStream out = res.getOutputStream();
						out.println(postData);
						handled = true;
					}				
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}				
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "lticontact";
	}

	/**
	 * Push the advisor
	 */
	public void pushAdvisor()
	{
		// setup a security advisor
		SecurityService.pushAdvisor(new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Pop the advisor
	 */
	public void popAdvisor()
	{
		SecurityService.popAdvisor();
	}
}
