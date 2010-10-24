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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.ControllerImpl;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.GradesRejectsAssessmentException;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPlugin;
import org.etudes.mneme.api.QuestionService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;

/**
 * The question_edit view for the mneme tool.
 */
public class QuestionEditView extends ControllerImpl
{
	/** Our log. */
	private static Log M_log = LogFactory.getLog(QuestionEditView.class);

	/** Assessment Service */
	protected AssessmentService assessmentService = null;

	/** Dependency: AttachmentService. */
	protected AttachmentService attachmentService = null;

	/** Dependency: EntityManager. */
	protected EntityManager entityManager = null;

	/** Dependency: mneme service. */
	protected MnemeService mnemeService = null;

	/** Question Service */
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
		// we need a qid, assessment and part id, then any number of parameters to form the return destination
		if (params.length < 5)
		{
			throw new IllegalArgumentException();
		}

		String destination = null;
		if (params.length > 5)
		{
			destination = "/" + StringUtil.unsplit(params, 5, params.length - 5, "/");
		}

		// if not specified, go to the main pools page
		else
		{
			destination = "/pools";
		}
		context.put("return", destination);

		String questionId = params[2];
		String assessmentId = params[3];
		String partId = params[4];

		boolean fixMode = params[1].equals("question_fix");
		if (fixMode) context.put("fix", Boolean.TRUE);

		// get the question to work on
		Question question = this.questionService.getQuestion(questionId);
		if (question == null) throw new IllegalArgumentException();

		// check security
		if (!this.questionService.allowEditQuestion(question))
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		// put the question in the context
		context.put("question", question);

		// next/prev for pools (not assessment) editing
		if (destination.startsWith("/pool_edit") || destination.startsWith("/pool_fix"))
		{
			context.put("nextPrev", Boolean.TRUE);

			// figure out the question id
			String returnDestParts[] = StringUtil.split(destination, "/");
			String sortCode = "0A";
			if (returnDestParts.length > 3 && (!"-".equals(returnDestParts[3]))) sortCode = returnDestParts[3];

			QuestionService.FindQuestionsSort sort = PoolEditView.findSort(sortCode);

			// get questions
			List<Question> questions = questionService.findQuestions(question.getPool(), sort, null, null, null, null, null, null);

			// find this one ( 0 based)
			int pos = 0;
			for (Question q : questions)
			{
				if (q.equals(question))
				{
					break;
				}
				pos++;
			}

			context.put("position", Integer.valueOf(pos + 1));
			context.put("size", Integer.valueOf(questions.size()));
		}

		// the question types
		List<QuestionPlugin> questionTypes = this.mnemeService.getQuestionPlugins();
		context.put("questionTypes", questionTypes);

		// select the question's current type
		Value value = this.uiService.newValue();
		value.setValue(question.getType());
		context.put("selectedQuestionType", value);

		// if we are adding directly to an assessment part
		if ((!"0".equals(assessmentId)) && (!"0".equals(partId)))
		{
			Assessment assessment = this.assessmentService.getAssessment(assessmentId);
			if (assessment == null)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
				return;
			}
			context.put("assessment", assessment);

			Part part = assessment.getParts().getPart(partId);
			if (part == null)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
				return;
			}
			context.put("part", part);

			// security check for the assessment edit
			if (!this.assessmentService.allowEditAssessment(assessment))
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}

			// for the selected "for" part
			value = this.uiService.newValue();
			value.setValue(part.getId());
			context.put("partId", value);
		}

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
		// we need a question id, then any number of parameters to form the return destination
		if (params.length < 5)
		{
			throw new IllegalArgumentException();
		}

		String returnDestination = null;
		if (params.length > 5)
		{
			returnDestination = "/" + StringUtil.unsplit(params, 5, params.length - 5, "/");
		}

		// if not specified, go to the main pools page
		else
		{
			returnDestination = "/pools";
		}

		boolean fixMode = params[1].equals("question_fix");
		String questionId = params[2];
		String assessmentId = params[3];
		String partId = params[4];

		String origPartId = partId;
		Assessment assessment = null;
		Part origPart = null;
		Part part = null;

		// get the assessment if specified
		if ((!"0".equals(assessmentId)) && (!"-".equals(assessmentId)))
		{
			assessment = this.assessmentService.getAssessment(assessmentId);
			if (assessment == null)
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
				return;
			}

			// security check for the assessment edit
			if (!this.assessmentService.allowEditAssessment(assessment))
			{
				// redirect to error
				res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
				return;
			}

			// get part id if specified
			if ((!"0".equals(partId)) && (!"-".equals(partId)))
			{
				origPart = assessment.getParts().getPart(partId);
				if (origPart == null)
				{
					// redirect to error
					res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.invalid)));
					return;
				}
				part = origPart;
			}
		}

		// get the question to work on
		Question question = this.questionService.getQuestion(questionId);
		if (question == null) throw new IllegalArgumentException();

		// put the question in the context
		context.put("question", question);

		// for the selected question type
		Value newType = this.uiService.newValue();
		context.put("selectedQuestionType", newType);

		// for the selected "for" part
		Value value = this.uiService.newValue();
		context.put("partId", value);

		// for the upload of attachments
		Upload upload = new Upload(this.toolManager.getCurrentPlacement().getContext(), AttachmentService.DOCS_AREA, this.attachmentService);
		context.put("upload", upload);

		// read form
		String destination = this.uiService.decode(req, context);

		// save the attachments upload
		if (upload.getUpload() != null)
		{
			question.getPresentation().addAttachment(upload.getUpload());
		}

		// handle an attachments remove
		if (destination.startsWith("REMOVE:"))
		{
			String[] parts = StringUtil.split(destination, ":");
			if (parts.length != 2)
			{
				throw new IllegalArgumentException();
			}
			String refString = parts[1];
			Reference ref = this.entityManager.newReference(refString);

			// remove from the assessment, but since the attachment was in the site's mneme docs and generally available, don't remove it
			question.getPresentation().removeAttachment(ref);
			// this.attachmentService.removeAttachment(ref);

			// stay here
			destination = context.getDestination();
		}

		// consolidate the question
		destination = question.getTypeSpecificQuestion().consolidate(destination);

		try
		{
			// save
			if ((!fixMode) && "RETYPE".equals(destination))
			{
				// save and re-type
				this.questionService.saveQuestionAsType(question, newType.getValue());
			}

			else
			{
				// just save - even if historical
				Boolean changed = question.getIsChanged();
				this.questionService.saveQuestion(question, Boolean.TRUE);

				// possible re-score needed if the assessment is locked
				if (fixMode && changed && (assessment != null) && assessment.getIsLocked())
				{
					this.assessmentService.rescoreAssessment(assessment);
				}
			}

			// update the assessment part details if the question is not mint (not for fix)
			if ((!fixMode) && (assessment != null) && (origPart != null) && (!question.getMint()))
			{
				// see if the user changed the part from the original
				String newPartId = value.getValue();
				if (!origPart.getId().equals(newPartId))
				{
					// see if the user wants a new part
					if ("0".equals(newPartId))
					{
						// create the new part
						try
						{
							Part created = assessment.getParts().addPart();
							this.assessmentService.saveAssessment(assessment);

							// here's the new id
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

					// get the desired part
					Part newPart = assessment.getParts().getPart(newPartId);
					if (newPart != null)
					{
						// remove the question from the old part (fails quietly if it was not there)
						part.removePickDetail(question);
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

						// set part and partId to the desired part
						part = newPart;
						partId = part.getId();
					}
				}

				// add to the desired part
				if (part != null)
				{
					part.addPickDetail(question);
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
			}

			if ((!fixMode) && "ADD".equals(destination))
			{
				destination = null;

				// setup a new question of the same type in the same pool
				try
				{
					Question newQuestion = this.questionService.newQuestion(question.getPool(), question.getType());

					// edit it
					destination = "/" + params[1] + "/" + newQuestion.getId() + "/" + assessmentId + "/" + partId + "/" + returnDestination;
				}
				catch (AssessmentPermissionException e)
				{
					// redirect to error
					res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
					return;
				}
			}
		}
		catch (AssessmentPermissionException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}
		catch (GradesRejectsAssessmentException e)
		{
			// redirect to error
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, "/error/" + Errors.unauthorized)));
			return;
		}

		if (("NEXT".equals(destination) || "PREV".equals(destination)))
		{
			// figure out the question id
			String returnDestParts[] = StringUtil.split(returnDestination, "/");
			String sortCode = "0A";
			if (!"-".equals(returnDestParts[3])) sortCode = returnDestParts[3];

			QuestionService.FindQuestionsSort sort = PoolEditView.findSort(sortCode);

			// get questions
			List<Question> questions = questionService.findQuestions(question.getPool(), sort, null, null, null, null, null, null);

			// find this one
			int pos = 0;
			for (Question q : questions)
			{
				if (q.equals(question))
				{
					break;
				}
				pos++;
			}

			// find next/prev w/ wrapping
			if ("NEXT".equals(destination))
			{
				if (pos == questions.size() - 1)
				{
					pos = 0;
				}
				else
				{
					pos++;
				}
			}
			else
			{
				if (pos == 0)
				{
					pos = questions.size() - 1;
				}
				else
				{
					pos--;
				}
			}

			String qid = questionId;
			if ((pos >= 0) && (pos <= questions.size() - 1))
			{
				qid = questions.get(pos).getId();
			}

			destination = "/" + params[1] + "/" + qid + "/" + assessmentId + "/" + partId + "/" + returnDestination;
		}

		// if destination became null, or is the stay here
		if ((destination == null) || ("STAY".equals(destination) || "RETYPE".equals(destination)))
		{
			destination = context.getDestination();
		}

		// adjust destination for proper part
		if ((!fixMode) && (!partId.equals(origPartId)))
		{
			String[] destParts = StringUtil.split(destination, "/");
			if (destParts[1].equals(params[1]))
			{
				destParts[4] = part.getId();
				destination = StringUtil.unsplit(destParts, 0, destParts.length, "/");
			}
		}

		// redirect
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
	 * Set the AttachmentService.
	 * 
	 * @param service
	 *        The AttachmentService.
	 */
	public void setAttachmentService(AttachmentService service)
	{
		this.attachmentService = service;
	}

	/**
	 * Set the EntityManager.
	 * 
	 * @param manager
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager manager)
	{
		entityManager = manager;
	}

	/**
	 * @param mnemeService
	 *        the mnemeService to set
	 */
	public void setMnemeService(MnemeService mnemeService)
	{
		this.mnemeService = mnemeService;
	}

	/**
	 * @param questionService
	 *        the questionService to set
	 */
	public void setQuestionService(QuestionService questionService)
	{
		this.questionService = questionService;
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
