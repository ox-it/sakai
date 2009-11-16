/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Values;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Web;
import org.springframework.core.io.ClassPathResource;

/**
 * The /part_edit view for the mneme tool.
 */
public class PartEditView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(PartEditView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	protected PoolService poolService = null;

	/** Dependency: Question service. */
	protected QuestionService questionService = null;

	/** tool manager */
	protected ToolManager toolManager = null;

	/** The UI (2). Used for manual parts (the main this.ui used for draw parts). */
	protected Component ui2 = null;

	/** The view declaration xml path. */
	protected String viewPath2 = null;

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
		// [2]sort for /assessments, [3]aid |[4] pid |optional->| [5]our sort
		if (params.length < 5 || params.length > 6)
		{
			throw new IllegalArgumentException();
		}

		// sort for the assessments view
		context.put("assessmentSort", params[2]);

		// sort parameter (default for dpart is pool ascending)
		String sortCode = "0A";
		if (params.length > 5) sortCode = params[5];

		String assessmentId = params[3];
		Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		context.put("assessment", assessment);

		String partId = params[4];
		Part part = assessment.getParts().getPart(partId);
		if (part == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		context.put("part", part);

		// security check
		if (!assessmentService.allowEditAssessment(assessment))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// TODO:
//		// based on the part type...
//		if (part instanceof DrawPart)
//		{
//			getDraw(assessment, (DrawPart) part, sortCode, context);
//		}
//		else
//		{
//			getManual(assessment, (ManualPart) part, req, res, context, params);
//		}
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		// interface from XML in the class path
		if (this.viewPath != null)
		{
			try
			{
				ClassPathResource rsrc = new ClassPathResource(this.viewPath2);
				this.ui2 = uiService.newInterface(rsrc.getInputStream());
			}
			catch (IOException e)
			{
			}
		}

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// [2]sort for /assessment, [3]aid |[4] pid |optional->| [5]our sort
		if (params.length < 5 || params.length > 6) throw new IllegalArgumentException();

		String assessmentId = params[3];
		String partId = params[4];

		Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		Part part = assessment.getParts().getPart(partId);
		if (part == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// security check
		if (!assessmentService.allowEditAssessment(assessment))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// setup the model: the selected assessment
		context.put("assessment", assessment);
		context.put("part", part);

		Values values = null;

		// based on the part type...
//		PopulatingSet draws = null;
//		if (part instanceof DrawPart)
//		{
//			final DrawPart dpart = (DrawPart) part;
//			final PoolService pservice = this.poolService;
//			draws = uiService.newPopulatingSet(new Factory()
//			{
//				public Object get(String id)
//				{
//					// add a virtual draw to the part, matching one from the DrawPart if there is one, else a new 0 count one.
//					PoolDraw draw = dpart.getVirtualDraw(pservice.getPool(id));
//					return draw;
//				}
//			}, new Id()
//			{
//				public String getId(Object o)
//				{
//					return ((PoolDraw) o).getPool().getId();
//				}
//			});
//
//			context.put("draws", draws);
//		}
//
//		// for mpart, we need to collect the checkbox ids
//		else
//		{
//			values = uiService.newValues();
//			context.put("questionIds", values);
//		}

		// read the form
		String destination = uiService.decode(req, context);

		// filter out draw part draws that are no questions
//		if (part instanceof DrawPart)
//		{
//			// apply the draws to the part
//			DrawPart dpart = (DrawPart) part;
//			dpart.updateDraws(new ArrayList<PoolDraw>(draws.getSet()));
//		}
//
//		// process the ids into the destination for a redirect to the remove confirm view...
//		else
//		{
//			if (destination.equals("DELETEQ"))
//			{
//				// get the ids
//				String[] removeQuesIds = values.getValues();
//				if (removeQuesIds != null && removeQuesIds.length != 0)
//				{
//					// remove questions from part
//					for (String removeQuesId : removeQuesIds)
//					{
//						if (removeQuesId != null)
//						{
//							// remove question from part
//							Question question = part.getQuestion(removeQuesId);
//							((ManualPart) part).removeQuestion(question);
//						}
//					}
//				}
//
//				destination = context.getDestination();
//			}
//		}

		// commit the save
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

		// redirect to the next destination
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
	 * Set the class path to the components (2) XML declaration for the view.
	 * 
	 * @param path
	 *        The class path to the components (2) XML declaration for the view.
	 */
	public void setComponents2(String path)
	{
		this.viewPath2 = path;
	}

	/**
	 * Set the PoolService.
	 * 
	 * @param service
	 *        The PoolService.
	 */
	public void setPoolService(PoolService service)
	{
		this.poolService = service;
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
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}

	/**
	 * Figure out the sort.
	 * 
	 * @param sortCode
	 *        The sort parameter.
	 * @return The sort.
	 */
	protected PoolService.FindPoolsSort findSortCode(String sortCode)
	{
		PoolService.FindPoolsSort sort = PoolService.FindPoolsSort.title_a;
		// 0 is title
		if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'A'))
		{
			sort = PoolService.FindPoolsSort.title_a;
		}
		else if ((sortCode.charAt(0) == '0') && (sortCode.charAt(1) == 'D'))
		{
			sort = PoolService.FindPoolsSort.title_d;
		}
		// 1 is points
		else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'A'))
		{
			sort = PoolService.FindPoolsSort.points_a;
		}
		else if ((sortCode.charAt(0) == '1') && (sortCode.charAt(1) == 'D'))
		{
			sort = PoolService.FindPoolsSort.points_d;
		}

		return sort;
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	protected void getDraw(Assessment assessment, DrawPart part, String sortCode, Context context) throws IOException
//	{
//		// sort
//		if ((sortCode == null) || (sortCode.length() != 2))
//		{
//			throw new IllegalArgumentException();
//		}
//		context.put("sort_column", sortCode.charAt(0));
//		context.put("sort_direction", sortCode.charAt(1));
//		PoolService.FindPoolsSort sort = findSortCode(sortCode);
//
//		// pre-read question counts per pool
//		this.questionService.preCountContextQuestions(toolManager.getCurrentPlacement().getContext(), Boolean.TRUE);
//
//		// get the pool draw list
//		// - all the pools for the user (select, sort, page) crossed with this part's actual draws
//		// - these are virtual draws, not part of the DrawPart
//		List<PoolDraw> draws = getDraws(assessment, part, sort);
//		context.put("draws", draws);
//
//		// render
//		uiService.render(ui, context);
//	}

//	/**
//	 * Get the draw of pools. If the assessment is live, just get the used pools, else get that joined with all possible pools.
//	 * 
//	 * @param assessment
//	 *        The assessment.
//	 * @param part
//	 *        The draw part.
//	 * @param sort
//	 *        The sort.
//	 * @return The draw of pools.
//	 */
//	protected List<PoolDraw> getDraws(Assessment assessment, DrawPart part, PoolService.FindPoolsSort sort)
//	{
//		if (assessment.getIsLocked())
//		{
//			List<PoolDraw> draws = part.getDraws(sort);
//			return draws;
//		}
//
//		// - all the pools for the user (select, sort, page) crossed with this part's actual draws
//		// - these are virtual draws, not part of the DrawPart
//		List<PoolDraw> draws = part.getDrawsForPools(toolManager.getCurrentPlacement().getContext(), sort, null);
//		return draws;
//	}

//	/**
//	 * {@inheritDoc}
//	 */
//	protected void getManual(Assessment assessment, ManualPart part, HttpServletRequest req, HttpServletResponse res, Context context, String[] params)
//			throws IOException
//	{
//		// checkboxes to remove questions
//		Values values = uiService.newValues();
//		context.put("questionIds", values);
//
//		// render
//		uiService.render(ui2, context);
//	}
}
