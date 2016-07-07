/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-test/test-tool/src/java/org/etudes/mneme/tool/TransferFromGradebookView.java $
 * $Id: TransferFromGradebookView.java 9685 2014-12-25 22:54:55Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2014 Etudes, Inc.
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

package org.etudes.mneme.tool;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * The /question_cleanup view for the mneme test tool.
 */
public class TransferFromGradebookView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(TransferFromGradebookView.class);

	/** Dependency: AssessmentService. */
	protected AssessmentService assessmentService = null;

	/** Dependency: GradebookService. */
	protected GradebookService gradebookService = null;

	/** The question service. */
	protected QuestionService questionService = null;

	/** The security service. */
	protected SecurityService securityService = null;

	/** Dependency: SqlService. */
	protected SqlService sqlService = null;

	/** The ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * Shutdown.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params)
	{
		// if not logged in as the super user, we won't do anything
		if (!securityService.isSuperUser())
		{
			throw new IllegalArgumentException();
		}

		// one parameter expected - term
		if (params.length != 3)
		{
			throw new IllegalArgumentException();
		}

		String termId = params[2];

		// get the sites for the term
		List<String> siteIds = getAllTermSiteIds(termId);

		// transfer gradebook assignments into Mneme in the sites in this term
		StringBuilder buf = new StringBuilder();
		for (String siteId : siteIds)
		{
			buf.append("Site: " + siteId + "<br />");
			String rv = transferGradebookAssignments(siteId);
			buf.append(rv);
			buf.append("<br />");
		}

		context.put("rv", buf.toString());

		// render
		uiService.render(ui, context);
	}

	@SuppressWarnings("unchecked")
	public List<String> getAllTermSiteIds(String termId)
	{
		String sql = "SELECT ST.SITE_ID FROM ARCHIVES_SITE_TERM ST INNER JOIN ARCHIVES_TERM T ON ST.TERM_ID = T.ID WHERE T.TERM = ? ORDER BY ST.ID ASC";
		Object[] fields = new Object[1];
		fields[0] = termId;
		List<String> rv = this.sqlService.dbRead(sql.toString(), fields, null);
		return rv;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		throw new IllegalArgumentException();
	}

	/**
	 * Set the AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Set the gradebook service.
	 * 
	 * @param service
	 *        The gradebook service.
	 */
	public void setGradebookService(GradebookService gradebookService)
	{
		this.gradebookService = gradebookService;
	}

	/**
	 * Set the QuestionService.
	 * 
	 * @param service
	 *        The QuestionService.
	 */
	public void setQuestionService(QuestionService service)
	{
		this.questionService = service;
	}

	/**
	 * Set the security service.
	 * 
	 * @param service
	 *        The security service.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Set the SQL service.
	 * 
	 * @param service
	 *        The SQL service.
	 */
	public void setSqlService(SqlService service)
	{
		this.sqlService = service;
	}

	@SuppressWarnings("unchecked")
	protected String transferGradebookAssignments(String siteId)
	{
		StringBuilder results = new StringBuilder();

		try
		{
			List<Assignment> gbs = this.gradebookService.getAssignments(siteId);
			for (Assignment gba : gbs)
			{
				if (!gba.isExternallyMaintained())
				{
					Assessment assessment = null;
					try
					{
						assessment = this.assessmentService.newAssessment(siteId);
						assessment.setType(AssessmentType.offline);
						assessment.setTitle(gba.getName());
						if (gba.getPoints() != null) assessment.setPoints(Float.valueOf(gba.getPoints().floatValue()));
						if (gba.getDueDate() != null) assessment.getDates().setDueDate(gba.getDueDate());
						this.assessmentService.saveAssessment(assessment);

						// remove only if saved successfully
						this.gradebookService.removeAssessment(siteId, gba.getName());

						// now that it has been removed from the GB, we can publish (else we are invalid because the name already is the GB)
						assessment.setPublished(Boolean.TRUE);
						this.assessmentService.saveAssessment(assessment);

						results.append("transfered: " + gba.getName() + "<br />");
					}
					catch (AssessmentPermissionException e)
					{
						results.append("transfer FAILED: " + gba.getName() + "<br />");
						if (assessment != null)
						{
							try
							{
								this.assessmentService.removeAssessment(assessment);
							}
							catch (AssessmentPolicyException ee)
							{
							}
							catch (AssessmentPermissionException ee)
							{
							}
						}
					}
					catch (AssessmentPolicyException e)
					{
						results.append("transfer FAILED: " + gba.getName() + "<br />");
						if (assessment != null)
						{
							try
							{
								this.assessmentService.removeAssessment(assessment);
							}
							catch (AssessmentPolicyException ee)
							{
							}
							catch (AssessmentPermissionException ee)
							{
							}
						}
					}
					catch (Throwable t)
					{
						results.append("transfer FAILED: " + gba.getName() + ": " + t.toString() + "<br />");
						if (assessment != null)
						{
							try
							{
								this.assessmentService.removeAssessment(assessment);
							}
							catch (AssessmentPolicyException ee)
							{
							}
							catch (AssessmentPermissionException ee)
							{
							}
						}
					}
				}
			}
		}
		catch (GradebookNotFoundException e)
		{
			results.append("Gradebook not found for site: " + siteId);
		}

		return results.toString();
	}
}
