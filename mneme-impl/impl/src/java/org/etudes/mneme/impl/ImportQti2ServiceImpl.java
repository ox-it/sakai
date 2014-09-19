/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/ImportQti2ServiceImpl.java $
 * $Id: ImportQti2ServiceImpl.java 8562 2014-08-30 06:07:15Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2013, 2014 Etudes, Inc.
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

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.ImportQti2Service;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolDraw;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionGrouping;
import org.etudes.mneme.api.QuestionPick;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.ReviewShowCorrect;
import org.etudes.mneme.api.ReviewTiming;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.impl.EssayQuestionImpl.SubmissionType;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * <p>
 * ImportQtiServiceImpl implements ImportQtiService
 * </p>
 */
public class ImportQti2ServiceImpl implements ImportQti2Service
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

	public static final int MAX_NAME_LENGTH = 150;

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ImportQti2ServiceImpl.class);

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService = null;

	/** Dependency: AttachmentService */
	protected AttachmentService attachmentService = null;

	/** Messages bundle name. */
	protected String bundle = null;

	/** Dependency: EntityManager */
	protected EntityManager entityManager = null;

	/** Dependency: EventTrackingService */
	protected EventTrackingService eventTrackingService = null;

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
	public void importPool(Document doc, String context, String unzipBackUpLocation) throws AssessmentPermissionException
	{
		if ((doc == null) || (!doc.hasChildNodes())) return;

		// get a name for the pool
		Average pointsAverage = new Average();
		HashMap<String, Question> allQuestions = new HashMap<String, Question>();
		HashMap<String, String> allQuestionPoints = new HashMap<String, String>();
		HashMap<String, Pool> allPools = new HashMap<String, Pool>();

		// 1. Create one defaultPool for all questions
		Pool pool = this.poolService.newPool(context);
		pool.setTitle(findPoolTitle(context, "", doc));

		String poolDescription = findPoolDescription(doc);
		pool.setDescription(poolDescription);
		allPools.put("defaultPool", pool);

		// 2. Read questions of type item from manifest file
		try
		{
			// if etudes export package has pool resources
			XPath poolPath = new DOMXPath("//resources/resource[starts-with(@identifier,'POOL')]");

			List<Element> poolItems = poolPath.selectNodes(doc);
			for (Element poolItem : poolItems)
			{
				String pId = poolItem.getAttribute("identifier");
				pId = pId.replace("POOL", "");

				if (!allPools.containsKey(pId))
				{
					Pool p = this.poolService.newPool(context);
					p.setTitle(findPoolTitle(context, poolItem.getAttribute("title"), doc));
					allPools.put(pId, p);
				}
			}

			XPath itemPath = new DOMXPath("//*[contains(local-name(),'resource')]");
			List<Element> items = itemPath.selectNodes(doc);

			for (Element item : items)
			{
				String type = item.getAttribute("type");
				if (type == null || !type.startsWith("imsqti_item_")) continue;

				// read href value
				String fileLocation = item.getAttribute("href");
				if ("".equals(fileLocation)) continue;

				// read Xml file and create question
				Question question = null;
				try
				{
					String baseName = "";
					fileLocation = fileLocation.replace("\\", "/");
					if (fileLocation.lastIndexOf("/") != -1) baseName = "/"+ fileLocation.substring(0,fileLocation.lastIndexOf("/"));

					question = processQuestionItemFile(allPools, allQuestions, context, doc, item, allQuestionPoints, pointsAverage,
							unzipBackUpLocation, fileLocation, baseName);
				}
				catch (Exception e)
				{
					M_log.debug(e.getMessage());
					continue;
				}
			}

			// 3. save pool
			// pool.setPointsEdit(pointsAverage.getAverage());
			Iterator<String> poolIterator = allPools.keySet().iterator();
			while (poolIterator.hasNext())
			{
				String poolId = poolIterator.next();
				Pool p = allPools.get(poolId);
				if (p.getNumQuestions().intValue() == 0) continue;
				this.poolService.savePool(p);
			}

			// 4. then later read assessments and integrate with gradebook and save
			String rights = findRightsInformation(doc);
			int testCount = 0;
			for (Element testItem : items)
			{
				String type = testItem.getAttribute("type");
				if (type == null || !type.startsWith("imsqti_test_")) continue;

				// read href value
				String fileLocation = testItem.getAttribute("href");
				if ("".equals(fileLocation)) continue;
				
				String baseName = "";
				fileLocation = fileLocation.replace("\\", "/");
				if (fileLocation.lastIndexOf("/") != -1) baseName = "/"+ fileLocation.substring(0,fileLocation.lastIndexOf("/"));
				
				processAssessmentFiles(context, testItem, unzipBackUpLocation, fileLocation, baseName, rights, allQuestions, allQuestionPoints, allPools);
				testCount++;
			}
			
			// create one default test for the pool just created if manifest has no resource of imsqi_test type.
			if (testCount == 0)
				createDefaultAssessment(context, allPools, allQuestions, allQuestionPoints);

		}
		catch (JaxenException e)
		{
			M_log.warn(e.toString());
		}
		catch (Exception e)
		{
			M_log.warn(e.toString());
		}
	}

	/**
	 * If there are no resource for test then create one default assessment
	 * @param context
	 * @param allPools
	 * @param allQuestions
	 * @param allQuestionPoints
	 * @return
	 * @throws Exception
	 */
	private Assessment createDefaultAssessment(String context, HashMap<String, Pool> allPools, HashMap<String, Question> allQuestions,
			HashMap<String, String> allQuestionPoints) throws Exception
	{
		if (allPools == null || allPools.size() == 0) return null;

		// create assessment
		Assessment test = assessmentService.newAssessment(context);
		test.setType(AssessmentType.test);
		Part part = test.getParts().addPart();
		part.setTitle("");

		Boolean autoRelease = null;
		int qCount = 0;
		String title = "untitled (import QTI)";
		for (Iterator<String> i = allQuestions.keySet().iterator(); i.hasNext();)
		{
			String key = i.next();
			Question question = allQuestions.get(key);

			// score
			String points = "1";
			if (allQuestionPoints.containsKey(key)) points = allQuestionPoints.get(key);
			QuestionPick questionPick = part.addPickDetail(question);
			questionPick.setPoints(Float.parseFloat(points));
			
			if ("mneme:Essay".equals(question.getType())) autoRelease = new Boolean(false);
			
			qCount++;
			if (qCount == allQuestions.size())
			{
				title = question.getPool().getTitle();
			}
		}
		test.setTitle(title);
		
		// auto release of grading
		if (autoRelease != null) test.getGrading().setAutoRelease(autoRelease);

		test.getGrading().setGradebookIntegration(Boolean.TRUE);

		assessmentService.saveAssessment(test);
		return test;
	}
	
	/**
	 * Read assessment.xml file and create an assessment with all of its settings.
	 * 
	 * @param context
	 * @param testItem
	 * @param unzipBackUpLocation
	 * @param fileLocation
	 * @param allQuestions
	 * @return
	 */
	private Assessment processAssessmentFiles(String context, Element testItem, String unzipBackUpLocation, String fileLocation, String baseName, String rights, 
			HashMap<String, Question> allQuestions, HashMap<String, String> allQuestionPoints, HashMap<String, Pool> allPools) throws Exception
	{
		Assessment test = null;
		int partCount = 1;
		Boolean allLikert = null;
		Boolean autoRelease = null;
		boolean randomAccess = false;
		String layout = "question";
		boolean partNumbering = true;
		
		// Read Assessment.Xml file
		Document contentsDOM = Xml.readDocument(unzipBackUpLocation + File.separator + fileLocation);
		if (contentsDOM == null) return null;

		Element rootAssessment = contentsDOM.getDocumentElement();
		String title = rootAssessment.getAttribute("title");

		// create assessment
		test = assessmentService.newAssessment(context);
		test.setTitle(findAssessmentTitle(context, title));

		// read identifier to find survey, assignment or test
		AssessmentType testType = findAssessmentType(testItem);
		test.setType(testType);

		// read title and description from testItem
		String description = findDescription(testItem);
		if (description != null && description.length() > 0)
		{
			description = processInstructionsEmbedMedia(unzipBackUpLocation, context, description, new ArrayList<String>());
			test.getPresentation().setText(description);
		}

		// time limit
		XPath timeLimitPath = new DOMXPath("/assessmentTest/timeLimits");
		Element timeElement = (Element) timeLimitPath.selectSingleNode(contentsDOM);
		String maxTime = (timeElement != null) ? timeElement.getAttribute("maxTime") : "";

		if (maxTime.length() > 0)
		{
			test.setHasTimeLimit(true);
			if (maxTime.indexOf(".") != -1) maxTime = maxTime.substring(0, maxTime.indexOf("."));
			test.setTimeLimit(new Long(maxTime));
		}

		// settings from outcome declaration
		HashMap<String, String> settings = processOutcomeDeclaration(contentsDOM);
		test = processSettingsfromOutcomeDeclaration (settings, test);
			
		//part numbering
		if (settings.containsKey("PartNumbering")) 
			partNumbering = new Boolean(settings.get("PartNumbering")).booleanValue();
	
		// final message
		XPath finalMessagePath = new DOMXPath("/assessmentTest/testFeedback[@access='atEnd']");
		String finalMessage = finalMessagePath.stringValueOf(contentsDOM);
		if (finalMessage != null && finalMessage.length() > 0)
			test.getSubmitPresentation().setText(finalMessage);		

		// add parts and questions
		List<Element> partElements = new ArrayList();
		XPath partsPath = new DOMXPath("/assessmentTest/testPart");
		partElements = partsPath.selectNodes(contentsDOM);

		// <itemSessionControl maxAttempts="2" allowReview="true" showFeedback="true">
		test = processSettingsfromItemSessionElement(partElements, test);
		
		if (partElements != null && partElements.size() > 0)
		{
			// partNumbering from outcome declaration
			test.getParts().setContinuousNumbering(partNumbering);
			
			for (Element partElement : partElements)
			{
				if (partElement == null) continue;
				String navigationMode = partElement.getAttribute("navigationMode");
				if (!"linear".equalsIgnoreCase(navigationMode))	randomAccess = true;
				
				// section if more than one section bring as etudes parts
				List<Element> sectionElements = new ArrayList();
				XPath sectionPath = new DOMXPath("assessmentSection");
				sectionElements = sectionPath.selectNodes(partElement);

				if (sectionElements == null || sectionElements.size() == 0) continue;
				for (Element sectionElement : sectionElements)
				{
					String sectionIdentifier = sectionElement.getAttribute("identifier");
					// rubric
					XPath rubricPath = new DOMXPath("rubricBlock");
					Element rubricElement = (Element) rubricPath.selectSingleNode(sectionElement);

					// if part has no questions then skip
					XPath ItemRefPath = new DOMXPath("assessmentItemRef");
					List<Element> refElements = ItemRefPath.selectNodes(sectionElement);
					if (refElements == null || refElements.size() == 0)
					{
						if (rubricElement != null)
						{
							String partInstructions = normalizeElementBody(contentsDOM, rubricElement);
							partInstructions = processInstructionsEmbedMedia(unzipBackUpLocation.concat(baseName), context, partInstructions, new ArrayList<String>());
							test.getPresentation().setText(partInstructions);
						}
						continue;
					}

					// create Part
					Part part = test.getParts().addPart();
					String partTitle = sectionElement.getAttribute("title");
					partTitle = (partTitle != null && partTitle.length() > 0) ? partTitle : "";
					part.setTitle(partTitle);

					// rubric as Part Instructions
					if (rubricElement != null)
					{
						String partInstructions = normalizeElementBody(contentsDOM, rubricElement);
						partInstructions = processInstructionsEmbedMedia(unzipBackUpLocation.concat(baseName), context, partInstructions, new ArrayList<String>());
						part.getPresentation().setText(partInstructions);
					}

					// orderingShuffle is true then randomize the order of questions
					String orderingShuffle = findPartOrderingShuffle(sectionElement);
					part.setRandomize(new Boolean(orderingShuffle));

					boolean randomDraw = false;
					int totalPartQuestions = 0;
					List<Element> requiredQuestions = new ArrayList<Element>();

					XPath randomSelectionPath = new DOMXPath(".//assessmentSection[@identifier='" + sectionIdentifier + "']/selection");
					Element selection = (Element) randomSelectionPath.selectSingleNode(contentsDOM);

					// look for selection if yes then there is random draw
					if (selection != null)
					{
						randomDraw = true;
						randomAccess = randomAccess || randomDraw;

						// <selection select="2"/>
						String randomSelectionCount = selection.getAttribute("select");
						totalPartQuestions = Integer.parseInt(randomSelectionCount);

						// select assessmentItemRef with required clause and add as question pick
						XPath assessmentItemRefPath = new DOMXPath(".//assessmentSection[@identifier='" + sectionIdentifier
								+ "']/assessmentItemRef[@required='true']");
						requiredQuestions = assessmentItemRefPath.selectNodes(contentsDOM);
					}
					else
					{
						// add questions to assessment
						XPath assessmentItemRefPath = new DOMXPath(".//assessmentSection[@identifier='" + sectionIdentifier + "']/assessmentItemRef");
						requiredQuestions = assessmentItemRefPath.selectNodes(contentsDOM);
					} // else end

					for (Element questionElement : requiredQuestions)
					{
						Question question = null;
						String identifer = questionElement.getAttribute("identifier");
						String href = questionElement.getAttribute("href");
						question = getQuestionfromAllQuestions(identifer, unzipBackUpLocation.concat(baseName) + File.separator +href, allQuestions);
						if (question == null) continue;

						// all likert questions then survey
						if ("mneme:LikertScale".equals(question.getType()) || question.getIsSurvey())
						{
							if (allLikert == null)
								allLikert = new Boolean(true);
							else
								allLikert = new Boolean(allLikert.booleanValue() && true);
						}
						else
						{
							if (allLikert == null)
								allLikert = new Boolean(false);
							else
								allLikert = new Boolean(allLikert.booleanValue() && false);
						}
						
						if ("mneme:Essay".equals(question.getType())) autoRelease = new Boolean(false);

						// score
						String points = "1";
						if (allQuestionPoints.containsKey(identifer)) points = allQuestionPoints.get(identifer);
						QuestionPick questionPick = part.addPickDetail(question);
						questionPick.setPoints(Float.parseFloat(points));
					}

					// add Random detail
					int randomCount = totalPartQuestions - requiredQuestions.size();
					part = buildRandomDrawPart(randomDraw, randomCount, context, sectionIdentifier, unzipBackUpLocation.concat(baseName), contentsDOM, part, allPools, allQuestions,
							allQuestionPoints);

				} // section element for
			} // part element for
		}

		// if all likert questions and not already a survey then mark it as survey
		if (testType != AssessmentType.survey && (allLikert != null && allLikert.booleanValue() == true)) test.setType(AssessmentType.survey);

		// random access
		if (randomAccess) test.setRandomAccess(new Boolean(true));	
		else test.setRandomAccess(new Boolean(false));
		
		// rights information
//		if (rights != null) test.getPresentation().setText(test.getPresentation().getText().concat(rights));
			
		// auto release of grading
		if (autoRelease != null) test.getGrading().setAutoRelease(autoRelease);

		// send to gradebook
		if (test.getType() != AssessmentType.survey)
			test.getGrading().setGradebookIntegration(Boolean.TRUE);
		
		assessmentService.saveAssessment(test);
		return test;
	}

	/**
	 * read all outcome declarations in a hashmap.
	 * @param contentsDOM
	 * @return
	 */
	private HashMap<String, String> processOutcomeDeclaration(Document contentsDOM)
	{
		HashMap<String, String> settings = new HashMap<String, String>();
		try
		{
			XPath outcomes = new DOMXPath("/assessmentTest/outcomeDeclaration");
			List<Element> outcomeElements = outcomes.selectNodes(contentsDOM);

			for (Element e : outcomeElements)
			{
				String key = e.getAttribute("identifier");
				String value = e.getTextContent();
				if (key == null || key.length() == 0 || value == null || value.trim().length() == 0) continue;
				settings.put(key, value.trim());
			}
		}
		catch (Exception ex)
		{
		}
		return settings;
	}
	
	/**
	 * 
	 * @param partElements
	 * @param test
	 * @return
	 * @throws Exception
	 */
	private Assessment processSettingsfromItemSessionElement(List<Element> partElements, Assessment test) throws Exception
	{
		String tries = "";

		if (partElements != null && partElements.size() > 0)
		{
			Element partElement = partElements.get(0);
			XPath itemSessionControlPath = new DOMXPath("itemSessionControl");
			Element itemSessionControlElement = (Element) itemSessionControlPath.selectSingleNode(partElement);

			if (itemSessionControlElement != null)
			{
				tries = (itemSessionControlElement.getAttribute("maxAttempts") != null) ? itemSessionControlElement.getAttribute("maxAttempts") : "";
				// tries
				if (!tries.equals(""))
				{
					tries = tries.trim();
					test.setHasTriesLimit(true);
					test.setTries(new Integer(tries));
				}

				// review options
				if ("false".equalsIgnoreCase(itemSessionControlElement.getAttribute("allowReview").trim()))
					test.getReview().setTiming(ReviewTiming.never);
				else
					test.getReview().setTiming(ReviewTiming.submitted);

				// show hints
				if (!("").equals(itemSessionControlElement.getAttribute("showFeedback").trim()))
					test.setShowHints(new Boolean(itemSessionControlElement.getAttribute("showFeedback")));

				// show model answer
				if (!("").equals(itemSessionControlElement.getAttribute("showSolution").trim()))
					test.setShowModelAnswer(new Boolean(itemSessionControlElement.getAttribute("showSolution")));
			}
		}
		return test;
	}
	
	/**
	 * Assign settings based on outcome declaration
	 * @param settings
	 * @param test
	 * @return
	 */
	private Assessment processSettingsfromOutcomeDeclaration(HashMap<String, String> settings, Assessment test)
	{
		if (settings.size() == 0) return test;
		// passing percentage
		if (settings.containsKey("PASSFACTOR"))
		{
			test.setMinScore(new Integer(settings.get("PASSFACTOR")));
			test.setMinScoreSet(new Boolean(true));
		}

		//review options
		if (settings.containsKey("ReviewShowSummary")) test.getReview().setShowSummary(new Boolean(true));
		
		if (settings.containsKey("ReviewShowFeedback"))
		{
			M_log.debug("review feedback true");
			test.getReview().setShowFeedback(new Boolean(true));
		}
		
		if (settings.containsKey("ReviewCorrectAnswer"))
		{
			String correctAnswer = settings.get("ReviewCorrectAnswer");
			if ("incorrect_only".equalsIgnoreCase(correctAnswer))test.getReview().setShowCorrectAnswer(ReviewShowCorrect.incorrect_only);
			else if ("correct_only".equalsIgnoreCase(correctAnswer))test.getReview().setShowCorrectAnswer(ReviewShowCorrect.correct_only);
			else if ("no".equalsIgnoreCase(correctAnswer))test.getReview().setShowCorrectAnswer(ReviewShowCorrect.no);
		}
		
		// AnonymousGrading
		if (settings.containsKey("AnonymousGrading")) test.getGrading().setAnonymous(new Boolean(settings.get("AnonymousGrading")));

		// layout
		if (settings.containsKey("QuestionLayout"))
		{
			String layout = settings.get("QuestionLayout");
			if ("part".equalsIgnoreCase(layout))
				test.setQuestionGrouping(QuestionGrouping.part);
			else if ("assessment".equalsIgnoreCase(layout))
				test.setQuestionGrouping(QuestionGrouping.assessment);
			else
				test.setQuestionGrouping(QuestionGrouping.question);
		}
		else
			test.setQuestionGrouping(QuestionGrouping.question);

		// pledge
		if (settings.containsKey("HonorPledge")) test.setRequireHonorPledge(new Boolean(settings.get("HonorPledge")));
		
		// ShuffleChoicesOverride
		if (settings.containsKey("ShuffleChoicesOverride")) test.setShuffleChoicesOverride(new Boolean(true));
		
		//open date
		if (settings.containsKey("OpenDate"))
				 test.getDates().setOpenDate(getDateFromString(settings.get("OpenDate")));
			
		if (settings.containsKey("HideUntilOpen"))
			 test.getDates().setHideUntilOpen(new Boolean(settings.get("HideUntilOpen")));
			
		if (settings.containsKey("DueDate"))
			 test.getDates().setDueDate(getDateFromString(settings.get("DueDate")));
		
		if (settings.containsKey("AcceptUntil"))
			 test.getDates().setAcceptUntilDate(getDateFromString(settings.get("AcceptUntil")));
		
		return test;
	}
	
	/**
	 * Find Attachments and bring them in. Resource file elements which are not in embedMedia list are attachments.
	 * 
	 * @param resourceItem
	 * @param context
	 * @param question
	 * @param embedMedia
	 * @return
	 */
	private Question processQuestionAttachments(Element resourceItem, String context, Question question, List<String> embedMedia, String unzipLocation)
	{
		if (question == null) return question;
		List<Reference> currAttachments = question.getPresentation().getAttachments();

		NodeList attachments = resourceItem.getElementsByTagName("file");
		for (int i = 0; i < attachments.getLength(); i++)
		{
			Element attach = (Element) attachments.item(i);
			String hrefLocation = attach.getAttribute("href");
			if (!findInEmbedMedia(hrefLocation, embedMedia))
			{
				Reference attachRef = transferEmbeddedData(unzipLocation + File.separator + hrefLocation, hrefLocation, context);
				if (attachRef == null) return question;
				if (currAttachments == null) currAttachments = new ArrayList<Reference>();
				currAttachments.add(attachRef);
			}
		}
		question.getPresentation().setAttachments(currAttachments);
		return question;
	}

	/**
	 * Read Question.xml file and process it to create a question.
	 * 
	 * @param pool
	 * @param context
	 * @param doc
	 * @param resourceItem
	 * @param allQuestionPoints
	 * @param pointsAvg
	 * @param unzipBackUpLocation
	 * @param fileName
	 * @param baseName
	 * @return
	 * @throws Exception
	 */
	private Question processQuestionItemFile(HashMap<String, Pool> allPools, HashMap<String, Question> allQuestions, String context, Document doc,
			Element resourceItem, HashMap<String, String> allQuestionPoints, Average pointsAvg, String unzipBackUpLocation, String fileName,
			String baseName) throws Exception
	{
		Document contentsDOM = Xml.readDocument(unzipBackUpLocation + File.separator + fileName);
		if (contentsDOM == null) return null;

		String interaction = null;

		XPath textPath = new DOMXPath("/assessmentItem/itemBody");
		Element itemBody = (Element) textPath.selectSingleNode(contentsDOM);
		boolean likertClass = (itemBody != null && "likert".equals(itemBody.getAttribute("class"))) ? true : false;
		boolean surveyType = findQuestionSurvey(resourceItem);
		
		// find question type...very important step
		if (resourceItem != null) interaction = findInteraction(contentsDOM);

		// create question text and collect all embed media and question.xml file
		ArrayList<String> embedMedia = new ArrayList<String>();
		String text = processQuestionText(context, contentsDOM, itemBody, embedMedia, interaction, unzipBackUpLocation.concat(baseName));
		embedMedia.add(fileName);
		
		String questionIdentifier = resourceItem.getAttribute("identifier");
		int poolIndex = questionIdentifier.indexOf(":");
		String poolId = "defaultPool";

		if (poolIndex > -1) poolId = questionIdentifier.substring(0, poolIndex);
				
		Pool pool = allPools.get(poolId);
		if (pool == null) pool = allPools.get("defaultPool");

		// create the question
		Question question = null;
		if (questionIdentifier != null && allQuestions.containsKey(questionIdentifier)) 
			question = allQuestions.get(questionIdentifier);
		
		if (likertClass) question = buildLikertScaleChoice(pool, text, interaction, contentsDOM);
		if ("".equals(interaction) && question == null) question = buildTask(pool, text, interaction, contentsDOM);
		if (question == null) question = buildEssay(pool, text, interaction, contentsDOM);
		if (question == null) question = buildMatchforManyMultipleChoice(pool, text, interaction, contentsDOM);
		if (question == null) question = buildTrueFalse(pool, text, interaction, contentsDOM);
		if (question == null) question = buildMultipleChoice(pool, text, interaction, contentsDOM);		
		if (question == null) question = buildMatch(pool, text, interaction, contentsDOM);
		if (question == null) question = buildFillBlanks(pool, text, interaction, contentsDOM);
		if (question == null) question = buildFillBlankforGapText(pool, text, interaction, contentsDOM);
		if (question == null) question = buildFillBlankforOrdered(pool, text, interaction, contentsDOM);
		if (question == null) question = buildMultipleChoiceforUnsupported(pool, text, unzipBackUpLocation.concat(baseName), context, contentsDOM);
		
		if (question == null) return null;

		// save
		if (question != null)
		{		
			// read file elements and if other than embed media, bring in as attachments
			question = processQuestionAttachments(resourceItem, context, question, embedMedia, unzipBackUpLocation);

			// question description
			String description = findDescription(resourceItem);

			// explain reason
			Set<String> allInteractions = findAllInteraction(contentsDOM);
			if (allInteractions.size() > 1 && allInteractions.contains("textEntryInteraction"))
			{
				question.setExplainReason(new Boolean(true));
			}

			// hints mostly from identifier as correct otherwise from the correctresponse Identifier
			String hints = getQuestionHints(contentsDOM);
			if (question.getHints() != null) hints = question.getHints().concat(hints);
			question.setHints(hints);

			// feedback
			XPath modalFeedbackPath = new DOMXPath("/assessmentItem/modalFeedback");
			String feedback = modalFeedbackPath.stringValueOf(contentsDOM);
			question.setFeedback(feedback);

			// points ...some packages have que_score or score1 or SCORE or MAXSCORE or multiple of these records
			XPath outcomePath = new DOMXPath(
					"/assessmentItem/outcomeDeclaration[contains(@identifier,'SCORE')] | /assessmentItem/outcomeDeclaration[contains(@identifier,'score')]");
			List<Element> scores = outcomePath.selectNodes(contentsDOM);

			String pointsValue = "";
			for (Element score : scores)
			{
				String p = score.getTextContent().trim();
				if (!"".equals(p) && !"0.0".equals(p)) pointsValue = p;
			}

			if (pointsValue != null && pointsValue != "")
			{
				if (questionIdentifier != null) allQuestionPoints.put(questionIdentifier, pointsValue);
			}

			// survey
			if (likertClass || surveyType)
				question.setIsSurvey(true);
			else
				question.setIsSurvey(false);

			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);
		}

		if (questionIdentifier != null && !allQuestions.containsKey(questionIdentifier)) allQuestions.put(questionIdentifier, question);
		return question;
	}

	/**
	 * 
	 * @param file
	 * @param embedMedia
	 * @return
	 */
	private boolean findInEmbedMedia(String file, List<String> embedMedia)
	{
		boolean found = false;
		file = file.replace("\\", "/");
		if (file.lastIndexOf("/") != -1) file = file.substring(file.lastIndexOf("/")).trim();

		for (String m : embedMedia)
		{
			m = m.replace("\\", "/");
			if (m.lastIndexOf("/") != -1) m = m.substring(m.lastIndexOf("/")).trim();
			if (m.equalsIgnoreCase(file))
			{
				found = true;
				break;
			}
		}
		return found;
	}
	
	/**
	 * Find the Question type by identifying Interaction name.
	 * 
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private String findInteraction(Document contentsDOM) throws Exception
	{
		String interaction = null;
		Element interactionElement = null;
		XPath interactionTypePath = new DOMXPath("/assessmentItem/itemBody//*[contains(local-name(),'Interaction')]");
		interaction = ((interactionElement = (Element) interactionTypePath.selectSingleNode(contentsDOM)) != null) ? interactionElement.getNodeName()
				: "";
		return interaction;
	}

	/**
	 * Find all interaction to check explain reason. If more than one kind of interaction like inlinechoice and textEntry then textEntry shows explain reason.
	 * 
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Set<String> findAllInteraction(Document contentsDOM) throws Exception
	{
		List<Element> interactionElements = new ArrayList<Element>();
		Set<String> allTypesInteraction = new HashSet<String>();

		XPath interactionTypePath = new DOMXPath("/assessmentItem/itemBody//*[contains(local-name(),'Interaction')]");
		interactionElements = interactionTypePath.selectNodes(contentsDOM);

		for (Element i : interactionElements)
			allTypesInteraction.add(i.getNodeName());
		return allTypesInteraction;
	}

	/**
	 * Pool description from metadata element
	 * 
	 * @param doc
	 * @return
	 */
	private String findPoolDescription(Document doc)
	{
		String poolDesc = "";
		try
		{
			Element manifestElement = doc.getDocumentElement();
			XPath metadataPath = new DOMXPath(".//*[contains(local-name(),'metadata')]");
			Element metadataElement = (Element) metadataPath.selectSingleNode(manifestElement);

			XPath poolDescriptionPath = new DOMXPath(".//*[contains(local-name(),'description')]");
			Element descElement = (Element) poolDescriptionPath.selectSingleNode(metadataElement);
			poolDesc = descElement.getTextContent().trim();
		}
		catch (Exception ex)
		{
			M_log.debug("pool title exception" + ex.getMessage());
		}
		return poolDesc;
	}

	private String findRightsInformation(Document doc)
	{
		String rights = "";
		try
		{
			Element manifestElement = doc.getDocumentElement();
			XPath metadataPath = new DOMXPath(".//*[contains(local-name(),'metadata')]");
			Element metadataElement = (Element) metadataPath.selectSingleNode(manifestElement);

			XPath rightsPath = new DOMXPath(".//*[contains(local-name(),'rights')]");
			Element rightElement = (Element) rightsPath.selectSingleNode(metadataElement);
			
			rights = rightElement.getTextContent().trim();
		}
		catch (Exception ex)
		{
			M_log.debug("pool title exception" + ex.getMessage());
		}
		return rights;
	}

	/**
	 * Find exisiting same titles.If found append (copy X)
	 * @param context
	 * @param title
	 * @return
	 */
	private String findAssessmentTitle(String context, String title)
	{
		String testTitle = "Untitled Assessment";

		try
		{
			if (title != null && title.length() > 0) testTitle = title;

			List<Assessment> assessments = assessmentService.getContextAssessments(context, null, Boolean.FALSE);
			if (assessments == null || assessments.size() == 0) return testTitle;

			// if title exists then add (copy x)
			int sameTitle = 0;
			for (Assessment a : assessments)
			{
				String readTitle = a.getTitle();
				if (readTitle.contains(testTitle))
				{
					if (readTitle.equals(testTitle)) sameTitle++;
					else
					{
						readTitle = readTitle.replace(testTitle,"");
						if (readTitle.contains("(copy")) sameTitle++;
					}
				}
			}

			if (sameTitle > 0) testTitle = testTitle + " (copy" + sameTitle + ")";

		}
		catch (Exception ex)
		{
			M_log.debug("assessment title exception" + ex.getMessage());
		}
		return testTitle;
	}
	
	/**
	 * Find the title for pool
	 * 
	 * @param doc
	 * @return
	 */
	private String findPoolTitle(String context, String title, Document doc)
	{
		String poolTitle = "defaultPool";

		try
		{
			if ("".equals(title))
			{
				Element manifestElement = doc.getDocumentElement();
				XPath metadataPath = new DOMXPath(".//*[contains(local-name(),'metadata')]");
				Element metadataElement = (Element) metadataPath.selectSingleNode(manifestElement);
				if (metadataElement == null) return poolTitle;
				
				XPath poolTitlePath = new DOMXPath(".//*[contains(local-name(),'title')]");
				Element titleElement = (Element) poolTitlePath.selectSingleNode(metadataElement);
				if (titleElement != null && titleElement.getTextContent().length() > 0) poolTitle = titleElement.getTextContent().trim();
				if (poolTitle.length() > 255) poolTitle = poolTitle.substring(0, 245);
			}
			else
				poolTitle = title;

			List<Pool> pools = poolService.getAllPools(context);
			if (pools == null || pools.size() == 0) return poolTitle;

			// if title exists then add (copy x)
			int sameTitle = 0;
			for (Pool p : pools)
			{
				String readTitle = p.getTitle();
				if (readTitle.contains(poolTitle))
				{
					if (readTitle.equals(poolTitle)) sameTitle++;
					else
					{
						readTitle = readTitle.replace(poolTitle,"");
						if (readTitle.contains("(copy")) sameTitle++;
					}
				}
			}

			if (sameTitle > 0) poolTitle = poolTitle + " (copy" + sameTitle + ")";

		}
		catch (Exception ex)
		{
			M_log.debug("pool title exception" + ex.getMessage());
		}
		return poolTitle;
	}

	/**
	 * 
	 * @param testItem
	 * @return
	 */
	private String findDescription(Element testItem)
	{
		try
		{
			XPath descPath = new DOMXPath(".//*[contains(local-name(),'description')]");
			Element descElement = (Element) descPath.selectSingleNode(testItem);
			if (descElement != null)
			{
				// return descElement.getTextContent().trim();
				descPath = new DOMXPath(".//*[contains(local-name(),'langstring')]");
				descElement = (Element) descPath.selectSingleNode(descElement);
				if (descElement != null) return normalizeElementBody(descElement.getOwnerDocument(), descElement);
			}
		}
		catch (Exception e)
		{
		}
		return "";
	}

	/**
	 * 
	 * @param testItem
	 * @return
	 */
	private AssessmentType findAssessmentType(Element testItem)
	{
		AssessmentType testType = AssessmentType.test;
		try
		{
			XPath typePath = new DOMXPath(".//*[contains(local-name(),'identifier')]");
			Element typeElement = (Element) typePath.selectSingleNode(testItem);
			if (typeElement != null)
			{
				String type = typeElement.getTextContent();
				if ("assignment".equals(type)) testType = AssessmentType.assignment;
				if ("survey".equals(type)) testType = AssessmentType.survey;
			}
		}
		catch (Exception e)
		{
		}
		return testType;
	}

	/**
	 * 
	 * @param sectionElement
	 * @return
	 */
	private String findPartOrderingShuffle(Element sectionElement)
	{
		NodeList orderingPath = sectionElement.getElementsByTagName("ordering");
		if (orderingPath != null && orderingPath.getLength() > 0)
		{
			Element orderingElement = (Element) orderingPath.item(0);
			return orderingElement.getAttribute("shuffle");
		}
		else
			return "false";
	}

	/**
	 * Find if Question is of survey type
	 * @param testItem
	 * @return
	 */
	private boolean findQuestionSurvey(Element testItem)
	{
		if (testItem == null) return false;
		try
		{
			XPath typePath = new DOMXPath(".//*[contains(local-name(),'identifier')]");
			Element typeElement = (Element) typePath.selectSingleNode(testItem);
			if (typeElement != null)
			{
				String type = typeElement.getTextContent();
				if (type != null && "survey".equals(type.trim())) return true;
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}
	
	/**
	 * return answer text if answerText is true. Multiple choice needs id and fill blanks need text.
	 * @param response
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private String getCorrectResponse(Element value, Document contentsDOM, boolean answerTextFlag) throws Exception
	{
		if (value == null) return "";
		
		Element response = (Element)value.getParentNode().getParentNode();
		String baseType = response.getAttribute("baseType");
		String responseIdentifier = response.getAttribute("identifier");

		// if basetype is string then value is the answer. If identifier then fetch the element with this identifier for answer.
		String answerText = "";
		if (("string").equalsIgnoreCase(baseType) && value != null)
		{
			answerText = value.getTextContent();
		}
		else if ("identifier".equalsIgnoreCase(baseType))
		{
			// /assessmentItem/responseDeclaration[1]/correctResponse[1]/value[1] has identifier
			answerText = value.getTextContent();
			
			if (answerTextFlag)
			{
				// sometimes answertext is also identifier
				XPath answerPath = new DOMXPath(".//*[@identifier='" + answerText + "']");
				String checkIdentifier = answerPath.stringValueOf(contentsDOM);
				if (checkIdentifier != null && checkIdentifier != "" && checkIdentifier.length() != 0) answerText = checkIdentifier;
			}
		}
		else answerText = value.getTextContent();
		
		return answerText;
	}
	
	/**
	 * 
	 * @param contentsDOM
	 * @param responseIdentifier
	 * @param answerTextFlag
	 * @return
	 */
	private List<String> getCorrectResponsefromResponseProcessing(Document contentsDOM, String responseIdentifier, boolean answerTextFlag)
	{
		ArrayList<String> correctResponses = new ArrayList<String>();
		try
		{
			XPath answerPath = new DOMXPath("/assessmentItem/responseProcessing/responseCondition//variable[@identifier='" + responseIdentifier
					+ "']");
			List<Element> answerIdElements = answerPath.selectNodes(contentsDOM);

			for (Element answerIdElement : answerIdElements)
			{
				// match element
				Element matchElement = (Element) answerIdElement.getParentNode();
				answerPath = new DOMXPath(".//*[@baseType='identifier']");
				answerIdElement = (Element) answerPath.selectSingleNode(matchElement);
				String answerIdentifier = answerIdElement.getTextContent();

				// setoutcome element
				Element responseConditionElement = (Element) matchElement.getParentNode();
				XPath outcomePath = new DOMXPath(
						".//setOutcomeValue[contains(@identifier,'SCORE')] | .//setOutcomeValue[contains(@identifier,'score')] | .//setOutcomeValue[contains(@identifier,'Correct')]");
				List<Element> scoreElements = outcomePath.selectNodes(responseConditionElement);
				String scoreValue = "";
				for (Element score : scoreElements)
				{
					scoreValue = score.getTextContent();
					if (scoreValue == null || scoreValue.equals("0") || scoreValue.equals("0.0") || scoreValue.startsWith("-")) continue;
				}

				if (scoreValue == null || scoreValue.equals("") || scoreValue.equals("0") || scoreValue.equals("0.0") || scoreValue.startsWith("-")) continue;
				if (!answerTextFlag)
					correctResponses.add(answerIdentifier);
				else
				{
					// get answer text
					answerPath = new DOMXPath(".//*[@identifier='" + answerIdentifier + "']");
					correctResponses.add(answerPath.stringValueOf(contentsDOM));
				}
			}
		}
		catch (Exception e)
		{
			// do nothing
		}
		return correctResponses;
	}
	
	/**
	 * 
	 * @param dateStr
	 * @return
	 */
	public Date getDateFromString(String dateStr)
	{
		Date date = null;
		 
		try
		{
			if (dateStr == null || dateStr.length() == 0 || dateStr.equals("")) return null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			date = sdf.parse(dateStr);
		}
		catch (ParseException e)
		{
			return null;
		}
		return date;
	}
	
	/**
	 * 
	 * @param identifer
	 * @param href
	 * @param allQuestions
	 * @return
	 */
	private Question getQuestionfromAllQuestions(String identifer, String href, HashMap<String, Question> allQuestions)
	{
		if (allQuestions == null) return null;

		Question question = null;
		if (allQuestions.containsKey(identifer)) question = allQuestions.get(identifer);
		if (question == null)
		{
			// Read Question.Xml file
			Document contentsDOM = Xml.readDocument(href);
			if (contentsDOM == null) return null;

			Element rootQuestion = contentsDOM.getDocumentElement();
			String questionIdentifier = rootQuestion.getAttribute("identifier");
			if (allQuestions.containsKey(questionIdentifier)) question = allQuestions.get(questionIdentifier);
		}
		return question;
	}
	
	/**
	 * Get the correct answer hints
	 * 
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private String getQuestionHints(Document contentsDOM) throws Exception
	{
		String hints = "";
		XPath hintsPath = new DOMXPath(".//feedbackInline[@identifier='Correct'] | .//feedbackBlock[@identifier='Correct']");
		hints = hintsPath.stringValueOf(contentsDOM);

		XPath feedbackIdDeterminePath = new DOMXPath(
				"/assessmentItem/responseProcessing/setOutcomeValue[@identifier='FEEDBACK']/variable[@identifier='RESPONSE']");
		String feedbackIdentifier = feedbackIdDeterminePath.stringValueOf(contentsDOM);
		if (feedbackIdentifier != null && feedbackIdentifier.length() > 0)
		{
			XPath correctAnswerPath = new DOMXPath("/assessmentItem/responseDeclaration/correctResponse/value");
			String correctAnswerIdentifier = correctAnswerPath.stringValueOf(contentsDOM);
			if (correctAnswerIdentifier != null && correctAnswerIdentifier.length() > 0)
			{
				hintsPath = new DOMXPath(".//feedbackInline[@identifier='" + correctAnswerIdentifier + "']");
				hints = hintsPath.stringValueOf(contentsDOM);
			}
		}
		else
		{
			feedbackIdDeterminePath = new DOMXPath("/assessmentItem/responseProcessing/responseIf/setOutcomeValue[@identifier='FEEDBACK']/baseValue");
			feedbackIdentifier = feedbackIdDeterminePath.stringValueOf(contentsDOM);
			if (feedbackIdentifier != null && feedbackIdentifier.length() > 0)
			{
				hintsPath = new DOMXPath(".//feedbackInline[@identifier='" + feedbackIdentifier + "']");
				hints = hintsPath.stringValueOf(contentsDOM);
			}
		}
		return hints;
	}

	/**
	 * BringIn embed media from instrcutions
	 * 
	 * @param unzipBackUpLocation
	 * @param context
	 * @param text
	 * @param embedMedia
	 * @return
	 * @throws Exception
	 */
	private String processInstructionsEmbedMedia(String unzipBackUpLocation, String context, String text, List<String> embedMedia) throws Exception
	{
		Pattern p = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^#\"]*)([#\"])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		StringBuffer sb = new StringBuffer();
		Matcher m = p.matcher(text);

		while (m.find())
		{
			if (m.groupCount() != 3) continue;
			String fileName = m.group(2);
			if (embedMedia != null) embedMedia.add(fileName);
			// add to collection
			Reference ref = transferEmbeddedData(unzipBackUpLocation + File.separator + fileName, fileName, context);
			if (ref == null) continue;

			// replace with collection Url
			String ref_id = attachmentService.processMnemeUrls(ref.getId());
			m.appendReplacement(sb, m.group(1) + "= \"" + ref_id + "\"");
		}
		m.appendTail(sb);
		return sb.toString();
	}	
	
	/**
	 * Find embed media from question text and bring it in 
	 * @param itemBodyElement
	 * @param unzipBackUpLocation
	 * @param context
	 * @param embedMedia
	 * @throws Exception
	 */
	private void processEmbedMedia(Element itemBodyElement, String unzipBackUpLocation, String context, List<String> embedMedia) throws Exception
	{
		List<Element> objects = new ArrayList<Element>();

		if (itemBodyElement.getNodeName().equals("object") || itemBodyElement.getNodeName().equals("img")
				|| itemBodyElement.getNodeName().equals("a"))
		{
			objects.add(itemBodyElement);
		}
		else
		{
			XPath objectPath = new DOMXPath(".//object|.//img|.//a");
			objects = objectPath.selectNodes(itemBodyElement);
		}

		if (objects == null || objects.size() == 0) return;

		for (Element obj : objects)
		{
			// find fileName
			String fileName = null;
			if (obj.getNodeName().equals("object"))
				fileName = obj.getAttribute("data");
			else if (obj.getNodeName().equals("img"))
				fileName = obj.getAttribute("src");
			else if (obj.getNodeName().equals("a")) fileName = obj.getAttribute("href");

			// add to collection
			Reference ref = transferEmbeddedData(unzipBackUpLocation + File.separator + fileName, fileName, context);
			if (ref == null) continue;

			// replace with collection Url
			String ref_id = attachmentService.processMnemeUrls(ref.getId());
			if (ref_id != null && obj.getNodeName().equals("object"))
				obj.setAttribute("data", ref_id);
			else if (ref_id != null && obj.getNodeName().equals("img"))
				obj.setAttribute("src", ref_id);
			else if (ref_id != null && obj.getNodeName().equals("a")) obj.setAttribute("href", ref_id);

			if (embedMedia != null) embedMedia.add(fileName);
		}

	}
	
	/**
	 * Build the question presentation text. Its important to remove inline feedback text and interaction text while creating this text.
	 * 
	 * @param contentsDOM
	 * @param itemBodyElement
	 * @param interaction
	 * @return
	 * @throws Exception
	 */
	private String processQuestionText(String context, Document contentsDOM, Element itemElement, List<String> embedMedia, String interaction,
			String unzipBackUpLocation) throws Exception
	{
		String text = "";

		if (itemElement == null) return text;

		boolean fillBlanks = ("inlineChoiceInteraction".equalsIgnoreCase(interaction) || "textEntryInteraction".equalsIgnoreCase(interaction)) ? true
				: false;

		Element itemBodyElement = (Element) itemElement.cloneNode(true);
		List<Element> interactionElements = new ArrayList<Element>();

		// embed images
		processEmbedMedia(itemBodyElement, unzipBackUpLocation, context, embedMedia);
		
		itemBodyElement = removeFeedback(itemBodyElement);

		if (interaction == null || "".equals(interaction)) return normalizeElementBody(contentsDOM, itemBodyElement);

		XPath interactionPath = new DOMXPath(".//" + interaction);
		interactionElements = interactionPath.selectNodes(itemBodyElement);

		if (interactionElements == null || interactionElements.size() == 0) return normalizeElementBody(contentsDOM, itemBodyElement);

		// add prompt or block quote inside interaction to question text

		for (Element i : interactionElements)
		{
			String additionalText = "";
			XPath promptPath = new DOMXPath("prompt|blockquote");
			List<Element> prompts = promptPath.selectNodes(i);
			for (Element prompt : prompts)
			{
				additionalText = additionalText.concat(normalizeElementBody(contentsDOM, prompt));
			}

			if (fillBlanks)
			{
				Element replaceDivElement = contentsDOM.createElement("div");
				i.setTextContent(additionalText + "{}");
				itemBodyElement.appendChild(replaceDivElement);
			}
			else
				i.setTextContent(additionalText);
		}

		// normalize all child nodes and create a string
		String content = normalizeElementBody(contentsDOM, itemBodyElement);
		return content;
	}

	/**
	 * read each node and write it as is.
	 * 
	 * @param doc
	 * @param itemBodyElement
	 * @return
	 */
	private String normalizeElementBody(Document doc, Element itemBodyElement)
	{
		if (itemBodyElement == null) return "";

		try
		{
			doc.getDocumentElement().normalize();

			DocumentTraversal traversal = (DocumentTraversal) doc;
			NodeIterator iterator = traversal.createNodeIterator(itemBodyElement, NodeFilter.SHOW_ELEMENT, null, true);
			for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode())
			{
				String tagname = ((Element) n).getTagName();

				if (tagname.equalsIgnoreCase(itemBodyElement.getTagName()))
				{
					StringBuilder textContent = new StringBuilder();

					String nodeContent = getAllLevelsTextContent(n, textContent, false, null, null, null);
					return nodeContent;
				}
			}
		}
		catch (Exception e)
		{
			return itemBodyElement.getTextContent();
		}
		return itemBodyElement.getTextContent();
	}

	/**
	 * For unsupported question , get the whole text along with options.
	 * @param doc
	 * @param itemBodyElement
	 * @param wholeText
	 * @param unzipLocation
	 * @param context
	 * @param embedMedia
	 * @return
	 */
	private String normalizeItemBodyElement(Document doc, Element itemBodyElement, boolean wholeText, String unzipLocation, String context, List<String> embedMedia)
	{
		if (itemBodyElement == null) return "";

		try
		{
			doc.getDocumentElement().normalize();

			DocumentTraversal traversal = (DocumentTraversal) doc;
			NodeIterator iterator = traversal.createNodeIterator(itemBodyElement, NodeFilter.SHOW_ELEMENT, null, true);
			for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode())
			{
				String tagname = ((Element) n).getTagName();

				if (tagname.equalsIgnoreCase(itemBodyElement.getTagName()))
				{
					StringBuilder textContent = new StringBuilder();

					String nodeContent = getAllLevelsTextContent(n, textContent, wholeText, unzipLocation, context, embedMedia);
					return nodeContent;
				}
			}
		}
		catch (Exception e)
		{
			return itemBodyElement.getTextContent();
		}
		return itemBodyElement.getTextContent();
	}
	
	/**
	 * 
	 * @param node
	 * @param textContent
	 * @return
	 */
	private String getAllLevelsTextContent(Node node, StringBuilder textContent, boolean wholeText, String unzipLocation, String context,
			List<String> embedMedia)
	{
		NodeList list = node.getChildNodes();

		for (int i = 0; i < list.getLength(); ++i)
		{
			Node child = list.item(i);
			
			String childTagName = child.getNodeName();
			
			if (child.getNodeType() == Node.TEXT_NODE)
			{
				textContent.append(child.getTextContent());

			}
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				if (wholeText && ("img".equalsIgnoreCase(childTagName) || "a".equalsIgnoreCase(childTagName) || "object".equalsIgnoreCase(childTagName)))
				{
					try
					{
						processEmbedMedia((Element) child, unzipLocation, context, embedMedia);
					}
					catch (Exception e)
					{
						// do nothing
					}
				}

				if (!wholeText && child.getNodeName().contains("Interaction"))
				{
					if (child.getTextContent() != null) textContent.append(child.getTextContent());
				}
				else if (child.getNodeName().contains("feedback"))
				{
					// do nothing skip it
				}
				else if (child.getNodeName().contains("printedVariable"))
				{
					Element printedVariableTemplate = null;
					Element currNodeElement = (Element) child;
					String id = currNodeElement.getAttribute("identifier");
					try
					{
						XPath printedPath = new DOMXPath(".//setTemplateValue[@identifier='" + id + "']//randomInteger");
						printedVariableTemplate = (Element) printedPath.selectSingleNode(child.getOwnerDocument());
					}
					catch (Exception e)
					{
						printedVariableTemplate = null;
					}
					if (printedVariableTemplate != null)
						textContent.append(this.messages.getString("import_qti2_printedVariable_text") + printedVariableTemplate.getAttribute("min")
								+ " - " + printedVariableTemplate.getAttribute("max") + " "
								+ this.messages.getString("import_qti2_printedVariable_text2"));
				}
				else
				{
					textContent.append("<" + child.getNodeName());

					if (child.hasAttributes())
					{
						NamedNodeMap attrs = child.getAttributes();
						for (int k = 0; k < attrs.getLength(); k++)
						{
							Node attr = attrs.item(k);
							textContent.append(" " + attr.getNodeName() + " = \"" + attr.getTextContent() + "\" ");
						}
					}
					textContent.append(">");

					getAllLevelsTextContent(child, textContent, wholeText, unzipLocation, context, embedMedia);

					textContent.append("</" + child.getNodeName() + ">");
				}
			}
		}
		return textContent.toString();
	}

	/**
	 * Create essay type question
	 * 
	 * @param essay
	 * @param text
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildEssay(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		String modelAnswer = "";
		SubmissionType setting = SubmissionType.inline;
		List<Element> interactions = null;
		boolean essayType = false;

		// correct answer
		XPath identifierPath = new DOMXPath("/assessmentItem/responseDeclaration/correctResponse/value");
		List<Element> values = identifierPath.selectNodes(contentsDOM);

		if (interactionText != null)
		{
			XPath interactionPath = new DOMXPath(".//" + interactionText);
			interactions = interactionPath.selectNodes(contentsDOM);
			// check if fill in blanks or essay. If one text entry and no correct response then essay
			if ("textEntryInteraction".equals(interactionText) && interactions.size() == 1 && values.size() == 0) essayType = true;
			if ("extendedTextInteraction".equals(interactionText)) essayType = true;
			if ("uploadInteraction".equals(interactionText)) essayType = true;
		}
		
		XPath itemBodyPath = new DOMXPath(".//itemBody");
		Element itemBody = (Element)itemBodyPath.selectSingleNode(contentsDOM);
//		if (containsPrintedText(itemBody))essayType = true;
		
		if (interactions == null || !essayType) return null;
		// submission type
		XPath responsePath = new DOMXPath("/assessmentItem/responseDeclaration");
		Element responseElement = (Element) responsePath.selectSingleNode(contentsDOM);
		String baseType = (responseElement != null && responseElement.getAttribute("baseType") != null) ? responseElement.getAttribute("baseType")
				: null;
		if ("file".equals(baseType)) setting = SubmissionType.attachments;

		if (values != null)
		{
			for (Element value : values)
			{
				modelAnswer = modelAnswer.concat(value.getTextContent());
			}
		}

		Question question = this.questionService.newQuestion(pool, "mneme:Essay");
		EssayQuestionImpl essay = (EssayQuestionImpl) (question.getTypeSpecificQuestion());
		text = text.replace("{}", "");
		question.getPresentation().setText(text);
		essay.setModelAnswer(modelAnswer);
		essay.setSubmissionType(setting);

		question.getTypeSpecificQuestion().consolidate("");

		return question;
	}

	/**
	 * Build Fill in the blanks question
	 * 
	 * @param question
	 * @param text
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildFillBlanks(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		if (!(("textEntryInteraction").equalsIgnoreCase(interactionText) || ("inlineChoiceInteraction").equalsIgnoreCase(interactionText)))
			return null;

		// choice interaction
		XPath interactionPath = new DOMXPath(".//" + interactionText);
		List<Element> interactions = interactionPath.selectNodes(contentsDOM);

		if (interactions == null || interactions.size() == 0) return null;

		// correct answer
		XPath responsePath = new DOMXPath("/assessmentItem/responseDeclaration");
		List<Element> responses = responsePath.selectNodes(contentsDOM);
		Boolean responseTextual = null;

		if (responses != null)
		{
			for (Element response : responses)
			{	
				String answerText = ""; 
				
				XPath valuePath = new DOMXPath(".//correctResponse/value");
				Element value = (Element)valuePath.selectSingleNode(response);
			
				// if response declaration has correct answer 
				if (value != null)
				{
					answerText = getCorrectResponse(value, contentsDOM, true);			
				}
				else
				{
					// get correct answers from RequestProcessing
					List<String> answers = getCorrectResponsefromResponseProcessing(contentsDOM, response.getAttribute("identifier"), true);
					if (answers != null && answers.size() > 0) answerText = answers.get(0);
				}
				
				text = text.replaceFirst("\\{\\}", "{" + answerText + "}");
				if (responseTextual == null)
					responseTextual = checkIfTextualorNumeric(answerText);
				else
					responseTextual = (responseTextual || checkIfTextualorNumeric(answerText));
			}
		}

		return buildMnemeFillBlanks(pool, responseTextual, text);
	}

	/**
	 * Check if answer is textual or numeric
	 * 
	 * @param check
	 *        String to check
	 * @return false if numeric
	 */
	private boolean checkIfTextualorNumeric(String check)
	{
		try
		{
			Float.parseFloat(check.trim());
			return false;
		}
		catch (NumberFormatException e)
		{
			return true;
		}
	}
 
	/**
	 * If text contains printedVariable then make it as Essay Question
	 * @param itemBody
	 * @return
	 */
	private boolean containsPrintedText(Element itemBody)
	{
		boolean printedVariable = false;
		try
		{
			XPath printedPath = new DOMXPath(".//printedVariable");
			List<Element> variables = printedPath.selectNodes(itemBody);
			if (variables != null && variables.size() > 0) printedVariable = true;
		}
		catch (Exception e)
		{
			printedVariable = false;
		}
		return printedVariable;
	}
	
	/**
	 * 
	 * @param randomDraw
	 * @param randomCount
	 * @param context
	 * @param sectionIdentifier
	 * @param contentsDOM
	 * @param part
	 * @param allPools
	 * @param allQuestions
	 * @return
	 */
	private Part buildRandomDrawPart(boolean randomDraw, int randomCount, String context, String sectionIdentifier, String unzipLocation, Document contentsDOM, Part part,
			HashMap<String, Pool> allPools, HashMap<String, Question> allQuestions, HashMap<String, String> allQuestionPoints)
	{
		try
		{
			if (!randomDraw) return part;

			// find all questions
			XPath assessmentItemRefPath = new DOMXPath(".//assessmentSection[@identifier='" + sectionIdentifier + "']/assessmentItemRef");
			List<Element> optionalQuestions = assessmentItemRefPath.selectNodes(contentsDOM);

			// create randomPool for these questions
			XPath assessmentSectionPath = new DOMXPath(".//assessmentSection[@identifier='" + sectionIdentifier + "']");
			Element section = (Element) assessmentSectionPath.selectSingleNode(contentsDOM);
			String randomPoolTitle = (section != null) ? section.getAttribute("title") : part.getId() + "_randomPool";
			Pool randomPool = poolService.newPool(context);
			randomPool.setTitle(findPoolTitle(context, randomPoolTitle , contentsDOM));

			// store draw count for each pool
			HashMap<String, Integer> countDrawQuestions = new HashMap<String, Integer>();
			HashMap<String, String> pointDrawQuestions = new HashMap<String, String>();

			// for each question
			for (Element questionElement : optionalQuestions)
			{
				Question question = null;

				// if question is required skip it, already added as pickDetail
				String required = questionElement.getAttribute("required");
				if ("true".equalsIgnoreCase(required)) continue;

				// check identifier
				String identifer = questionElement.getAttribute("identifier");
				String href = questionElement.getAttribute("href");
				question = getQuestionfromAllQuestions(identifer, unzipLocation + File.separator + href, allQuestions);
				if (question == null) continue;

				// if already in etudes pool - count it
				if (identifer.indexOf(":") != -1)
				{
					if (!"false".equalsIgnoreCase(required)) continue;
					String poolId = identifer.substring(0, identifer.indexOf(":"));

					pointDrawQuestions.put(poolId, allQuestionPoints.get(identifer));

					if (countDrawQuestions.containsKey(poolId))
					{
						Integer count = countDrawQuestions.get(poolId);
						countDrawQuestions.put(poolId, count.intValue() + 1);
					}
					else
						countDrawQuestions.put(poolId, new Integer("1"));
				}
				// otherwise copy from defaultpool to randomPool
				else
				{
					questionService.moveQuestion(question, randomPool);
					pointDrawQuestions.put(randomPool.getId(), allQuestionPoints.get(identifer));
				}
			}
			// add draw detail
			if (countDrawQuestions.size() > 0)
			{
				// for etudes different pools
				Iterator<String> iter = countDrawQuestions.keySet().iterator();
				while (iter.hasNext())
				{
					String id = iter.next();
					PoolDraw draw = part.addDrawDetail(allPools.get(id), countDrawQuestions.get(id));
					// points draw.setEffectivePoints(points)
					if (pointDrawQuestions.get(id) != null && countDrawQuestions.get(id) != null)
						draw.setEffectivePoints(Float.parseFloat(pointDrawQuestions.get(id)) * countDrawQuestions.get(id));
				}
			}
			else
			{
				// for other packages
				poolService.savePool(randomPool);
				PoolDraw draw = part.addDrawDetail(randomPool, randomCount);
				if (pointDrawQuestions.get(randomPool.getId()) != null)
					draw.setEffectivePoints(Float.parseFloat(pointDrawQuestions.get(randomPool.getId())) * randomCount);
				return part;
			}
		}
		catch (Exception e)
		{
		}
		return part;
	}

	/**
	 * Create Task when there is no interaction
	 * 
	 * @param question
	 * @param text
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildTask(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		Question question = this.questionService.newQuestion(pool, "mneme:Task");
		question.getPresentation().setText(text);
		question.getTypeSpecificQuestion().consolidate("");

		return question;
	}

	/**
	 * Create likert scale question
	 * 
	 * @param pool
	 * @param text
	 * @param interactionText
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildLikertScaleChoice(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		if (interactionText == null || !interactionText.equals("choiceInteraction")) return null;

		XPath scalePath = new DOMXPath(".//correctResponse/value");
		String scale = scalePath.stringValueOf(contentsDOM);

		if (scale == null || scale.length() == 0)
		{
			XPath simpleChoicesPath = new DOMXPath("//simpleChoice");
			List<Element> choiceList = (List<Element>) simpleChoicesPath.selectNodes(contentsDOM);
			List<String> answerChoices = new ArrayList<String>();

			for (Element Choice : choiceList)
			{
				String label = normalizeElementBody(contentsDOM, Choice);
				answerChoices.add(label);
			}
		}

		Question question = this.questionService.newQuestion(pool, "mneme:LikertScale");
		LikertScaleQuestionImpl mc = (LikertScaleQuestionImpl) (question.getTypeSpecificQuestion());
		question.getPresentation().setText(text);
		if (scale != null && scale.length() > 0) mc.setScale(scale);

		question.getTypeSpecificQuestion().consolidate("");
		return question;
	}

	/**
	 * Create match type question when interaction is matchInteraction or associateInteraction
	 * 
	 * @param question
	 * @param text
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildMatch(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		if (!(("matchInteraction").equals(interactionText) || ("associateInteraction").equals(interactionText))) return null;

		XPath responsePath = new DOMXPath("/assessmentItem/responseDeclaration");
		Element responseElement = (Element) responsePath.selectSingleNode(contentsDOM);
		String baseType = (responseElement != null && responseElement.getAttribute("baseType") != null) ? responseElement.getAttribute("baseType")
				: null;
		if (!("directedPair".equalsIgnoreCase(baseType) || "Pair".equalsIgnoreCase(baseType))) return null;

		// match interaction
		XPath choicesPath = new DOMXPath("/assessmentItem/itemBody//" + interactionText);
		Element choices = (Element) choicesPath.selectSingleNode(contentsDOM);

		// correct answer
		XPath identifierPath = new DOMXPath("/assessmentItem/responseDeclaration/correctResponse/value");
		List<Element> values = (List<Element>) identifierPath.selectNodes(contentsDOM);

		if (choices == null || values == null) return null;

		// if one maxAssociation then create MC Question
		int maxAssociations = 0;
		if (!"".equals(choices.getAttribute("maxAssociations"))) maxAssociations = Integer.parseInt(choices.getAttribute("maxAssociations"));
		if (maxAssociations == 1 || values.size() == 1) return buildMultipleChoiceFromMatch(pool, text, values, interactionText, contentsDOM);

		Question question = this.questionService.newQuestion(pool, "mneme:Match");
		MatchQuestionImpl mc = (MatchQuestionImpl) (question.getTypeSpecificQuestion());
		mc = buildMatchforAssociate(values, choices, interactionText, contentsDOM, mc);

		if (text == null || text.equals("")) text = "Match the equivalent:";
		question.getPresentation().setText(text);
		// save
		question.getTypeSpecificQuestion().consolidate("");

		return question;
	}

	/**
	 * 
	 * @param correctValues
	 * @param interactionText
	 * @param contentsDOM
	 * @param mc
	 * @return
	 * @throws Exception
	 */
	private MatchQuestionImpl buildMatchforAssociate(List<Element> correctValues, Element choices, String interactionText, Document contentsDOM,
			MatchQuestionImpl mc) throws Exception
	{
		if (!(("matchInteraction").equals(interactionText) || ("associateInteraction").equals(interactionText))) return mc;

		ArrayList<Element> matchDone = new ArrayList<Element>();
		int maxAssociations = 0;
		maxAssociations = Integer.parseInt(choices.getAttribute("maxAssociations"));

		int doneAssociation = 0;

		for (Element value : correctValues)
		{
			String valuePair = value.getTextContent();
			if (valuePair == null) continue;
			String[] parts = StringUtil.split(valuePair, " ");
			if (parts.length == 2)
			{
				String labelIdentifier1 = parts[0];
				String labelIdentifier2 = parts[1];

				XPath choicePath = new DOMXPath(".//simpleAssociableChoice[@identifier='" + labelIdentifier1 + "']");
				Element choice1 = (Element) choicePath.selectSingleNode(contentsDOM);
				String choiceLabel1 = normalizeElementBody(contentsDOM, choice1);
				matchDone.add(choice1);

				XPath choicePath2 = new DOMXPath("/assessmentItem/itemBody//simpleAssociableChoice[@identifier='" + labelIdentifier2 + "']");
				Element choice2 = (Element) choicePath2.selectSingleNode(contentsDOM);
				String choiceLabel2 = normalizeElementBody(contentsDOM, choice2);
				matchDone.add(choice2);

				if (choiceLabel1 != null && choiceLabel2 != null)
				{
					mc.addPair(choiceLabel1, choiceLabel2);
				}
			}
			doneAssociation++;
		}
		// distractor
		XPath choicePath = new DOMXPath(".//simpleAssociableChoice");
		List<Element> allChoices = choicePath.selectNodes(contentsDOM);

		for (Element c : allChoices)
		{
			if (!matchDone.contains(c)) mc.setDistractor(normalizeElementBody(contentsDOM, c));
		}

		return mc;
	}
	
	/**
	 * 
	 * @param pool
	 * @param text
	 * @param interactionText
	 * @param unzipLocation
	 * @param context
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildMatchforManyMultipleChoice(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		if (!("choiceInteraction").equals(interactionText)) return null;

		// number of choice interaction
		XPath choicesPath = new DOMXPath("/assessmentItem/itemBody//" + interactionText);
		List<Element> choices = choicesPath.selectNodes(contentsDOM);
		if (choices == null || choices.size() <= 1) return null;

		Question question = this.questionService.newQuestion(pool, "mneme:Match");
		MatchQuestionImpl mc = (MatchQuestionImpl) (question.getTypeSpecificQuestion());

		// for finding distractors
		ArrayList<String> choiceStrings = new ArrayList<String>();
		
		for (Element choice: choices)
		{
			String choice1 = "";
			XPath promptPath = new DOMXPath(".//prompt|.//blockquote");
			List<Element> prompts = promptPath.selectNodes(choice);
			for (Element prompt : prompts)
			{
				choice1 = choice1.concat(normalizeElementBody(contentsDOM, prompt));
			}
			
			//choice 2 from responseDeclaration
			String choiceIdentifier = choice.getAttribute("responseIdentifier");
			if (choiceIdentifier == null || choiceIdentifier.length() == 0) continue;
			XPath responsePath = new DOMXPath("/assessmentItem/responseDeclaration[@identifier='" + choiceIdentifier + "']/correctResponse/value");
			Element responseElement = (Element) responsePath.selectSingleNode(contentsDOM);
			String choice2 = "";
			// if response declaration has correct answer 
			if (responseElement != null)
			{
				choice2 = getCorrectResponse(responseElement, contentsDOM, false);			
			}
			else
			{
				// get correct answers from RequestProcessing
				List<String> correctAnswerChoices = getCorrectResponsefromResponseProcessing(contentsDOM, choiceIdentifier, true);
				if(correctAnswerChoices != null && correctAnswerChoices.size() > 0) choice2 = correctAnswerChoices.get(0);
			}
			
			choiceStrings.add(choice2);			
			mc.addPair(choice1, choice2);	
		}

		// find distractor
/*		XPath distractorPath = new DOMXPath(".//simpleChoice");
		List<Element> distractors = distractorPath.selectNodes(contentsDOM);
		for (Element distractor : distractors)
		{
			String dist_text = normalizeElementBody(contentsDOM, distractor);
			if (!choiceStrings.contains(dist_text)) mc.setDistractor(dist_text);
		}*/
		
		if (text == null || text.equals("")) text = "Match the equivalent:";
		question.getPresentation().setText(text);
		// save
		question.getTypeSpecificQuestion().consolidate("");

		return question;
	}

	/**
	 * 
	 * @param correctValues
	 * @param interactionText
	 * @param contentsDOM
	 * @param mc
	 * @return
	 * @throws Exception
	 */
	private Question buildFillBlankforGapText(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		if (!("gapMatchInteraction").equals(interactionText)) return null;

		String questionText = "";

		// correct answer
		XPath identifierPath = new DOMXPath("/assessmentItem/responseDeclaration/correctResponse/value");
		List<Element> values = identifierPath.selectNodes(contentsDOM);

		XPath gapInteractionPath = new DOMXPath("/assessmentItem/itemBody//gapMatchInteraction");
		Element gapInteractionElement = (Element) gapInteractionPath.selectSingleNode(contentsDOM);
		String interactionIdentifier = gapInteractionElement.getAttribute("responseIdentifier");

		// The bandit killed her <gap identifier="G1"/>
		XPath gapPath = new DOMXPath("/assessmentItem/itemBody//gap");
		List<Element> gaps = gapPath.selectNodes(contentsDOM);

		for (Element gap : gaps)
		{
			String identifier = gap.getAttribute("identifier");
			String otherId = "";
			
			XPath responseValuePath = new DOMXPath("/assessmentItem/responseDeclaration[@identifier='" + interactionIdentifier
					+ "']/correctResponse/value[contains(text(),'" + identifier + "')]");
			Element value = (Element) responseValuePath.selectSingleNode(contentsDOM);

			if (value != null)
			{
				String valuePair = value.getTextContent();
				otherId = valuePair.replace(identifier, "").trim();
			}
			else
			{
				List<String> answers = getCorrectResponsefromResponseProcessing(contentsDOM, interactionIdentifier, false);
				if (answers != null && answers.size() > 0) otherId = answers.get(0);
			}
			// <gapText identifier="F" matchMax="1">family</gapText>
			if ("".equals(otherId)) continue;
			XPath idPath = new DOMXPath(".//gapText[@identifier='" + otherId + "']");
			Element gapTextElement = (Element) idPath.selectSingleNode(contentsDOM);
			String choiceLabel2 = normalizeElementBody(contentsDOM, gapTextElement);

			gap.setTextContent("{" + choiceLabel2 + "}");
		}

		// add hints
		XPath gaptextPath = new DOMXPath(".//gapText");
		List<Element> allGapTexts = gaptextPath.selectNodes(contentsDOM);

		String hints = "";
		for (Element g : allGapTexts)
		{
			hints = hints.concat("\t" + normalizeElementBody(contentsDOM, g));
			g.setTextContent("");
		}

		XPath itemBodyPath = new DOMXPath("/assessmentItem/itemBody");
		Element itemBodyElement = (Element) itemBodyPath.selectSingleNode(contentsDOM);
		questionText = normalizeElementBody(contentsDOM, itemBodyElement);

		Question question = buildMnemeFillBlanks(pool, true, questionText);
		question.setHints(hints);

		return question;
	}

	/**
	 * 
	 * @param pool
	 * @param text
	 * @param interactionText
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildFillBlankforOrdered(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		if (!"orderInteraction".equals(interactionText)) return null;

		// correct answer
		XPath responseDeclarePath = new DOMXPath("/assessmentItem/responseDeclaration");

		// choice interaction
		XPath choicesPath = new DOMXPath("/assessmentItem/itemBody//" + interactionText);
		Element choices = (Element) choicesPath.selectSingleNode(contentsDOM);

		if (choices == null) return null;

		// correct answer
		XPath identifierPath = new DOMXPath("/assessmentItem/responseDeclaration/correctResponse/value");
		List<Element> values = identifierPath.selectNodes(contentsDOM);

		XPath simpleChoicesPath = new DOMXPath("//simpleChoice");
		List<Element> choiceList = simpleChoicesPath.selectNodes(contentsDOM);
		
		text = text.concat("\n <table width='55%' border='0'>");
		for (Element Choice : choiceList)
		{
			String id = Choice.getAttribute("identifier");
			String label = normalizeElementBody(contentsDOM, Choice);
			text = text.concat("<tr><td><b>" + id + "</b> </td><td>" +label +"</td></tr>");
		}

		text = text.concat("</table> \n Enter the letters in the correct order ");

		int i = 1;
		if (values != null)
		{
			for (Element value : values)
			{
				String correctValue = getCorrectResponse(value, contentsDOM, false);
				if (i != values.size())
					text = text.concat("{" + correctValue + "},");
				else
					text = text.concat("{" + correctValue + "}.");
				i++;
			}
		}
		return buildMnemeFillBlanks(pool, true, text);
	}
		
	/**
	 * 
	 * @param pool
	 * @param text
	 * @param interactionText
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildMultipleChoiceforUnsupported(Pool pool, String text, String unzipLocation, String context, Document contentsDOM) throws Exception
	{
		// text has all content
		XPath itemPath = new DOMXPath(".//itemBody");
		Element item = (Element) itemPath.selectSingleNode(contentsDOM);
		List<String> bringMedia = new ArrayList<String>();
		text = normalizeItemBodyElement(contentsDOM, item, true, unzipLocation, context, bringMedia);
		
		int maxChoices = 1;
		boolean shuffle = false;
		List<String> answerChoices = new ArrayList<String>();
		Set<Integer> correctAnswers = new HashSet<Integer>();

		return buildMnemeMultipleChoice(pool, text, maxChoices, shuffle, answerChoices, correctAnswers);
	}
	
	/**
	 * 
	 * @param pool
	 * @param text
	 * @param correctValues
	 * @param interactionText
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildMultipleChoiceFromMatch(Pool pool, String text, List<Element> correctValues, String interactionText, Document contentsDOM)
			throws Exception
	{
		if (!(("matchInteraction").equals(interactionText) || ("associateInteraction").equals(interactionText))) return null;

		List<String> answerChoices = new ArrayList<String>();
		Set<Integer> correctAnswers = new HashSet<Integer>();
		String[] parts = null;

		for (Element value : correctValues)
		{
			String valuePair = value.getTextContent();
			if (valuePair == null) continue;
			parts = StringUtil.split(valuePair, " ");
		}

		int i = 0;
		XPath choicePath = new DOMXPath(".//simpleAssociableChoice");
		List<Element> allChoices = choicePath.selectNodes(contentsDOM);

		for (Element c : allChoices)
		{
			String id = c.getAttribute("identifier");
			answerChoices.add(normalizeElementBody(contentsDOM, c));

			if (parts[1].contains(id))
			{
				text = text.concat(normalizeElementBody(contentsDOM, c));
				continue;
			}
			if (parts[0].contains(id)) correctAnswers.add(i);

			i++;
		}

		return buildMnemeMultipleChoice(pool, text, 1, false, answerChoices, correctAnswers);
	}

	

	/**
	 * Create Multiple Choice Question when interaction is Choice and more than 2 choices.
	 * 
	 * @param mc
	 * @param text
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildMultipleChoice(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{
		if (!("choiceInteraction").equals(interactionText)) return null;

		// correct answer
		XPath responseDeclarePath = new DOMXPath("/assessmentItem/responseDeclaration");
		Element responseDeclare = (Element) responseDeclarePath.selectSingleNode(contentsDOM);
		String cardinality = (responseDeclare.getAttribute("cardinality") != null) ? responseDeclare.getAttribute("cardinality") : null;
		String basetype = (responseDeclare.getAttribute("baseType") != null) ? responseDeclare.getAttribute("baseType") : null;

		// choice interaction
		XPath choicesPath = new DOMXPath("/assessmentItem/itemBody//" + interactionText);
		Element choices = (Element) choicesPath.selectSingleNode(contentsDOM);

		if (choices == null) return null;

		// correct answer
		XPath identifierPath = new DOMXPath("/assessmentItem/responseDeclaration/correctResponse/value");
		List<Element> values = identifierPath.selectNodes(contentsDOM);

		XPath simpleChoicesPath = new DOMXPath("//simpleChoice");
		List<Element> choiceList = simpleChoicesPath.selectNodes(contentsDOM);

		if (!("identifier".equalsIgnoreCase(basetype) && cardinality != null && choiceList.size() > 0)) return null;

		int maxChoices = (choices.getAttribute("maxChoices") != null && choices.getAttribute("maxChoices").length() > 0) ? Integer.parseInt(choices
				.getAttribute("maxChoices")) : 1;
		boolean shuffle = (choices.getAttribute("shuffle") != null) ? Boolean.parseBoolean(choices.getAttribute("shuffle")) : false;
		List<String> answerChoices = new ArrayList<String>();
		List<String> correctAnswerChoices = new ArrayList<String>();
		Set<Integer> correctAnswers = new HashSet<Integer>();

		// if response declaration has correct answer 
		if (values != null && values.size() > 0)
		{
			for (Element value : values)
				correctAnswerChoices.add(getCorrectResponse(value, contentsDOM, false));			
		}
		else
		{
			// get correct answers from RequestProcessing
			correctAnswerChoices = getCorrectResponsefromResponseProcessing(contentsDOM, choices.getAttribute("responseIdentifier"), false);
		}

		int i = 0;
		for (Element Choice : choiceList)
		{
			String id = Choice.getAttribute("identifier");
			String label = normalizeElementBody(contentsDOM, Choice);
			answerChoices.add(label);

			if (correctAnswerChoices.contains(id)) correctAnswers.add(i);
			i++;
		}

		if (maxChoices < correctAnswers.size()) maxChoices = correctAnswers.size();
		return buildMnemeMultipleChoice(pool, text, maxChoices, shuffle, answerChoices, correctAnswers);
	}

	/**
	 * 
	 * @param pool
	 * @param responseTextual
	 * @param text
	 * @return
	 * @throws Exception
	 */
	private Question buildMnemeFillBlanks(Pool pool, Boolean responseTextual, String text) throws Exception
	{
		Question question = this.questionService.newQuestion(pool, "mneme:FillBlanks");
		FillBlanksQuestionImpl fb = (FillBlanksQuestionImpl) (question.getTypeSpecificQuestion());

		fb.setText(text);
		// case sensitive
		fb.setCaseSensitive(Boolean.FALSE.toString());

		// mutually exclusive
		fb.setAnyOrder(Boolean.FALSE.toString());

		// text or numeric
		if (responseTextual == null) responseTextual = true;
		fb.setResponseTextual(responseTextual.toString());

		question.getPresentation().setText(text);

		question.getTypeSpecificQuestion().consolidate("");

		return question;
	}

	/**
	 * Commonly used method to create mneme's multiple choice question
	 * 
	 * @param pool
	 * @param text
	 * @param maxChoices
	 * @param shuffle
	 * @param values
	 * @param answerChoices
	 * @param correctAnswers
	 * @return
	 */
	private Question buildMnemeMultipleChoice(Pool pool, String text, int maxChoices, boolean shuffle, List<String> answerChoices, Set<Integer> correctAnswers) throws Exception
	{
		Question question = this.questionService.newQuestion(pool, "mneme:MultipleChoice");

		MultipleChoiceQuestionImpl mc = (MultipleChoiceQuestionImpl) (question.getTypeSpecificQuestion());
		question.getPresentation().setText(text);

		boolean singleCorrect = false;
		
		if (maxChoices < 1 || correctAnswers.size() <= 1) singleCorrect = true;

		mc.setSingleCorrect(new Boolean(singleCorrect).toString());
		mc.setShuffleChoices(new Boolean(shuffle).toString());
		mc.setAnswerChoices(answerChoices);
		mc.setCorrectAnswerSet(correctAnswers);

		question.getTypeSpecificQuestion().consolidate("");
		return question;
	}

	/**
	 * Create True False question when interaction is choiceInteraction and 2 choices.
	 * 
	 * @param tf
	 * @param text
	 * @param contentsDOM
	 * @return
	 * @throws Exception
	 */
	private Question buildTrueFalse(Pool pool, String text, String interactionText, Document contentsDOM) throws Exception
	{

		if (interactionText == null || !interactionText.equals("choiceInteraction")) return null;

		XPath responseDeclarePath = new DOMXPath("/assessmentItem/responseDeclaration");
		Element responseDeclare = (Element) responseDeclarePath.selectSingleNode(contentsDOM);
		String cardinality = (responseDeclare.getAttribute("cardinality") != null) ? responseDeclare.getAttribute("cardinality") : null;
		String basetype = (responseDeclare.getAttribute("baseType") != null) ? responseDeclare.getAttribute("baseType") : null;

		// choice interaction
		XPath choicesPath = new DOMXPath("/assessmentItem/itemBody//choiceInteraction");
		Element choices = (Element) choicesPath.selectSingleNode(contentsDOM);

		if (choices == null) return null;

		XPath identifierPath = new DOMXPath("/assessmentItem/responseDeclaration/correctResponse/value");
		List<Element> values = identifierPath.selectNodes(contentsDOM);

		XPath simpleChoicesPath = new DOMXPath("//simpleChoice");
		List<Element> choiceList = simpleChoicesPath.selectNodes(contentsDOM);

		// check for true/false traits
		if (!("identifier".equalsIgnoreCase(basetype) && cardinality != null && choiceList.size() == 2)) return null;

		List<String> answerChoices = new ArrayList<String>();
		List<String> correctAnswerChoices = new ArrayList<String>();
		String correctAnswer = null;

		// if response declaration has correct answer 
		if (values != null && values.size() > 0)
		{
			for (Element value : values)
				correctAnswerChoices.add(getCorrectResponse(value, contentsDOM, false));			
		}
		else
		{
			// get correct answers from RequestProcessing
			correctAnswerChoices = getCorrectResponsefromResponseProcessing(contentsDOM, choices.getAttribute("responseIdentifier"), false);
		}

		for (Element Choice : choiceList)
		{
			Element ChoiceCopy = (Element) Choice.cloneNode(true);
			ChoiceCopy = removeFeedback(ChoiceCopy);

			String id = ChoiceCopy.getAttribute("identifier");
			String label = normalizeElementBody(contentsDOM, ChoiceCopy);
			if (label == null) continue;
			label = label.trim();
			answerChoices.add(label);

			if (correctAnswerChoices.contains(id))
			{
				if ("yes".equalsIgnoreCase(label) || "Right".equalsIgnoreCase(label) || "True".equalsIgnoreCase(label))
					correctAnswer = new Boolean("true").toString();
				else if ("no".equalsIgnoreCase(label) || "Wrong".equalsIgnoreCase(label) || "False".equalsIgnoreCase(label))
					correctAnswer = new Boolean("false").toString();
			}
		}

		if (correctAnswer == null) return null;

		Question question = this.questionService.newQuestion(pool, "mneme:TrueFalse");
		TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());
		question.getPresentation().setText(text);
		tf.setCorrectAnswer(correctAnswer);

		question.getTypeSpecificQuestion().consolidate("");
		return question;
	}

	/**
	 * Read file contents
	 * 
	 * @param fileUploadResource
	 * @return
	 * @throws Exception
	 */
	private byte[] readDatafromFile(File fileUploadResource) throws Exception
	{
		if (fileUploadResource.exists() && fileUploadResource.isFile())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(fileUploadResource);

				byte buf[] = new byte[(int) fileUploadResource.length()];
				fis.read(buf);
				return buf;
			}
			catch (Exception ex)
			{
				throw ex;
			}
			finally
			{
				if (fis != null) fis.close();
			}
		}
		else
			return null;
	}

	/**
	 * Remove feedback element from the choice and item body so that getTextContent() doesn't bring their content
	 * 
	 * @param itemBodyElement
	 * @return
	 * @throws Exception
	 */
	private Element removeFeedback(Element itemBodyElement) throws Exception
	{
		XPath inlineFeedbackPath = new DOMXPath(".//feedbackInline | .//feedbackBlock");
		List<Element> feedbackElements = inlineFeedbackPath.selectNodes(itemBodyElement);

		if (feedbackElements != null && feedbackElements.size() > 0)
		{
			for (Element fb : feedbackElements)
				fb.setTextContent("");
		}

		return itemBodyElement;
	}

	/**
	 * Import embedded data.
	 * 
	 * @param fileName
	 *        Embedded media file name
	 * @return the embedded resource Id or null if file doesn't exist
	 */
	private Reference transferEmbeddedData(String fileName, String name, String context)
	{
		String addCollectionId = "/private/mneme/" + context + "/docs/";

		try
		{
			name = name.replace("\\", "/");
			if (name.lastIndexOf("/") != -1) name = name.substring(name.lastIndexOf("/") + 1);

			String res_mime_type = name.substring(name.lastIndexOf(".") + 1);
			res_mime_type = ContentTypeImageService.getContentType(res_mime_type);
			byte[] content_data = readDatafromFile(new File(fileName));
			if (content_data == null || content_data.length == 0) return null;
			ResourcePropertiesEdit res = ContentHostingService.newResourceProperties();
			res.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

			Reference ref = attachmentService.addAttachment(AttachmentService.MNEME_APPLICATION, context, AttachmentService.DOCS_AREA,
					AttachmentService.NameConflictResolution.keepExisting, name, content_data, res_mime_type, AttachmentService.MNEME_THUMB_POLICY,
					AttachmentService.REFERENCE_ROOT);

			return ref;
		}
		catch (IdUsedException e)
		{
			// return a reference to the existing file
			Reference reference = entityManager.newReference(ContentHostingService.getReference(addCollectionId + name));
			return reference;
		}
		catch (Exception e)
		{
			M_log.debug(e.getMessage());
		}

		return null;
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
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		threadLocalManager = service;
	}

}
