/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 Etudes, Inc.
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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentClosedException;
import org.etudes.mneme.api.AssessmentCompletedException;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionGrouping;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionCompletedException;
import org.etudes.mneme.api.SubmissionService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The /question view for the mneme tool.
 */
public class QuestionView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(QuestionView.class);

	/** Dependency: AssessmentService. */
	protected AssessmentService assessmentService = null;

	/** Dependency: SubmissionService. */
	protected SubmissionService submissionService = null;

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
		// sid/question selector/anchor and return
		// question selector may end in "!" for "last chance" - linear repeat question when it was not answered
		if (params.length < 5)
		{
			throw new IllegalArgumentException();
		}

		String submissionId = params[2];
		String questionSelector = params[3];
		boolean lastChance = false;
		if (questionSelector.endsWith("!"))
		{
			questionSelector = questionSelector.substring(0, questionSelector.length() - 1);
			lastChance = true;
		}
		String anchor = params[4];
		if (anchor.equals("-")) anchor = null;

		String destination = null;
		if (params.length > 5)
		{
			destination = "/" + StringUtil.unsplit(params, 5, params.length - 5, "/");
		}
		// if not specified, go to the main list view
		else
		{
			destination = "/list";
		}
		context.put("return", destination);

		// adjust the current destination to remove the anchor
		params[4] = "-";
		String curDestination = "/" + StringUtil.unsplit(params, 1, params.length - 1, "/");
		context.put("curDestination", curDestination);

		Submission submission = submissionService.getSubmission(submissionId);

		if (submission == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// if not in-progress (i.e. already closed)
		if (submission.getIsComplete())
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.over)));
			return;
		}

		// handle our 'z' selector - redirect to the appropriate question to re-enter this submission
		if ("z".equals(questionSelector))
		{
			try
			{
				// Note: enterSubmission() can create a new submission, if the submission is not in progress.
				// Our submission has no sibling count (=0), since we used getSubmission(), which does not set it.
				// enterSubmission() will think there are no other submissions, so would create one even if the max allowed
				// for the user has been exceeded. Since we check getIsComplete() above, this should be avoided -ggolden
				this.submissionService.enterSubmission(submission);
			}
			catch (AssessmentPermissionException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}
			catch (AssessmentClosedException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
				return;
			}
			catch (AssessmentCompletedException e)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
				return;
			}

			redirectToQuestion(req, res, submission, true, false, destination);
			return;
		}

		// if the submission has past a hard deadline or ran out of time, close it and tell the user
		if (submission.completeIfOver())
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.over)));
			return;
		}

		context.put("actionTitle", messages.getString("question-header-work"));

		// collect the questions (actually their answers) to put on the page
		List<Answer> answers = new ArrayList<Answer>();

		Errors err = questionSetup(submission, questionSelector, context, answers, true);
		if (err != null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + err + "/" + submissionId)));
			return;
		}

		// for the tool navigation
		if (this.assessmentService.allowManageAssessments(toolManager.getCurrentPlacement().getContext()))
		{
			context.put("maintainer", Boolean.TRUE);
		}

		if (anchor != null) context.put("anchor", anchor);
		if (lastChance) context.put("lastChance", Boolean.TRUE);

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
		// we need two parameters (sid/question selector) and optional anchor
		// question selector may end in "!" for "last chance" - linear repeat question when it was not answered
		if (params.length < 5)
		{
			throw new IllegalArgumentException();
		}

		String submissionId = params[2];
		String questionSelector = params[3];
		boolean lastChance = false;
		if (questionSelector.endsWith("!"))
		{
			questionSelector = questionSelector.substring(0, questionSelector.length() - 1);
			lastChance = true;
		}

		String returnDestination = null;
		if (params.length > 5)
		{
			returnDestination = "/" + StringUtil.unsplit(params, 5, params.length - 5, "/");
		}
		// if not specified, go to the main list view
		else
		{
			returnDestination = "/list";
		}

		// if (!context.getPostExpected())
		// {
		// // redirect to error
		// res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unexpected)));
		// return;
		// }

		// collect the questions (actually their answers) to put on the page
		List<Answer> answers = new ArrayList<Answer>();

		// get the submission
		Submission submission = submissionService.getSubmission(submissionId);
		if (submission == null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
			return;
		}

		// setup receiving context
		Errors err = questionSetup(submission, questionSelector, context, answers, false);
		if (Errors.invalid == err) err = Errors.invalidpost;
		if (err != null)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + err)));
			return;
		}

		// read form
		String destination = uiService.decode(req, context);

		// check for file upload error
		boolean uploadError = ((req.getAttribute("upload.status") != null) && (!req.getAttribute("upload.status").equals("ok")));

		// if we are going to submitted, we must complete the submission (unless there was an upload error)
		Boolean complete = Boolean.valueOf((!uploadError) && destination.startsWith("/submitted"));

		// unless we are going to list, instructions, or this very same question, or we have a file upload error, mark the
		// answers as complete
		Boolean answersComplete = Boolean.valueOf(!(uploadError || destination.startsWith("LIST") || destination.startsWith("STAY")
				|| destination.startsWith("/instructions") || destination.equals(context.getPreviousDestination())));

		// and if we are working in a random access test, answers are always complete
		if (submission.getAssessment().getRandomAccess()) answersComplete = Boolean.TRUE;

		// post-process the answers
		for (Answer answer : answers)
		{
			answer.getTypeSpecificAnswer().consolidate(destination);
		}

		// linear order check - unless this was a last chance display
		boolean repeat = false;
		if (!lastChance)
		{
			if (!submission.getAssessment().getRandomAccess().booleanValue())
			{
				// linear order is always presented by-question
				if (answers.size() == 1)
				{
					Answer answer = answers.get(0);
					boolean answered = answer.getIsAnswered().booleanValue();
					if (!answered)
					{
						repeat = true;

						// don't mark the answer as complete, so we can re-enter
						answersComplete = false;
					}
				}
			}
		}

		// where are we going?
		destination = questionChooseDestination(context, destination, questionSelector, submissionId, params, repeat, returnDestination);

		// submit all answers
		try
		{
			submissionService.submitAnswers(answers, answersComplete, complete);

			// if there was an upload error, send to the upload error
			if ((req.getAttribute("upload.status") != null) && (!req.getAttribute("upload.status").equals("ok")))
			{
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.upload + "/" + req.getAttribute("upload.limit"))));
				return;
			}

			if (destination.equals("SUBMIT"))
			{
				// get the submission again, to make sure that the answers we just posted are reflected
				submission = submissionService.getSubmission(submissionId);

				// if linear, or the submission is all answered, or this is a single question assessment, we can complete the submission and go to submitted
				if ((!submission.getAssessment().getRandomAccess()) || (submission.getIsAnswered())
						|| (submission.getAssessment().getIsSingleQuestion()))
				{
					submissionService.completeSubmission(submission);

					destination = "/submitted/" + submissionId + returnDestination;
				}

				// if not linear, and there are unanswered parts, send to final review
				else
				{
					destination = "/final_review/" + submissionId + returnDestination;
				}
			}

			// redirect to the next destination
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
			return;
		}
		catch (AssessmentClosedException e)
		{
		}
		catch (SubmissionCompletedException e)
		{
		}
		catch (AssessmentPermissionException e)
		{
		}

		// redirect to error
		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
	}

	/**
	 * Set the assessment service.
	 * 
	 * @param service
	 *        The assessment service.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		this.assessmentService = service;
	}

	/**
	 * Set the submission service.
	 * 
	 * @param service
	 *        The submission service.
	 */
	public void setSubmissionService(SubmissionService service)
	{
		this.submissionService = service;
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

	/**
	 * for PREV and NEXT, choose the destination.
	 * 
	 * @param destination
	 *        The destination encoded in the request.
	 * @param questionSelector
	 *        Which question(s) to put on the page: q followed by a questionId picks one, s followed by a sectionId picks a sections
	 * @param submisssionId
	 *        The selected submission id.
	 * @param params
	 *        The current destination parameters.
	 * @param repeat
	 *        If true, and we would be sending the user to another question, repeat the current question.
	 * @param returnDestination
	 *        The final return destination path.
	 */
	protected String questionChooseDestination(Context context, String destination, String questionSelector, String submissionId, String[] params,
			boolean repeat, String returnDestination)
	{
		// get the submission
		Submission submission = submissionService.getSubmission(submissionId);
		if (submission == null)
		{
			return "/error/" + Errors.invalid;
		}

		// for LIST do the return destination
		if (destination.startsWith("LIST"))
		{
			return returnDestination;
		}

		// if we are staying here
		if (destination.startsWith("STAY_"))
		{
			String[] parts = StringUtil.splitFirst(destination, ":");
			if (parts.length == 2)
			{
				String[] anchor = StringUtil.splitFirst(parts[1], ":");
				if (anchor.length > 0)
				{
					// replace the original anchor
					params[4] = anchor[0];
				}
			}

			String curDestination = "/" + StringUtil.unsplit(params, 1, params.length - 1, "/");
			return curDestination;
		}

		// for requests for a single question
		if (questionSelector.startsWith("q"))
		{
			// make sure by-question is valid for this assessment
			if (submission.getAssessment().getQuestionGrouping() != QuestionGrouping.question)
			{
				return "/error/" + Errors.invalid;
			}

			String questionId = questionSelector.substring(1);
			Question question = submission.getAssessment().getParts().getQuestion(questionId);
			if (question == null)
			{
				return "/error/" + Errors.invalid;
			}

			if ("NEXT".equals(destination))
			{
				// only for NEXT, if repeat, do the question again with a "!" to indicate a second chance
				if (repeat)
				{
					return "/question/" + submissionId + "/q" + question.getId() + "!" + "/-" + returnDestination;
				}

				// if the question is not the last of the part, go to the next question
				if (!question.getPartOrdering().getIsLast())
				{
					return "/question/" + submissionId + "/q" + question.getAssessmentOrdering().getNext().getId() + "/-" + returnDestination;
				}

				// if there's a next part
				if (!question.getPart().getOrdering().getIsLast())
				{
					// if showing part presentation
					Part next = question.getPart().getOrdering().getNext();
					if (submission.getAssessment().getParts().getShowPresentation())
					{
						// choose the part instructions
						return "/part_instructions/" + submissionId + "/" + next.getId() + returnDestination;
					}

					// otherwise choose the first question of the next part
					return "/question/" + submissionId + "/q" + next.getFirstQuestion().getId() + "/-" + returnDestination;
				}

				// no next part, this is an error
				return "/error/" + Errors.invalid;
			}

			else if ("PREV".equals(destination))
			{
				// if the question is not the first of the part, go to the prev question
				if (!question.getPartOrdering().getIsFirst())
				{
					return "/question/" + submissionId + "/q" + question.getAssessmentOrdering().getPrevious().getId() + "/-" + returnDestination;
				}

				// prev into this part's instructions... if showing part presentation
				Part part = question.getPart();
				if (submission.getAssessment().getParts().getShowPresentation())
				{
					// choose the part instructions
					return "/part_instructions/" + submissionId + "/" + part.getId() + returnDestination;
				}

				// otherwise choose the last question of the prev part, if we have one
				Part prev = part.getOrdering().getPrevious();
				if (prev != null)
				{
					return "/question/" + submissionId + "/q" + prev.getLastQuestion().getId() + "/-" + returnDestination;
				}

				// no prev part, this is an error
				return "/error/" + Errors.invalid;
			}
		}

		// for part-per-page
		else if (questionSelector.startsWith("p"))
		{
			// make sure by-part is valid for this assessment
			if (submission.getAssessment().getQuestionGrouping() != QuestionGrouping.part)
			{
				return "/error /" + Errors.invalid;
			}

			String sectionId = questionSelector.substring(1);
			Part part = submission.getAssessment().getParts().getPart(sectionId);
			if (part == null)
			{
				return "/error/" + Errors.invalid;
			}

			if ("NEXT".equals(destination))
			{
				// if there's a next part, go there
				if (!part.getOrdering().getIsLast())
				{
					Part next = part.getOrdering().getNext();
					return "/question/" + submissionId + "/p" + next.getId() + "/-" + returnDestination;
				}

				// no next part, this is an error
				return "/error/" + Errors.invalid;
			}

			else if ("PREV".equals(destination))
			{
				// if there's a prev part, choose to enter that
				if (!part.getOrdering().getIsFirst())
				{
					Part prev = part.getOrdering().getPrevious();
					return "/question/" + submissionId + "/p" + prev.getId() + "/-" + returnDestination;
				}

				// no prev part, this is an error
				return "/error/" + Errors.invalid;
			}
		}

		return destination;
	}

	/**
	 * Setup the context for question get and post
	 * 
	 * @param submisssion
	 *        The selected submission.
	 * @param questionSelector
	 *        Which question(s) to put on the page: q followed by a questionId picks one, s followed by a sectionId picks a sections worth, and a picks them all.
	 * @param context
	 *        UiContext.
	 * @param answers
	 *        A list to fill in with the answers for this page.
	 * @param out
	 *        Output writer.
	 * @return null if all went well, else an Errors to indicate what went wrong.
	 */
	protected Errors questionSetup(Submission submission, String questionSelector, Context context, List<Answer> answers, boolean linearCheck)
	{
		// not in review mode
		context.put("review", Boolean.FALSE);

		// put in the selector
		context.put("questionSelector", questionSelector);

		if (!submissionService.allowCompleteSubmission(submission))
		{
			return Errors.unauthorized;
		}

		context.put("submission", submission);

		// for requests for a single question
		if (questionSelector.startsWith("q"))
		{
			// TODO: assure the test is by-question

			String questionId = questionSelector.substring(1);
			Question question = submission.getAssessment().getParts().getQuestion(questionId);
			if (question == null)
			{
				return Errors.invalid;
			}

			// if we need to do our linear assessment check, and this is a linear assessment,
			// we will reject if the question has been marked as 'complete'
			if (linearCheck && !question.getPart().getAssessment().getRandomAccess() && submission.getIsCompleteQuestion(question))
			{
				return Errors.linear;
			}

			// find the answer (or have one created) for this submission / question
			Answer answer = submission.getAnswer(question);
			if (answer != null)
			{
				answers.add(answer);
			}

			// tell the UI that we are doing single question
			context.put("question", question);
		}

		// for requests for a part
		else if (questionSelector.startsWith("p"))
		{
			// TODO: assure the test is by-part

			String sectionId = questionSelector.substring(1);
			Part part = submission.getAssessment().getParts().getPart(sectionId);
			if (part == null)
			{
				return Errors.invalid;
			}

			// get all the answers for this part
			for (Question question : part.getQuestions())
			{
				Answer answer = submission.getAnswer(question);
				if (answer != null)
				{
					answers.add(answer);
				}
			}

			// tell the UI that we are doing single part
			context.put("part", part);
		}

		// for requests for the entire assessment
		else if (questionSelector.startsWith("a"))
		{
			// TODO: assure the test is by-test

			answers.addAll(submission.getAnswersOrdered());
		}

		context.put("answers", answers);
		return null;
	}

	/**
	 * Redirect to the appropriate question screen for this submission
	 * 
	 * @param req
	 *        Servlet request.
	 * @param res
	 *        Servlet response.
	 * @param submission
	 *        The submission.
	 * @param toc
	 *        if true, send to TOC if possible (not possible for linear).
	 * @param instructions
	 *        if true, send to part instructions for first question.
	 * @param returnDestination
	 *        The final return destination path.
	 */
	protected void redirectToQuestion(HttpServletRequest req, HttpServletResponse res, Submission submission, boolean toc, boolean instructions,
			String returnDestination) throws IOException
	{
		String destination = null;
		Assessment assessment = submission.getAssessment();

		// if we are random access, and not a single question, and allowed, send to TOC
		if (toc && assessment.getRandomAccess() && !assessment.getIsSingleQuestion())
		{
			destination = "/toc/" + submission.getId() + returnDestination;
		}

		else
		{
			// find the first incomplete question
			Question question = submission.getFirstIncompleteQuestion();

			// if not found, and we have only one, go there
			if ((question == null) && (assessment.getIsSingleQuestion()))
			{
				question = submission.getFirstQuestion();
			}

			// if we don't have one, we will go to the toc (or final_review for linear)
			if (question == null)
			{
				if (!assessment.getRandomAccess())
				{
					destination = "/final_review/" + submission.getId() + returnDestination;
				}
				else
				{
					destination = "/toc/" + submission.getId() + returnDestination;
				}
			}

			else
			{
				// send to the part instructions if it's a first question and by-question
				if (instructions && (question.getPartOrdering().getIsFirst()) && (assessment.getParts().getShowPresentation())
						&& (assessment.getQuestionGrouping() == QuestionGrouping.question))
				{
					// to instructions
					destination = "/part_instructions/" + submission.getId() + "/" + question.getPart().getId() + returnDestination;
				}

				// or to the question
				else
				{
					if (assessment.getQuestionGrouping() == QuestionGrouping.question)
					{
						destination = "/question/" + submission.getId() + "/q" + question.getId() + "/-" + returnDestination;
					}
					else if (assessment.getQuestionGrouping() == QuestionGrouping.part)
					{
						destination = "/question/" + submission.getId() + "/p" + question.getPart().getId();

						// include the question target if not the first question in the part
						if (!question.getPartOrdering().getIsFirst())
						{
							destination = destination + "/" + question.getId();
						}
						else
						{
							destination = destination + "/-";
						}

						destination = destination + returnDestination;
					}
					else
					{
						destination = "/question/" + submission.getId() + "/a";

						// include the question target if not the first question in the assessment
						if (!question.getAssessmentOrdering().getIsFirst())
						{
							destination = destination + "/" + question.getId();
						}
						else
						{
							destination = destination + "/-";
						}

						destination = destination + returnDestination;
					}
				}
			}
		}

		res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));
		return;
	}
}
