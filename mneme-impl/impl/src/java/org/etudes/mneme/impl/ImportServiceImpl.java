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

package org.etudes.mneme.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.Ent;
import org.etudes.mneme.api.GradesService;
import org.etudes.mneme.api.ImportService;
import org.etudes.mneme.api.MnemeTransferService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.ReviewTiming;
import org.etudes.mneme.api.SecurityService;
import org.etudes.util.api.Translation;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * ImportServiceImpl implements ImportService
 * </p>
 */
public class ImportServiceImpl implements ImportService
{
	public class AttachmentInfo
	{
		String fileName = null;

		Boolean isLink = null;

		String itemId = null;

		String mimeType = null;

		String ref = null;
	}

	public class PoolInfo
	{
		String description = null;

		String title = null;
	}

	public class SamigoQuestion
	{
		String answerMatchText;

		Integer answerSeq;

		Boolean caseSensitive;

		Boolean correct;

		String correctFeedback;

		Boolean exclusive;

		String generalFeedback;

		String incorrectFeedback;

		String instruction;

		String itemId;

		String questionChoiceText;

		Boolean randomize;

		Boolean reason;

		Float score;

		Integer type;
	}

	/**
	 * Sort the Ents by description, case insensitive
	 */
	protected class EntComparator implements Comparator
	{
		public int compare(Object arg0, Object arg1)
		{
			int rv = ((Ent) arg0).getDescription().toLowerCase().compareTo(((Ent) arg1).getDescription().toLowerCase());
			return rv;
		}
	}

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ImportServiceImpl.class);

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService = null;

	/** Dependency: AssignmentService */
	protected AssignmentService assignmentService = null;

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

	/** Configuration: offer import from Samigo support. */
	protected Boolean offerAssignment = Boolean.TRUE;

	/** Configuration: offer import from assignments support. */
	protected Boolean offerSamigo = Boolean.TRUE;

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

	/** Dependency: SqlService */
	protected SqlService sqlService = null;

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/** Dependency: AssessmentService */
	protected MnemeTransferService transferService = null;

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
	public List<Ent> getAssessments(String source, String destionation)
	{
		List<Ent> rv = new ArrayList<Ent>();

		List<Assessment> assessments = this.assessmentService.getContextAssessments(source, AssessmentService.AssessmentsSort.title_a, Boolean.FALSE);

		// to check the title conflicts in the destination
		List<Assessment> existingAssessments = this.assessmentService.getContextAssessments(destionation, AssessmentService.AssessmentsSort.cdate_a,
				Boolean.FALSE);

		// make an End from each assessment
		for (Assessment assessment : assessments)
		{
			Ent ent = new EntImpl(assessment.getId(), assessment.getTitle());
			rv.add(ent);

			// mark any that have a title conflict in the destination
			for (Assessment candidate : existingAssessments)
			{
				if (!StringUtil.different(candidate.getTitle(), assessment.getTitle()))
				{
					ent.setMarked(Boolean.TRUE);
					break;
				}
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Ent> getAssignments(String context)
	{
		if (context == null) throw new IllegalArgumentException();

		List<Ent> rv = readAssignments(context);

		// sort
		Collections.sort(rv, new EntComparator());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Ent> getAssignmentSites(String userId)
	{
		if (userId == null) userId = sessionManager.getCurrentSessionUserId();

		return getAuthSites(userId, "asn.new", null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Ent> getMnemeSites(String userId, String context)
	{
		if (userId == null) userId = sessionManager.getCurrentSessionUserId();

		return getAuthSites(userId, "mneme.manage", context);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getOfferAssignment()
	{
		return this.offerAssignment;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getOfferSamigo()
	{
		return this.offerSamigo;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Ent> getSamigoAssessments(String context)
	{
		if (context == null) throw new IllegalArgumentException();

		List<Ent> rv = readSamigoAssessments(context);

		Collections.sort(rv, new EntComparator());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Ent> getSamigoPools(String userId)
	{
		if (userId == null) userId = sessionManager.getCurrentSessionUserId();

		List<Ent> rv = readSamigoPools(userId);

		// sort
		Collections.sort(rv, new EntComparator());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Ent> getSamigoSites(String userId)
	{
		if (userId == null) userId = sessionManager.getCurrentSessionUserId();

		List<Ent> rv = new ArrayList<Ent>();

		// get the authz groups in which this user has samigo permission
		Set refs = this.authzGroupService.getAuthzGroupsIsAllowed(userId, "assessment.createAssessment", null);
		for (Object o : refs)
		{
			String ref = (String) o;

			// each is a site ref
			Reference siteRef = this.entityManager.newReference(ref);

			// get the site display
			String display = this.siteService.getSiteDisplay(siteRef.getId());

			// take only the site title (between first and last quotes)
			int firstPos = display.indexOf("\"");
			int lastPos = display.lastIndexOf("\"");
			if ((firstPos != -1) && (lastPos != -1))
			{
				display = display.substring(firstPos + 1, lastPos);
			}

			// record for return
			Ent ent = new EntImpl(siteRef.getId(), display);
			rv.add(ent);
		}

		// sort
		Collections.sort(rv, new EntComparator());

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void importAssessment(String id, String context) throws AssessmentPermissionException
	{
		// create the pool
		Pool pool = createAssessmentPool(id, context);

		if (pool != null)
		{
			// import the questions
			importSamigoAssessmentQuestions(id, pool);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void importAssignment(String id, String context, boolean draftSource) throws AssessmentPermissionException
	{
		try
		{
			// get the Assignment
			Assignment assignment = this.assignmentService.getAssignment(id);
			AssignmentContent content = assignment.getContent();

			// create the pool
			Pool pool = this.poolService.newPool(context);
			pool.setTitle(addDate("import-assignment-text", assignment.getTitle(), new Date()));
			if (content.getTypeOfGrade() == 3/* Assignment.SCORE_GRADE_TYPE */)
			{
				pool.setPoints(Float.valueOf(content.getMaxGradePointDisplay()));
			}
			boolean noPoints = pool.getPoints().floatValue() == 0;

			// pool.setDescription(info.description);
			this.poolService.savePool(pool);

			// create the question (task)
			Question question = this.questionService.newQuestion(pool, "mneme:Task");
			EssayQuestionImpl e = (EssayQuestionImpl) (question.getTypeSpecificQuestion());

			// find the assignment attachments
			Set<String> docs = new LinkedHashSet<String>();
			for (Reference ref : (List<Reference>) content.getAttachments())
			{
				docs.add(ref.getReference());
			}

			// remember just the attachments
			Set<String> attachmentDocs = new LinkedHashSet<String>();
			attachmentDocs.addAll(docs);

			// find the embedded documents in the question text
			docs.addAll(this.attachmentService.harvestAttachmentsReferenced(content.getInstructions(), true));

			// import the embedded documents and attachments
			List<Translation> translations = importEmbeddedDocs(docs, pool.getContext());

			// convert the question text's embedded documents to reference the harvested documents
			String questionText = this.attachmentService.translateEmbeddedReferences(content.getInstructions(), translations);

			// format attachments references to add to the text
			StringBuilder attachments = new StringBuilder();
			for (String ref : attachmentDocs)
			{
				// translate the old refs to their new locations
				for (Translation t : translations)
				{
					ref = t.translate(ref);
				}
				attachments.append(formatRef(ref));
			}

			if (attachments.length() > 0)
			{
				attachments.insert(0, "<p><ul>");
				attachments.append("</ul></p>");
			}

			// clean the text
			String clean = HtmlHelper.clean(questionText + attachments.toString());

			// set the text
			question.getPresentation().setText(clean);

			// type
			EssayQuestionImpl.SubmissionType type = EssayQuestionImpl.SubmissionType.none;
			switch (content.getTypeOfSubmission())
			{
				case 2 /* Assignment.ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION */:
				{
					type = EssayQuestionImpl.SubmissionType.attachments;
					break;
				}
				case 1 /* Assignment.TEXT_ONLY_ASSIGNMENT_SUBMISSION */:
				{
					type = EssayQuestionImpl.SubmissionType.inline;
					break;
				}
				case 3 /* Assignment.TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION */:
				{
					type = EssayQuestionImpl.SubmissionType.both;
					break;
				}
				case 4 /* Assignment.NON_ELECTRONIC_SUBMISSION */:
				{
					type = EssayQuestionImpl.SubmissionType.none;
				}
			}
			e.setSubmissionType(type);

			// save
			question.getTypeSpecificQuestion().consolidate("");
			this.questionService.saveQuestion(question);

			// create the assessment
			Assessment assessment = this.assessmentService.newAssessment(context);
			assessment.setType(AssessmentType.assignment);
			assessment.setTitle(content.getTitle());
			assessment.getGrading().setAutoRelease(Boolean.FALSE);
			if (assignment.getOpenTime() != null)
			{
				assessment.getDates().setOpenDate(new Date(assignment.getOpenTime().getTime()));
			}
			if (assignment.getDueTime() != null)
			{
				assessment.getDates().setDueDate(new Date(assignment.getDueTime().getTime()));
			}
			if (assignment.getCloseTime() != null)
			{
				assessment.getDates().setAcceptUntilDate(new Date(assignment.getCloseTime().getTime()));
			}
			assessment.setRequireHonorPledge(Boolean.valueOf(content.getHonorPledge() > 1));
			assessment.getReview().setTiming(ReviewTiming.graded);

			boolean sendToGb = !("no".equals(assignment.getProperties().getProperty("new_assignment_add_to_gradebook")));
			if (noPoints) sendToGb = false;
			assessment.getGrading().setGradebookIntegration(Boolean.valueOf(sendToGb));

			Part part = assessment.getParts().addPart();
			part.addPickDetail(question);

			this.assessmentService.saveAssessment(assessment);

			// if requested, and the assessment was send to gb, change it to not.
			if (draftSource)
			{
				// change to no gradebook, and draft
				AssignmentEdit edit = this.assignmentService.editAssignment(assignment.getReference());
				edit.getPropertiesEdit().addProperty("new_assignment_add_to_gradebook", "no");
				edit.setDraft(true);
				this.assignmentService.commitEdit(edit);

				// clear from the gradebook if we are set to go in there
				// Note: Mneme associates with the gb by title, but assignments uses the assignment reference string,
				// so we will change the title to the reference string for this (and then throw the change away)
				assessment.setTitle(assignment.getReference());
				if (sendToGb && this.gradesService.assessmentReported(assessment))
				{
					this.gradesService.retractAssessmentGrades(assessment);
				}
			}
		}
		catch (IdUnusedException e)
		{
		}
		catch (PermissionException e)
		{
		}
		catch (AssessmentPolicyException e)
		{
		}
		catch (InUseException e)
		{
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void importMneme(Set<String> ids, String fromContext, String toContext) throws AssessmentPermissionException
	{
		this.transferService.importFromSite(fromContext, toContext, ids);
	}

	/**
	 * {@inheritDoc}
	 */
	public void importPool(String id, String context) throws AssessmentPermissionException
	{
		// create the pool
		Pool pool = createPool(id, context);

		if (pool != null)
		{
			// import the questions
			importSamigoPoolQuestions(id, pool);
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
	 * Dependency: AssignmentService.
	 * 
	 * @param service
	 *        The AssignmentService.
	 */
	public void setAssignmentService(AssignmentService service)
	{
		assignmentService = service;
	}

	/**
	 * Configuration: Assignment support.
	 * 
	 * @param support
	 *        TRUE to offer support, FALSE to not.
	 */
	public void setAssignmentSupport(String support)
	{
		this.offerAssignment = Boolean.valueOf(support);
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
	 * Configuration: Samigo support.
	 * 
	 * @param support
	 *        TRUE to offer support, FALSE to not.
	 */
	public void setSamigoSupport(String support)
	{
		this.offerSamigo = Boolean.valueOf(support);
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
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		sqlService = service;
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
	 * Dependency: MnemeTransferService.
	 * 
	 * @param service
	 *        The MnemeTransferService.
	 */
	public void setTransferService(MnemeTransferService service)
	{
		this.transferService = service;
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
	 * @return The source and date passed throught the selector message.
	 */
	protected String addDate(String selector, String source, Date date)
	{
		// format the date
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
		String fmt = format.format(date);

		// the args
		Object[] args = new Object[2];
		args[0] = source;
		args[1] = fmt;

		// format the works
		String rv = this.messages.getFormattedMessage(selector, args);

		return rv;
	}

	/**
	 * Convert all references to embedded documents that have been imported to their new locations in the question and attachment data.
	 * 
	 * @param questionData
	 *        The Samigo question data.
	 * @param attachments
	 *        The attachments info.
	 * @param translations
	 *        The translations for each imported embedded document reference.
	 */
	protected void convertDocumentsReferenced(List<SamigoQuestion> questionData, List<AttachmentInfo> attachments, List<Translation> translations)
	{
		for (Translation t : translations)
		{
			for (SamigoQuestion q : questionData)
			{
				if (q.answerMatchText != null)
					q.answerMatchText = this.attachmentService.translateEmbeddedReferences(q.answerMatchText, translations);
				if (q.correctFeedback != null)
					q.correctFeedback = this.attachmentService.translateEmbeddedReferences(q.correctFeedback, translations);
				if (q.generalFeedback != null)
					q.generalFeedback = this.attachmentService.translateEmbeddedReferences(q.generalFeedback, translations);
				if (q.incorrectFeedback != null)
					q.incorrectFeedback = this.attachmentService.translateEmbeddedReferences(q.incorrectFeedback, translations);
				if (q.instruction != null) q.instruction = this.attachmentService.translateEmbeddedReferences(q.instruction, translations);
				if (q.questionChoiceText != null)
					q.questionChoiceText = this.attachmentService.translateEmbeddedReferences(q.questionChoiceText, translations);
			}
		}

		for (AttachmentInfo a : attachments)
		{
			// not for links
			if ((a.isLink == null) || (!a.isLink.booleanValue()))
			{
				// find our translation
				for (Translation t : translations)
				{
					if (t.getFrom().equals(a.ref))
					{
						a.ref = t.getTo();
						break;
					}
				}
			}
		}
	}

	/**
	 * Create a pool in Mneme from Samigo assessment information.
	 * 
	 * @param assessmentId
	 *        The Samigo assessment id.
	 * @return The pool, or null if it was not created.
	 */
	protected Pool createAssessmentPool(String assessmentId, final String context) throws AssessmentPermissionException
	{
		// read the details
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT A.TITLE FROM SAM_ASSESSMENTBASE_T A WHERE A.ID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(assessmentId);
		final PoolInfo info = new PoolInfo();

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					info.title = SqlHelper.readString(result, 1);
					info.description = null;

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("createAssessmentPool: reading pool details: " + e);
					return null;
				}
			}
		});

		if (info.title != null)
		{
			Pool pool = this.poolService.newPool(context);
			pool.setTitle(addDate("import-text", info.title, new Date()));
			// pool.setDescription(info.description);
			this.poolService.savePool(pool);

			return pool;
		}

		return null;
	}

	/**
	 * Create an essay question from Samigo data.
	 * 
	 * @param these
	 *        The Samigo data entries.
	 * @param pool
	 *        The pool for the question.
	 * @param upload
	 *        true for an upload type, false for inline.
	 * @return The question, or null if it was not made
	 * @throws AssessmentPermissionException
	 */
	protected Question createEssay(SamigoQuestion[] these, String attachments, Pool pool, boolean upload) throws AssessmentPermissionException
	{
		// validate: fist questionChoiceText for the question text not null
		boolean valid = (these[0].questionChoiceText != null);

		// there must be only one entry
		if (valid)
		{
			valid = (these.length == 1);
		}

		if (!valid)
		{
			M_log.info("createEssay: invalid samigo question: " + these[0].itemId);
			return null;
		}

		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:Essay");
		EssayQuestionImpl e = (EssayQuestionImpl) (question.getTypeSpecificQuestion());

		// set the text
		String clean = HtmlHelper.clean(these[0].questionChoiceText + attachments);
		question.getPresentation().setText(clean);

		// model answer
		if (these[0].answerMatchText != null)
		{
			clean = HtmlHelper.clean(these[0].answerMatchText);
			e.setModelAnswer(clean);
		}

		// type
		e.setSubmissionType(upload ? EssayQuestionImpl.SubmissionType.attachments : EssayQuestionImpl.SubmissionType.inline);

		return question;
	}

	/**
	 * Create a fillin question from Samigo data.
	 * 
	 * @param these
	 *        The Samigo data entries.
	 * @param pool
	 *        The pool for the question.
	 * @param text
	 *        true if text, false if numeric.
	 * @return The question, or null if it was not made
	 * @throws AssessmentPermissionException
	 */
	protected Question createFillin(SamigoQuestion[] these, String attachments, Pool pool, boolean text) throws AssessmentPermissionException
	{
		// validate: fist questionChoiceText for the question text not null
		boolean valid = (these[0].questionChoiceText != null);

		// answerMatchText from all for the choices not null
		if (valid)
		{
			for (int index = 0; index < these.length; index++)
			{
				if (these[index].answerMatchText == null)
				{
					valid = false;
					break;
				}
			}
		}

		if (!valid)
		{
			M_log.info("createFillin: invalid samigo question: " + these[0].itemId);
			return null;
		}

		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:FillBlanks");
		FillBlanksQuestionImpl f = (FillBlanksQuestionImpl) (question.getTypeSpecificQuestion());

		// detect these[0].exclusive and translate to our "any order"
		f.setAnyOrder(Boolean.FALSE.toString());
		if ((these[0].exclusive != null) && (these[0].exclusive.booleanValue()))
		{
			// if we find that all the fill-in correct patterns are the same,
			// and there are is one for each answer (comma separated),
			// spread them out, one per fill-in, and set any-order
			boolean mutualExclusive = true;
			for (int index = 1; index < these.length; index++)
			{
				if (!these[index].answerMatchText.equals(these[0].answerMatchText))
				{
					mutualExclusive = false;
					break;
				}
			}

			if (mutualExclusive)
			{
				String[] parts = these[0].answerMatchText.split("\\|");
				if ((parts != null) && (parts.length == these.length))
				{
					for (int index = 0; index < these.length; index++)
					{
						these[index].answerMatchText = parts[index];
					}

					f.setAnyOrder(Boolean.TRUE.toString());
				}
			}
		}

		// case sensitive
		if (these[0].caseSensitive != null) f.setCaseSensitive(these[0].caseSensitive.toString());

		// text or numeric
		f.setResponseTextual(Boolean.toString(text));

		// recreate the text, fillin in the "{}" with these answerMatchText

		String questionText = these[0].questionChoiceText;
		for (int index = 0; index < these.length; index++)
		{
			questionText = questionText.replaceFirst("\\{\\}", Matcher.quoteReplacement("{" + these[index].answerMatchText + "}"));
		}

		// set the text
		String clean = HtmlHelper.clean(questionText + attachments);
		f.setText(clean);

		return question;
	}

	/**
	 * Create a Likert scale question from Samigo data.
	 * 
	 * @param these
	 *        The Samigo data entries.
	 * @param pool
	 *        The pool for the question.
	 * @return The question, or null if it was not made
	 * @throws AssessmentPermissionException
	 */
	protected Question createLikert(SamigoQuestion[] these, String attachments, Pool pool) throws AssessmentPermissionException
	{
		// validate: fist questionChoiceText for the question text not null
		boolean valid = (these[0].questionChoiceText != null);

		// recognize a scale
		String scale = null;
		// "0" for our 5 point "strongly-agree"
		// "1" for our 4 point "excellent"
		// "2" for our 3 point "above-average"
		// "3" for our 2 point "yes"
		// "4" for our 5 point "5"
		// "5" for our 2 point "rocks"

		// 3 choices is below/average/above or disagree/undecided/agree
		if (these.length == 3)
		{
			if ("Below Average".equals(these[0].answerMatchText))
			{
				scale = "2";
			}
			else
			{
				scale = "0";
			}
		}

		// 2 is yes/no, or agree / disagree
		else if (these.length == 2)
		{
			if ("No".equals(these[0].answerMatchText))
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
		else if (these.length == 5)
		{
			if ("1".equals(these[0].answerMatchText))
			{
				scale = "4";
			}
			else if ("Strongly Disagree".equals(these[0].answerMatchText))
			{
				scale = "0";
			}
			else
			{
				scale = "1";
			}
		}

		// 10 is 1..10
		else if (these.length == 10)
		{
			scale = "4";
		}

		if (scale == null)
		{
			valid = false;
		}

		if (!valid)
		{
			M_log.info("createLikert: invalid samigo question: " + these[0].itemId);
			return null;
		}

		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:LikertScale");
		LikertScaleQuestionImpl l = (LikertScaleQuestionImpl) (question.getTypeSpecificQuestion());

		// set the text
		String clean = HtmlHelper.clean(these[0].questionChoiceText + attachments);
		question.getPresentation().setText(clean);

		// set the scale
		l.setScale(scale);

		return question;
	}

	/**
	 * Create a match question from Samigo data.
	 * 
	 * @param these
	 *        The Samigo data entries.
	 * @param pool
	 *        The pool for the question.
	 * @return The question, or null if it was not made
	 * @throws AssessmentPermissionException
	 */
	protected Question createMatch(SamigoQuestion[] these, String attachments, Pool pool) throws AssessmentPermissionException
	{
		// validate: fist instruction for the question text not null
		boolean valid = (these[0].instruction != null);

		// answerMatchText and questionChoiceText from all for the match and choices not null
		if (valid)
		{
			for (int index = 0; index < these.length; index++)
			{
				if ((these[index].answerMatchText == null) || (these[index].questionChoiceText == null))
				{
					valid = false;
					break;
				}
			}
		}

		if (!valid)
		{
			M_log.info("createMatch: invalid samigo question: " + these[0].itemId);
			return null;
		}

		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:Match");
		MatchQuestionImpl m = (MatchQuestionImpl) (question.getTypeSpecificQuestion());

		// set the text
		String clean = HtmlHelper.clean(these[0].instruction + attachments);
		question.getPresentation().setText(clean);

		// set the # pairs
		m.consolidate("INIT:" + these.length);

		// set the pair values
		List<MatchQuestionImpl.MatchQuestionPair> pairs = m.getPairs();
		for (int index = 0; index < these.length; index++)
		{
			clean = HtmlHelper.clean(these[index].questionChoiceText);
			pairs.get(index).setChoice(clean);

			clean = HtmlHelper.clean(these[index].answerMatchText);
			pairs.get(index).setMatch(clean);
		}

		return question;
	}

	/**
	 * Create a multiple choice question from Samigo data.
	 * 
	 * @param these
	 *        The Samigo data entries.
	 * @param pool
	 *        The pool for the question.
	 * @param multiAllowed
	 *        true if we can have multiple answers, false if not.
	 * @return The question, or null if it was not made
	 * @throws AssessmentPermissionException
	 */
	protected Question createMc(SamigoQuestion[] these, String attachments, Pool pool, boolean multiAllowed) throws AssessmentPermissionException
	{
		// validate: fist questionChoiceText for the question text not null
		boolean valid = (these[0].questionChoiceText != null);

		// Note: answerMatchText may actually be null
		// we must have one or more marked correct
		boolean multiCorrect = false;
		if (valid)
		{
			boolean seenCorrect = false;
			for (int index = 0; index < these.length; index++)
			{
				if (these[index].answerMatchText != null)
				{
					if ((these[index].correct != null) && (these[index].correct.booleanValue()))
					{
						if (seenCorrect) multiCorrect = true;
						seenCorrect = true;
					}
				}
			}
			if (!seenCorrect)
			{
				valid = false;
			}
		}

		if (valid && multiCorrect && !multiAllowed)
		{
			valid = false;
		}

		if (!valid)
		{
			M_log.info("createMc: invalid samigo question: " + these[0].itemId);
			return null;
		}

		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:MultipleChoice");
		MultipleChoiceQuestionImpl mc = (MultipleChoiceQuestionImpl) (question.getTypeSpecificQuestion());

		// set the text
		String clean = HtmlHelper.clean(these[0].questionChoiceText + attachments);
		question.getPresentation().setText(clean);

		// randomize
		if (these[0].randomize != null) mc.setShuffleChoices(these[0].randomize.toString());

		// single / multiple select
		mc.setSingleCorrect(Boolean.toString(!multiCorrect));

		// set the choices
		List<String> choices = new ArrayList<String>();
		for (int index = 0; index < these.length; index++)
		{
			if (these[index].answerMatchText != null)
			{
				clean = HtmlHelper.clean(these[index].answerMatchText);
				choices.add(clean);
			}
		}
		mc.setAnswerChoices(choices);

		// corrects
		Set<Integer> correctAnswers = new HashSet<Integer>();
		List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choicesAuthored = mc.getChoicesAsAuthored();
		int authoredIndex = 0;
		for (int index = 0; index < these.length; index++)
		{
			if (these[index].answerMatchText != null)
			{
				if ((these[index].correct != null) && (these[index].correct.booleanValue()))
				{
					correctAnswers.add(Integer.valueOf(choicesAuthored.get(authoredIndex).getId()));
				}
				authoredIndex++;
			}
		}
		mc.setCorrectAnswerSet(correctAnswers);

		return question;
	}

	/**
	 * Create a pool in Mneme from Samigo pool information.
	 * 
	 * @param poolId
	 *        The Samigo pool id.
	 * @return The pool, or null if it was not created.
	 */
	protected Pool createPool(String poolId, final String context) throws AssessmentPermissionException
	{
		// read the details
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.TITLE, P.DESCRIPTION FROM SAM_QUESTIONPOOL_T P WHERE P.QUESTIONPOOLID=?");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(poolId);
		final PoolInfo info = new PoolInfo();

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					info.title = SqlHelper.readString(result, 1);
					info.description = SqlHelper.readString(result, 2);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("createPool: reading pool details: " + e);
					return null;
				}
			}
		});

		if (info.title != null)
		{
			Pool pool = this.poolService.newPool(context);
			pool.setTitle(addDate("import-text", info.title, new Date()));
			pool.setDescription(info.description);
			this.poolService.savePool(pool);

			return pool;
		}

		return null;
	}

	/**
	 * Create a true false question from Samigo data.
	 * 
	 * @param these
	 *        The Samigo data entries.
	 * @param pool
	 *        The pool for the question.
	 * @return The question, or null if it was not made
	 * @throws AssessmentPermissionException
	 */
	protected Question createTf(SamigoQuestion[] these, String attachments, Pool pool) throws AssessmentPermissionException
	{
		// validate: fist questionChoiceText for the question text not null
		boolean valid = (these[0].questionChoiceText != null);

		// there must be two of these
		if (valid)
		{
			valid = (these.length == 2);
		}

		// they must have "true" and "false" marked in their answerMatchText
		if (valid)
		{
			valid = ("true".equals(these[0].answerMatchText) && "false".equals(these[1].answerMatchText));
		}

		// one of these must be marked correct
		if (valid)
		{
			int count = 0;
			if ((these[0].correct != null) && (these[0].correct.booleanValue())) count++;
			if ((these[1].correct != null) && (these[1].correct.booleanValue())) count++;
			valid = (count == 1);
		}

		if (!valid)
		{
			M_log.info("createTf: invalid samigo question: " + these[0].itemId);
			return null;
		}

		// create the question
		Question question = this.questionService.newQuestion(pool, "mneme:TrueFalse");
		TrueFalseQuestionImpl tf = (TrueFalseQuestionImpl) (question.getTypeSpecificQuestion());

		// set the text
		String clean = HtmlHelper.clean(these[0].questionChoiceText + attachments);
		question.getPresentation().setText(clean);

		// the correct answer
		tf.setCorrectAnswer((((these[0].correct != null) && (these[0].correct.booleanValue()))) ? Boolean.TRUE.toString() : Boolean.FALSE.toString());

		return question;
	}

	/**
	 * Form html for attachments for this item.
	 * 
	 * @param attachments
	 *        The attachments definitions.
	 * @param id
	 *        The item id.
	 * @return The html for attachments for this item.
	 */
	protected String formatAttachments(List<AttachmentInfo> attachments, String id)
	{
		StringBuilder attachmentsHtml = new StringBuilder();
		for (AttachmentInfo a : attachments)
		{
			if (id.equals(a.itemId))
			{
				if ((a.isLink != null) && (a.isLink.booleanValue()))
				{
					attachmentsHtml.append("<li><a href=\"" + a.fileName + "\" target=\"_blank\">" + a.fileName + "</a></li>");
				}
				else
				{
					attachmentsHtml.append(formatRef(a.ref));
				}
			}
		}
		if (attachmentsHtml.length() > 0)
		{
			attachmentsHtml.insert(0, "<p><ul>");
			attachmentsHtml.append("</ul></p>");
		}

		return attachmentsHtml.toString();
	}

	/**
	 * Form html for one attachment from a ref.
	 * 
	 * @param refStr
	 *        The reference string.
	 * @return The html for attachment.
	 */
	protected String formatRef(String refStr)
	{
		StringBuilder rv = new StringBuilder();

		Reference ref = this.entityManager.newReference(refStr);

		// if we can't get the properties, assume that the attachment is to a deleted entity and skip it
		ResourceProperties props = ref.getProperties();
		if (props != null)
		{
			try
			{
				// for folders
				if (props.getBooleanProperty(ResourceProperties.PROP_IS_COLLECTION))
				{
					rv.append("<li><img src=\"/library/image/" + ContentTypeImageService.getContentTypeImage("folder")
							+ "\" style=\"border-style: none;\" />&nbsp;");
				}

				// otherwise lookup the icon from the mime type
				else
				{
					String type = props.getProperty(ResourceProperties.PROP_CONTENT_TYPE);
					rv.append("<li><img src=\"/library/image/" + ContentTypeImageService.getContentTypeImage(type)
							+ "\" style=\"border-style: none;\" alt=\"" + type + "\"/>&nbsp;");
				}

				// the link
				rv.append("<a href=\"" + ref.getUrl() + "\" target=\"_blank\" title=\""
						+ Validator.escapeHtml(props.getPropertyFormatted("DAV:displayname")) + "\">"
						+ Validator.escapeHtml(props.getPropertyFormatted("DAV:displayname")) + "</a>");

				// size
				rv.append("&nbsp;(" + props.getPropertyFormatted(ResourceProperties.PROP_CONTENT_LENGTH) + ")");

				rv.append("</li>");
			}
			catch (EntityPropertyNotDefinedException e)
			{
			}
			catch (EntityPropertyTypeException e)
			{
			}
		}

		return rv.toString();
	}

	/**
	 * Find the sites in which this user has this permission.
	 * 
	 * @param userId
	 *        The user id.
	 * @param permission
	 *        The security permission.
	 * @param excludeContext
	 *        a context to skip.
	 * @return The List of Ent's of sites in which this user has this permission.
	 */
	protected List<Ent> getAuthSites(String userId, String permission, String excludeContext)
	{
		List<Ent> rv = new ArrayList<Ent>();

		// get the authz groups in which this user has assignment permission
		Set refs = this.authzGroupService.getAuthzGroupsIsAllowed(userId, permission, null);
		for (Object o : refs)
		{
			String ref = (String) o;

			// each is a site ref
			Reference siteRef = this.entityManager.newReference(ref);

			// skip this one
			if ((excludeContext != null) && siteRef.getId().equals(excludeContext)) continue;

			// get the site display
			String display = this.siteService.getSiteDisplay(siteRef.getId());

			// take only the site title (between first and last quotes)
			int firstPos = display.indexOf("\"");
			int lastPos = display.lastIndexOf("\"");
			if ((firstPos != -1) && (lastPos != -1))
			{
				display = display.substring(firstPos + 1, lastPos);
			}

			// record for return
			Ent ent = new EntImpl(siteRef.getId(), display);
			rv.add(ent);
		}

		// sort
		Collections.sort(rv, new EntComparator());

		return rv;
	}

	/**
	 * Collect all the attachment references in the Samigo question html data:<br />
	 * Anything referenced by a src= or href=.
	 * 
	 * @param questionData
	 *        The Samigo question data.
	 * @param attachments
	 *        The attachments info.
	 * @return The set of attachment references.
	 */
	protected Set<String> harvestDocumentsReferenced(List<SamigoQuestion> questionData, List<AttachmentInfo> attachments)
	{
		Set<String> rv = new HashSet<String>();

		// collect all the references
		for (SamigoQuestion q : questionData)
		{
			rv.addAll(this.attachmentService.harvestAttachmentsReferenced(q.answerMatchText, true));
			rv.addAll(this.attachmentService.harvestAttachmentsReferenced(q.correctFeedback, true));
			rv.addAll(this.attachmentService.harvestAttachmentsReferenced(q.generalFeedback, true));
			rv.addAll(this.attachmentService.harvestAttachmentsReferenced(q.incorrectFeedback, true));
			rv.addAll(this.attachmentService.harvestAttachmentsReferenced(q.instruction, true));
			rv.addAll(this.attachmentService.harvestAttachmentsReferenced(q.questionChoiceText, true));
		}

		for (AttachmentInfo a : attachments)
		{
			// not for links
			if ((a.isLink == null) || (!a.isLink.booleanValue()))
			{
				rv.addAll(this.attachmentService.harvestEmbedded(a.ref, true));
			}
		}

		return rv;
	}

	/**
	 * Import the embedded documents to the MnemeDocs for the pool's context.
	 * 
	 * @param refs
	 *        The set of references to embedded document.
	 * @param context
	 *        The destination context.
	 * @return a Translation list for each imported document.
	 */
	protected List<Translation> importEmbeddedDocs(Set<String> refs, String context)
	{
		List<Translation> rv = this.attachmentService.importResources(AttachmentService.MNEME_APPLICATION, context, AttachmentService.DOCS_AREA,
				AttachmentService.NameConflictResolution.keepExisting, refs, AttachmentService.MNEME_THUMB_POLICY, AttachmentService.REFERENCE_ROOT);

		return rv;
	}

	/**
	 * Import the questions from a Samigo assessment.
	 * 
	 * @param assessmentId
	 *        The Samigo assessment id.
	 * @param pool
	 *        The pool for the questions.
	 */
	protected void importSamigoAssessmentQuestions(String assessmentId, Pool pool) throws AssessmentPermissionException
	{
		// read the questions
		final List<SamigoQuestion> questionData = new ArrayList<SamigoQuestion>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append(" I.ITEMID, I.TYPEID, I.SCORE, I.HASRATIONALE, I.INSTRUCTION, T.TEXT,");
		sql.append(" A.TEXT, A.SEQUENCE, A.ISCORRECT,");
		sql.append(" F1.TEXT, F2.TEXT, F3.TEXT,");
		sql.append(" M1.ENTRY, M2.ENTRY, M3.ENTRY");
		sql.append(" FROM SAM_SECTION_T S");
		sql.append(" JOIN SAM_ITEM_T I ON I.SECTIONID=S.SECTIONID");
		sql.append(" JOIN SAM_ITEMTEXT_T T ON I.ITEMID=T.ITEMID");
		sql.append(" INNER JOIN SAM_SECTIONMETADATA_T M ON S.SECTIONID=M.SECTIONID AND M.LABEL='AUTHOR_TYPE' AND M.ENTRY='1'");
		sql.append(" LEFT OUTER JOIN SAM_ANSWER_T A ON I.ITEMID=A.ITEMID AND ((I.TYPEID != 9) OR (A.ISCORRECT='1' AND A.ITEMTEXTID=T.ITEMTEXTID))");
		sql.append(" LEFT OUTER JOIN SAM_ITEMFEEDBACK_T F1 ON I.ITEMID=F1.ITEMID AND F1.TYPEID='INCORRECT FEEDBACK'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMFEEDBACK_T F2 ON I.ITEMID=F2.ITEMID AND F2.TYPEID='CORRECT FEEDBACK'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMFEEDBACK_T F3 ON I.ITEMID=F3.ITEMID AND F3.TYPEID='GENERAL FEEDBACK'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMMETADATA_T M1 ON I.ITEMID=M1.ITEMID AND M1.LABEL='CASE_SENSITIVE'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMMETADATA_T M2 ON I.ITEMID=M2.ITEMID AND M2.LABEL='MUTUALLY_EXCLUSIVE'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMMETADATA_T M3 ON I.ITEMID=M3.ITEMID AND M3.LABEL='RANDOMIZE'");
		sql.append(" WHERE S.ASSESSMENTID=?");
		sql.append(" ORDER BY I.ITEMID ASC, A.SEQUENCE ASC ");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(assessmentId);

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					SamigoQuestion sq = new SamigoQuestion();
					questionData.add(sq);

					sq.itemId = SqlHelper.readId(result, 1);
					sq.type = SqlHelper.readInteger(result, 2);
					sq.score = SqlHelper.readFloat(result, 3);
					sq.reason = SqlHelper.readBitBoolean(result, 4);
					sq.instruction = SqlHelper.readString(result, 5);
					sq.questionChoiceText = SqlHelper.readString(result, 6);
					sq.answerMatchText = SqlHelper.readString(result, 7);
					sq.answerSeq = SqlHelper.readInteger(result, 8);
					sq.correct = SqlHelper.readBitBoolean(result, 9);
					sq.incorrectFeedback = SqlHelper.readString(result, 10);
					sq.correctFeedback = SqlHelper.readString(result, 11);
					sq.generalFeedback = SqlHelper.readString(result, 12);
					sq.caseSensitive = SqlHelper.readBitBoolean(result, 13);
					sq.exclusive = SqlHelper.readBitBoolean(result, 14);
					sq.randomize = SqlHelper.readBitBoolean(result, 15);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("importSamigoQuestions-questions: " + e);
					return null;
				}
			}
		});

		// read all attachment references for these questions
		final List<AttachmentInfo> attachments = new ArrayList<AttachmentInfo>();

		sql = new StringBuilder();
		sql.append("SELECT A.ITEMID, A.RESOURCEID, A.FILENAME, A.MIMETYPE, A.ISLINK");
		sql.append(" FROM SAM_SECTION_T S");
		sql.append(" JOIN SAM_ITEM_T I ON I.SECTIONID=S.SECTIONID");
		sql.append(" JOIN SAM_ATTACHMENT_T A ON A.ITEMID=I.ITEMID");
		sql.append(" WHERE S.ASSESSMENTID=?");
		sql.append(" ORDER BY A.ATTACHMENTID ASC");

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					AttachmentInfo a = new AttachmentInfo();
					attachments.add(a);

					a.itemId = SqlHelper.readId(result, 1);
					a.ref = SqlHelper.readString(result, 2);
					if (a.ref != null) a.ref = "/content" + a.ref;
					a.fileName = SqlHelper.readString(result, 3);
					a.mimeType = SqlHelper.readString(result, 4);
					a.isLink = SqlHelper.readBitBoolean(result, 5);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("importSamigoQuestions-attachments: " + e);
					return null;
				}
			}
		});

		// bring in these questions
		importSamigoQuestions(questionData, attachments, pool);
	}

	/**
	 * Import the questions from a Samigo pool.
	 * 
	 * @param poolId
	 *        The Samigo pool id.
	 * @param pool
	 *        The pool for the questions.
	 */
	protected void importSamigoPoolQuestions(String poolId, Pool pool) throws AssessmentPermissionException
	{
		// read the questions
		final List<SamigoQuestion> questionData = new ArrayList<SamigoQuestion>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append(" P.ITEMID, I.TYPEID, I.SCORE, I.HASRATIONALE, I.INSTRUCTION, T.TEXT,");
		sql.append(" A.TEXT, A.SEQUENCE, A.ISCORRECT,");
		sql.append(" F1.TEXT, F2.TEXT, F3.TEXT,");
		sql.append(" M1.ENTRY, M2.ENTRY, M3.ENTRY");
		sql.append(" FROM SAM_QUESTIONPOOLITEM_T P");
		sql.append(" JOIN SAM_ITEM_T I ON P.ITEMID=I.ITEMID");
		sql.append(" JOIN SAM_ITEMTEXT_T T ON P.ITEMID=T.ITEMID");
		sql.append(" LEFT OUTER JOIN SAM_ANSWER_T A ON P.ITEMID=A.ITEMID AND ((I.TYPEID != 9) OR (A.ISCORRECT='1' AND A.ITEMTEXTID=T.ITEMTEXTID))");
		sql.append(" LEFT OUTER JOIN SAM_ITEMFEEDBACK_T F1 ON P.ITEMID=F1.ITEMID AND F1.TYPEID='INCORRECT FEEDBACK'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMFEEDBACK_T F2 ON P.ITEMID=F2.ITEMID AND F2.TYPEID='CORRECT FEEDBACK'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMFEEDBACK_T F3 ON P.ITEMID=F3.ITEMID AND F3.TYPEID='GENERAL FEEDBACK'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMMETADATA_T M1 ON P.ITEMID=M1.ITEMID AND M1.LABEL='CASE_SENSITIVE'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMMETADATA_T M2 ON P.ITEMID=M2.ITEMID AND M2.LABEL='MUTUALLY_EXCLUSIVE'");
		sql.append(" LEFT OUTER JOIN SAM_ITEMMETADATA_T M3 ON P.ITEMID=M3.ITEMID AND M3.LABEL='RANDOMIZE'");
		sql.append(" WHERE P.QUESTIONPOOLID=?");
		sql.append(" ORDER BY P.ITEMID ASC, A.SEQUENCE ASC ");

		Object[] fields = new Object[1];
		fields[0] = Long.valueOf(poolId);

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					SamigoQuestion sq = new SamigoQuestion();
					questionData.add(sq);

					sq.itemId = SqlHelper.readId(result, 1);
					sq.type = SqlHelper.readInteger(result, 2);
					sq.score = SqlHelper.readFloat(result, 3);
					sq.reason = SqlHelper.readBitBoolean(result, 4);
					sq.instruction = SqlHelper.readString(result, 5);
					sq.questionChoiceText = SqlHelper.readString(result, 6);
					sq.answerMatchText = SqlHelper.readString(result, 7);
					sq.answerSeq = SqlHelper.readInteger(result, 8);
					sq.correct = SqlHelper.readBitBoolean(result, 9);
					sq.incorrectFeedback = SqlHelper.readString(result, 10);
					sq.correctFeedback = SqlHelper.readString(result, 11);
					sq.generalFeedback = SqlHelper.readString(result, 12);
					sq.caseSensitive = SqlHelper.readBitBoolean(result, 13);
					sq.exclusive = SqlHelper.readBitBoolean(result, 14);
					sq.randomize = SqlHelper.readBitBoolean(result, 15);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("importSamigoQuestions-questions: " + e);
					return null;
				}
			}
		});

		// read all attachment references for these questions
		final List<AttachmentInfo> attachments = new ArrayList<AttachmentInfo>();

		sql = new StringBuilder();
		sql.append("SELECT A.ITEMID, A.RESOURCEID, A.FILENAME, A.MIMETYPE, A.ISLINK");
		sql.append(" FROM SAM_ATTACHMENT_T A");
		sql.append(" JOIN SAM_QUESTIONPOOLITEM_T P ON A.ITEMID=P.ITEMID AND P.QUESTIONPOOLID=?");
		sql.append(" ORDER BY A.ATTACHMENTID ASC");

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					AttachmentInfo a = new AttachmentInfo();
					attachments.add(a);

					a.itemId = SqlHelper.readId(result, 1);
					a.ref = SqlHelper.readString(result, 2);
					if (a.ref != null) a.ref = "/content" + a.ref;
					a.fileName = SqlHelper.readString(result, 3);
					a.mimeType = SqlHelper.readString(result, 4);
					a.isLink = SqlHelper.readBitBoolean(result, 5);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("importSamigoQuestions-attachments: " + e);
					return null;
				}
			}
		});

		// bring in these questions
		importSamigoQuestions(questionData, attachments, pool);
	}

	/**
	 * Create questions from this data.
	 * 
	 * @param questionData
	 *        The Samigo question data.
	 * @param attachments
	 *        The attachments info.
	 */
	protected void importSamigoQuestions(List<SamigoQuestion> questionData, List<AttachmentInfo> attachments, Pool pool)
			throws AssessmentPermissionException
	{
		// find any additional docs references in the question data and attachments
		Set<String> docs = harvestDocumentsReferenced(questionData, attachments);

		// import those docs, returning the translation mapping
		List<Translation> translations = importEmbeddedDocs(docs, pool.getContext());

		// modify the question and attachment data to reference the new doc locations
		convertDocumentsReferenced(questionData, attachments, translations);

		// build the questions
		float total = 0f;
		int count = 0;
		for (int i = 0; i < questionData.size(); i++)
		{
			SamigoQuestion sq = questionData.get(i);

			// accumulate the score for an average for the pool
			if (sq.score != null)
			{
				count++;
				total += sq.score.floatValue();
			}

			// get the rest
			int next = i + 1;
			for (; next < questionData.size(); next++)
			{
				SamigoQuestion sqNext = questionData.get(next);
				if (!sqNext.itemId.equals(sq.itemId))
				{
					next--;
					break;
				}
			}
			if (next == questionData.size()) next--;

			String attachmentsHtml = formatAttachments(attachments, sq.itemId);

			// we have from i .. next, inclusive
			SamigoQuestion[] these = new SamigoQuestion[(next - i) + 1];
			for (int index = i; index <= next; index++)
			{
				these[index - i] = questionData.get(index);
			}
			i = next;

			Question question = null;
			switch (sq.type)
			{
				case 1:
				{
					// single correct
					question = createMc(these, attachmentsHtml, pool, false);

					break;
				}
				case 2:
				{
					// multi correct
					question = createMc(these, attachmentsHtml, pool, true);

					break;
				}
				case 3:
				{
					// mnemeType = "mneme:LikertScale";
					question = createLikert(these, attachmentsHtml, pool);
					break;
				}
				case 4:
				{
					question = createTf(these, attachmentsHtml, pool);
					break;
				}
				case 5:
				{
					// inline essay
					question = createEssay(these, attachmentsHtml, pool, false);
					break;
				}
				case 6:
				{
					// upload essay
					question = createEssay(these, attachmentsHtml, pool, true);
					break;
				}
				case 8:
				{
					// text
					question = createFillin(these, attachmentsHtml.toString(), pool, true);
					break;
				}
				case 9:
				{
					question = createMatch(these, attachmentsHtml, pool);
					break;
				}
				case 11:
				{
					// numeric
					question = createFillin(these, attachmentsHtml, pool, false);
					break;
				}
			}

			if (question != null)
			{
				// reason
				if (sq.reason != null) question.setExplainReason(sq.reason);

				// feedback
				if (sq.generalFeedback != null)
				{
					question.setFeedback(sq.generalFeedback);
				}
				else if (sq.correctFeedback != null)
				{
					question.setFeedback(sq.correctFeedback);
				}
				else if (sq.incorrectFeedback != null)
				{
					question.setFeedback(sq.incorrectFeedback);
				}

				// save
				question.getTypeSpecificQuestion().consolidate("");
				this.questionService.saveQuestion(question);
			}
		}

		// set the pool's points to the average
		if (count > 0)
		{
			Float average = Float.valueOf(total / count);
			pool.setPoints(average);
			this.poolService.savePool(pool);
		}
	}

	/**
	 * Read the Assignments for this context.
	 * 
	 * @param context
	 *        The context.
	 * @return The list of Ents describing the assignments for this context.
	 */
	protected List<Ent> readAssignments(String context)
	{
		List<Ent> rv = new ArrayList<Ent>();

		Iterator i = this.assignmentService.getAssignmentsForContext(context);
		while (i.hasNext())
		{
			Assignment a = (Assignment) i.next();
			Ent ent = new EntImpl(a.getId(), a.getTitle());
			rv.add(ent);
		}

		return rv;
	}

	/**
	 * Read the Samigo assessments for this context.
	 * 
	 * @param context
	 *        The context.
	 * @return The list of Ents describing the assessments for this context.
	 */
	protected List<Ent> readSamigoAssessments(String context)
	{
		final List<Ent> rv = new ArrayList<Ent>();

		// test if the samigo table exists
		if (!tableExists("SAM_ASSESSMENTBASE_T")) return rv;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT A.ID, A.TITLE FROM SAM_ASSESSMENTBASE_T A");
		sql.append(" INNER JOIN SAM_AUTHZDATA_T Z ON A.ID=Z.QUALIFIERID AND Z.FUNCTIONID=? AND Z.AGENTID=?");
		sql.append(" INNER JOIN SAM_SECTION_T S ON S.ASSESSMENTID=A.ID");
		sql.append(" INNER JOIN SAM_SECTIONMETADATA_T M ON S.SECTIONID=M.SECTIONID AND M.LABEL='AUTHOR_TYPE' AND M.ENTRY='1'");

		Object[] fields = new Object[2];
		fields[0] = "EDIT_ASSESSMENT";
		fields[1] = StringUtil.trimToNull(context);

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					String id = SqlHelper.readId(result, 1);
					String title = SqlHelper.readString(result, 2);

					Ent ent = new EntImpl(id, title);
					if (!rv.contains(ent)) rv.add(ent);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("readSamigoAssessments: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * Read the Samigo pools for this user id.
	 * 
	 * @param userId
	 *        The user id.
	 * @return The list of Ents describing the pools for this user id.
	 */
	protected List<Ent> readSamigoPools(String userId)
	{
		final List<Ent> rv = new ArrayList<Ent>();

		// test if the samigo table exists
		if (!tableExists("SAM_QUESTIONPOOL_T")) return rv;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.QUESTIONPOOLID, P.TITLE FROM SAM_QUESTIONPOOL_T P WHERE P.OWNERID=?");

		Object[] fields = new Object[1];
		fields[0] = StringUtil.trimToNull(userId);

		this.sqlService.dbRead(sql.toString(), fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					String id = SqlHelper.readId(result, 1);
					String title = SqlHelper.readString(result, 2);

					Ent ent = new EntImpl(id, title);
					rv.add(ent);

					return null;
				}
				catch (SQLException e)
				{
					M_log.warn("readSamigoPools: " + e);
					return null;
				}
			}
		});

		return rv;
	}

	/**
	 * Check if this table exists.
	 * 
	 * @param table
	 *        The table name.
	 * @return true if the table exists, false if not.
	 */
	protected boolean tableExists(String table)
	{
		String sql = "SHOW TABLES LIKE '" + table + "'";
		List rv = this.sqlService.dbRead(sql);
		return !rv.isEmpty();
	}
}
