/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-impl/impl/src/java/org/etudes/mneme/impl/ExportQtiServiceImpl.java $
 * $Id: ExportQtiServiceImpl.java 11231 2015-07-13 21:00:02Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2013, 2014, 2015 Etudes, Inc.
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

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentParts;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.ExportQtiService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.impl.LikertScaleQuestionImpl.LikertScaleQuestionChoice;
import org.etudes.mneme.impl.MatchQuestionImpl.MatchQuestionPair;
import org.etudes.mneme.impl.MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice;
import org.etudes.mneme.impl.OrderQuestionImpl.OrderQuestionChoice;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS; 
import org.w3c.dom.ls.LSSerializer;

/**
 * <p>
 * ImportQtiServiceImpl implements ImportQtiService
 * </p>
 */
public class ExportQtiServiceImpl implements ExportQtiService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ExportQtiServiceImpl.class);

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService = null;

	/** Messages bundle name. */
	protected String bundle = null;

	/** Messages. */
	protected transient InternationalizedMessages messages = null;

	/** Dependency: QuestionService */
	protected QuestionService questionService = null;

	/** Dependency: SecurityService */
	protected org.sakaiproject.authz.api.SecurityService securityServiceSakai = null;

	protected ContentHostingService contentHostingService = null;

	private class PoolDetails
	{
		String pool_id;
		String pool_title;

		public PoolDetails(String pool_id, String pool_title)
		{
			this.pool_id = pool_id;
			this.pool_title = pool_title;
		}

		public String getPool_id()
		{
			return pool_id;
		}

		public String getPool_title()
		{
			return pool_title;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((pool_id == null) ? 0 : pool_id.hashCode());
			result = prime * result + ((pool_title == null) ? 0 : pool_title.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PoolDetails other = (PoolDetails) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (pool_id == null)
			{
				if (other.pool_id != null) return false;
			}
			else if (!pool_id.equals(other.pool_id)) return false;
			if (pool_title == null)
			{
				if (other.pool_title != null) return false;
			}
			else if (!pool_title.equals(other.pool_title)) return false;
			return true;
		}

		private ExportQtiServiceImpl getOuterType()
		{
			return ExportQtiServiceImpl.this;
		}
		
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * Get the random id
	 * 
	 * @return
	 */
	String getUUID()
	{
		return IdManager.createUuid();
	}

	/**
	 * Create Manifest file root element
	 * 
	 * @param doc
	 * @return
	 */
	public Element createManifest(Document doc)
	{
		Element root = doc.createElementNS("http://www.imsglobal.org/xsd/imscp_v1p1", "manifest");
		root.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("xsi:schemaLocation", "http://www.imsglobal.org/xsd/imscp_v1p1 http://www.imsglobal.org/xsd/qti/qtiv2p1/qtiv2p1_imscpv1p2_v1p0.xsd http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/qti/qtiv2p1/imsqti_v2p1p1.xsd");
		root.setAttribute("xmlns:imsqti", "http://www.imsglobal.org/xsd/imsqti_v2p1");
		root.setAttribute("xmlns:imsmd", "http://www.imsglobal.org/xsd/imsmd_v1p2");
		root.setAttribute("xmlns", "http://www.imsglobal.org/xsd/imscp_v1p1");
		root.setAttribute("identifier", "Manifest-" + getUUID());
		return root;
	}

	/**
	 * Creates the Metadata element for the manifest file which mainly includes the title of the pool
	 * 
	 * @param doc
	 * @return
	 */
	public Element createManifestMetadata(Document doc, String title)
	{
		Element metadata = doc.createElementNS("http://www.imsglobal.org/xsd/imscp_v1p1", "metadata");

		// schema element
		Element schema = doc.createElementNS("http://www.imsglobal.org/xsd/imscp_v1p1", "schema");

		schema.setTextContent("IMS Content");
		metadata.appendChild(schema);

		// schema version element
		Element schemaVersion = doc.createElementNS("http://www.imsglobal.org/xsd/imscp_v1p1", "schemaversion");
		schemaVersion.setTextContent("2.1");
		metadata.appendChild(schemaVersion);
		Element lom = createLomElement(doc, null, title, null, null, "", null);
		metadata.appendChild(lom);

		return metadata;
	}

	/**
	 * Create resource element for each pool and its corresponding file element. resource element also lists all embedded data files. <resource identifier="RES-B38DF83F-A291-86DA-4EC3-B2CEBD1515A4" type="imsqti_test_xmlv2p1" href="choice.xml">
	 * 
	 * @param doc
	 * @param count
	 * @param poolFile
	 * @return
	 */
	public Element createResourceElementforAssessment(ZipOutputStream zip, Document doc, int count, Assessment test)
	{
		String titleFile = test.getId() + "/assessment" + count + ".xml";

		Element resource = doc.createElement("resource");
		resource.setAttribute("identifier", "Resource" + count);
		resource.setAttribute("type", "imsqti_test_xmlv2p1");
		resource.setAttribute("href", titleFile);

		ArrayList<String> mediaFiles = new ArrayList<String>();
		Element metadata = doc.createElement("metadata");
		String subFolder = test.getId() + "/Resources/";
		metadata.appendChild(createLomElement(doc, test.getType().toString(), test.getTitle(), (test.getPresentation() != null) ? test
				.getPresentation().getText() : null, zip, subFolder, mediaFiles));

		resource.appendChild(metadata);

		Element file = doc.createElement("file");
		file.setAttribute("href", titleFile);
		resource.appendChild(file);

		if (mediaFiles.size() > 0)
		{
			for (String m : mediaFiles)
			{
				Element mediafile = doc.createElement("file");
				mediafile.setAttribute("href", m);
				resource.appendChild(mediafile);
			}
		}

		return resource;
	}

	/**
	 * Creates ims manifest resource element for a question.
	 * 
	 * @param doc
	 *        Manifest Document
	 * @param question
	 *        Question
	 * @return resource Element
	 */
	public Element createResourceElementforQuestion(Document doc, String testId, Question question, List<String> embedMedia)
	{
		String titleFile = testId + "/question" + question.getId() + ".xml";

		Element resource = doc.createElement("resource");
		resource.setAttribute("identifier", question.getPool().getId()+":" + question.getId());
		resource.setAttribute("type", "imsqti_item_xmlv2p1");
		resource.setAttribute("href", titleFile);
		
		Element metadata = doc.createElement("metadata");

		// if question is a survey add imsmd:identifier similar to test type
		Boolean survey = question.getIsSurvey();
		if (Boolean.TRUE.equals(survey)) metadata.appendChild(createLomElement(doc, "survey", "", null, null, "", null));

		metadata.appendChild(createQTIMetadataElement(doc, question.getType()));
		resource.appendChild(metadata);

		Element file = doc.createElement("file");
		file.setAttribute("href", titleFile);
		resource.appendChild(file);

		// add embeds in resource element as file elements
		for (String media : embedMedia)
		{
			Element fileElement = doc.createElement("file");
			fileElement.setAttribute("href", media);
			resource.appendChild(fileElement);
		}
		return resource;
	}

	/**
	 * 
	 * @param doc
	 * @param type
	 * @param title
	 * @param description
	 * @param zip
	 * @param mediaFiles
	 * @return
	 */
	public Element createLomElement(Document doc, String type, String title, String description, ZipOutputStream zip, String subFolder,
			List<String> mediaFiles)
	{
		Element lom = doc.createElementNS("http://www.imsglobal.org/xsd/imsmd_v1p2", "imsmd:lom");
		Element general = doc.createElementNS("http://www.imsglobal.org/xsd/imsmd_v1p2", "imsmd:general");

		// <imsmd:identifier>QUE_1106</imsmd:identifier>
		if (type != null && type.length() > 0)
		{
			Element typeElement = doc.createElementNS("http://www.imsglobal.org/xsd/imsmd_v1p2", "imsmd:identifier");
			typeElement.setTextContent(type);
			general.appendChild(typeElement);
		}
		if (title != null && title.length() > 0)
		{
			Element titleElement = doc.createElementNS("http://www.imsglobal.org/xsd/imsmd_v1p2", "imsmd:title");
			Element langstring = doc.createElementNS("http://www.imsglobal.org/xsd/imsmd_v1p2", "imsmd:langstring");
			title = FormattedText.unEscapeHtml(title);
			langstring.appendChild(doc.createCDATASection(title));
			titleElement.appendChild(langstring);
			general.appendChild(titleElement);
		}
		if (description != null && description.length() > 0)
		{
			Element descElement = doc.createElementNS("http://www.imsglobal.org/xsd/imsmd_v1p2", "imsmd:description");
			Element langstring = doc.createElementNS("http://www.imsglobal.org/xsd/imsmd_v1p2", "imsmd:langstring");
			description = FormattedText.unEscapeHtml(description);
			if (zip != null && mediaFiles != null) description = translateEmbedData(zip, subFolder, subFolder, description, mediaFiles);
			langstring.appendChild(doc.createCDATASection(description));
			descElement.appendChild(langstring);
			general.appendChild(descElement);
		}
		lom.appendChild(general);
		return lom;
	}

	/**
	 * create outcomeDeclaration element which contains the score
	 * 
	 * @param document
	 * @param points
	 * @return
	 */
	private Element createOutcomeElement(Document document, String identifier, String baseType, String points)
	{
		Element outcomeDeclarationElement = document.createElement("outcomeDeclaration");
		outcomeDeclarationElement.setAttribute("baseType", baseType);
		outcomeDeclarationElement.setAttribute("cardinality", "single");
		outcomeDeclarationElement.setAttribute("identifier", identifier);
		if (points != null && points.length() > 0)
		{
			Element defaultValueElement = document.createElement("defaultValue");
			Element valueElement = document.createElement("value");
			valueElement.setTextContent(points);
			defaultValueElement.appendChild(valueElement);
			outcomeDeclarationElement.appendChild(defaultValueElement);
		}
		return outcomeDeclarationElement;
	}

	/**
	 * Creates response declaration element.
	 * 
	 * @param questionDocument
	 * @param indentifier
	 * @param cardinality
	 * @param basetype
	 * @return
	 */
	private Element createResponseDeclaration(Document questionDocument, String indentifier, String cardinality, String basetype)
	{
		Element responseDeclaration = questionDocument.createElement("responseDeclaration");
		responseDeclaration.setAttribute("identifier", indentifier);
		responseDeclaration.setAttribute("cardinality", cardinality);
		responseDeclaration.setAttribute("baseType", basetype);
		return responseDeclaration;
	}

	/**
	 * 
	 * @param assessmentTestDocument
	 * @param tries
	 * @param reviewOptions
	 * @param showHints
	 * @param showModelAnswer
	 * @return
	 */
	private Element createItemSessionElement(Document assessmentTestDocument, String tries, String reviewOptions, String showHints,
			boolean showModelAnswer)
	{
		// <itemSessionControl maxAttempts="0"/>
		Element itemSessionControlElement = assessmentTestDocument.createElement("itemSessionControl");
		if (!tries.equals("")) itemSessionControlElement.setAttribute("maxAttempts", tries);

		// review options
		if (reviewOptions.equalsIgnoreCase("Never"))
			itemSessionControlElement.setAttribute("allowReview", "false");
		else
			itemSessionControlElement.setAttribute("allowReview", "true");

		// show hints
		itemSessionControlElement.setAttribute("showFeedback", showHints);

		// show model answer
		if (showModelAnswer) itemSessionControlElement.setAttribute("showSolution", "true");

		return itemSessionControlElement;
	}
	
	/**
	 * 
	 * @param assessmentTestDocument
	 * @param partTitle
	 * @param partsCount
	 * @param partRandomize
	 * @return
	 */
	private Element createSectionElement(Document assessmentTestDocument, String partTitle, int partsCount, boolean partRandomize)
	{
		String sectionTitle = (partTitle != null && partTitle.length() > 0) ? partTitle : "";
		Element assessmentSectionElement = assessmentTestDocument.createElement("assessmentSection");
		assessmentSectionElement.setAttribute("identifier", "Section" + partsCount);
		assessmentSectionElement.setAttribute("title", sectionTitle);
		assessmentSectionElement.setAttribute("visible", "true");

		// randomize <ordering shuffle="true"/>
		if (partRandomize)
		{
			Element orderingElement = assessmentTestDocument.createElement("ordering");
			orderingElement.setAttribute("shuffle", "true");
			assessmentSectionElement.appendChild(orderingElement);
		}
		return assessmentSectionElement;
	}

	/**
	 * create final message element.
	 * 
	 * @param assessmentTestDocument
	 * @param text
	 * @param zip
	 *        The zip output stream for export
	 * @param testId
	 *        test id of folder to export to
	 * @return
	 */
	private Element createFinalFeedbackElement(Document assessmentTestDocument, String text, ZipOutputStream zip, String testId)
	{
		Element testFeedbackElement = assessmentTestDocument.createElement("testFeedback");
		testFeedbackElement.setAttribute("access", "atEnd");
		testFeedbackElement.setAttribute("showHide", "hide");
		testFeedbackElement.setAttribute("identifier", "FB_Total");

		Element feedbackContentElement = assessmentTestDocument.createElement("div");
		ArrayList fbFiles = new ArrayList<String>();
		text = translateEmbedData(zip,  testId + "/Resources/", "Resources/", text, fbFiles);
		
		feedbackContentElement.appendChild(assessmentTestDocument.createCDATASection(text));
		testFeedbackElement.appendChild(feedbackContentElement);
		return testFeedbackElement;
	}


	/**
	 * Create QTI meta data element
	 * 
	 * @param doc
	 * @param questionType
	 * @return
	 */
	private Element createQTIMetadataElement(Document doc, String questionType)
	{
		Element qtiMetaData = doc.createElementNS("http://www.imsglobal.org/xsd/imsqti_v2p1", "imsqti:qtiMetadata");

		Element itemTemplate = doc.createElementNS("http://www.imsglobal.org/xsd/imsqti_v2p1", "imsqti:itemTemplate");
		itemTemplate.setTextContent("false");
		qtiMetaData.appendChild(itemTemplate);

		Element composite = doc.createElementNS("http://www.imsglobal.org/xsd/imsqti_v2p1", "imsqti:composite");
		composite.setTextContent("false");
		qtiMetaData.appendChild(composite);

		Element interactionType = doc.createElementNS("http://www.imsglobal.org/xsd/imsqti_v2p1", "imsqti:interactionType");
		String interaction = "";
		if (("mneme:MultipleChoice").equals(questionType) || ("mneme:TrueFalse").equals(questionType) || ("mneme:LikertScale").equals(questionType))
			interaction = "choiceInteraction";
		if (("mneme:Essay").equals(questionType)) interaction = "extendedTextInteraction";
		if (("mneme:Match").equals(questionType)) interaction = "matchInteraction";
		if (("mneme:FillBlanks").equals(questionType)) interaction = "textEntryInteraction";
		if (("mneme:FillInline").equals(questionType)) interaction = "inlineChoiceInteraction";
		if (("mneme:Order").equals(questionType)) interaction = "orderInteraction";

		interactionType.setTextContent(interaction);
		qtiMetaData.appendChild(interactionType);

		Element feedbackType = doc.createElementNS("http://www.imsglobal.org/xsd/imsqti_v2p1", "imsqti:feedbackType");
		feedbackType.setTextContent("none");
		qtiMetaData.appendChild(feedbackType);

		return qtiMetaData;
	}

	/**
	 * {@inheritDoc}
	 */
	public void exportAssessments(String context, String[] ids, ZipOutputStream zip) throws AssessmentPermissionException, IOException
	{
		// create manifest element
		Document doc = Xml.createDocument();
		Element manifestRoot = createManifest(doc);
		doc.appendChild(manifestRoot);
		String poolTitle = "";
		ArrayList<PoolDetails> pools = new ArrayList<PoolDetails>();

		// create organization element
		if (ids == null || ids.length == 0) return;
		Element organizations = doc.createElement("organizations");
		manifestRoot.appendChild(organizations);

		// create resources parent element
		Element resources = doc.createElement("resources");
		manifestRoot.appendChild(resources);

		int count = 0;
		for (String pId : ids)
		{
			Assessment test = this.assessmentService.getAssessment(pId);
			if (test == null) continue;

			// 2. add the resource element entry in manifest file
			Element resourceAssessment = createResourceElementforAssessment(zip, doc, ++count, test);
			resourceAssessment = getAttachments(zip, test.getId(), doc, resourceAssessment, test);

			// 3. create assessmentTest document
			String resourceAssessmentIdent = resourceAssessment.getAttribute("identifier");
			String assessmentFileName = resourceAssessment.getAttribute("href");
			HashMap<String, List<Element>> miscellaneousItems = createAssessmentDocument(context, test, doc, resourceAssessmentIdent,
					assessmentFileName, zip, pools);

			List<Element> items = (miscellaneousItems.containsKey("questionItems")) ? miscellaneousItems.get("questionItems") : null;
			List<String> questionIds = new ArrayList<String>();
			// 4. add dependency to test element and question as a resource
			// <dependency identifierref="RES-BCA84FC0-53F9-ABBD-C3FE-BDB5B825CA9E"/>
			if (items != null)
			{
				for (Element item : items)
				{
					Element dependecyElement = doc.createElement("dependency");
					dependecyElement.setAttribute("identifierref", item.getAttribute("identifier"));
					questionIds.add(item.getAttribute("identifier"));
					resourceAssessment.appendChild(dependecyElement);
					resources.appendChild(item);
				}
			}
			resources.appendChild(resourceAssessment);
		}

		// write pools information
		for (PoolDetails p : pools)
		{
			poolTitle = poolTitle.concat(" " + p.getPool_title());
			Element poolElement = doc.createElement("resource");
			poolElement.setAttribute("title", p.getPool_title());
			poolElement.setAttribute("identifier", "POOL" + p.getPool_id());
			resources.appendChild(poolElement);
		}
		
		// write manifest file
		if ("".equals(poolTitle)) poolTitle = "defaultPool";
		manifestRoot.appendChild(createManifestMetadata(doc, poolTitle));
		manifestRoot.appendChild(resources);
		writeDocumentToZip(zip, null, "imsmanifest.xml", doc);

	}

	/**
	 * 
	 * @param details
	 * @param assessmentTestDocument
	 * @param assessmentSectionElement
	 * @param drawPools
	 * @param count
	 * @return
	 */
	private Element findDrawPools(List<PartDetail> details, Document assessmentTestDocument, Element assessmentSectionElement,
			List<PartDetail> drawPools, Integer count)
	{
		boolean randomDraw = false;
		for (PartDetail detail : details)
		{
			if (detail.getType().contains("Draw"))
			{
				randomDraw = true;
				drawPools.add(detail);
			}
		}

		if (randomDraw)
		{
			// <selection select="2"/>
			Element selection = assessmentTestDocument.createElement("selection");
			selection.setAttribute("select", count.toString());
			assessmentSectionElement.appendChild(selection);
		}
		return assessmentSectionElement;
	}
	
	/**
	 * Creates the assessment.xml file and writes to the zip package
	 * 
	 * @param test
	 * @param resourceAssessmentIdent
	 * @param assessmentFileName
	 * @param zip
	 * @return
	 */
	public HashMap<String, List<Element>> createAssessmentDocument(String context, Assessment test, Document doc, String resourceAssessmentIdent,
			String assessmentFileName, ZipOutputStream zip, ArrayList<PoolDetails> pools)
	{
		Document assessmentTestDocument = Xml.createDocument();
		Element assessmentTestElement = assessmentTestDocument.createElement("assessmentTest");

		assessmentTestElement.setAttribute("xmlns", "http://www.imsglobal.org/xsd/imsqti_v2p1");
		assessmentTestElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		assessmentTestElement.setAttribute("xsi:schemaLocation", "http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/qti/qtiv2p1/imsqti_v2p1p1.xsd");
		
		HashMap<String, List<Element>> miscellaneousItems = new HashMap<String, List<Element>>();
		HashMap<String, Element> questionsList = new HashMap<String, Element>();

		assessmentTestElement.setAttribute("identifier", resourceAssessmentIdent);
		assessmentTestElement.setAttribute("title", test.getTitle());
		float pointsValue = 0;
		String points = "10";
		String tries = "";
		String navigationMode = (test.getRandomAccess()) ? "nonlinear" : "linear";
		String submissionMode = "simultaneous";
		String reviewOptions = "Never";
		String showHints = "false";
		boolean randomDraw = false;

		if (test.getHasTriesLimit()) tries = test.getTries().toString();
		if (test.getReview() != null && test.getReview().getTiming() != null) reviewOptions = test.getReview().getTiming().toString();
		if (test.getShowHints() != null) showHints = test.getShowHints().toString();

		// <timeLimits maxTime="" allowLateSubmission="0 or 1"/>
		if (test.getHasTimeLimit())
		{
			Element limitElement = assessmentTestDocument.createElement("timeLimits");
			limitElement.setAttribute("maxTime", test.getTimeLimit().toString());
			limitElement.setAttribute("allowLateSubmission", "0");
			assessmentTestElement.appendChild(limitElement);
		}

		if (test.getType() == AssessmentType.offline) points = String.valueOf(test.getPoints());
		// create outcomeDeclaration
		Element outcomeDeclarationElement = createOutcomeElement(assessmentTestDocument, "SCORE", "float", points);
		if (outcomeDeclarationElement != null) assessmentTestElement.appendChild(outcomeDeclarationElement);
		
		assessmentTestElement = createOtherSettingsOutcomeElements(assessmentTestDocument, assessmentTestElement, test);
			
		// testPart
		AssessmentParts assessmentParts = test.getParts();
		
		// part numbering
		if (!assessmentParts.getContinuousNumbering())
		{
			Element outcomeDeclarationPartNumberingElement = createOutcomeElement(assessmentTestDocument, "PartNumbering", "string", assessmentParts
					.getContinuousNumbering().toString());
			if (outcomeDeclarationPartNumberingElement != null) assessmentTestElement.appendChild(outcomeDeclarationPartNumberingElement);
		}
		
		List<Part> parts = assessmentParts.getParts();
		
		int partsCount = 1;

		for (Part p : parts)
		{
			//get the master pool and questions id. 
			for (Iterator<PartDetail> i = p.getDetails().iterator(); i.hasNext();)
			{
				PartDetail detail = i.next();
				if (!detail.restoreToOriginal(null, null))
				{
					i.remove();
				}
			}
					
			pointsValue = pointsValue + p.getTotalPoints();
			// <testPart identifier="part01" navigationMode="nonlinear" submissionMode="simultaneous">
			Element testPartElement = assessmentTestDocument.createElement("testPart");
			testPartElement.setAttribute("identifier", p.getId());
			testPartElement.setAttribute("navigationMode", navigationMode);
			testPartElement.setAttribute("submissionMode", submissionMode);

			Element itemSession = createItemSessionElement(assessmentTestDocument, tries, reviewOptions, showHints, test.getShowModelAnswer());
			testPartElement.appendChild(itemSession);

			// <assessmentSection identifier="sectionA" title="Section A" visible="true">
			Element assessmentSectionElement = createSectionElement(assessmentTestDocument, p.getTitle(), partsCount++, p.getRandomize());

			// rubricBlock
			Element rubricElement = assessmentTestDocument.createElement("rubricBlock");
			if (p.getPresentation() != null && p.getPresentation().getText() != null)
			{
				String partDescription = p.getPresentation().getText();
				ArrayList<String> mediaFiles = new ArrayList<String>();
				String subFolder = test.getId() + "/Resources/";
				if (zip != null && mediaFiles != null) partDescription = translateEmbedData(zip, subFolder,"Resources/", partDescription, mediaFiles);
				rubricElement.setTextContent(partDescription);
			}
			assessmentSectionElement.appendChild(rubricElement);

			// create AssessmentItem document for the question
			int question_count = 1;
			List<Question> questions = p.getQuestions();
			List<PartDetail> details = p.getDetails();
			List<PartDetail> drawPools = new ArrayList<PartDetail>();
	
			assessmentSectionElement = findDrawPools(details, assessmentTestDocument, assessmentSectionElement, drawPools, p.getNumQuestions());
			if (drawPools.size() > 0) randomDraw = true;
			
			for (Question question : questions)
			{
				try
				{
					String questionPoolId = question.getPool().getId();
					PoolDetails questionPool = new PoolDetails(questionPoolId, question.getPool().getTitle());
					if (!pools.contains(questionPool)) pools.add(questionPool);

					// create question.xml file 
					ArrayList<String> mediaFiles = new ArrayList<String>();
					Element assessmentItem = createAssessmentItemforQuestion(zip, context, test.getId(), mediaFiles, question, question.getPartDetail().getQuestionPoints(), question_count++);
					if (assessmentItem == null) continue;
					Element itemResourceElement = createResourceElementforQuestion(doc, test.getId(), question, mediaFiles);
					itemResourceElement = getAttachments(zip, test.getId(), doc, itemResourceElement, question);
					questionsList.put(question.getId(), itemResourceElement);

					// add assessmentItemRef -- <assessmentItemRef identifier="item034" href="adaptive.xml">
					Element assessmentItemRefElement = assessmentTestDocument.createElement("assessmentItemRef");
					assessmentItemRefElement.setAttribute("identifier", questionPoolId+":"+question.getId());
					assessmentItemRefElement.setAttribute("href", "question" + question.getId() + ".xml");

					if (randomDraw && !question.getPartDetail().getType().contains("Draw"))
						assessmentItemRefElement.setAttribute("required", "true");
					else if (randomDraw && question.getPartDetail().getType().contains("Draw"))
						assessmentItemRefElement.setAttribute("required", "false");
					
					assessmentSectionElement.appendChild(assessmentItemRefElement);
				}
				catch (Exception e)
				{
					M_log.debug("Export qti2:" + e.getMessage());
				}
			} // questions for

			// if random add other questions from the pool to draw from
			for (PartDetail pd : drawPools)
			{
				Float pdQuestionPoints = pd.getEffectivePoints()/pd.getNumQuestions();
				
				List<Element> otherResources = addOtherPoolQuestions(doc, assessmentTestDocument, questionsList, pd.getPool(), zip, context, test.getId(), pdQuestionPoints, question_count++);
				for (Element r : otherResources)
					assessmentSectionElement.appendChild(r);
				question_count = question_count + otherResources.size();
			}
			
			ArrayList<Element> items = new ArrayList<Element>();
			for (Element e : questionsList.values())
				items.add(e);
			
			miscellaneousItems.put("questionItems", items);
			testPartElement.appendChild(assessmentSectionElement);
			assessmentTestElement.appendChild(testPartElement);
		} // parts end

		if (pointsValue > 0)
		{
			NodeList value = outcomeDeclarationElement.getElementsByTagName("value");
			Element valueElement = (Element) value.item(0);
			valueElement.setTextContent(new Float(pointsValue).toString());
		}

		// outcomeProcessing
		Element outcomeProcessingElement = assessmentTestDocument.createElement("outcomeProcessing");
		assessmentTestElement.appendChild(outcomeProcessingElement);

		// Final Message
		if (test.getSubmitPresentation() != null && test.getSubmitPresentation().getText() != null)
		{
			Element testFeedbackElement = createFinalFeedbackElement(assessmentTestDocument, test.getSubmitPresentation().getText(), zip, test.getId());
			assessmentTestElement.appendChild(testFeedbackElement);
		}

		assessmentTestDocument.appendChild(assessmentTestElement);
		// 4. write assessment.xml to the zip file
		writeDocumentToZip(zip, test.getId() + "/", assessmentFileName, assessmentTestDocument);

		return miscellaneousItems;
	}

	/**
	 * Creates the question.xml file and writes to the zip package
	 * 
	 * @param zip
	 * @param question
	 * @param count
	 * @return
	 * @throws Exception
	 */
	public Element createAssessmentItemforQuestion(ZipOutputStream zip, String context, String testId, List<String> mediaFiles, Question question,
			float points, int count) throws Exception
	{
		if (question == null) return null;

		String fileTitle = testId + "/question" + question.getId() + ".xml";
		String title;

		if (question.getTitle() != null && question.getTitle().trim().length() > 0) title = question.getTitle();
		else title = "question" + count;

		Document assessmentItemDocument = Xml.createDocument();
		Element item = assessmentItemDocument.createElement("assessmentItem");
		item.setAttribute("xmlns", "http://www.imsglobal.org/xsd/imsqti_v2p1");
		item.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		item.setAttribute("xsi:schemaLocation", "http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/qti/qtiv2p1/imsqti_v2p1p1.xsd");
		
		item.setAttribute("identifier", question.getPool().getId() + ":"+ question.getId());
		item.setAttribute("title", title);
		item.setAttribute("adaptive", "false");
		item.setAttribute("timeDependent", "false");

		Map<String, Element> map_question = getallQuestionElements(zip, assessmentItemDocument, testId, question, context, mediaFiles);

		// <responseDeclaration identifier="RESPONSE" cardinality="single" baseType="identifier">
		if (map_question.containsKey("responseDeclarationCount"))
		{
			// fill blanks have multiple response declare
			int responseCount = Integer.parseInt(map_question.get("responseDeclarationCount").getTextContent());
			for (int i = 1; i < responseCount; i++)
			{
				if (map_question.containsKey("responseDeclaration" + i)) item.appendChild(map_question.get("responseDeclaration" + i));
			}
		}
		else if (map_question.containsKey("responseDeclaration")) item.appendChild(map_question.get("responseDeclaration"));

		// create outcomeDeclaration
		item.appendChild(createOutcomeElement(assessmentItemDocument,"SCORE","float", new Float(points).toString()));

		// <itemBody>
		if (map_question.containsKey("itemBody")) item.appendChild(map_question.get("itemBody"));

		// responseProcessing
		if (map_question.containsKey("responseProcessing")) item.appendChild(map_question.get("responseProcessing"));

		// modal feedback
		if (map_question.containsKey("modalFeedback")) item.appendChild(map_question.get("modalFeedback"));

		assessmentItemDocument.appendChild(item);

		writeDocumentToZip(zip, null, fileTitle, assessmentItemDocument);

		return item;
	}

	/**
	 * create outcome declarations for other settings
	 * @param assessmentTestDocument
	 * @param test
	 * @return
	 */
	private Element createOtherSettingsOutcomeElements(Document assessmentTestDocument, Element assessmentTestElement, Assessment test)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		
		// pass percentage
		if (test.getMinScoreSet())
		{
			Element outcomeDeclarationPassedElement = createOutcomeElement(assessmentTestDocument, "PASSFACTOR", "float", test.getMinScore()
					.toString());
			if (outcomeDeclarationPassedElement != null) assessmentTestElement.appendChild(outcomeDeclarationPassedElement);
		}

		//review show summary
		if (test.getReview().getShowSummary())
		{
			Element outcomeDeclarationShowSummaryElement = createOutcomeElement(assessmentTestDocument, "ReviewShowSummary", "string", test
					.getReview().getShowSummary().toString());
			if (outcomeDeclarationShowSummaryElement != null) assessmentTestElement.appendChild(outcomeDeclarationShowSummaryElement);
		}
		
		//review show feedback
		if (test.getReview().getShowFeedback())
		{
			Element outcomeDeclarationShowFeedbackElement = createOutcomeElement(assessmentTestDocument, "ReviewShowFeedback", "string", test
					.getReview().getShowFeedback().toString());
			if (outcomeDeclarationShowFeedbackElement != null) assessmentTestElement.appendChild(outcomeDeclarationShowFeedbackElement);
		}
		
		// show correct answer
		Element outcomeDeclarationShowAnswerElement = createOutcomeElement(assessmentTestDocument, "ReviewCorrectAnswer", "string", test.getReview()
				.getShowCorrectAnswer().toString());
		if (outcomeDeclarationShowAnswerElement != null) assessmentTestElement.appendChild(outcomeDeclarationShowAnswerElement);	

		Element outcomeDeclarationReleaseElement = createOutcomeElement(assessmentTestDocument, "AutoRelease", "string", test.getGrading().getAutoRelease().toString());
		if (outcomeDeclarationReleaseElement != null) assessmentTestElement.appendChild(outcomeDeclarationReleaseElement);

		Element outcomeDeclarationIntegrationElement = createOutcomeElement(assessmentTestDocument, "GradebookIntegration", "string", test.getGradebookIntegration().toString());
		if (outcomeDeclarationIntegrationElement != null) assessmentTestElement.appendChild(outcomeDeclarationIntegrationElement);

		// anonymous grading
		if (test.getAnonymous())
		{
			Element outcomeDeclarationAnonymousElement = createOutcomeElement(assessmentTestDocument, "AnonymousGrading", "string", test
					.getAnonymous().toString());
			if (outcomeDeclarationAnonymousElement != null) assessmentTestElement.appendChild(outcomeDeclarationAnonymousElement);
		}

		// question grouping
		Element outcomeDeclarationGroupingElement = createOutcomeElement(assessmentTestDocument, "QuestionLayout", "string", test
				.getQuestionGrouping().toString());
		if (outcomeDeclarationGroupingElement != null) assessmentTestElement.appendChild(outcomeDeclarationGroupingElement);

		if (test.getResultsEmail() != null && test.getResultsEmail().trim().length() > 0)
		{
			Element outcomeDeclarationEmailElement = createOutcomeElement(assessmentTestDocument, "ResultsEmail", "string", test
					.getResultsEmail());
			if (outcomeDeclarationEmailElement != null) assessmentTestElement.appendChild(outcomeDeclarationEmailElement);
		}
		
		if (test.getPassword().getPassword() != null && test.getPassword().getPassword().trim().length() > 0)
		{
			Element outcomePasswordElement = createOutcomeElement(assessmentTestDocument, "Password", "string", test
					.getPassword().getPassword());
			if (outcomePasswordElement != null) assessmentTestElement.appendChild(outcomePasswordElement);
		}

		// honor pledge
		if (test.getRequireHonorPledge())
		{
			Element outcomeDeclarationPledgeElement = createOutcomeElement(assessmentTestDocument, "HonorPledge", "string", test
					.getRequireHonorPledge().toString());
			if (outcomeDeclarationPledgeElement != null) assessmentTestElement.appendChild(outcomeDeclarationPledgeElement);
		}

		// shuffle choices for all MCs
		if (test.getShuffleChoicesOverride())
		{
			Element outcomeDeclarationShuffleElement = createOutcomeElement(assessmentTestDocument, "ShuffleChoicesOverride", "string", test
					.getShuffleChoicesOverride().toString());
			if (outcomeDeclarationShuffleElement != null) assessmentTestElement.appendChild(outcomeDeclarationShuffleElement);
		}

		// dates
		if (test.getDates() != null)
		{
			if (test.getDates().getOpenDate() != null)
			{
				Date open = test.getDates().getOpenDate();

				Element outcomeDeclarationOpenElement = createOutcomeElement(assessmentTestDocument, "OpenDate", "string", sdf.format(open));
				if (outcomeDeclarationOpenElement != null) assessmentTestElement.appendChild(outcomeDeclarationOpenElement);
			}

			if (test.getDates().getHideUntilOpen() != null)
			{
				Element outcomeDeclarationHideOpenElement = createOutcomeElement(assessmentTestDocument, "HideUntilOpen", "string", test.getDates().getHideUntilOpen().toString());
				if (outcomeDeclarationHideOpenElement != null) assessmentTestElement.appendChild(outcomeDeclarationHideOpenElement);
			}

			if (test.getDates().getDueDate() != null)
			{
				Date due = test.getDates().getDueDate();

				Element outcomeDeclarationDueElement = createOutcomeElement(assessmentTestDocument, "DueDate", "string", sdf.format(due));
				if (outcomeDeclarationDueElement != null) assessmentTestElement.appendChild(outcomeDeclarationDueElement);
			}

			if (test.getDates().getAcceptUntilDate() != null)
			{
				Date acceptUntil = test.getDates().getAcceptUntilDate();

				Element outcomeDeclarationSubmitUntilElement = createOutcomeElement(assessmentTestDocument, "AcceptUntil", "string",
						sdf.format(acceptUntil));
				if (outcomeDeclarationSubmitUntilElement != null) assessmentTestElement.appendChild(outcomeDeclarationSubmitUntilElement);
			}
		}

		return assessmentTestElement;
	}
		
	/**
	 * 
	 * @param resourceDocument
	 * @param assessmentDocument
	 * @param questionsList
	 * @param pool
	 * @return 
	 * 	List of AssessmentItemRef elements
	 */
	private List<Element> addOtherPoolQuestions(Document resourceDocument, Document assessmentDocument, HashMap<String, Element> questionsList,
			Pool pool, ZipOutputStream zip, String context, String testId, float detailPoints, int questionCount)
	{
		// get all pool questions
		List<String> poolQuestions = pool.getAllQuestionIds(null, true);
		List<Element> otherRandomQuestions = new ArrayList<Element>();

		for (String chkId : poolQuestions)
		{
			try
			{
				// if not in questionsList
				if (questionsList.containsKey(chkId)) continue;

				// create question.xml file
				Question question = questionService.getQuestion(chkId);

				ArrayList<String> mediaFiles = new ArrayList<String>();
				Element assessmentItem = createAssessmentItemforQuestion(zip, context, testId, mediaFiles, question, detailPoints, questionCount++);
				if (assessmentItem == null) continue;

				// add to resourceDocument
				Element itemResourceElement = createResourceElementforQuestion(resourceDocument, testId, question, mediaFiles);
				itemResourceElement = getAttachments(zip, testId, resourceDocument, itemResourceElement, question);
				questionsList.put(question.getId(), itemResourceElement);

				// add to assessmentDocument and to arraylist
				Element assessmentItemRefElement = assessmentDocument.createElement("assessmentItemRef");
				assessmentItemRefElement.setAttribute("identifier", pool.getId()+":"+question.getId());
				assessmentItemRefElement.setAttribute("href", "question" + question.getId() + ".xml");
				otherRandomQuestions.add(assessmentItemRefElement);

			}
			catch (Exception e)
			{
			}
		}

		return otherRandomQuestions;
	}

	/**
	 * 
	 * @param zip
	 * @param questionDocument
	 * @param questionResourceElement
	 * @param question
	 * @return
	 */
	public Element getAttachments(ZipOutputStream zip, String testId, Document document, Element questionResourceElement, Question question)
	{
		List<Reference> attachments = question.getPresentation().getAttachments();

		return getCoreAttachments(zip, testId, document, questionResourceElement, attachments);
	}
	
	/**
	 * 
	 * @param zip
	 * @param assessmentDocument
	 * @param assessmentResourceElement
	 * @param assessment
	 * @return
	 */
	public Element getAttachments(ZipOutputStream zip, String testId, Document document, Element assessmentResourceElement, Assessment assessment)
	{
		List<Reference> attachments = assessment.getPresentation().getAttachments();

		return getCoreAttachments(zip, testId, document, assessmentResourceElement, attachments);
	}

	/**
	 * 
	 * @param zip
	 * @param assessmentDocument
	 * @param assessmentResourceElement
	 * @param attachments
	 * @return
	 */
	public Element getCoreAttachments(ZipOutputStream zip, String testId, Document document, Element resourceElement, List<Reference> attachments)
	{
		if (attachments != null && attachments.size() > 0)
		{
			// security advisor
			pushAdvisor();

			String subFolder = testId + "/Resources/";
			for (Reference attachment : attachments)
			{
				try
				{
					// get a usable file name for the attachment					
					String fileName = Validator.getFileName(attachment.getId().replaceAll("%20", " "));
					fileName = fileName.replaceAll("%20", " ");
					fileName = Validator.escapeResourceName(fileName);
					
					writeContentResourceToZip(zip, subFolder, attachment.getId(), fileName);

					Element file = document.createElement("file");
					file.setAttribute("href", subFolder + fileName);
					resourceElement.appendChild(file);
				}
				catch (Exception e)
				{
					M_log.warn("ExportQtiService: zipping attachments: " + e.toString());
				}
			}
			popAdvisor();
		}
		return resourceElement;
	}

	/**
	 * 
	 * @param poolDocument
	 * @param question
	 * @param text
	 * @return
	 */
	public Map<String, Element> getallQuestionElements(ZipOutputStream zip, Document questionDocument, String testId, Question question,
			String context, List<String> mediaFiles) throws Exception
	{
		Map<String, Element> questionParts = new HashMap<String, Element>();

		String text = question.getPresentation().getText();
		Element itemBody = questionDocument.createElement("itemBody");

		// for fill blanks text will be chopped
		if (!"mneme:FillBlanks".equals(question.getType())&&!"mneme:FillInline".equals(question.getType()))
		{
			if (text == null) return questionParts;
			// process embed media and change path as Resources/xxxx.jpg

			// security advisor
			pushAdvisor();
			itemBody = translateEmbedData(zip, testId + "/Resources/", text, itemBody, mediaFiles, questionDocument);
	
			popAdvisor();

			if (mediaFiles.isEmpty())
			{
				itemBody.appendChild(questionDocument.createCDATASection(text));
			}
		}

		// split the text based on <br/>
		if ("mneme:FillBlanks".equals(question.getType()))
		{
			FillBlanksQuestionImpl f = (FillBlanksQuestionImpl) (question.getTypeSpecificQuestion());
			text = f.getText();
			itemBody = getFillBlanksResponseChoices(questionDocument, itemBody, f, questionParts, zip, testId);
		}
		else if ("mneme:FillInline".equals(question.getType()))
		{
			FillInlineQuestionImpl f = (FillInlineQuestionImpl) (question.getTypeSpecificQuestion());
			text = f.getText();
			itemBody = getFillInlineResponseChoices(questionDocument, itemBody, f, questionParts, zip, testId);
		}
		else if ("mneme:TrueFalse".equals(question.getType()))
		{
			getTFResponseChoices(questionDocument, question, questionParts);
			if (questionParts.containsKey("choiceInteraction")) itemBody.appendChild(questionParts.get("choiceInteraction"));
		}
		else if ("mneme:MultipleChoice".equals(question.getType()))
		{
			getMCResponseChoices(questionDocument, question, questionParts, zip, testId);
			if (questionParts.containsKey("choiceInteraction")) itemBody.appendChild(questionParts.get("choiceInteraction"));
		}
		else if ("mneme:Order".equals(question.getType()))
		{
			getOrderResponseChoices(questionDocument, question, questionParts, zip, testId);
			if (questionParts.containsKey("orderInteraction")) itemBody.appendChild(questionParts.get("orderInteraction"));
		}
		else if ("mneme:Match".equals(question.getType()))
		{
			getMatchResponseChoices(questionDocument, question, questionParts, zip, testId);
			if (questionParts.containsKey("matchInteraction")) itemBody.appendChild(questionParts.get("matchInteraction"));
		}
		else if ("mneme:Essay".equals(question.getType()))
		{
			getEssayResponseChoices(questionDocument, question, questionParts, zip, testId);
			if (questionParts.containsKey("extendedTextInteraction")) itemBody.appendChild(questionParts.get("extendedTextInteraction"));
			if (questionParts.containsKey("uploadInteraction")) itemBody.appendChild(questionParts.get("uploadInteraction"));
		}
		else if ("mneme:Task".equals(question.getType()))
		{
			// no model answer and no submission type and no basetype
			Element responseDeclaration = questionDocument.createElement("responseDeclaration");
			responseDeclaration.setAttribute("identifier", "RESPONSE");
			responseDeclaration.setAttribute("cardinality", "single");
			questionParts.put("responseDeclaration", responseDeclaration);
		}
		else if ("mneme:LikertScale".equals(question.getType()))
		{
			// <itemBody class="likert">
			itemBody.setAttribute("class", "likert");
			getLikertScaleResponseChoices(questionDocument, question, questionParts);
			if (questionParts.containsKey("choiceInteraction")) itemBody.appendChild(questionParts.get("choiceInteraction"));
		}

		// Hints are part of item body
		if (question.getHints() != null && question.getHints().length() != 0)
		{
			Element feedbackInlineElement = questionDocument.createElement("feedbackInline");
			feedbackInlineElement.setAttribute("showHide", "hide");
			feedbackInlineElement.setAttribute("identifier", "FB_Hints");
			ArrayList hintFiles = new ArrayList<String>();
			String hints = question.getHints();
			hints = translateEmbedData(zip,  testId + "/Resources/", "Resources/", hints, hintFiles);
			feedbackInlineElement.appendChild(questionDocument.createCDATASection(hints));
			itemBody.appendChild(feedbackInlineElement);
		}

		// question feedback
		if (question.getFeedback() != null && question.getFeedback().length() != 0)
		{
			Element feedbackElement = questionDocument.createElement("modalFeedback");
			feedbackElement.setAttribute("showHide", "hide");
			feedbackElement.setAttribute("identifier", "FB_Question");
			ArrayList fbFiles = new ArrayList<String>();
			String feedback = question.getFeedback();
			feedback = translateEmbedData(zip,  testId + "/Resources/", "Resources/", feedback, fbFiles);
			feedbackElement.appendChild(questionDocument.createCDATASection(feedback));
			questionParts.put("modalFeedback", feedbackElement);
		}
		questionParts.put("itemBody", itemBody);
		return questionParts;
	}

	/**
	 * 
	 * @param questionDocument
	 * @param question
	 * @param questionParts
	 * @param zip
	 *        The zip output stream for export
	 * @param testId
	 *        test id of folder to export to
	 */
	public void getEssayResponseChoices(Document questionDocument, Question question, Map<String, Element> questionParts, ZipOutputStream zip, String testId)
	{
		if (question == null) return;

		EssayQuestionImpl essay = (EssayQuestionImpl) (question.getTypeSpecificQuestion());
		String basetype = ("attachments".equals(essay.getSubmissionType().toString())) ? "file" : "string";

		if ("string".equals(basetype))
		{
			// <extendedTextInteraction responseIdentifier="RESPONSE" expectedLength="200">
			Element interaction = questionDocument.createElement("extendedTextInteraction");
			interaction.setAttribute("responseIdentifier", "RESPONSE");
			interaction.setAttribute("expectedLength", "200");
			questionParts.put("extendedTextInteraction", interaction);
		}
		else if ("file".equals(basetype))
		{
			// <uploadInteraction responseIdentifier="RESPONSE">
			Element interaction = questionDocument.createElement("uploadInteraction");
			interaction.setAttribute("responseIdentifier", "RESPONSE");
			questionParts.put("uploadInteraction", interaction);
		}

		// <responseDeclaration identifier="RESPONSE" cardinality="single" baseType="string"/>
		Element responseDeclaration = createResponseDeclaration(questionDocument, "RESPONSE", "single", basetype);

		// Modal answer
		String answer = essay.getModelAnswer();
		Element correctResponse = questionDocument.createElement("correctResponse");
		responseDeclaration.appendChild(correctResponse);
		if (answer != null && answer.length() > 0)
		{
			answer = FormattedText.unEscapeHtml(answer);
			ArrayList anFiles = new ArrayList<String>();
			answer = translateEmbedData(zip,  testId + "/Resources/", "Resources/", answer, anFiles);
			Element correctResponseValue = questionDocument.createElement("value");			
			correctResponseValue.appendChild(questionDocument.createCDATASection(answer));
			correctResponse.appendChild(correctResponseValue);
		}

		questionParts.put("responseDeclaration", responseDeclaration);
		return;
	}

	/**
	 * 
	 * @param questionDocument
	 * @param itemBody
	 * @param question
	 * @param questionParts
	 * @param zip
	 *        The zip output stream for export
	 * @param testId
	 *        test id of folder to export to
	 * @return
	 */
	public Element getFillBlanksResponseChoices(Document questionDocument, Element itemBody, FillBlanksQuestionImpl question,
			Map<String, Element> questionParts, ZipOutputStream zip, String testId)
	{
		if (question == null) return itemBody;

		// itemBody
		String text = question.getText();

		Pattern p_fillBlanks_curly = Pattern
				.compile("([^{]*.?)(\\{)([^}]*.?)(\\})", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

		Matcher m_fillBlanks = p_fillBlanks_curly.matcher(text);
		StringBuffer sb = new StringBuffer();

		// in each part look for {} fill in the blank symbol and create render_fib tag
		int count = 1;
		while (m_fillBlanks.find())
		{
			String fib = m_fillBlanks.group(1);
			itemBody.appendChild(questionDocument.createCDATASection(fib));

			String fib_curly = m_fillBlanks.group(2);
			Element fbInteraction = questionDocument.createElement("textEntryInteraction");
			fbInteraction.setAttribute("responseIdentifier", "RESPONSE" + (count++));
			fbInteraction.setAttribute("expectedLength", Integer.toString(fib_curly.length()));
			itemBody.appendChild(fbInteraction);
			m_fillBlanks.appendReplacement(sb, "");
		}
		m_fillBlanks.appendTail(sb);

		if (sb.length() > 0)
		{
			ArrayList mediaFiles = new ArrayList<String>();
			itemBody = translateEmbedData(zip, testId + "/Resources/", sb.toString(), itemBody, mediaFiles, questionDocument);
			if (mediaFiles.isEmpty())
			{
				itemBody.appendChild(questionDocument.createCDATASection(sb.toString()));
			}
		}

		// answer
		List<String> correctAnswers = new ArrayList<String>();
		question.parseCorrectAnswers(correctAnswers);

		int responseCount = 1;
		for (String answer : correctAnswers)
		{
			Element responseDeclaration = createResponseDeclaration(questionDocument, "RESPONSE" + responseCount, "single", "string");
			Element correctResponse = questionDocument.createElement("correctResponse");
			Element correctResponseValue = questionDocument.createElement("value");
			answer = FormattedText.unEscapeHtml(answer);
			correctResponseValue.setTextContent(answer);
			correctResponse.appendChild(correctResponseValue);
			responseDeclaration.appendChild(correctResponse);
			questionParts.put("responseDeclaration" + responseCount, responseDeclaration);
			responseCount++;
		}

		Element countDiv = questionDocument.createElement("div");
		countDiv.setTextContent(Integer.toString(responseCount));
		questionParts.put("responseDeclarationCount", countDiv);
		questionParts.put("itemBody", itemBody);
		return itemBody;
	}

	/**
	 * 
	 * @param questionDocument
	 * @param itemBody
	 * @param question
	 * @param questionParts
	 * @param zip
	 *        The zip output stream for export
	 * @param testId
	 *        test id of folder to export to
	 * @return
	 */
	public Element getFillInlineResponseChoices(Document questionDocument, Element itemBody, FillInlineQuestionImpl question,
			Map<String, Element> questionParts, ZipOutputStream zip, String testId)
	{
		if (question == null) return itemBody;

		// itemBody
		String text = question.getText();

		Pattern p_fillInline_curly = Pattern
				.compile("([^{]*.?)(\\{)([^}]*.?)(\\})", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

		Matcher m_fillInline = p_fillInline_curly.matcher(text);
		StringBuffer sb = new StringBuffer();

		List<ArrayList<String>> selectionLists = new ArrayList<ArrayList<String>>();
		List<String> correctAnswers = new ArrayList<String>();
		question.parseSelectionLists(selectionLists, correctAnswers);

		// in each part look for {} fill in the blank symbol and create render_fib tag
		int count = 1;
		while (m_fillInline.find())
		{
			String fib = m_fillInline.group(1);
			Element textDiv = questionDocument.createElement("div");
			textDiv.setTextContent(fib);
			itemBody.appendChild(textDiv);

			String fib_curly = m_fillInline.group(2);

			List<String> choices = selectionLists.get(count - 1);

			// <inlineChoiceInteraction responseIdentifier="RESPONSE" shuffle="false" maxChoices="1">
			Element inlineChoiceInteraction = questionDocument.createElement("inlineChoiceInteraction");
			inlineChoiceInteraction.setAttribute("responseIdentifier", "RESPONSE" + count);

			// <responseDeclaration identifier="RESPONSE" cardinality="single" baseType="identifier">
			Element responseDeclaration = createResponseDeclaration(questionDocument, "RESPONSE" + count, "single", "identifier");
			Element correctResponse = questionDocument.createElement("correctResponse");
			responseDeclaration.appendChild(correctResponse);

			// response choices
			int ccount = 1;
			for (String str : choices)
			{
				// <inlineChoice identifier="ChoiceA">You must stay with your luggage at all times.</inlineChoice>
				Element inlineChoice = questionDocument.createElement("inlineChoice");
				inlineChoice.setAttribute("identifier", "Choice" + Integer.toString(ccount++) + "_" + count + "_" + question.getId());
				str = FormattedText.unEscapeHtml(str);
				inlineChoice.setTextContent(str);
				inlineChoiceInteraction.appendChild(inlineChoice);
			}

			String correct = correctAnswers.get(count - 1);
			// correct answers
			ccount = 1;
			for (String str : choices)
			{
				Element correctResponseValue = questionDocument.createElement("value");
				if (str.equals(correct))
				{
					correctResponseValue.setTextContent("Choice" + Integer.toString(ccount) + "_" + count + "_" + question.getId());
					correctResponse.appendChild(correctResponseValue);
					break;
				}
				ccount++;
			}
			// questionParts.put("inlineChoiceInteraction", inlineChoiceInteraction);
			questionParts.put("responseDeclaration" + count, responseDeclaration);
			
			itemBody.appendChild(inlineChoiceInteraction);
			m_fillInline.appendReplacement(sb, "");
			count++;
		}
		m_fillInline.appendTail(sb);

		if (sb.length() > 0)
		{
			ArrayList mediaFiles = new ArrayList<String>();
			itemBody = translateEmbedData(zip, testId + "/Resources/", sb.toString(), itemBody, mediaFiles, questionDocument);
			if (mediaFiles.isEmpty())
			{
				Element textDiv = questionDocument.createElement("div");
				textDiv.setTextContent(sb.toString());
				itemBody.appendChild(textDiv);
			}
		}

		Element countDiv = questionDocument.createElement("div");
		countDiv.setTextContent(Integer.toString(count));
		questionParts.put("responseDeclarationCount", countDiv);
		questionParts.put("itemBody", itemBody);
		return itemBody;
	}

	/**
	 * 
	 * @param questionDocument
	 * @param question
	 * @param questionParts
	 */
	public void getLikertScaleResponseChoices(Document questionDocument, Question question, Map<String, Element> questionParts)
	{
		if (question == null) return;

		LikertScaleQuestionImpl likert = (LikertScaleQuestionImpl) (question.getTypeSpecificQuestion());
		List<LikertScaleQuestionChoice> choices = likert.getChoices();

		// <choiceInteraction responseIdentifier="RESPONSE" shuffle="false" maxChoices="1">
		Element choiceInteraction = questionDocument.createElement("choiceInteraction");
		choiceInteraction.setAttribute("responseIdentifier", "RESPONSE");
		choiceInteraction.setAttribute("shuffle", "false");
		choiceInteraction.setAttribute("maxChoices", "1");

		// <responseDeclaration identifier="RESPONSE" cardinality="single" baseType="identifier">
		Element responseDeclaration = createResponseDeclaration(questionDocument, "RESPONSE", "single", "identifier");

		// add scale as the correct answer
		Element correctResponse = questionDocument.createElement("correctResponse");
		Element value = questionDocument.createElement("value");
		value.setTextContent(likert.getScale());
		correctResponse.appendChild(value);
		responseDeclaration.appendChild(correctResponse);

		// response choices
		int count = 1;
		for (LikertScaleQuestionChoice c : choices)
		{
			// <simpleChoice identifier="ChoiceA">You must stay with your luggage at all times.</simpleChoice>
			Element simpleChoice = questionDocument.createElement("simpleChoice");
			simpleChoice.setAttribute("identifier", "Choice" + Integer.toString(count++) + "_" + question.getId());
			String choiceText = c.getText();
			choiceText = FormattedText.unEscapeHtml(choiceText);
			simpleChoice.setTextContent(choiceText);
			choiceInteraction.appendChild(simpleChoice);
		}

		questionParts.put("choiceInteraction", choiceInteraction);
		questionParts.put("responseDeclaration", responseDeclaration);
		return;
	}

	/**
	 * Get the Elements needed for question.xml
	 * 
	 * @param questionDocument
	 *        The AssessmentItem Document
	 * @param question
	 *        The Question
	 * @param questionParts
	 *        Map containing different w3c dom elements
	 * @param zip
	 *        The zip output stream for export
	 * @param testId
	 *        test id of folder to export to
	 */
	public void getMCResponseChoices(Document questionDocument, Question question, Map<String, Element> questionParts, ZipOutputStream zip, String testId)
	{
		if (question == null) return;

		MultipleChoiceQuestionImpl mc = (MultipleChoiceQuestionImpl) (question.getTypeSpecificQuestion());
//		String cardinality = ("False".equalsIgnoreCase(mc.getSingleCorrect())) ? "multiple" : "single";
		boolean shuffle = mc.shuffleChoicesSetting();
		List<MultipleChoiceQuestionChoice> choices = mc.getChoices();
		Set<Integer> correctAnswers = mc.getCorrectAnswerSet();
		int maxChoice = (correctAnswers != null) ? correctAnswers.size() : 0;

		// <choiceInteraction responseIdentifier="RESPONSE" shuffle="false" maxChoices="1">
		Element choiceInteraction = questionDocument.createElement("choiceInteraction");
		choiceInteraction.setAttribute("responseIdentifier", "RESPONSE");
		choiceInteraction.setAttribute("shuffle", new Boolean(shuffle).toString());
		choiceInteraction.setAttribute("maxChoices", new Integer(maxChoice).toString());

		// <responseDeclaration identifier="RESPONSE" cardinality="single" baseType="identifier">
		Element responseDeclaration = createResponseDeclaration(questionDocument, "RESPONSE", "single", "identifier");
		Element correctResponse = questionDocument.createElement("correctResponse");
		responseDeclaration.appendChild(correctResponse);

		// response choices
		int count = 1;
		for (MultipleChoiceQuestionChoice c : choices)
		{
			// <simpleChoice identifier="ChoiceA">You must stay with your luggage at all times.</simpleChoice>
			Element simpleChoice = questionDocument.createElement("simpleChoice");
			simpleChoice.setAttribute("identifier", "Choice" + Integer.toString(count++) + "_" + question.getId());
			String choiceText = c.getText();
			choiceText = FormattedText.unEscapeHtml(choiceText);
			ArrayList mediaFiles = new ArrayList<String>();
			simpleChoice = translateEmbedData(zip, testId + "/Resources/", choiceText, simpleChoice, mediaFiles, questionDocument);
			if (mediaFiles.isEmpty())
			{
				simpleChoice.appendChild(questionDocument.createCDATASection(choiceText));
			}
			choiceInteraction.appendChild(simpleChoice);
		}

		// correct answers
		for (Integer correct : correctAnswers)
		{
			Element correctResponseValue = questionDocument.createElement("value");
			correctResponseValue.setTextContent("Choice" + (correct.intValue() + 1) + "_" + question.getId());
			correctResponse.appendChild(correctResponseValue);
		}
		questionParts.put("choiceInteraction", choiceInteraction);
		questionParts.put("responseDeclaration", responseDeclaration);
		return;
	}

	/**
	 * Get the Elements needed for question.xml
	 * 
	 * @param questionDocument
	 *        The AssessmentItem Document
	 * @param question
	 *        The Question
	 * @param questionParts
	 *        Map containing different w3c dom elements
	 * @param zip
	 *        The zip output stream for export
	 * @param testId
	 *        test id of folder to export to
	 */
	public void getOrderResponseChoices(Document questionDocument, Question question, Map<String, Element> questionParts, ZipOutputStream zip, String testId)
	{
		if (question == null) return;

		OrderQuestionImpl mc = (OrderQuestionImpl) (question.getTypeSpecificQuestion());
//		String cardinality = ("False".equalsIgnoreCase(mc.getSingleCorrect())) ? "multiple" : "single";
		List<OrderQuestionChoice> choices = mc.getChoicesAsAuthored();
		Set<Integer> correctAnswers = mc.getCorrectAnswerSet();
		int maxChoice = (correctAnswers != null) ? correctAnswers.size() : 0;

		// <choiceInteraction responseIdentifier="RESPONSE" shuffle="false" maxChoices="1">
		Element orderInteraction = questionDocument.createElement("orderInteraction");
		orderInteraction.setAttribute("responseIdentifier", "RESPONSE");
		orderInteraction.setAttribute("shuffle", "true");
		
		// <responseDeclaration identifier="RESPONSE" cardinality="single" baseType="identifier">
		Element responseDeclaration = createResponseDeclaration(questionDocument, "RESPONSE", "single", "identifier");
		Element correctResponse = questionDocument.createElement("correctResponse");
		responseDeclaration.appendChild(correctResponse);

		// response choices
		int count = 1;
		for (OrderQuestionChoice c : choices)
		{
			// <simpleChoice identifier="ChoiceA">You must stay with your luggage at all times.</simpleChoice>
			Element simpleChoice = questionDocument.createElement("simpleChoice");
			simpleChoice.setAttribute("identifier", "Choice" + Integer.toString(count++) + "_" + question.getId());
			String choiceText = c.getText();
			choiceText = FormattedText.unEscapeHtml(choiceText);
			ArrayList mediaFiles = new ArrayList<String>();
			simpleChoice = translateEmbedData(zip, testId + "/Resources/", choiceText, simpleChoice, mediaFiles, questionDocument);
			if (mediaFiles.isEmpty())
			{
				simpleChoice.setTextContent(choiceText);
			}
			orderInteraction.appendChild(simpleChoice);
		}

		// correct answers
		for (Integer correct : correctAnswers)
		{
			Element correctResponseValue = questionDocument.createElement("value");
			correctResponseValue.setTextContent("Choice" + (correct.intValue() + 1) + "_" + question.getId());
			correctResponse.appendChild(correctResponseValue);
		}
		questionParts.put("orderInteraction", orderInteraction);
		questionParts.put("responseDeclaration", responseDeclaration);
		return;
	}
	

	/**
	 * Get different components of a match type question
	 * 
	 * @param questionDocument
	 * @param question
	 * @param questionParts
	 * @param zip
	 *        The zip output stream for export
	 * @param testId
	 *        test id of folder to export to
	 */
	public void getMatchResponseChoices(Document questionDocument, Question question, Map<String, Element> questionParts, ZipOutputStream zip, String testId)
	{
		if (question == null) return;

		MatchQuestionImpl mc = (MatchQuestionImpl) (question.getTypeSpecificQuestion());
		String cardinality = "multiple";
		boolean shuffle = true;
		List<MatchQuestionPair> choicePairs = mc.getPairs();

		int maxChoice = (choicePairs != null) ? choicePairs.size() : 0;

		// <matchInteraction responseIdentifier="RESPONSE" shuffle="true" maxAssociations="4">
		Element matchInteraction = questionDocument.createElement("matchInteraction");
		matchInteraction.setAttribute("responseIdentifier", "RESPONSE");
		matchInteraction.setAttribute("shuffle", new Boolean(shuffle).toString());
		matchInteraction.setAttribute("maxAssociations", new Integer(maxChoice).toString());

		// <responseDeclaration identifier="RESPONSE" cardinality="single" baseType="identifier">
		Element responseDeclaration = createResponseDeclaration(questionDocument, "RESPONSE", cardinality, "directedPair");
		Element correctResponse = questionDocument.createElement("correctResponse");
		responseDeclaration.appendChild(correctResponse);

		// response choices
		int count = 0;
		Element simpleMatchSet1 = questionDocument.createElement("simpleMatchSet");
		Element simpleMatchSet2 = questionDocument.createElement("simpleMatchSet");

		for (MatchQuestionPair c : choicePairs)
		{
			// <simpleAssociableChoice identifier="C" matchMax="1">Capulet</simpleAssociableChoice>
			Element simpleAssociableChoice = questionDocument.createElement("simpleAssociableChoice");
			String id1 = "Choice" + Integer.toString(++count) + "_" + question.getId();
			simpleAssociableChoice.setAttribute("identifier", id1);
			simpleAssociableChoice.setAttribute("matchMax", "1");
			String choiceText = c.getChoice();
			choiceText = FormattedText.unEscapeHtml(choiceText);
			ArrayList mediaFiles = new ArrayList<String>();
			simpleAssociableChoice = translateEmbedData(zip, testId + "/Resources/", choiceText, simpleAssociableChoice, mediaFiles, questionDocument);
			if (mediaFiles.isEmpty())
			{
				simpleAssociableChoice.appendChild(questionDocument.createCDATASection(choiceText));
			}
			simpleMatchSet1.appendChild(simpleAssociableChoice);

			// match
			Element simpleAssociableMatch = questionDocument.createElement("simpleAssociableChoice");
			String id2 = "Choice" + Integer.toString(++count) + "_" + question.getId();
			simpleAssociableMatch.setAttribute("identifier", id2);
			simpleAssociableMatch.setAttribute("matchMax", "1");
			String matchText = c.getMatch();
			matchText = FormattedText.unEscapeHtml(matchText);
			mediaFiles = new ArrayList<String>();
			simpleAssociableMatch = translateEmbedData(zip, testId + "/Resources/", matchText, simpleAssociableMatch, mediaFiles, questionDocument);
			if (mediaFiles.isEmpty())
			{
				simpleAssociableMatch.appendChild(questionDocument.createCDATASection(matchText));
			}
			simpleMatchSet2.appendChild(simpleAssociableMatch);

			// correct pair
			Element correctResponseValue = questionDocument.createElement("value");
			correctResponseValue.setTextContent(id1 + " " + id2);
			correctResponse.appendChild(correctResponseValue);
		}
		matchInteraction.appendChild(simpleMatchSet1);
		matchInteraction.appendChild(simpleMatchSet2);

		questionParts.put("matchInteraction", matchInteraction);
		questionParts.put("responseDeclaration", responseDeclaration);
		return;
	}

	/**
	 * Get True-false question elements
	 * 
	 * @param questionDocument
	 * @param question
	 * @param questionParts
	 */
	public void getTFResponseChoices(Document questionDocument, Question question, Map<String, Element> questionParts)
	{
		if (question == null) return;

		TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());
		String cardinality = "single";

		// <choiceInteraction responseIdentifier="RESPONSE" shuffle="false" maxChoices="1">
		Element choiceInteraction = questionDocument.createElement("choiceInteraction");
		choiceInteraction.setAttribute("responseIdentifier", "RESPONSE");
		choiceInteraction.setAttribute("shuffle", "false");
		choiceInteraction.setAttribute("maxChoices", "1");

		// <responseDeclaration identifier="RESPONSE" cardinality="single" baseType="identifier">
		Element responseDeclaration = createResponseDeclaration(questionDocument, "RESPONSE", cardinality, "identifier");
		Element correctResponse = questionDocument.createElement("correctResponse");
		responseDeclaration.appendChild(correctResponse);
		Element correctResponseValue = questionDocument.createElement("value");

		// response choices
		// <simpleChoice identifier="ChoiceA">True</simpleChoice>
		Element simpleChoice = questionDocument.createElement("simpleChoice");
		simpleChoice.setAttribute("identifier", "ChoiceA" + "_" + question.getId());
		simpleChoice.setTextContent("True");
		choiceInteraction.appendChild(simpleChoice);

		Element simpleChoiceFalse = questionDocument.createElement("simpleChoice");
		simpleChoiceFalse.setAttribute("identifier", "ChoiceB" + "_" + question.getId());
		simpleChoiceFalse.setTextContent("False");
		choiceInteraction.appendChild(simpleChoiceFalse);

		if (tf.correctAnswer)
			correctResponseValue.setTextContent("ChoiceA" + "_" + question.getId());
		else
			correctResponseValue.setTextContent("ChoiceB" + "_" + question.getId());
		correctResponse.appendChild(correctResponseValue);

		questionParts.put("choiceInteraction", choiceInteraction);
		questionParts.put("responseDeclaration", responseDeclaration);
		return;
	}

	/**
	 * Parses the text and if embed media found then translates the path and adds the file to the zip package.
	 * 
	 * @param zip
	 *        The zip package
	 * @param text
	 *        Text
	 * @param mediaFiles
	 *        List of embedded files found
	 * @return The translated Text
	 */
	protected String translateEmbedData(ZipOutputStream zip, String subFolder, String writeSubFolder, String text, List<String> mediaFiles)
	{
		Pattern p = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^#\"]*)([#\"])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		StringBuffer sb = new StringBuffer();
		subFolder = (subFolder == null || subFolder.length() == 0) ? "Resources/" : subFolder;
		Matcher m = p.matcher(text);

		// security advisor
		pushAdvisor();

		while (m.find())
		{
			if (m.groupCount() != 3) continue;
			String ref = m.group(2);
			if (!ref.contains("/access/mneme/content/")) continue;

			String resource_id = ref.replace("/access/mneme", "");
			resource_id = resource_id.replaceAll("%20", " ");
			//resource with comma and other special characters
			String resource_name = Validator.getFileName(resource_id);
			try
			{
				resource_name = URLDecoder.decode(resource_name, "UTF-8");
			}
			catch(Exception e)
			{
				// do nothing
			}
			mediaFiles.add(subFolder + resource_name);
			m.appendReplacement(sb, m.group(1) + "= \"" + writeSubFolder + resource_name  + "\"");

			writeContentResourceToZip(zip, subFolder, resource_id, resource_name);
		}

		popAdvisor();
		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Creates elements for all embed media with in itembody element. Use this for question text
	 * 
	 * @param zip
	 * @param subFolder
	 * @param text
	 * @param itemBody
	 * @param mediaFiles
	 * @return
	 */
	private Element translateEmbedData(ZipOutputStream zip, String subFolder, String text, Element itemBody, List<String> mediaFiles,
			Document questionDocument)
	{
		StringBuilder appender=new StringBuilder();
		if (text == null || text.length() == 0) return itemBody;
		
		Element media = null;
		try
		{
			Pattern pa = Pattern.compile("<(img|a|embed)\\s+.*?/*>", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);

			// TODO: write all attributes
			Pattern p_srcAttribute = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^#\"]*)([#\"])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
			Matcher m = pa.matcher(text);
			StringBuffer sb = new StringBuffer();
			subFolder = (subFolder == null || subFolder.length() == 0) ? "Resources/" : subFolder;
			String embedSubFolder = "Resources/";
			int start = 0;
			
			while (m.find())
			{
				int startIdx = m.start();

				String img_content = m.group(0);
				Matcher m_src = p_srcAttribute.matcher(img_content);
				if (m_src.find())
				{
					String ref = m_src.group(2);
					if (!ref.contains("/access/mneme/content/")) continue;
					
					Element div = questionDocument.createElement("div");
					if (startIdx <= text.length())
					{
						String txt = text.substring(start, startIdx);
						appender.append(txt);

						start = m.end();
					}
					ref = ref.replaceAll("%20", " ");
					String resource_id = ref.replace("/access/mneme", "");
					//resource with comma and other special characters
					String embedFileName = Validator.getFileName(resource_id);
					try
					{
						embedFileName = URLDecoder.decode(embedFileName, "UTF-8");
					}
					catch(Exception e)
					{
						// do nothing
					}
					ref = subFolder + embedFileName;
					mediaFiles.add(ref);
				
					media = questionDocument.createElement(m.group(1));
					if ("a".equalsIgnoreCase(m.group(1))) media.setAttribute("target", "_blank");
					media.setAttribute(m_src.group(1), embedSubFolder + embedFileName);
					Document ownerDocument = media.getOwnerDocument();
					//converting the element to string to add to the appender variable 
					DOMImplementationLS domImplLS = (DOMImplementationLS) ownerDocument
									.getImplementation();
					LSSerializer serializer = domImplLS.createLSSerializer();
					serializer.getDomConfig().setParameter("xml-declaration", false);
					String mediaString = serializer.writeToString(media);

					m.appendReplacement(sb, "");

					writeContentResourceToZip(zip, subFolder, resource_id, embedFileName);
					appender.append(mediaString);
				}				
			}
			m.appendTail(sb);
			if (start > 0 && start < text.length())
			{
				String substring = text.substring(start);
				appender.append(substring);
			}
			itemBody.appendChild(questionDocument.createCDATASection(appender.toString()));
			return itemBody;
		}
		catch (Exception e)
		{
			M_log.debug("error in translating embed up blank img tags:" + e.getMessage());
		}
		return itemBody;
	}

	/**
	 * Writes the media file from content resource to the zip package. Its important to push the security advisor before calling this method.
	 * 
	 * @param zip
	 *        The zip package
	 * @param id
	 *        resource id
	 * @param fileName
	 *        file name
	 */
	protected void writeContentResourceToZip(ZipOutputStream zip, String subFolder, String id, String fileName)
	{
		try
		{
			// Reference does not know how to make the id from a private docs reference.
			if (id.startsWith("/content/"))
			{
				id = id.substring("/content".length());
			}

			if (subFolder != null) fileName = subFolder + fileName;

			// write attachment to the zip
			id = id.replaceAll("%20", " ");
			// if resource has special characters like comma or so then decode for CHS
			try
			{
				id = URLDecoder.decode(id, "UTF-8");
			}
			catch(Exception e)
			{
				// do nothing
			}
			ContentResource resource = this.contentHostingService.getResource(id);
			zip.putNextEntry(new ZipEntry(fileName));
			zip.write(resource.getContent());
			zip.closeEntry();
		}
		catch (Exception e)
		{
			M_log.warn("ExportQtiService: zipping embed or attachments: " + e.toString());	
		}
	}

	/**
	 * Write Xml document to zip package
	 * 
	 * @param zip
	 * @param fileTitle
	 * @param document
	 */
	protected void writeDocumentToZip(ZipOutputStream zip, String subFolder, String fileTitle, Document document)
	{
		try
		{
			if (subFolder != null) zip.putNextEntry(new ZipEntry(subFolder));
			zip.putNextEntry(new ZipEntry(fileTitle));
			zip.write(Xml.writeDocumentToString(document).getBytes("UTF-8"));
			zip.closeEntry();
			zip.flush();
		}
		catch (IOException e)
		{
			M_log.warn("zipSubmissionsQuestion: zipping question: " + e.toString());
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
	 * Remove our security advisor.
	 */
	protected void popAdvisor()
	{
		securityServiceSakai.popAdvisor();
	}

	/**
	 * Setup a security advisor.
	 */
	protected void pushAdvisor()
	{
		// setup a security advisor
		securityServiceSakai.pushAdvisor(new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		});
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
	 * Dependency: QuestionService.
	 * 
	 * @param service
	 *        The QuestionService.
	 */
	public void setQuestionService(QuestionService service)
	{
		this.questionService = service;
	}

	public void setSecurityServiceSakai(org.sakaiproject.authz.api.SecurityService securityServiceSakai)
	{
		this.securityServiceSakai = securityServiceSakai;
	}

	public void setContentHostingService(ContentHostingService contentHostingService)
	{
		this.contentHostingService = contentHostingService;
	}

}
