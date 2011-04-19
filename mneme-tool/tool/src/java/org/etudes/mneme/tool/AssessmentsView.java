/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.Values;
import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /tests view for the mneme tool.
 */
public class AssessmentsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(AssessmentsView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Dependency: Question service. */
	protected QuestionService questionService = null;

	/** tool manager reference. */
	protected ToolManager toolManager = null;

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
	public void get(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// sort (optional)
		if ((params.length != 2) && (params.length != 3))
		{
			throw new IllegalArgumentException();
		}

		// security
		if (!this.assessmentService.allowManageAssessments(toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// default is due date, ascending
		String sortCode = (params.length > 2) ? params[2] : "0A";
		if (sortCode.length() != 2)
		{
			throw new IllegalArgumentException();
		}

		AssessmentService.AssessmentsSort sort = figureSort(sortCode);
		context.put("sort_column", sortCode.charAt(0));
		context.put("sort_direction", sortCode.charAt(1));

		// collect the assessments in this context
		List<Assessment> assessments = this.assessmentService.getContextAssessments(this.toolManager.getCurrentPlacement().getContext(), sort,
				Boolean.FALSE);
		context.put("assessments", assessments);

		// disable the tool navigation to this view
		context.put("disableAssessments", Boolean.TRUE);

		// pre-read question counts per pool
		this.questionService.preCountContextQuestions(toolManager.getCurrentPlacement().getContext(), Boolean.TRUE);

		// render
		uiService.render(ui, context);
	}

	/**
	 * Figure the sort from the sort code.
	 * 
	 * @param sortCode
	 *        The sort code.
	 * @return The sort.
	 */
	protected static AssessmentService.AssessmentsSort figureSort(String sortCode)
	{
		// due (0), open (1), title (2), publish (3), view/type (4)
		AssessmentService.AssessmentsSort sort = null;
		if (sortCode.charAt(0) == '0')
		{
			if (sortCode.charAt(1) == 'A')
			{
				sort = AssessmentService.AssessmentsSort.ddate_a;
			}
			else
			{
				sort = AssessmentService.AssessmentsSort.ddate_d;
			}
		}
		else if (sortCode.charAt(0) == '1')
		{
			if (sortCode.charAt(1) == 'A')
			{
				sort = AssessmentService.AssessmentsSort.odate_a;
			}
			else
			{
				sort = AssessmentService.AssessmentsSort.odate_d;
			}
		}
		else if (sortCode.charAt(0) == '2')
		{
			if (sortCode.charAt(1) == 'A')
			{
				sort = AssessmentService.AssessmentsSort.title_a;
			}
			else
			{
				sort = AssessmentService.AssessmentsSort.title_d;
			}

		}
		else if (sortCode.charAt(0) == '3')
		{
			if (sortCode.charAt(1) == 'A')
			{
				sort = AssessmentService.AssessmentsSort.published_a;
			}
			else
			{
				sort = AssessmentService.AssessmentsSort.published_d;
			}
		}
		else if (sortCode.charAt(0) == '4')
		{
			if (sortCode.charAt(1) == 'A')
			{
				sort = AssessmentService.AssessmentsSort.type_a;
			}
			else
			{
				sort = AssessmentService.AssessmentsSort.type_d;
			}
		}
		else
		{
			throw new IllegalArgumentException();
		}

		return sort;
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
		// sort (optional)
		if ((params.length != 2) && (params.length != 3))
		{
			throw new IllegalArgumentException();
		}

		// security check
		if (!assessmentService.allowManageAssessments(this.toolManager.getCurrentPlacement().getContext()))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// default is due date, ascending
		String sort = (params.length > 2) ? params[2] : "0A";

		// for the selected select
		Values values = this.uiService.newValues();
		context.put("ids", values);

		// for the dates
		final AssessmentService assessmentService = this.assessmentService;
		PopulatingSet assessments = uiService.newPopulatingSet(new Factory()
		{
			public Object get(String id)
			{
				// add a draw to the part
				Assessment assessment = assessmentService.getAssessment(id);
				return assessment;
			}
		}, new Id()
		{
			public String getId(Object o)
			{
				return ((Assessment) o).getId();
			}
		});
		context.put("assessments", assessments);

		// read the form
		String destination = uiService.decode(req, context);

		// save the dates
		for (Iterator i = assessments.getSet().iterator(); i.hasNext();)
		{
			Assessment assessment = (Assessment) i.next();
			try
			{
				this.assessmentService.saveAssessment(assessment);
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}
			catch (AssessmentPolicyException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
				return;
			}
		}

		// for an add
		if (destination.equals("ADD"))
		{
			try
			{
				Assessment assessment = this.assessmentService.newAssessment(this.toolManager.getCurrentPlacement().getContext());
				destination = "/assessment_edit/" + assessment.getId() + "/assessments/" + sort;
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}
		}

		else if (destination.equals("ARCHIVE"))
		{
			for (String id : values.getValues())
			{
				Assessment assessment = this.assessmentService.getAssessment(id);
				if (assessment != null)
				{
					assessment.setArchived(Boolean.TRUE);
					try
					{
						this.assessmentService.saveAssessment(assessment);
						destination = context.getDestination();
					}
					catch (AssessmentPermissionException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
						return;
					}
					catch (AssessmentPolicyException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
						return;
					}
				}
			}
		}

		else if (destination.equals("PUBLISH"))
		{
			for (String id : values.getValues())
			{
				Assessment assessment = this.assessmentService.getAssessment(id);
				if (assessment != null)
				{
					try
					{
						// for invalid assessments, the setPublished will be ignored
						assessment.setPublished(Boolean.TRUE);
						this.assessmentService.saveAssessment(assessment);
					}
					catch (AssessmentPermissionException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
						return;
					}
					catch (AssessmentPolicyException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
						return;
					}
				}
			}

			destination = context.getDestination();
		}

		else if (destination.equals("UNPUBLISH"))
		{
			for (String id : values.getValues())
			{
				Assessment assessment = this.assessmentService.getAssessment(id);
				if (assessment != null)
				{
					try
					{
						assessment.setPublished(Boolean.FALSE);
						this.assessmentService.saveAssessment(assessment);
					}
					catch (AssessmentPermissionException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
						return;
					}
					catch (AssessmentPolicyException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
						return;
					}
				}
			}

			destination = context.getDestination();
		}

		else if (destination.equals("DELETE"))
		{
			for (String id : values.getValues())
			{
				Assessment assessment = this.assessmentService.getAssessment(id);
				if (assessment != null)
				{
					try
					{
						if (this.assessmentService.allowRemoveAssessment(assessment))
						{
							this.assessmentService.removeAssessment(assessment);
						}
					}
					catch (AssessmentPermissionException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
						return;
					}
					catch (AssessmentPolicyException e)
					{
						// redirect to error
						res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.policy)));
						return;
					}
				}
			}

			destination = context.getDestination();
		}

		else if (destination.startsWith("DUPLICATE:"))
		{
			String[] parts = StringUtil.split(destination, ":");
			if (parts.length != 2)
			{
				throw new IllegalArgumentException();
			}
			String aid = parts[1];
			try
			{
				Assessment assessment = this.assessmentService.getAssessment(aid);
				if (assessment == null)
				{
					throw new IllegalArgumentException();
				}
				this.assessmentService.copyAssessment(toolManager.getCurrentPlacement().getContext(), assessment);
				destination = context.getDestination();
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
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
	 * Set the tool manager.
	 * 
	 * @param manager
	 *        The tool manager.
	 */
	public void setToolManager(ToolManager manager)
	{
		toolManager = manager;
	}
}
