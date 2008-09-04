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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.mneme.impl;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.ImportQtiService;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.SecurityService;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * ImportQtiServiceImpl implements ImportQtiService
 * </p>
 */
public class ImportQtiServiceImpl implements ImportQtiService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ImportQtiServiceImpl.class);

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService = null;

	/** Dependency: AttachmentService */
	protected AttachmentService attachmentService = null;

	/** Dependency: AuthzGroupService */
	protected AuthzGroupService authzGroupService = null;

	/** Messages bundle name. */
	protected String bundle = null;

	/** Dependency: EntityManager */
	protected EntityManager entityManager = null;

	/** Dependency: EventTrackingService */
	protected EventTrackingService eventTrackingService = null;

	/** Dependency: GradesService */
	protected GradesService gradesService = null;

	/** Messages. */
	protected transient InternationalizedMessages messages = null;

	/** Dependency: PoolService */
	protected PoolService poolService = null;

	/** Dependency: QuestionService */
	protected QuestionService questionService = null;

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/** Dependency: SiteService */
	protected SiteService siteService = null;

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

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
	public void importPool(Document doc, String context) throws AssessmentPermissionException
	{
		if ((doc == null) || (!doc.hasChildNodes())) return;

		// get a name for the pool, with the date
		String poolId = "pool";
		try
		{
			XPath x = new DOMXPath("/questestinterop/assessment");
			List l = x.selectNodes(doc);
			for (Object o : l)
			{
				Element e = (Element) o;
				String title = e.getAttribute("title");
				if (title != null)
				{
					poolId = title;
				}
			}
		}
		catch (JaxenException e)
		{
			M_log.warn(e);
		}

		// add a date stamp
		poolId = addDate("import-text", poolId, new Date());

		// create the pool
		Pool pool = this.poolService.newPool(context);
		pool.setTitle(poolId);
		// pool.setDescription(info.description);
		this.poolService.savePool(pool);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// messages
		if (this.bundle != null) this.messages = new ResourceLoader(this.bundle);

		M_log.info("init()");
	}

	/**
	 * Dependency: AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Dependency: AttachmentService.
	 * 
	 * @param service
	 *        The AttachmentService.
	 */
	public void setAttachmentService(AttachmentService service)
	{
		attachmentService = service;
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
	 * Set the message bundle.
	 * 
	 * @param bundle
	 *        The message bundle.
	 */
	public void setBundle(String name)
	{
		this.bundle = name;
	}

	/**
	 * Dependency: EntityManager.
	 * 
	 * @param service
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager service)
	{
		entityManager = service;
	}

	/**
	 * Dependency: EventTrackingService.
	 * 
	 * @param service
	 *        The EventTrackingService.
	 */
	public void setEventTrackingService(EventTrackingService service)
	{
		eventTrackingService = service;
	}

	/**
	 * Dependency: GradesService.
	 * 
	 * @param service
	 *        The GradesService.
	 */
	public void setGradesService(GradesService service)
	{
		gradesService = service;
	}

	/**
	 * Set the PoolService.
	 * 
	 * @param service
	 *        the PoolService.
	 */
	public void setPoolService(PoolService service)
	{
		this.poolService = service;
	}

	/**
	 * Dependency: QuestionService.
	 * 
	 * @param service
	 *        The QuestionService.
	 */
	public void setQuestionService(QuestionService service)
	{
		this.questionService = service;
	}

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		securityService = service;
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		sessionManager = service;
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
	 *        The SqlService.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		threadLocalManager = service;
	}

	/**
	 * Add a formatted date to a source string, using a message selector.
	 * 
	 * @param selector
	 *        The message selector.
	 * @param source
	 *        The original string.
	 * @param date
	 *        The date to format.
	 * @return The source and date passed through the selector message.
	 */
	protected String addDate(String selector, String source, Date date)
	{
		// format the date
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		String fmt = format.format(date);

		// the args
		Object[] args = new Object[2];
		args[0] = source;
		args[1] = fmt;

		// format the works
		String rv = this.messages.getFormattedMessage(selector, args);

		return rv;
	}
}
