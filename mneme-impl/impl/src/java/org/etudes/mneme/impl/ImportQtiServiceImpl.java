/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013 Etudes, Inc.
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.ImportQtiService;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.SecurityService;
import org.etudes.util.DateHelper;
import org.etudes.util.HtmlHelper;
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
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * ImportQtiServiceImpl implements ImportQtiService
 * </p>
 */
public class ImportQtiServiceImpl implements ImportQtiService
{
	protected class Average
	{
		protected int count = 0;

		protected float total = 0.0f;

		public void add(float value)
		{
			count++;
			total += value;
		}

		public float getAverage()
		{
			if (count > 0)
			{
				return total / count;
			}
			return 0.0f;
		}
	}

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
			XPath assessmentTitlePath = new DOMXPath("/questestinterop/assessment/@title");
			String title = StringUtil.trimToNull(assessmentTitlePath.stringValueOf(doc));
			if (title == null)
			{
				// try for a single /item
				XPath itemTitlePath = new DOMXPath("/item/@title");
				title = StringUtil.trimToNull(itemTitlePath.stringValueOf(doc));
			}

			if (title != null)
			{
				poolId = title;
			}
		}
		catch (JaxenException e)
		{
			M_log.warn(e.toString());
		}

		// add a date stamp
		poolId = addDate("import-text", poolId, new Date());

		// create the pool
		Pool pool = this.poolService.newPool(context);
		pool.setTitle(poolId);
		// pool.setDescription(info.description);

		// average the question points for the pool's point value
		Average pointsAverage = new Average();

		// process questions
		try
		{
			XPath itemPath = new DOMXPath("//item");
			List items = itemPath.selectNodes(doc);

			for (Object oItem : items)
			{
				Element item = (Element) oItem;

				if (processSamigoTrueFalse(item, pool, pointsAverage)) continue;
				if (processSamigoMultipleChoice(item, pool, pointsAverage)) continue;
				if (processSamigoSurvey(item, pool)) continue;
				if (processSamigoFillIn(item, pool, pointsAverage)) continue;

				// process Respondous 3.5 type of format
				if (processRespondousTrueFalse(item, pool, pointsAverage)) continue;
				if (processRespondousMultipleChoice(item, pool, pointsAverage)) continue;
				if (processRespondousEssay(item, pool)) continue;
				if (processRespondousFillIn(item, pool, pointsAverage)) continue;
				if (processRespondousMatching(item, pool, pointsAverage)) continue;

				M_log.warn("item recognized: " + item.getAttribute("ident"));
			}
		}
		catch (JaxenException e)
		{
			M_log.warn(e.toString());
		}

		// set the pool point value
		pool.setPointsEdit(Float.valueOf(pointsAverage.getAverage()));

		// save
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
		String fmt = DateHelper.formatDateForName(date, null);

		// the args
		Object[] args = new Object[2];
		args[0] = source;
		args[1] = fmt;

		// format the works
		String rv = this.messages.getFormattedMessage(selector, args);

		return rv;
	}

	/**
	 * Find all the items from the root using the path, and combine their text content.
	 * 
	 * @param path
	 *        The XPath
	 * @param root
	 *        The Document or Element root
	 * @return The combined values for these hits.
	 */
	protected String combineHits(XPath path, Object root)
	{
		StringBuilder rv = new StringBuilder();
		try
		{
			List hits = path.selectNodes(root);
			for (Object o : hits)
			{
				Element e = (Element) o;
				String value = StringUtil.trimToNull(e.getTextContent());
				if (value != null)
				{
					if (rv.length() > 0)
					{
						rv.append("/n");
					}
					rv.append(value);
				}
			}
		}
		catch (JaxenException e)
		{
			System.out.println(e.toString());
		}

		if (rv.length() == 0) return null;
		return rv.toString();
	}

	/**
	 * Find all the items from the root using the path, and combine their text content.
	 * 
	 * @param textPath
	 *        Text XPath
	 * @param blanksPath
	 *        Blanks XPath
	 * @param answersPath
	 *        Answers XPath
	 * @param root
	 *        The Document or Element root
	 * @return The question.
	 */
	protected String buildFillInQuestionText(XPath textPath, XPath blanksPath, XPath answersPath, Object root)
	{
		StringBuilder rv = new StringBuilder();
		String questionText = null;
		try
		{
			List hits = textPath.selectNodes(root);
			List blankHits = blanksPath.selectNodes(root);
			List answerHits = answersPath.selectNodes(root);

			if (answerHits.size() != blankHits.size()) return null;

			int count = 0, blanksCount = 0;

			blanksCount = blankHits.size();

			for (Object o : hits)
			{
				// add braces for the text
				if (count > 0 && blanksCount > 0)
				{
					rv.append(" {} ");
					blanksCount--;
				}

				Element e = (Element) o;
				String value = StringUtil.trimToNull(e.getTextContent());
				if (value != null)
				{
					rv.append(value);
				}
				count++;
			}

			// fill the braces({}) with answers
			questionText = rv.toString();
			Element e;
			String answer;
			for (int index = 0; index < answerHits.size(); index++)
			{
				e = (Element) answerHits.get(index);
				answer = StringUtil.trimToNull(e.getTextContent());
				questionText = questionText.replaceFirst("\\{\\}", Matcher.quoteReplacement("{" + answer + "}"));
			}
		}
		catch (JaxenException e)
		{
			M_log.warn(e.toString());
		}

		if (questionText.length() == 0) return null;
		return questionText;
	}

	/**
	 * Process the item if it is recognized as a Samigo multiple choice.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 */
	protected boolean processSamigoMultipleChoice(Element item, Pool pool, Average pointsAverage) throws AssessmentPermissionException
	{
		try
		{
			// the attributes of a multiple choice question
			String presentation = null;
			boolean rationale = false;
			String feedback = null;
			String hint = null;
			boolean survey = false;
			float points = 0.0f;
			String externalId = null;
			boolean shuffle = true;
			boolean singleAnswer = true;

			externalId = StringUtil.trimToNull(item.getAttribute("ident"));

			XPath metaDataPath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='qmd_itemtype']/fieldentry");
			String qmdItemType = StringUtil.trimToNull(metaDataPath.stringValueOf(item));
			if ("Multiple Correct Answer".equalsIgnoreCase(qmdItemType))
			{
				singleAnswer = false;
			}
			else if (!"Multiple Choice".equalsIgnoreCase(qmdItemType))
			{
				return false;
			}

			XPath rationalePath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='hasRationale']/fieldentry");
			String rationaleValue = StringUtil.trimToNull(rationalePath.stringValueOf(item));
			if (rationaleValue == null) return false;
			rationale = "true".equalsIgnoreCase(rationaleValue);

			XPath shufflePath = new DOMXPath("presentation//response_lid//render_choice/@shuffle");
			String shuffleValue = StringUtil.trimToNull(shufflePath.stringValueOf(item));
			if (shuffleValue == null) return false;
			shuffle = "yes".equalsIgnoreCase(shuffleValue);

			// XPath singleAnswerPath = new DOMXPath("presentation//response_lid/@rcardinality");
			// String singleAnswerValue = StringUtil.trimToNull(singleAnswerPath.stringValueOf(item));
			// if (singleAnswerValue == null) return false;
			// boolean singleAnswer2 = "single".equalsIgnoreCase(singleAnswerValue);
			//
			// if (singleAnswer2 != singleAnswer)
			// {
			// System.out.println(" !!!!!!!!!!! single answer mismatch");
			// }

			XPath textPath = new DOMXPath("presentation//material[not(ancestor::response_lid)]/mattext");
			presentation = combineHits(textPath, item);
			if (presentation == null) return false;

			XPath pointsPath = new DOMXPath("resprocessing/outcomes/decvar/@maxvalue");
			String pointsValue = StringUtil.trimToNull(pointsPath.stringValueOf(item));
			if (pointsValue == null) return false;
			try
			{
				points = Float.valueOf(pointsValue);
			}
			catch (NumberFormatException e)
			{
				return false;
			}

			// use the first (correct / incorrect) feedback as the feedback
			XPath feedbackPath = new DOMXPath(".//itemfeedback//material/mattext");
			List feedbacks = feedbackPath.selectNodes(item);
			for (Object oFeedback : feedbacks)
			{
				Element feedbackElement = (Element) oFeedback;
				String feedbackValue = StringUtil.trimToNull(feedbackElement.getTextContent());
				if (feedbackValue != null)
				{
					feedback = feedbackValue;
					break;
				}
			}

			// answers - w/ id
			Map<String, String> answerMap = new LinkedHashMap<String, String>();
			XPath answersPath = new DOMXPath(".//presentation//response_lid//render_choice//response_label");
			List answers = answersPath.selectNodes(item);
			for (Object oAnswer : answers)
			{
				Element answerElement = (Element) oAnswer;

				String id = StringUtil.trimToNull(answerElement.getAttribute("ident"));
				if (id == null) continue;

				XPath answerMaterialPath = new DOMXPath(".//material//mattext");
				String answer = combineHits(answerMaterialPath, answerElement);
				if (answer == null) continue;

				answerMap.put(id, answer);
			}

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:MultipleChoice");
			MultipleChoiceQuestionImpl mc = (MultipleChoiceQuestionImpl) (question.getTypeSpecificQuestion());

			// set the text
			String clean = HtmlHelper.cleanAndAssureAnchorTarget(presentation, true);
			question.getPresentation().setText(clean);

			// randomize
			mc.setShuffleChoices(Boolean.toString(shuffle));

			// single / multiple select
			mc.setSingleCorrect(Boolean.toString(singleAnswer));

			// set the choices
			List<String> choices = new ArrayList<String>();
			for (String key : answerMap.keySet())
			{
				String value = answerMap.get(key);

				clean = HtmlHelper.cleanAndAssureAnchorTarget(value, true);
				choices.add(clean);
			}
			mc.setAnswerChoices(choices);

			// corrects
			Set<Integer> correctAnswers = new HashSet<Integer>();
			List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choicesAuthored = mc.getChoicesAsAuthored();

			// the correct answers
			XPath correctAnswerPath = new DOMXPath("resprocessing/respcondition[displayfeedback/@linkrefid='Correct']/conditionvar/varequal");
			List corrects = correctAnswerPath.selectNodes(item);
			for (Object oCorrect : corrects)
			{
				Element correctElement = (Element) oCorrect;
				String correctId = StringUtil.trimToNull(correctElement.getTextContent());
				if (correctId != null)
				{
					String correctValue = answerMap.get(correctId);

					// find this answer
					for (int index = 0; index < choicesAuthored.size(); index++)
					{
						MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice choice = choicesAuthored.get(index);
						if (choice.getText().equals(correctValue))
						{
							// use this answer's id
							correctAnswers.add(Integer.valueOf(choice.getId()));
						}
					}
				}
			}
			mc.setCorrectAnswerSet(correctAnswers);

			// reason
			question.setExplainReason(Boolean.valueOf(rationale));

			// feedback
			if (feedback != null)
			{
				question.setFeedback(HtmlHelper.cleanAndAssureAnchorTarget(feedback, true));
			}

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			// add to the points average
			pointsAverage.add(points);

			return true;
		}
		catch (JaxenException e)
		{
			return false;
		}
	}

	/**
	 * Process the item if it is recognized as a Samigo multiple choice.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 */
	protected boolean processSamigoSurvey(Element item, Pool pool) throws AssessmentPermissionException
	{
		try
		{
			// the attributes of a survey question
			String presentation = null;
			boolean rationale = false;
			String feedback = null;
			String externalId = null;

			externalId = StringUtil.trimToNull(item.getAttribute("ident"));

			XPath metaDataPath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='qmd_itemtype']/fieldentry");
			String qmdItemType = StringUtil.trimToNull(metaDataPath.stringValueOf(item));
			if (!"Multiple Choice Survey".equalsIgnoreCase(qmdItemType))
			{
				return false;
			}

			XPath rationalePath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='hasRationale']/fieldentry");
			String rationaleValue = StringUtil.trimToNull(rationalePath.stringValueOf(item));
			if (rationaleValue != null)
			{
				rationale = "true".equalsIgnoreCase(rationaleValue);
			}

			XPath textPath = new DOMXPath("presentation//material[not(ancestor::response_lid)]/mattext");
			presentation = combineHits(textPath, item);
			if (presentation == null) return false;

			// use the first (correct / incorrect) feedback as the feedback
			XPath feedbackPath = new DOMXPath(".//itemfeedback//material/mattext");
			List feedbacks = feedbackPath.selectNodes(item);
			for (Object oFeedback : feedbacks)
			{
				Element feedbackElement = (Element) oFeedback;
				String feedbackValue = StringUtil.trimToNull(feedbackElement.getTextContent());
				if (feedbackValue != null)
				{
					feedback = feedbackValue;
					break;
				}
			}

			// answers
			Set<String> answerSet = new HashSet<String>();
			XPath answersPath = new DOMXPath(".//presentation//response_lid//render_choice//response_label");
			List answers = answersPath.selectNodes(item);
			for (Object oAnswer : answers)
			{
				Element answerElement = (Element) oAnswer;

				String id = StringUtil.trimToNull(answerElement.getAttribute("ident"));
				if (id == null) continue;

				XPath answerMaterialPath = new DOMXPath(".//material//mattext");
				String answer = combineHits(answerMaterialPath, answerElement);
				if (answer == null) continue;

				answerSet.add(answer);
			}

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:LikertScale");
			LikertScaleQuestionImpl l = (LikertScaleQuestionImpl) (question.getTypeSpecificQuestion());

			// set the text
			String clean = HtmlHelper.cleanAndAssureAnchorTarget(presentation, true);
			question.getPresentation().setText(clean);

			String scale = null;
			// "0" for our 5 point "strongly-agree"
			// "1" for our 4 point "excellent"
			// "2" for our 3 point "above-average"
			// "3" for our 2 point "yes"
			// "4" for our 5 point "5"
			// "5" for our 2 point "rocks"

			// 3 choices is below/average/above or disagree/undecided/agree
			if (answerSet.size() == 3)
			{
				if (answerSet.contains("Below Average"))
				{
					scale = "2";
				}
				else
				{
					scale = "0";
				}
			}

			// 2 is yes/no, or agree / disagree
			else if (answerSet.size() == 2)
			{
				if (answerSet.contains("No"))
				{
					scale = "3";
				}

				else
				{
					scale = "0";
				}
			}

			// 5 is strongly agree -> strongly disagree or unacceptable/below average/average/above average/excelent
			// or 1..5
			else if (answerSet.size() == 5)
			{
				if (answerSet.contains("1"))
				{
					scale = "4";
				}
				else if (answerSet.contains("Strongly Disagree"))
				{
					scale = "0";
				}
				else
				{
					scale = "1";
				}
			}

			// 10 is 1..10
			else if (answerSet.size() == 10)
			{
				scale = "4";
			}

			if (scale == null)
			{
				return false;
			}

			// set the scale
			l.setScale(scale);

			// reason
			question.setExplainReason(Boolean.valueOf(rationale));

			// feedback
			if (feedback != null)
			{
				question.setFeedback(HtmlHelper.cleanAndAssureAnchorTarget(feedback, true));
			}

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			return true;
		}
		catch (JaxenException e)
		{
			return false;
		}
	}

	/**
	 * Process this item if it is recognized as a Samigo true false.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 * @return true if successfully recognized and processed, false if not.
	 */
	protected boolean processSamigoTrueFalse(Element item, Pool pool, Average pointsAverage) throws AssessmentPermissionException
	{
		try
		{
			// the attributes of a true/false question
			String presentation = null;
			boolean rationale = false;
			boolean isTrue = true;
			String feedback = null;
			String hint = null;
			boolean survey = false;
			float points = 0.0f;
			String externalId = null;

			externalId = StringUtil.trimToNull(item.getAttribute("ident"));

			XPath metaDataPath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='qmd_itemtype']/fieldentry");
			String qmdItemType = StringUtil.trimToNull(metaDataPath.stringValueOf(item));
			if (!"True False".equalsIgnoreCase(qmdItemType)) return false;

			XPath rationalePath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='hasRationale']/fieldentry");
			String rationaleValue = StringUtil.trimToNull(rationalePath.stringValueOf(item));
			if (rationaleValue == null) return false;
			rationale = "true".equalsIgnoreCase(rationaleValue);

			XPath textPath = new DOMXPath("presentation//material[not(ancestor::response_lid)]/mattext");
			presentation = combineHits(textPath, item);
			if (presentation == null) return false;

			XPath pointsPath = new DOMXPath("resprocessing/outcomes/decvar/@maxvalue");
			String pointsValue = StringUtil.trimToNull(pointsPath.stringValueOf(item));
			if (pointsValue == null) return false;
			try
			{
				points = Float.valueOf(pointsValue);
			}
			catch (NumberFormatException e)
			{
				return false;
			}

			// use the first (correct / incorrect) feedback as the feedback
			XPath feedbackPath = new DOMXPath(".//itemfeedback//material/mattext");
			List feedbacks = feedbackPath.selectNodes(item);
			for (Object oFeedback : feedbacks)
			{
				Element feedbackElement = (Element) oFeedback;
				String feedbackValue = StringUtil.trimToNull(feedbackElement.getTextContent());
				if (feedbackValue != null)
				{
					feedback = feedbackValue;
					break;
				}
			}

			// answers - w/ id
			Map<String, String> answerMap = new HashMap<String, String>();
			XPath answersPath = new DOMXPath(".//presentation//response_lid//render_choice//response_label");
			List answers = answersPath.selectNodes(item);
			for (Object oAnswer : answers)
			{
				Element answerElement = (Element) oAnswer;

				String id = StringUtil.trimToNull(answerElement.getAttribute("ident"));

				XPath answerMaterialPath = new DOMXPath(".//material//mattext");
				String answer = combineHits(answerMaterialPath, answerElement);
				if (answer != null)
				{
					answerMap.put(id, answer);
				}
			}

			boolean falseSeen = false;
			boolean trueSeen = false;
			if (answerMap.size() != 2) return false;
			for (String key : answerMap.keySet())
			{
				String value = answerMap.get(key);
				if ("true".equalsIgnoreCase(value)) trueSeen = true;
				if ("false".equalsIgnoreCase(value)) falseSeen = true;
			}
			if (!falseSeen) return false;
			if (!trueSeen) return false;

			// the id of the correct answer
			XPath correctAnswerPath = new DOMXPath("resprocessing/respcondition[@title='Correct']/conditionvar/varequal");
			String correctId = StringUtil.trimToNull(correctAnswerPath.stringValueOf(item));
			if (correctId == null) return false;
			String correctValue = answerMap.get(correctId);
			if (correctValue == null) return false;
			isTrue = "true".equalsIgnoreCase(correctValue);

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:TrueFalse");
			TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());

			// set the text
			String clean = HtmlHelper.cleanAndAssureAnchorTarget(presentation, true);
			question.getPresentation().setText(clean);

			// the correct answer
			tf.setCorrectAnswer(Boolean.toString(isTrue));

			// reason
			question.setExplainReason(Boolean.valueOf(rationale));

			// feedback
			if (feedback != null)
			{
				question.setFeedback(HtmlHelper.cleanAndAssureAnchorTarget(feedback, true));
			}

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			// add to the points average
			pointsAverage.add(points);

			return true;
		}
		catch (JaxenException e)
		{
			return false;
		}
	}

	/**
	 * Process this item if it is recognized as a Samigo Fill in the blank.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 * @return true if successfully recognized and processed, false if not.
	 */
	protected boolean processSamigoFillIn(Element item, Pool pool, Average pointsAverage) throws AssessmentPermissionException
	{
		// the attributes of a Fill In the Blank question
		boolean caseSensitive = false;
		boolean mutuallyExclusive = false;
		String presentation = null;
		float points = 0.0f;

		String externalId = null;

		// identifier
		externalId = StringUtil.trimToNull(item.getAttribute("ident"));

		try
		{
			XPath metaDataPath;
			metaDataPath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='qmd_itemtype']/fieldentry");
			String qmdItemType = StringUtil.trimToNull(metaDataPath.stringValueOf(item));
			if (!"Fill In the Blank".equalsIgnoreCase(qmdItemType)) return false;

			XPath caseSensitivePath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='CASE_SENSITIVE']/fieldentry");
			String caseSensitiveValue = StringUtil.trimToNull(caseSensitivePath.stringValueOf(item));
			if (caseSensitiveValue != null) caseSensitive = "true".equalsIgnoreCase(caseSensitiveValue);

			XPath mutuallyExclusivePath = new DOMXPath("itemmetadata/qtimetadata/qtimetadatafield[fieldlabel='MUTUALLY_EXCLUSIVE']/fieldentry");
			String mutuallyExclusiveValue = StringUtil.trimToNull(mutuallyExclusivePath.stringValueOf(item));
			if (mutuallyExclusiveValue != null) mutuallyExclusive = "true".equalsIgnoreCase(mutuallyExclusiveValue);

			XPath textPath = new DOMXPath("presentation//material[not(ancestor::response_lid)]/mattext");
			XPath blanksPath = new DOMXPath("presentation//response_str/render_fib");
			XPath answersPath = new DOMXPath("resprocessing//respcondition/conditionvar/or/varequal");

			presentation = buildFillInQuestionText(textPath, blanksPath, answersPath, item);
			if (presentation == null) return false;

			XPath pointsPath = new DOMXPath("resprocessing/outcomes/decvar/@maxvalue");
			String pointsValue = StringUtil.trimToNull(pointsPath.stringValueOf(item));
			if (pointsValue == null) return false;
			try
			{
				points = Float.valueOf(pointsValue);
			}
			catch (NumberFormatException e)
			{
				return false;
			}

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:FillBlanks");
			FillBlanksQuestionImpl f = (FillBlanksQuestionImpl) (question.getTypeSpecificQuestion());

			f.setAnyOrder(Boolean.FALSE.toString());

			// case sensitive
			f.setCaseSensitive(Boolean.toString(caseSensitive));

			// mutually exclusive
			f.setAnyOrder(Boolean.toString(mutuallyExclusive));

			// text or numeric
			f.setResponseTextual(Boolean.TRUE.toString());

			// TODO attachments

			// set the text
			String clean = HtmlHelper.cleanAndAssureAnchorTarget(presentation, true);
			f.setText(clean);

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			// add to the points average
			pointsAverage.add(points);

			return true;

		}
		catch (JaxenException e)
		{

			return false;
		}
	}

	/**
	 * Process this item if it is recognized as a Respondous true false.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 * @return true if successfully recognized and processed, false if not.
	 */
	protected boolean processRespondousTrueFalse(Element item, Pool pool, Average pointsAverage) throws AssessmentPermissionException
	{
		// the attributes of a true/false question
		String presentation = null;
		boolean rationale = false;
		boolean isTrue = true;
		String feedback = null;
		String hint = null;
		boolean survey = false;
		float points = 0.0f;
		String itemId = null;

		// item identifier
		itemId = StringUtil.trimToNull(item.getAttribute("ident"));

		try
		{
			// presentation text
			// Respondous is using the format - presentation/material/mattext
			XPath presentationTextPath = new DOMXPath("presentation/material/mattext");
			List presentationMaterialTexts = presentationTextPath.selectNodes(item);
			StringBuilder presentationTextBuilder = new StringBuilder();
			for (Object presentationMaterialText : presentationMaterialTexts)
			{
				Element presentationTextElement = (Element) presentationMaterialText;
				XPath matTextPath = new DOMXPath(".");
				String matText = StringUtil.trimToNull(matTextPath.stringValueOf(presentationTextElement));

				if (matText != null) presentationTextBuilder.append(matText);
			}
			presentation = presentationTextBuilder.toString();

			if (presentation == null)
			{
				// QTI format - presentation/flow/material/mattext
				presentationTextPath = new DOMXPath("presentation/flow/material/mattext");
				presentation = StringUtil.trimToNull(presentationTextPath.stringValueOf(item));
			}

			if (presentation == null) return false;

			// reponse_lid
			XPath reponseLidPath = new DOMXPath("presentation//response_lid");
			List responseLids = reponseLidPath.selectNodes(item);

			if ((responseLids.size() == 0) || (responseLids.size() > 1)) return false;

			Element responseLidElement = (Element) responseLids.get(0);
			String rcardinality = StringUtil.trimToNull(responseLidElement.getAttribute("rcardinality"));

			if (rcardinality == null) return false;

			if (!"Single".equalsIgnoreCase(rcardinality)) return false;

			// answers - w/ id
			Map<String, String> answerMap = new HashMap<String, String>();
			XPath answersPath = new DOMXPath(".//render_choice//response_label");
			List answers = answersPath.selectNodes(responseLidElement);
			for (Object oAnswer : answers)
			{
				Element answerElement = (Element) oAnswer;

				String id = StringUtil.trimToNull(answerElement.getAttribute("ident"));

				XPath answerMaterialPath = new DOMXPath(".//material//mattext");
				String answer = combineHits(answerMaterialPath, answerElement);
				if (answer != null)
				{
					answerMap.put(id, answer);
				}
			}

			boolean falseSeen = false;
			boolean trueSeen = false;
			if (answerMap.size() != 2) return false;
			for (String key : answerMap.keySet())
			{
				String value = answerMap.get(key);
				if ("true".equalsIgnoreCase(value))
					trueSeen = true;
				else if ("false".equalsIgnoreCase(value))
					falseSeen = true;
				else
					return false;
			}
			if (!falseSeen) return false;
			if (!trueSeen) return false;

			// score declaration - decvar
			XPath scoreDecVarPath = new DOMXPath("resprocessing/outcomes/decvar");
			Element scoreDecVarElement = (Element) scoreDecVarPath.selectSingleNode(item);

			if (scoreDecVarElement == null) return false;

			String vartype = StringUtil.trimToNull(scoreDecVarElement.getAttribute("vartype"));

			if ((vartype != null) && !("Integer".equalsIgnoreCase(vartype) || "Decimal".equalsIgnoreCase(vartype))) return false;

			// correct answer
			XPath respConditionPath = new DOMXPath("resprocessing/respcondition");
			List responses = respConditionPath.selectNodes(item);

			if (responses == null || responses.size() == 0) return false;

			for (Object oResponse : responses)
			{
				Element responseElement = (Element) oResponse;

				XPath responsePath = new DOMXPath("conditionvar/varequal");
				String responseText = StringUtil.trimToNull(responsePath.stringValueOf(responseElement));

				if (responseText != null)
				{
					if (!answerMap.containsKey(responseText)) return false;

					// score
					XPath setVarPath = new DOMXPath("setvar");
					Element setVarElement = (Element) setVarPath.selectSingleNode(responseElement);

					if (setVarElement == null) return false;

					if ("Set".equalsIgnoreCase(setVarElement.getAttribute("action")))
					{
						isTrue = Boolean.valueOf(answerMap.get(responseText));
						String pointsValue = StringUtil.trimToNull(setVarElement.getTextContent());

						if (pointsValue == null) return false;

						try
						{
							points = Float.valueOf(pointsValue);
						}
						catch (NumberFormatException e)
						{
							return false;
						}

						// feedback optional and can be Response, Solution, Hint
						XPath displayFeedbackPath = new DOMXPath("displayfeedback");
						Element displayFeedbackElement = (Element) displayFeedbackPath.selectSingleNode(responseElement);

						if (displayFeedbackElement == null) continue;

						String feedbackType = StringUtil.trimToNull(displayFeedbackElement.getAttribute("feedbacktype"));

						if (feedbackType == null || "Response".equalsIgnoreCase(feedbackType))
						{
							String linkRefId = StringUtil.trimToNull(displayFeedbackElement.getAttribute("linkrefid"));

							if (linkRefId == null) continue;

							XPath itemfeedbackPath = new DOMXPath("//itemfeedback[@ident='" + linkRefId + "']");
							Element feedbackElement = (Element) itemfeedbackPath.selectSingleNode(item);

							if (feedbackElement == null) continue;

							XPath feedbackTextPath = new DOMXPath("material/mattext");
							String feedbackText = StringUtil.trimToNull(feedbackTextPath.stringValueOf(feedbackElement));

							feedback = feedbackText;
						}
					}
				}
			}

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:TrueFalse");
			TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());

			// set the text
			String clean = HtmlHelper.cleanAndAssureAnchorTarget(presentation, true);
			question.getPresentation().setText(clean);

			// the correct answer
			tf.setCorrectAnswer(Boolean.toString(isTrue));

			// reason
			question.setExplainReason(Boolean.valueOf(rationale));

			// feedback
			if (feedback != null)
			{
				question.setFeedback(HtmlHelper.cleanAndAssureAnchorTarget(feedback, true));
			}

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			// add to the points average
			pointsAverage.add(points);

			return true;

		}
		catch (JaxenException e)
		{
			return false;
		}
	}

	/**
	 * Process the item if it is recognized as a Respondous multiple choice.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 */
	protected boolean processRespondousMultipleChoice(Element item, Pool pool, Average pointsAverage) throws AssessmentPermissionException
	{
		try
		{
			// the attributes of a multiple choice question
			String presentation = null;
			boolean rationale = false;
			String feedback = null;
			String hint = null;
			boolean survey = false;
			float points = 0.0f;
			float maxPoints = 0.0f;
			String externalId = null;
			boolean shuffle = true;
			boolean singleAnswer = true;

			externalId = StringUtil.trimToNull(item.getAttribute("ident"));

			// presentation text
			// Respondous is using the format - presentation/material/mattext
			XPath presentationTextPath = new DOMXPath("presentation/material/mattext");
			List presentationMaterialTexts = presentationTextPath.selectNodes(item);
			StringBuilder presentationTextBuilder = new StringBuilder();
			for (Object presentationMaterialText : presentationMaterialTexts)
			{
				Element presentationTextElement = (Element) presentationMaterialText;
				XPath matTextPath = new DOMXPath(".");
				String matText = StringUtil.trimToNull(matTextPath.stringValueOf(presentationTextElement));

				if (matText != null) presentationTextBuilder.append(matText);
			}
			presentation = presentationTextBuilder.toString();

			if (presentation == null)
			{
				// QTI format - presentation/flow/material/mattext
				presentationTextPath = new DOMXPath("presentation/flow/material/mattext");
				presentation = StringUtil.trimToNull(presentationTextPath.stringValueOf(item));
			}

			if (presentation == null) return false;
			
			// reponse_lid
			XPath reponseLidPath = new DOMXPath("presentation//response_lid");
			List responseLids = reponseLidPath.selectNodes(item);

			if ((responseLids.size() == 0) || (responseLids.size() > 1)) return false;

			Element responseLidElement = (Element) responseLids.get(0);
			String rcardinality = StringUtil.trimToNull(responseLidElement.getAttribute("rcardinality"));

			if (rcardinality == null) return false;

			if (!("Single".equalsIgnoreCase(rcardinality) || "Multiple".equalsIgnoreCase(rcardinality))) return false;

			if ("Multiple".equalsIgnoreCase(rcardinality)) singleAnswer = false;

			XPath shufflePath = new DOMXPath(".//render_choice/@shuffle");
			String shuffleValue = StringUtil.trimToNull(shufflePath.stringValueOf(responseLidElement));
			shuffle = "yes".equalsIgnoreCase(shuffleValue);

			
			// answers - w/ id
			Map<String, String> answerMap = new LinkedHashMap<String, String>();
			XPath answersPath = new DOMXPath(".//render_choice//response_label");
			List answers = answersPath.selectNodes(responseLidElement);
			for (Object oAnswer : answers)
			{
				Element answerElement = (Element) oAnswer;

				String id = StringUtil.trimToNull(answerElement.getAttribute("ident"));

				XPath answerMaterialPath = new DOMXPath(".//material//mattext");
				String answer = combineHits(answerMaterialPath, answerElement);
				if (answer != null)
				{
					answerMap.put(id, answer);
				}
				
				String rShuffle = StringUtil.trimToNull(answerElement.getAttribute("rshuffle"));
				if ("No".equalsIgnoreCase(rShuffle)) shuffle = false;
			}

			if (answerMap.size() < 2) return false;

			// score declaration - decvar
			XPath scoreDecVarPath = new DOMXPath("resprocessing/outcomes/decvar");
			Element scoreDecVarElement = (Element) scoreDecVarPath.selectSingleNode(item);

			if (scoreDecVarElement == null) return false;

			String vartype = StringUtil.trimToNull(scoreDecVarElement.getAttribute("vartype"));

			if ((vartype != null) && !("Integer".equalsIgnoreCase(vartype) || "Decimal".equalsIgnoreCase(vartype))) return false;

			String maxValue = StringUtil.trimToNull(scoreDecVarElement.getAttribute("maxvalue"));
			String minValue = StringUtil.trimToNull(scoreDecVarElement.getAttribute("minvalue"));

			if (!singleAnswer && ((maxValue == null) || (minValue == null))) return false;

			if (!singleAnswer)
			{
				try
				{
					maxPoints = Float.valueOf(maxValue);
				}
				catch (NumberFormatException e)
				{
					return false;
				}
			}

			// correct answer
			XPath respConditionPath = new DOMXPath("resprocessing/respcondition");
			List responses = respConditionPath.selectNodes(item);

			// correct answers
			Set<String> responseAnswers = new HashSet<String>();

			if (responses == null || responses.size() == 0) return false;

			for (Object oResponse : responses)
			{
				Element responseElement = (Element) oResponse;

				XPath responsePath = new DOMXPath("conditionvar/varequal");
				String responseText = StringUtil.trimToNull(responsePath.stringValueOf(responseElement));

				if (responseText != null)
				{
					if (!answerMap.containsKey(responseText)) return false;

					// score
					XPath setVarPath = new DOMXPath("setvar");
					Element setVarElement = (Element) setVarPath.selectSingleNode(responseElement);

					if (setVarElement == null) return false;

					if ("Set".equalsIgnoreCase(setVarElement.getAttribute("action")))
					{
						// this is the answer for multiple choice - single answer
						if (singleAnswer)
						{
							if (responseAnswers.size() > 0) return false;

							responseAnswers.add(responseText);
						}
						else
							return false;

						String pointsValue = StringUtil.trimToNull(setVarElement.getTextContent());

						if (pointsValue == null) return false;

						try
						{
							points = Float.valueOf(pointsValue);
						}
						catch (NumberFormatException e)
						{
							return false;
						}

						// feedback optional and can be Response, Solution, Hint
						XPath displayFeedbackPath = new DOMXPath("displayfeedback");
						Element displayFeedbackElement = (Element) displayFeedbackPath.selectSingleNode(responseElement);

						if (displayFeedbackElement == null) continue;

						String feedbackType = StringUtil.trimToNull(displayFeedbackElement.getAttribute("feedbacktype"));

						if (feedbackType == null || "Response".equalsIgnoreCase(feedbackType))
						{
							String linkRefId = StringUtil.trimToNull(displayFeedbackElement.getAttribute("linkrefid"));

							if (linkRefId == null) continue;

							XPath itemfeedbackPath = new DOMXPath("//itemfeedback[@ident='" + linkRefId + "']");
							Element feedbackElement = (Element) itemfeedbackPath.selectSingleNode(item);

							if (feedbackElement == null) continue;

							XPath feedbackTextPath = new DOMXPath("material/mattext");
							String feedbackText = StringUtil.trimToNull(feedbackTextPath.stringValueOf(feedbackElement));

							feedback = feedbackText;
						}
					}
					else if (!singleAnswer && "Add".equalsIgnoreCase(setVarElement.getAttribute("action")))
					{
						String pointsValue = StringUtil.trimToNull(setVarElement.getTextContent());

						if (pointsValue == null) return false;

						float resPoints = 0.0f;

						try
						{
							resPoints = Float.valueOf(pointsValue);
						}
						catch (NumberFormatException e)
						{
							return false;
						}

						if (resPoints <= 0) continue;

						responseAnswers.add(responseText);
						points += resPoints;

						// feedback optional and can be Response, Solution, Hint
						XPath displayFeedbackPath = new DOMXPath("displayfeedback");
						Element displayFeedbackElement = (Element) displayFeedbackPath.selectSingleNode(responseElement);

						if (displayFeedbackElement == null) continue;

						// only one feedback is added as feed back is repeated in Respondous
						if ((feedback != null) && (feedback.length() > 0)) continue;

						String feedbackType = StringUtil.trimToNull(displayFeedbackElement.getAttribute("feedbacktype"));

						if (feedbackType == null || "Response".equalsIgnoreCase(feedbackType))
						{
							String linkRefId = StringUtil.trimToNull(displayFeedbackElement.getAttribute("linkrefid"));

							if (linkRefId == null) continue;

							XPath itemfeedbackPath = new DOMXPath("//itemfeedback[@ident='" + linkRefId + "']");
							Element feedbackElement = (Element) itemfeedbackPath.selectSingleNode(item);

							if (feedbackElement == null) continue;

							XPath feedbackTextPath = new DOMXPath("material/mattext");
							String feedbackText = StringUtil.trimToNull(feedbackTextPath.stringValueOf(feedbackElement));

							feedback = feedbackText;
						}
					}
				}
			}

			if (responseAnswers.size() == 0) return false;

			if (!singleAnswer)
			{
				maxPoints = Float.valueOf(((float) Math.round(maxPoints * 100.0f)) / 100.0f);
				// points = Float.valueOf(((float) Math.round(points * 100.0f)) / 100.0f);

				/*
				 * if (maxPoints != points) return false;
				 */

				points = maxPoints;
			}

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:MultipleChoice");
			MultipleChoiceQuestionImpl mc = (MultipleChoiceQuestionImpl) (question.getTypeSpecificQuestion());

			// set the text
			String clean = HtmlHelper.cleanAndAssureAnchorTarget(presentation, true);
			question.getPresentation().setText(clean);

			// randomize
			mc.setShuffleChoices(Boolean.toString(shuffle));

			// single / multiple select
			mc.setSingleCorrect(Boolean.toString(singleAnswer));

			// set the choices
			List<String> choices = new ArrayList<String>();
			for (String key : answerMap.keySet())
			{
				String value = answerMap.get(key);

				// clean = HtmlHelper.clean(value);
				// choices.add(clean);
				choices.add(value.trim());
			}
			mc.setAnswerChoices(choices);

			List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choicesAuthored = mc.getChoicesAsAuthored();
			// corrects
			Set<Integer> correctAnswers = new HashSet<Integer>();

			for (String oCorrect : responseAnswers)
			{
				String correctKey = StringUtil.trimToNull(oCorrect);
				if (correctKey != null)
				{
					String correctValue = answerMap.get(correctKey);

					// find this answer
					for (int index = 0; index < choicesAuthored.size(); index++)
					{
						MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice choice = choicesAuthored.get(index);
						if (choice.getText().equals(correctValue.trim()))
						{
							// use this answer's id
							correctAnswers.add(Integer.valueOf(choice.getId()));
						}
					}
				}
			}

			// the correct answers
			mc.setCorrectAnswerSet(correctAnswers);

			// reason
			question.setExplainReason(Boolean.valueOf(rationale));

			// feedback
			if (feedback != null)
			{
				question.setFeedback(HtmlHelper.cleanAndAssureAnchorTarget(feedback, true));
			}

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			// add to the points average
			pointsAverage.add(points);

			return true;
		}
		catch (JaxenException e)
		{
			return false;
		}
	}

	/**
	 * Process the item if it is recognized as a Respondous Essay question.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 */
	protected boolean processRespondousEssay(Element item, Pool pool) throws AssessmentPermissionException
	{
		try
		{
			// the attributes of a survey question
			String presentation = null;
			String feedback = null;
			String externalId = null;

			externalId = StringUtil.trimToNull(item.getAttribute("ident"));

			// presentation text
			// Respondous is using the format - presentation/material/mattext
			XPath presentationTextPath = new DOMXPath("presentation/material/mattext");
			List presentationMaterialTexts = presentationTextPath.selectNodes(item);
			StringBuilder presentationTextBuilder = new StringBuilder();
			for (Object presentationMaterialText : presentationMaterialTexts)
			{
				Element presentationTextElement = (Element) presentationMaterialText;
				XPath matTextPath = new DOMXPath(".");
				String matText = StringUtil.trimToNull(matTextPath.stringValueOf(presentationTextElement));

				if (matText != null) presentationTextBuilder.append(matText);
			}
			presentation = presentationTextBuilder.toString();

			if (presentation == null)
			{
				// QTI format - presentation/flow/material/mattext
				presentationTextPath = new DOMXPath("presentation/flow/material/mattext");
				presentation = StringUtil.trimToNull(presentationTextPath.stringValueOf(item));
			}

			if (presentation == null) return false;

			// reponse_str/response_fib
			XPath renderFibPath = new DOMXPath("presentation/response_str/render_fib");
			Element responseFib = (Element) renderFibPath.selectSingleNode(item);

			if (responseFib == null) return false;

			Attr promptAttr = responseFib.getAttributeNode("prompt");
			Attr rowsAttr = responseFib.getAttributeNode("rows");
			Attr columnsAttr = responseFib.getAttributeNode("columns");

			if (promptAttr == null || rowsAttr == null || columnsAttr == null) return false;

			if (!"Box".equalsIgnoreCase(promptAttr.getValue().trim())) return false;

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:Essay");
			EssayQuestionImpl e = (EssayQuestionImpl) (question.getTypeSpecificQuestion());

			String clean = HtmlHelper.cleanAndAssureAnchorTarget(presentation, true);

			question.getPresentation().setText(clean);

			// type
			e.setSubmissionType(EssayQuestionImpl.SubmissionType.inline);

			XPath itemfeedbackPath = new DOMXPath("itemfeedback/material/mattext");
			feedback = StringUtil.trimToNull(itemfeedbackPath.stringValueOf(item));

			// add feedback
			if (StringUtil.trimToNull(feedback) != null)
			{
				question.setFeedback(HtmlHelper.cleanAndAssureAnchorTarget(feedback, true));
			}

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			return true;
		}
		catch (JaxenException e)
		{
			return false;
		}
	}

	/**
	 * Process this item if it is recognized as a Respondous Fill in the blank.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 * @return true if successfully recognized and processed, false if not.
	 */
	protected boolean processRespondousFillIn(Element item, Pool pool, Average pointsAverage) throws AssessmentPermissionException
	{
		// the attributes of a Fill In the Blank question
		boolean caseSensitive = false;
		boolean mutuallyExclusive = false;
		String presentation = null;
		float points = 0.0f;
		String feedback = null;
		boolean isResponseTextual = false;

		String externalId = null;
		List<String> answers = new ArrayList<String>();

		try
		{
			// identifier
			externalId = StringUtil.trimToNull(item.getAttribute("ident"));

			// presentation text
			// Respondous is using the format - presentation/material/mattext
			XPath presentationTextPath = new DOMXPath("presentation/material/mattext");
			List presentationMaterialTexts = presentationTextPath.selectNodes(item);
			StringBuilder presentationTextBuilder = new StringBuilder();
			for (Object presentationMaterialText : presentationMaterialTexts)
			{
				Element presentationTextElement = (Element) presentationMaterialText;
				XPath matTextPath = new DOMXPath(".");
				String matText = StringUtil.trimToNull(matTextPath.stringValueOf(presentationTextElement));

				if (matText != null) presentationTextBuilder.append(matText);
			}
			presentation = presentationTextBuilder.toString();

			if (presentation == null)
			{
				// QTI format - presentation/flow/material/mattext
				presentationTextPath = new DOMXPath("presentation/flow/material/mattext");
				presentation = StringUtil.trimToNull(presentationTextPath.stringValueOf(item));
			}

			if (presentation == null) return false;

			// reponse_str/response_fib
			XPath renderFibPath = new DOMXPath("presentation/response_str/render_fib");
			Element responseFib = (Element) renderFibPath.selectSingleNode(item);

			if (responseFib == null) return false;

			Attr promptAttr = responseFib.getAttributeNode("prompt");
			Attr rowsAttr = responseFib.getAttributeNode("rows");
			Attr columnsAttr = responseFib.getAttributeNode("columns");

			if (promptAttr == null || rowsAttr != null || columnsAttr != null) return false;

			if (!"Box".equalsIgnoreCase(promptAttr.getValue().trim())) return false;

			// score declaration - decvar
			XPath scoreDecVarPath = new DOMXPath("resprocessing/outcomes/decvar");
			Element scoreDecVarElement = (Element) scoreDecVarPath.selectSingleNode(item);

			if (scoreDecVarElement == null) return false;

			String vartype = StringUtil.trimToNull(scoreDecVarElement.getAttribute("vartype"));

			if ((vartype != null) && !("Integer".equalsIgnoreCase(vartype) || "Decimal".equalsIgnoreCase(vartype))) return false;

			String maxValue = StringUtil.trimToNull(scoreDecVarElement.getAttribute("maxvalue"));
			String minValue = StringUtil.trimToNull(scoreDecVarElement.getAttribute("minvalue"));

			if (maxValue == null || minValue == null) return false;

			try
			{
				points = Float.valueOf(maxValue);
			}
			catch (NumberFormatException e)
			{
				return false;
			}

			// correct answer
			XPath respConditionPath = new DOMXPath("resprocessing/respcondition");
			List responses = respConditionPath.selectNodes(item);

			if (responses == null || responses.size() == 0) return false;

			for (Object oResponse : responses)
			{
				Element responseElement = (Element) oResponse;

				XPath responsePath = new DOMXPath("conditionvar/varequal");
				String responseText = StringUtil.trimToNull(responsePath.stringValueOf(responseElement));

				if (responseText != null)
				{
					// score
					XPath setVarPath = new DOMXPath("setvar");
					Element setVarElement = (Element) setVarPath.selectSingleNode(responseElement);

					if (setVarElement == null) return false;

					if ("Add".equalsIgnoreCase(setVarElement.getAttribute("action")))
					{
						String pointsValue = StringUtil.trimToNull(setVarElement.getTextContent());

						if (pointsValue == null) return false;

						answers.add(responseText.trim());

						// feedback optional and can be Response, Solution, Hint
						XPath displayFeedbackPath = new DOMXPath("displayfeedback");
						Element displayFeedbackElement = (Element) displayFeedbackPath.selectSingleNode(responseElement);

						if (displayFeedbackElement == null) continue;

						String feedbackType = StringUtil.trimToNull(displayFeedbackElement.getAttribute("feedbacktype"));

						if (feedbackType == null || "Response".equalsIgnoreCase(feedbackType))
						{
							String linkRefId = StringUtil.trimToNull(displayFeedbackElement.getAttribute("linkrefid"));

							if (linkRefId == null) continue;

							XPath itemfeedbackPath = new DOMXPath("//itemfeedback[@ident='" + linkRefId + "']");
							Element feedbackElement = (Element) itemfeedbackPath.selectSingleNode(item);

							if (feedbackElement == null) continue;

							XPath feedbackTextPath = new DOMXPath("material/mattext");
							String feedbackText = StringUtil.trimToNull(feedbackTextPath.stringValueOf(feedbackElement));

							feedback = feedbackText;
						}
					}
				}
			}

			if (answers.size() == 0) return false;

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:FillBlanks");
			FillBlanksQuestionImpl f = (FillBlanksQuestionImpl) (question.getTypeSpecificQuestion());

			// case sensitive
			f.setCaseSensitive(Boolean.FALSE.toString());

			// mutually exclusive
			f.setAnyOrder(Boolean.FALSE.toString());

			StringBuffer buildAnswers = new StringBuffer();
			buildAnswers.append("{");
			for (String answer : answers)
			{
				if (!isResponseTextual)
				{
					try
					{
						Float.parseFloat(answer);
					}
					catch (NumberFormatException e)
					{
						isResponseTextual = true;
					}
				}
				buildAnswers.append(answer);
				buildAnswers.append("|");
			}
			buildAnswers.replace(buildAnswers.length() - 1, buildAnswers.length(), "}");
			String questionText = presentation.concat(buildAnswers.toString());

			String clean = HtmlHelper.cleanAndAssureAnchorTarget(questionText, true);

			f.setText(clean);

			// text or numeric
			f.setResponseTextual(Boolean.toString(isResponseTextual));

			// add feedback
			if (StringUtil.trimToNull(feedback) != null)
			{
				question.setFeedback(HtmlHelper.cleanAndAssureAnchorTarget(feedback, true));
			}

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			// add to the points average
			pointsAverage.add(points);

			return true;
		}
		catch (JaxenException e)
		{
			return false;
		}
	}

	/**
	 * Process this item if it is recognized as a Respondous Matching question.
	 * 
	 * @param item
	 *        The QTI item from the QTI file DOM.
	 * @param pool
	 *        The pool to add the question to.
	 * @param pointsAverage
	 *        A running average to contribute the question's point value to for the pool.
	 * @return true if successfully recognized and processed, false if not.
	 */
	protected boolean processRespondousMatching(Element item, Pool pool, Average pointsAverage) throws AssessmentPermissionException
	{
		String externalId = null;
		String presentation = null;
		String feedback = null;
		float points = 0.0f;

		try
		{
			// identifier
			externalId = StringUtil.trimToNull(item.getAttribute("ident"));

			// presentation text
			// Respondous is using the format - presentation/material/mattext
			XPath presentationTextPath = new DOMXPath("presentation/material/mattext");
			List presentationMaterialTexts = presentationTextPath.selectNodes(item);
			StringBuilder presentationTextBuilder = new StringBuilder();
			for (Object presentationMaterialText : presentationMaterialTexts)
			{
				Element presentationTextElement = (Element) presentationMaterialText;
				XPath matTextPath = new DOMXPath(".");
				String matText = StringUtil.trimToNull(matTextPath.stringValueOf(presentationTextElement));

				if (matText != null) presentationTextBuilder.append(matText);
			}
			presentation = presentationTextBuilder.toString();

			if (presentation == null)
			{
				// QTI format - presentation/flow/material/mattext
				presentationTextPath = new DOMXPath("presentation/flow/material/mattext");
				presentation = StringUtil.trimToNull(presentationTextPath.stringValueOf(item));
			}

			if (presentation == null) return false;

			// reponse_lid
			XPath reponseLidPath = new DOMXPath("presentation//response_lid");
			List responseLids = reponseLidPath.selectNodes(item);

			if (responseLids.size() == 0) return false;

			Map<String, String> matchPresentations = new LinkedHashMap<String, String>();
			Map<String, Object> matchChoices = new HashMap<String, Object>();

			for (Object responseLid : responseLids)
			{
				Element responseLidElement = (Element) responseLid;

				String identifier = responseLidElement.getAttribute("ident");

				if (StringUtil.trimToNull(identifier) == null) continue;

				XPath matchPresentationPath = new DOMXPath("material/mattext");
				String matchPresentationText = StringUtil.trimToNull(matchPresentationPath.stringValueOf(responseLidElement));

				matchPresentations.put(identifier, matchPresentationText);

				// response_label
				XPath reponseChoicePath = new DOMXPath("render_choice/response_label");
				List reponseChoices = reponseChoicePath.selectNodes(responseLidElement);

				Map<String, String> answerChoices = new HashMap<String, String>();
				for (Object reponseChoice : reponseChoices)
				{
					Element reponseChoiceElement = (Element) reponseChoice;

					String responseChoiceId = reponseChoiceElement.getAttribute("ident");

					if (StringUtil.trimToNull(responseChoiceId) == null) continue;

					XPath choicePresentation = new DOMXPath("material/mattext");
					String matchChoicesText = StringUtil.trimToNull(choicePresentation.stringValueOf(reponseChoiceElement));

					if (StringUtil.trimToNull(matchChoicesText) == null) continue;

					answerChoices.put(responseChoiceId, matchChoicesText);
				}
				matchChoices.put(identifier, answerChoices);
			}

			Map<String, String> matchAnswers = new HashMap<String, String>();

			for (String matchPresId : matchPresentations.keySet())
			{
				// resprocessing
				XPath reponseAnswerPath = new DOMXPath("resprocessing//conditionvar/varequal[@respident='" + matchPresId + "']");
				List reponseAnswers = reponseAnswerPath.selectNodes(item);

				for (Object answer : reponseAnswers)
				{
					Element answerElement = (Element) answer;

					if (answerElement == null) continue;

					XPath setvarPath = new DOMXPath("../../setvar");
					Element setvarElement = (Element) setvarPath.selectSingleNode(answerElement);

					if (setvarElement == null) continue;

					if (!"setvar".equalsIgnoreCase(setvarElement.getNodeName())) continue;

					if (!"Respondus_Correct".equalsIgnoreCase(setvarElement.getAttribute("varname"))) continue;

					matchAnswers.put(matchPresId, answerElement.getTextContent());
				}
			}

			XPath pointsPath = new DOMXPath("resprocessing/outcomes/decvar[@varname='Respondus_Correct']/@maxvalue");
			String pointsValue = StringUtil.trimToNull(pointsPath.stringValueOf(item));

			try
			{
				if (pointsValue != null) points = Float.valueOf(pointsValue);
			}
			catch (NumberFormatException e)
			{
				return false;
			}

			if (matchAnswers.size() != matchPresentations.size()) return false;

			// create the question
			Question question = this.questionService.newQuestion(pool, "mneme:Match");
			MatchQuestionImpl m = (MatchQuestionImpl) (question.getTypeSpecificQuestion());

			String clean = HtmlHelper.cleanAndAssureAnchorTarget(presentation, true);

			question.getPresentation().setText(clean);

			m.consolidate("INIT:" + matchPresentations.size());

			// set the pair values
			List<MatchQuestionImpl.MatchQuestionPair> pairs = m.getPairs();
			String value;
			int index = 0;
			for (String key : matchPresentations.keySet())
			{
				clean = HtmlHelper.cleanAndAssureAnchorTarget(matchPresentations.get(key), true);
				pairs.get(index).setMatch(clean);

				Map choices = (Map) matchChoices.get(key);
				value = (String) choices.get(matchAnswers.get(key));

				if (StringUtil.trimToNull(value) == null) return false;

				clean = HtmlHelper.cleanAndAssureAnchorTarget(value, true);
				pairs.get(index).setChoice(clean);

				index++;
			}

			XPath itemfeedbackPath = new DOMXPath("itemfeedback/material/mattext");
			feedback = StringUtil.trimToNull(itemfeedbackPath.stringValueOf(item));

			if (feedback != null)
			{
				question.setFeedback(HtmlHelper.cleanAndAssureAnchorTarget(feedback, true));
			}

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			// add to the points average
			pointsAverage.add(points);

			return true;
		}
		catch (JaxenException e)
		{
			return false;
		}
	}
}
