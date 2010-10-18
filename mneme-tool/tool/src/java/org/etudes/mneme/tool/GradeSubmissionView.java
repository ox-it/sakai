/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Paging;
import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.Ordering;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /grade_submission view for the mneme tool.
 */
public class GradeSubmissionView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(GradeSubmissionView.class);

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** AttachmentService service. */
	protected AttachmentService attachmentService = null;

	/** The default page size. */
	protected Integer defaultPageSize = null;

	/** Configuration: the page sizes for the view. */
	protected List<Integer> pageSizes = new ArrayList<Integer>();

	/** Dependency: ServerConfigurationService. */
	protected ServerConfigurationService serverConfigurationService = null;

	/** Submission Service */
	protected SubmissionService submissionService = null;

	/** Dependency: ToolManager */
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
		// [2]sid, [3] paging, [4] anchor, [5]next/prev sort (optional- leave out to disable next/prev), optionally followed by a return destination
		if (params.length < 5) throw new IllegalArgumentException();

		Submission submission = this.submissionService.getSubmission(params[2]);
		if (submission == null)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check for user permission to access the submission for grading
		if (!this.submissionService.allowEvaluate(submission))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// check that the assessment is not a formal course evaluation
		if (submission.getAssessment().getFormalCourseEval())
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		context.put("submission", submission);

		// next and prev, based on the sort
		String sortCode = "userName_a";
		SubmissionService.FindAssessmentSubmissionsSort sort = null;
		int destinationStartsAt = 6;
		if (params.length > 4) sortCode = params[5];
		try
		{
			sort = SubmissionService.FindAssessmentSubmissionsSort.valueOf(sortCode);
		}
		catch (IllegalArgumentException e)
		{
			// no sort, so it must be part of destination
			destinationStartsAt = 5;
			sortCode = "userName_a";
		}
		if (sort != null)
		{
			// check the "official / all" parameter from the return for "official", except for survey, where we consider them all
			Boolean official = Boolean.FALSE;
			if ((params.length > 11) && ("official".equals(params[11])) && (submission.getAssessment().getType() != AssessmentType.survey))
			{
				official = Boolean.TRUE;
			}

			Ordering<String> nextPrev = submissionService.findPrevNextSubmissionIds(submission, sort, official);

			if (nextPrev.getPrevious() != null) context.put("prev", nextPrev.getPrevious());
			if (nextPrev.getNext() != null) context.put("next", nextPrev.getNext());
			context.put("position", nextPrev.getPosition());
			context.put("size", nextPrev.getSize());
		}
		context.put("sort", sortCode);

		String destination = null;
		if (params.length > destinationStartsAt)
		{
			destination = "/" + StringUtil.unsplit(params, destinationStartsAt, params.length - destinationStartsAt, "/");
		}

		// if not specified, go to the main grade_assessment page for this assessment
		else
		{
			destination = "/grade_assessment/0A/" + submission.getAssessment().getId();
		}
		context.put("return", destination);

		// paging parameter
		String pagingParameter = null;
		if (params.length > 3) pagingParameter = params[3];
		if ((pagingParameter == null) || (pagingParameter.length() == 0) || (pagingParameter.equals("-")))
		{
			pagingParameter = "1-" + Integer.toString(this.defaultPageSize);
		}

		// paging
		Paging paging = uiService.newPaging();
		paging.setMaxItems(submission.getAnswers().size());
		paging.setCurrentAndSize(pagingParameter);
		context.put("paging", paging);

		// pages sizes
		if (this.pageSizes.size() > 1)
		{
			context.put("pageSizes", this.pageSizes);
		}

		// pick the page of answers
		List<Answer> answers = submission.getAnswersOrdered();
		if (paging.getSize() != 0)
		{
			// start at ((pageNum-1)*pageSize)
			int start = ((paging.getCurrent().intValue() - 1) * paging.getSize().intValue());
			if (start < 0) start = 0;
			if (start > answers.size()) start = answers.size() - 1;

			// end at ((pageNum)*pageSize)-1, or max-1, (note: subList is not inclusive for the end position)
			int end = paging.getCurrent().intValue() * paging.getSize().intValue();
			if (end < 0) end = 0;
			if (end > answers.size()) end = answers.size();

			answers = answers.subList(start, end);
		}
		context.put("answers", answers);

		String anchor = params[4];
		if (!anchor.equals("-")) context.put("anchor", anchor);

		// needed by some of the delegates to show the score
		context.put("grading", Boolean.TRUE);

		uiService.render(ui, context);
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		String pageSize = StringUtil.trimToNull(this.serverConfigurationService.getString("pageSize@org.etudes.mneme.tool.GradeSubmissionView"));
		if (pageSize != null) setPageSize(pageSize);

		if (this.pageSizes.isEmpty())
		{
			this.pageSizes.add(Integer.valueOf(1));
			this.pageSizes.add(Integer.valueOf(25));
			this.pageSizes.add(Integer.valueOf(50));
			this.pageSizes.add(Integer.valueOf(100));
			this.pageSizes.add(Integer.valueOf(0));
			this.defaultPageSize = Integer.valueOf(50);
		}

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public void post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params) throws IOException
	{
		// [2]sid, [3] paging, [4] anchor, [5]next/prev sort (optional- leave out to disable next/prev), optionally followed by a return destination
		if (params.length < 5) throw new IllegalArgumentException();

		final Submission submission = this.submissionService.getSubmission(params[2]);
		if (submission == null)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// check for user permission to access the submission for grading
		if (!this.submissionService.allowEvaluate(submission))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		context.put("submission", submission);

		// so we can deal with the answers by id
		PopulatingSet answers = uiService.newPopulatingSet(new Factory()
		{
			public Object get(String id)
			{
				Answer answer = submission.getAnswer(id);
				return answer;
			}
		}, new Id()
		{
			public String getId(Object o)
			{
				return ((Answer) o).getId();
			}
		});
		context.put("answers", answers);

		// read form
		String destination = this.uiService.decode(req, context);

		// post-process the answers
		for (Object o : answers.getSet())
		{
			Answer a = (Answer) o;
			a.getTypeSpecificAnswer().consolidate(destination);
		}

		// check for remove
		if (destination.startsWith("STAY_REMOVE:"))
		{
			String[] parts = StringUtil.splitFirst(destination, ":");
			if (parts.length == 2)
			{
				parts = StringUtil.splitFirst(parts[1], ":");
				if (parts.length == 2)
				{
					Reference ref = this.attachmentService.getReference(parts[1]);
					this.attachmentService.removeAttachment(ref);

					// if this is for the overall evaluation
					if (parts[0].equals("SUBMISSION"))
					{
						submission.getEvaluation().removeAttachment(ref);
					}
					else
					{
						// find the answer, id=parts[0], ref=parts[1]
						Answer answer = submission.getAnswer(parts[0]);
						if (answer != null)
						{
							answer.getEvaluation().removeAttachment(ref);
						}
					}
				}
			}
		}

		if (destination.startsWith("STAY_"))
		{
			String newAnchor = "-";

			String[] parts = StringUtil.splitFirst(destination, ":");
			if (parts.length == 2)
			{
				String[] anchor = StringUtil.splitFirst(parts[1], ":");
				if (anchor.length > 0)
				{
					newAnchor = anchor[0];
				}
			}

			// rebuild the current destination with the new anchor
			params[4] = newAnchor;
			destination = StringUtil.unsplit(params, "/");
		}

		else if (destination.equals("SAVE"))
		{
			destination = context.getDestination();
		}

		// save graded submission
		try
		{
			this.submissionService.evaluateSubmission(submission);
		}
		catch (AssessmentPermissionException e)
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// if there was an upload error, send to the upload error
		if ((req.getAttribute("upload.status") != null) && (!req.getAttribute("upload.status").equals("ok")))
		{
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.upload + "/" + req.getAttribute("upload.limit"))));
			return;
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
	}

	/**
	 * @param assessmentService
	 *        the assessmentService to set
	 */
	public void setAssessmentService(AssessmentService assessmentService)
	{
		this.assessmentService = assessmentService;
	}

	/**
	 * Set the AttachmentService.
	 * 
	 * @param service
	 *        the AttachmentService.
	 */
	public void setAttachmentService(AttachmentService service)
	{
		this.attachmentService = service;
	}

	/**
	 * Set the the page size for the view.
	 * 
	 * @param sizes
	 *        The the page sizes for the view - integers, comma separated.
	 */
	public void setPageSize(String sizes)
	{
		this.pageSizes.clear();
		String[] parts = StringUtil.split(sizes, ",");
		for (String part : parts)
		{
			this.pageSizes.add(Integer.valueOf(part));
		}

		if (!this.pageSizes.isEmpty())
		{
			// use the first as the default
			this.defaultPageSize = this.pageSizes.get(0);

			// sort, putting 0 (all) at the end
			Collections.sort(this.pageSizes);
			if (this.pageSizes.get(0).equals(Integer.valueOf(0)))
			{
				this.pageSizes.remove(0);
				this.pageSizes.add(Integer.valueOf(0));
			}
		}
	}

	/**
	 * Set the ServerConfigurationService.
	 * 
	 * @param service
	 *        the ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		this.serverConfigurationService = service;
	}

	/**
	 * @param submissionService
	 *        the submissionService to set
	 */
	public void setSubmissionService(SubmissionService submissionService)
	{
		this.submissionService = submissionService;
	}

	/**
	 * @param toolManager
	 *        the toolManager to set
	 */
	public void setToolManager(ToolManager toolManager)
	{
		this.toolManager = toolManager;
	}
}
