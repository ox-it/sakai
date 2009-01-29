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
	
	/** hint key */
	protected static final String hintKey = "hint:";
	
	/** feedback key1 */
	protected static final String feedbackKey1 = "feedback:";
	
	/** feedback key2 */
	protected static final String feedbackKey2 = "general feedback:";

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
		if (processEssay(pool, lines)) return;
		if (processFillIn(pool, lines)) return;
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
		//if there are only two answer choices, and they are true and false and with one correct answer
		//then that may be a true/false question
		if (lines.length == 0 || lines.length < 3)
			return false;
			
		boolean isTrue = false;
		String feedback = null;
		String hints = null;
		
		String[] answer1 = lines[1].trim().split("\\s+");
		String[] answer2 = lines[2].trim().split("\\s+");
		
		if (answer1.length < 2 || answer2.length < 2)
			return false;
		
		// check for true and false answers
		if (("true".equalsIgnoreCase(answer1[1]) && "false".equalsIgnoreCase(answer2[1])) || 
				("false".equalsIgnoreCase(answer1[1]) && "true".equalsIgnoreCase(answer2[1])))
		{
			// true/false question should have only one answer
			if (answer1[0].startsWith("*") && answer2[0].startsWith("*"))
				return false;
			
			// there should be at least one answer
			if (!(answer1[0].startsWith("*") || answer2[0].startsWith("*")))
				return false;
			
			if ((answer1[0].startsWith("*") && "true".equalsIgnoreCase(answer1[1])) || 
				(answer2[0].startsWith("*") && "true".equalsIgnoreCase(answer2[1])))
			{
				isTrue = true;
			}
			
			// hints and feedback
			if (lines.length >= 4 && (StringUtil.trimToNull(lines[3]) != null)) 
			{
				String lower = lines[3].toLowerCase();
				if (lower.startsWith(hintKey) || lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
				{
					if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
					{
						String[] parts = StringUtil.splitFirst(lines[3], ":");
						if (parts.length > 1) feedback = parts[1].trim(); 
					} 
					else if (lower.startsWith(hintKey))
					{
						String[] parts = StringUtil.splitFirst(lines[3], ":");
						if (parts.length > 1) hints = parts[1].trim();
					}
				}
				else
					return false;
				
				if (lines.length >= 5 && (StringUtil.trimToNull(lines[4]) != null))
				{
					lower = lines[4].toLowerCase();
					if (lower.startsWith(hintKey) || lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
					{
						if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
						{
							String[] parts = StringUtil.splitFirst(lines[4], ":");
							if (parts.length > 1) feedback = parts[1].trim(); 
						} 
						else if (lower.startsWith(hintKey))
						{
							String[] parts = StringUtil.splitFirst(lines[4], ":");
							if (parts.length > 1) hints = parts[1].trim();
						}
					}
					else
						return false;
				}
					
			}
			
			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:TrueFalse");
			TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());

			// set the text
			String clean = HtmlHelper.clean(lines[0].trim());
			question.getPresentation().setText(clean);

			// the correct answer
			tf.setCorrectAnswer(Boolean.toString(isTrue));

			// add feedback
			if (StringUtil.trimToNull(feedback) != null)
			{
				question.setFeedback(HtmlHelper.clean(feedback));
			}
			
			// add hints
			if (StringUtil.trimToNull(hints) != null)
			{
				question.setHints(HtmlHelper.clean(hints));
			}
			
			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);
			
			return true;
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
		//if there is one or more answers for more answer choices then that may be a multiple choice question
		if (lines.length == 0 || lines.length < 3)
			return false;
		
		boolean first = true;
		boolean foundAnswer = false;
		String answerChoice = null;
		List<Integer> multipleAnswers = new ArrayList<Integer>();
		List<String> choices = new ArrayList<String>();
		String clean = null;
		
		String feedback = null;
		String hints = null;
		boolean foundHints = false, foundFeedback = false;
		
		int answersIndex = 0;
		for (String line : lines)
		{
			// ignore first line as first line is question text
			if (first)
			{
				first = false;
				continue;
			}
			
			// hints and feedback
			String lower = line.toLowerCase();
			if (foundAnswer)
			{
				if (lower.startsWith(hintKey) || lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
				{
					if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
					{
						String[] parts = StringUtil.splitFirst(line, ":");
						if (parts.length > 1) feedback = parts[1].trim();
						foundFeedback = true;
					} 
					else if (lower.startsWith(hintKey))
					{
						String[] parts = StringUtil.splitFirst(line, ":");
						if (parts.length > 1) hints = parts[1].trim();
						foundHints = true;
					}
					if (foundFeedback && foundHints)
						break;
					
					continue;
				} 
				// ignore the answer choices after hints or feedback found
				else if (foundFeedback || foundHints)
						continue;
			}
			
			String[] answer = line.trim().split("\\s+");
			if (answer.length < 2)
				return false;
			
			if (answer[0].startsWith("*"))
			{
				if (!foundAnswer) 
				{
					multipleAnswers.add(Integer.valueOf(answersIndex));
					foundAnswer = true;
				}
				else
				{
					multipleAnswers.add(Integer.valueOf(answersIndex));
				}
			}
			answerChoice = line.substring(answer[0].length()).trim();
			clean = HtmlHelper.clean(answerChoice);
			choices.add(clean);
			answersIndex++;
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

		// answer choices
		mc.setAnswerChoices(choices);
		
		Set<Integer> correctAnswers = new HashSet<Integer>();
		List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choicesAuthored = mc.getChoicesAsAuthored();

		// find the answers
		for (Integer answerIndex : multipleAnswers)
		{
			correctAnswers.add(Integer.valueOf(choicesAuthored.get(answerIndex).getId()));
		}
		
		// correct answer
		mc.setCorrectAnswerSet(correctAnswers);
		
		// single / multiple select
		if (correctAnswers.size() == 1)
			mc.setSingleCorrect(Boolean.toString(true));
		else
			mc.setSingleCorrect(Boolean.toString(false));
		
		// add feedback
		if (StringUtil.trimToNull(feedback) != null)
		{
			question.setFeedback(HtmlHelper.clean(feedback));
		}
		
		// add hints
		if (StringUtil.trimToNull(hints) != null)
		{
			question.setHints(HtmlHelper.clean(hints));
		}
		
		// save
		question.getTypeSpecificQuestion().consolidate("");
		this.questionService.saveQuestion(question);
		
		return true;
	}
	
	/**
	 * Process if it is recognized as an essay question.
	 * 
	 * @param pool
	 * 		  The pool to hold the question.
	 * @param lines
	 * 		  The lines to process.
	 * @return true if successfully recognized and processed, false if not.
	 * 
	 * @throws AssessmentPermissionException
	 */
	protected boolean processEssay(Pool pool, String[] lines) throws AssessmentPermissionException
	{
		//if there are no answers then that may be a essay question
		if (lines.length == 0 || lines.length > 4)
			return false;
		
		boolean first = true;
		String clean = null;
		String feedback = null;
		String hints = null;
		String modelAnswer = null;
		String modelAnswerKey = "model answer:";
		
		// question with braces may be a fill in question
		if ((lines[0].indexOf("{") != -1) && (lines[0].indexOf("}") != -1) && (lines[0].indexOf("{") < lines[0].indexOf("}")))
			return false;
		
		// model answer, hints and feedback
		for (String line : lines)
		{
			// ignore first line as first line is question text
			if (first)
			{
				first = false;
				continue;
			}
			
			String lower = line.toLowerCase();
			if (lower.startsWith(hintKey) || lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2) || lower.startsWith(modelAnswerKey))
			{
				if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
				{
					String[] parts = StringUtil.splitFirst(line, ":");
					if (parts.length > 1) feedback = parts[1].trim();
				} 
				else if (lower.startsWith(hintKey))
				{
					String[] parts = StringUtil.splitFirst(line, ":");
					if (parts.length > 1) hints = parts[1].trim();
				}
				else if (lower.startsWith(modelAnswerKey))
				{
					String[] parts = StringUtil.splitFirst(line, ":");
					if (parts.length > 1) modelAnswer = parts[1].trim();
				}
			} 
			else
				return false;
		}
		
		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:Essay");
		EssayQuestionImpl e = (EssayQuestionImpl) (question.getTypeSpecificQuestion());
		
		// set the text
		clean = HtmlHelper.clean(lines[0].trim());
		question.getPresentation().setText(clean);
				
		// type
		e.setSubmissionType(EssayQuestionImpl.SubmissionType.inline);
		
		// add model answer
		if (StringUtil.trimToNull(modelAnswer) != null)
		{
			e.setModelAnswer(HtmlHelper.clean(modelAnswer));
		}
		
		// add feedback
		if (StringUtil.trimToNull(feedback) != null)
		{
			question.setFeedback(HtmlHelper.clean(feedback));
		}
		
		// add hints
		if (StringUtil.trimToNull(hints) != null)
		{
			question.setHints(HtmlHelper.clean(hints));
		}
				
		// save
		question.getTypeSpecificQuestion().consolidate("");
		this.questionService.saveQuestion(question);
		
		return true;
	}
	
	/**
	 * Process if it is recognized as an fill-in question.
	 * 
	 * @param pool
	 * 		  The pool to hold the question.
	 * @param lines
	 * 		  The lines to process.
	 * @return true if successfully recognized and processed, false if not.
	 * 
	 * @throws AssessmentPermissionException
	 */
	protected boolean processFillIn(Pool pool, String[] lines) throws AssessmentPermissionException
	{
		// if there are only answers then that may be a fill-in question. Another case is if the question has braces that may be a fill-in question
		if (lines.length == 0)
			return false;
		
		boolean braces = false;
		boolean first = true;
		boolean foundAnswer = false;
		List<String> answers = new ArrayList<String>();
		String feedback = null, hints = null;
		boolean foundHints = false, foundFeedback = false;
		String clean = null;
			
		// question with braces may be a fill in question
		if ((lines[0].indexOf("{") != -1) && (lines[0].indexOf("}") != -1) && (lines[0].indexOf("{") < lines[0].indexOf("}")))
		{
			String validateBraces = lines[0];
			while (validateBraces.indexOf("{") != -1)
			{
				validateBraces = validateBraces.substring(validateBraces.indexOf("{")+1);
				int startBraceIndex = validateBraces.indexOf("{");
				int endBraceIndex = validateBraces.indexOf("}");
				String answer;
				
				if (startBraceIndex != -1 && endBraceIndex != -1)
				{
					if (endBraceIndex > startBraceIndex)
						return false;
				}
				if (endBraceIndex != -1)
				{
					answer = validateBraces.substring(0, endBraceIndex);
					if (StringUtil.trimToNull(answer) == null)
						return false;
				}
				else
					return false;
				
				validateBraces = validateBraces.substring(validateBraces.indexOf("}")+1);
			}
			
			braces = true;
		}
		
		if (braces)
		{
			// hints and feedback
			for (String line : lines)
			{
				// ignore first line as first line is question text
				if (first)
				{
					first = false;
					continue;
				}
				
				if (line.startsWith("*"))
					return false;
				
				// hints and feedback
				String lower = line.toLowerCase();
				if (lower.startsWith(hintKey) || lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
				{
					if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
					{
						String[] parts = StringUtil.splitFirst(line, ":");
						if (parts.length > 1) feedback = parts[1].trim();
						foundFeedback = true;
					} 
					else if (lower.startsWith(hintKey))
					{
						String[] parts = StringUtil.splitFirst(line, ":");
						if (parts.length > 1) hints = parts[1].trim();
						foundHints = true;
					}
					if (foundFeedback && foundHints)
						break;
				} 
			}			
		}
		else
		{
			for (String line : lines)
			{
				// ignore first line as first line is question text
				if (first)
				{
					first = false;
					continue;
				}
				
				if (line.startsWith("*"))
					return false;
				
				// hints and feedback
				String lower = line.toLowerCase();
				if (foundAnswer)
				{
					if (lower.startsWith(hintKey) || lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
					{
						if (lower.startsWith(feedbackKey1) || lower.startsWith(feedbackKey2))
						{
							String[] parts = StringUtil.splitFirst(line, ":");
							if (parts.length > 1) feedback = parts[1].trim();
							foundFeedback = true;
						} 
						else if (lower.startsWith(hintKey))
						{
							String[] parts = StringUtil.splitFirst(line, ":");
							if (parts.length > 1) hints = parts[1].trim();
							foundHints = true;
						}
						if (foundFeedback && foundHints)
							break;
						
						continue;
					} 
					// ignore the answer choices after hints or feedback found
					else if (foundFeedback || foundHints)
							continue;
				}
				
				String[] answer = line.trim().split("\\s+");
				if (answer.length < 2)
					return false;
				
				String answerChoice = line.substring(answer[0].length()).trim();
				clean = HtmlHelper.clean(answerChoice);
				answers.add(clean);
				if (!foundAnswer) foundAnswer = true;
			}
			
			if (!foundAnswer)
				return false;
		}
				
		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:FillBlanks");
		FillBlanksQuestionImpl f = (FillBlanksQuestionImpl) (question.getTypeSpecificQuestion());
		
		f.setAnyOrder(Boolean.FALSE.toString());
		
		// case sensitive
		f.setCaseSensitive(Boolean.FALSE.toString());
		
		//mutually exclusive
		f.setAnyOrder(Boolean.FALSE.toString());

		// text or numeric
		f.setResponseTextual(Boolean.TRUE.toString());
		
		String questionText = lines[0];
		if (!braces && foundAnswer) {
			StringBuffer buildAnswers = new StringBuffer();
			buildAnswers.append("{");
			for (String answer : answers)
			{
				buildAnswers.append(answer);
				buildAnswers.append("|");
			}
			buildAnswers.replace(buildAnswers.length() - 1, buildAnswers.length(), "}");
			questionText = questionText.concat(buildAnswers.toString());
		}
		
		// set the text
		clean = HtmlHelper.clean(questionText);
		f.setText(clean);
		
		// add feedback
		if (StringUtil.trimToNull(feedback) != null)
		{
			question.setFeedback(HtmlHelper.clean(feedback));
		}
		
		// add hints
		if (StringUtil.trimToNull(hints) != null)
		{
			question.setHints(HtmlHelper.clean(hints));
		}
		
		// save
		question.getTypeSpecificQuestion().consolidate("");
		this.questionService.saveQuestion(question);
		
		return true;
	}
	
}
