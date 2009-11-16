/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2009 Etudes, Inc.
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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PoolDraw;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /draw_questions view for the mneme tool.
 */
public class DrawQuestionsView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(DrawQuestionsView.class);

	/** Dependency: AssessmentService. */
	protected AssessmentService assessmentService = null;

	/** Dependency: PoolService. */
	protected PoolService poolService = null;

	/** Dependency: Question service. */
	protected QuestionService questionService = null;

	/** tool manager */
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
		// [2] assessment id, [3] part id, [4] sort, return address in the rest
		if (params.length < 5) throw new IllegalArgumentException();

		String destination = null;
		if (params.length > 5)
		{
			destination = "/" + StringUtil.unsplit(params, 5, params.length - 5, "/");
		}
		// if not specified, go to the main list page
		else
		{
			destination = "/assessments";
		}
		context.put("return", destination);

		String assessmentId = params[2];
		Assessment assessment = assessmentService.getAssessment(assessmentId);
		if (assessment == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}
		context.put("assessment", assessment);

		String partId = params[3];
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

		// sort
		String sortCode = params[4];
		if ((sortCode == null) || (sortCode.length() != 2))
		{
			throw new IllegalArgumentException();
		}
		context.put("sort_column", sortCode.charAt(0));
		context.put("sort_direction", sortCode.charAt(1));
		PoolService.FindPoolsSort sort = findSortCode(sortCode);

		// pre-read question counts per pool
		this.questionService.preCountContextQuestions(toolManager.getCurrentPlacement().getContext(), Boolean.TRUE);

		// get the pool draw list
		// - all the pools for the user (select, sort, page) crossed with the assessment's actual draws
		// - these are virtual draws, not part of the Part details
		List<PoolDraw> draws = getDraws(assessment, sort);
		context.put("draws", draws);

		// for the selected "for" part
		Value value = this.uiService.newValue();
		value.setValue(part.getId());
		context.put("partId", value);

		// render
		uiService.render(ui, context);
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
		// [2] assessment id, [3] part id, [4] sort, return address in the rest
		if (params.length < 5) throw new IllegalArgumentException();

		String assessmentId = params[2];
		String partId = params[3];

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

		final Assessment asmnt = assessment;
		final PoolService pservice = this.poolService;
		PopulatingSet draws = uiService.newPopulatingSet(new Factory()
		{
			public Object get(String id)
			{
				// add a virtual draw to the part, matching one from the DrawPart if there is one, else a new 0 count one.
				PoolDraw draw = asmnt.getParts().getVirtualDraw(pservice.getPool(id));
				return draw;
			}
		}, new Id()
		{
			public String getId(Object o)
			{
				return ((PoolDraw) o).getPool().getId();
			}
		});
		context.put("draws", draws);

		// for the selected "for" part
		Value value = this.uiService.newValue();
		context.put("partId", value);

		// read the form
		String destination = uiService.decode(req, context);

		// get the new part id
		String newPartId = value.getValue();
		if (!part.getId().equals(newPartId))
		{
			// create a new part?
			if ("0".equals(newPartId))
			{
				try
				{
					Part created = assessment.getParts().addPart();
					this.assessmentService.saveAssessment(assessment);
					newPartId = created.getId();
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

			Part newPart = assessment.getParts().getPart(newPartId);
			if (newPart != null)
			{
				part = newPart;

				// adjust the destination to use this part, if the destination is back to me
				String[] destParts = StringUtil.split(destination, "/");
				if (destParts[1].equals("draw_questions"))
				{
					destParts[3] = part.getId();
					destination = StringUtil.unsplit(destParts, 0, destParts.length, "/");
				}
			}
		}

		// update the draws in the assessment parts
		assessment.getParts().updateDraws(new ArrayList<PoolDraw>(draws.getSet()), part);

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

	/**
	 * Get the draw of pools. If the assessment is live, just get the used pools, else get that joined with all possible pools.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param sort
	 *        The sort.
	 * @return The draw of pools.
	 */
	protected List<PoolDraw> getDraws(Assessment assessment, PoolService.FindPoolsSort sort)
	{
		if (assessment.getIsLocked())
		{
			List<PoolDraw> draws = assessment.getParts().getDraws(sort);
			return draws;
		}

		// - all the pools for the user (select, sort, page) crossed with this part's actual draws
		// - these are virtual draws, not part of the DrawPart
		List<PoolDraw> draws = assessment.getParts().getDrawsForPools(toolManager.getCurrentPlacement().getContext(), sort, null);
		return draws;
	}
}
