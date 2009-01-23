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

package org.etudes.mneme.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.ImportTextService;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.SecurityService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * ImportQtiServiceImpl implements ImportQtiService
 * </p>
 */
public class ImportTextServiceImpl implements ImportTextService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ImportTextServiceImpl.class);

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
	public void importQuestions(String context, Pool pool, String text) throws AssessmentPermissionException
	{
		if ((text == null) || (text.length() == 0)) return;

		String titleKey = "title:";
		String pointsKey = "points:";
		String descriptionKey = "description:";
		String difficultyKey = "difficulty:";

		// replace any \r\n with just a \n
		text = text.replaceAll("\r\n", "\n");

		// parse the text into lines
		String[] lines = text.split("[\n]");

		// trim each one - record the blank index positions
		List<Integer> blanks = new ArrayList<Integer>();
		for (int line = 0; line < lines.length; line++)
		{
			lines[line] = lines[line].trim();
			if (lines[line].length() == 0)
			{
				blanks.add(Integer.valueOf(line));
			}
		}
		blanks.add(Integer.valueOf(lines.length));

		// make the groups
		List<String[]> groups = new ArrayList<String[]>();
		int pos = 0;
		for (Integer line : blanks)
		{
			// take from pos up to (not including) the index of the next blank into a new group
			String[] group = new String[line.intValue() - pos];
			int i = 0;
			while (pos < line.intValue())
			{
				group[i++] = lines[pos++];
			}
			groups.add(group);

			// eat the blank line
			pos++;
		}

		boolean topUsed = false;

		// if there's no pool given, create one
		if (pool == null)
		{
			// create the pool
			pool = this.poolService.newPool(context);

			// set the pool attributes
			String title = "untitled";
			Float points = null;
			String description = null;
			Integer difficulty = null;

			// get the title, description and points from the first group, if present
			String[] top = groups.get(0);
			for (String line : top)
			{
				String lower = line.toLowerCase();
				if (lower.startsWith(titleKey))
				{
					topUsed = true;
					String[] parts = StringUtil.splitFirst(line, ":");
					if (parts.length > 1) title = parts[1].trim();
				}
				else if (lower.startsWith(descriptionKey))
				{
					topUsed = true;
					String[] parts = StringUtil.splitFirst(line, ":");
					if (parts.length > 1) description = parts[1].trim();
				}
				else if (lower.startsWith(pointsKey))
				{
					topUsed = true;
					String[] parts = StringUtil.splitFirst(line, ":");
					if (parts.length > 1)
					{
						try
						{
							points = Float.valueOf(parts[1].trim());
						}
						catch (NumberFormatException ignore)
						{
						}
					}
				}
				else if (lower.startsWith(difficultyKey))
				{
					topUsed = true;
					String[] parts = StringUtil.splitFirst(line, ":");
					if (parts.length > 1)
					{
						try
						{
							difficulty = Integer.valueOf(parts[1].trim());
						}
						catch (NumberFormatException ignore)
						{
						}
					}
				}
			}

			pool.setTitle(title);
			if (points != null) pool.setPointsEdit(points);
			if (description != null) pool.setDescription(description);
			if (difficulty != null) pool.setDifficulty(difficulty);

			// save
			this.poolService.savePool(pool);
		}

		// process each one by creating a question and placing it into the pool
		boolean first = true;
		for (String[] group : groups)
		{
			if (first)
			{
				first = false;
				if (topUsed) continue;
			}

			processTextGroup(pool, group);
		}
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
	 * Process the lines into a question in the pool, if we can.
	 * 
	 * @param pool
	 *        The pool to hold the question.
	 * @param lines
	 *        The lines to process.
	 */
	protected void processTextGroup(Pool pool, String[] lines) throws AssessmentPermissionException
	{
		if (processTextTrueFalse(pool, lines)) return;
		if (processTextMultipleChoice(pool, lines)) return;
	}
	
	/**
	 * Process if it is recognized as a true false question.
	 * 
	 * @param pool
	 * 		  The pool to hold the question.
	 * @param lines
	 * 		  The lines to process.
	 * @return true if successfully recognized and processed, false if not.
	 * 
	 * @throws AssessmentPermissionException
	 */
	protected boolean processTextTrueFalse(Pool pool, String[] lines) throws AssessmentPermissionException
	{
		//if there are only two answer choices, and they are true and false and only one correct answer
		//then that may be a true/false question
		if (lines.length == 3)
		{
			boolean isTrue = false;
			
			String[] answer1 = lines[1].trim().split("\\s+");
			String[] answer2 = lines[2].trim().split("\\s+");
			
			if (answer1.length != 2 && answer2.length != 2)
				return false;
			
			//check for true and false answers
			if (("true".equalsIgnoreCase(answer1[1]) && "false".equalsIgnoreCase(answer2[1])) || 
					("false".equalsIgnoreCase(answer1[1]) && "true".equalsIgnoreCase(answer2[1])))
			{
				//true/false question should have only one answer
				if (answer1[0].startsWith("*") && answer2[0].startsWith("*"))
					return false;
				
				if (answer1[0].startsWith("*") && "true".equalsIgnoreCase(answer1[1]))
					isTrue = true;
				
				if (answer2[0].startsWith("*") && "true".equalsIgnoreCase(answer2[1]))
					isTrue = true;
					
				if ((answer1[0].startsWith("*") && "true".equalsIgnoreCase(answer1[1])) || 
					(answer2[0].startsWith("*") && "true".equalsIgnoreCase(answer2[1])))
				{
					isTrue = true;
				}
				
				// create the question
				Question question = this.questionService.newQuestion(pool, "mneme:TrueFalse");
				TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());

				// set the text
				String clean = HtmlHelper.clean(lines[0].trim());
				question.getPresentation().setText(clean);

				// the correct answer
				tf.setCorrectAnswer(Boolean.toString(isTrue));

				// save
				question.getTypeSpecificQuestion().consolidate("");
				this.questionService.saveQuestion(question);
				
				//TODO: reason
				
				//TODO: hints

				//TODO: feedback
				
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Process if it is recognized as a multiple choice question.
	 * 
	 * @param pool
	 * 		  The pool to hold the question.
	 * @param lines
	 * 		  The lines to process.
	 * @return true if successfully recognized and processed, false if not.
	 * 
	 * @throws AssessmentPermissionException
	 */
	protected boolean processTextMultipleChoice(Pool pool, String[] lines) throws AssessmentPermissionException
	{
		/*if there is only one answer for more answer choices then that may be a multiple choice question*/
		
		String correctAnswer = null, answerChoice = null;
		boolean first = true;
		boolean foundAnswer = false;
		// set the choices
		List<String> choices = new ArrayList<String>();
		String clean = null;
		
		for (String line : lines)
		{
			//ignore first line as first line is question text
			if (first)
			{
				first = false;
				continue;
			}
			String[] answer = line.trim().split("\\s+");
			if (answer.length < 2)
				return false;
			
			if (answer[0].startsWith("*"))
			{
				if (!foundAnswer) 
				{
					correctAnswer = line.substring(answer[0].length()).trim();
					foundAnswer = true;
				}
				else
					return false;
			}
			answerChoice = line.substring(answer[0].length()).trim();
			clean = HtmlHelper.clean(answerChoice.trim());
			choices.add(clean);
		}
		
		if (!foundAnswer)
			return false;
		
		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:MultipleChoice");
		MultipleChoiceQuestionImpl mc = (MultipleChoiceQuestionImpl) (question.getTypeSpecificQuestion());

		// set the text
		clean = HtmlHelper.clean(lines[0].trim());
		question.getPresentation().setText(clean);

		// randomize
		mc.setShuffleChoices(Boolean.toString(false));

		// single / multiple select
		mc.setSingleCorrect(Boolean.toString(true));
		
		// answer choices
		mc.setAnswerChoices(choices);
		
		Set<Integer> correctAnswers = new HashSet<Integer>();
		List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choicesAuthored = mc.getChoicesAsAuthored();

		// find this answer
		for (int index = 0; index < choicesAuthored.size(); index++)
		{
			MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice choice = choicesAuthored.get(index);
			if (choice.getText().equals(correctAnswer.trim()))
			{
				// use this answer's id
				correctAnswers.add(Integer.valueOf(choice.getId()));
			}
		}
		
		//correct answer
		mc.setCorrectAnswerSet(correctAnswers);
		
		// save
		question.getTypeSpecificQuestion().consolidate("");
		this.questionService.saveQuestion(question);
		
		//TODO: reason
		
		//TODO: hints

		//TODO: feedback
		
		return true;
	}
}
