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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.Attachment;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.MnemeTransferService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.PartDetail;
import org.etudes.mneme.api.Pool;
import org.etudes.mneme.api.PoolDraw;
import org.etudes.mneme.api.PoolService;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.QuestionPick;
import org.etudes.mneme.api.QuestionService;
import org.etudes.mneme.api.SecurityService;
import org.etudes.util.api.Translation;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * MnemeServiceImpl implements MnemeService
 */
public class MnemeTransferServiceImpl implements MnemeTransferService, EntityTransferrer, EntityProducer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(MnemeTransferServiceImpl.class);

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService = null;

	/** Dependency: AttachmentService */
	protected AttachmentService attachmentService = null;

	/** Messages bundle name. */
	protected String bundle = null;

	/** Dependency: EntityManager. */
	protected EntityManager entityManager = null;

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

	/** Dependency: ThreadLocalManager */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return null;
	}

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
	public Entity getEntity(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return this.messages.getFormattedMessage("test-center", null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void importFromSite(String fromContext, String toContext, Set<String> assessmentsToImport)
	{
		if (M_log.isDebugEnabled()) M_log.debug("copy from: " + fromContext + " to: " + toContext);

		// security check
		if (!this.securityService.checkSecurity(sessionManager.getCurrentSessionUserId(), MnemeService.MANAGE_PERMISSION, toContext))
		{
			return;
		}

		// get assessment pool and question dependencies
		Set<String> fullPoolsToImport = null;
		Set<String> partialPoolsToImport = null;
		Set<String> questionsToImport = null;
		Set<String> allPoolsToImport = null;
		if (assessmentsToImport != null)
		{
			fullPoolsToImport = new HashSet<String>();
			partialPoolsToImport = new HashSet<String>();
			questionsToImport = new HashSet<String>();
			for (String id : assessmentsToImport)
			{
				Assessment assessment = this.assessmentService.getAssessment(id);
				if (assessment != null)
				{
					findAssessmentDependencies(assessment, fullPoolsToImport, partialPoolsToImport, questionsToImport);
				}
			}

			allPoolsToImport = new HashSet<String>();
			if (fullPoolsToImport != null) allPoolsToImport.addAll(fullPoolsToImport);
			if (partialPoolsToImport != null) allPoolsToImport.addAll(partialPoolsToImport);
		}

		// map from old pool ids to new ids
		Map<String, String> pidMap = new HashMap<String, String>();

		// map from old question ids to new ids
		Map<String, String> qidMap = new HashMap<String, String>();

		// get all pools in the from context
		List<Pool> pools = this.poolService.getPools(fromContext);

		// get all the assessments in the from context
		List<Assessment> assessments = this.assessmentService.getContextAssessments(fromContext, null, Boolean.FALSE);

		// which assessments are we going to skip?
		Set<String> skippedAssessments = new HashSet<String>();
		List<Assessment> existingAssessments = this.assessmentService.getContextAssessments(toContext, AssessmentService.AssessmentsSort.cdate_a,
				Boolean.FALSE);
		for (Assessment assessment : assessments)
		{
			// if the assessment is not in the list, skip over
			if ((assessmentsToImport != null) && (!assessmentsToImport.contains(assessment.getId()))) continue;

			// check if we have an assessment that matches (check title only)
			for (Assessment candidate : existingAssessments)
			{
				if (!StringUtil.different(candidate.getTitle(), assessment.getTitle()))
				{
					skippedAssessments.add(assessment.getId());
					break;
				}
			}
		}

		// collect media references
		Set<String> refs = new HashSet<String>();

		// pools
		for (Pool pool : pools)
		{
			// skip if we are not needing this pool
			if ((allPoolsToImport != null) && (!allPoolsToImport.contains(pool.getId()))) continue;

			// from the pool description (Note: we might end up not taking this pool, if one exists in the destination, but we harvest anyway)
			refs.addAll(this.attachmentService.harvestAttachmentsReferenced(pool.getDescription(), true));

			List<String> qids = ((PoolImpl) pool).getAllQuestionIds(null, null);
			for (String qid : qids)
			{
				// if this pool is not fully needed
				if ((fullPoolsToImport != null) && (!fullPoolsToImport.contains(pool.getId())))
				{
					// if this question is not on the include list specifically
					if ((questionsToImport != null) && (!questionsToImport.contains(qid)))
					{
						// then we don't want to process it
						continue;
					}
				}

				Question question = this.questionService.getQuestion(qid);
				refs.addAll(this.attachmentService.harvestAttachmentsReferenced(question.getPresentation().getText(), true));
				for (Reference attachment : question.getPresentation().getAttachments())
				{
					refs.add(attachment.getReference());
				}
				refs.addAll(this.attachmentService.harvestAttachmentsReferenced(question.getHints(), true));
				refs.addAll(this.attachmentService.harvestAttachmentsReferenced(question.getFeedback(), true));
				for (String data : question.getTypeSpecificQuestion().getData())
				{
					refs.addAll(this.attachmentService.harvestAttachmentsReferenced(data, true));
				}
			}
		}

		// assessments
		for (Assessment assessment : assessments)
		{
			// if we are picking specific ones and this is not one, skip it
			if ((assessmentsToImport != null) && (!assessmentsToImport.contains(assessment.getId()))) continue;

			// if we are skipping this one, skip it
			if (skippedAssessments.contains(assessment.getId())) continue;

			refs.addAll(this.attachmentService.harvestAttachmentsReferenced(assessment.getPresentation().getText(), true));
			for (Reference attachment : assessment.getPresentation().getAttachments())
			{
				refs.add(attachment.getReference());
			}
			refs.addAll(this.attachmentService.harvestAttachmentsReferenced(assessment.getSubmitPresentation().getText(), true));
			for (Reference attachment : assessment.getSubmitPresentation().getAttachments())
			{
				refs.add(attachment.getReference());
			}

			for (Part part : assessment.getParts().getParts())
			{
				refs.addAll(this.attachmentService.harvestAttachmentsReferenced(part.getPresentation().getText(), true));
				for (Reference attachment : part.getPresentation().getAttachments())
				{
					refs.add(attachment.getReference());
				}
			}
		}

		// any others in MnemeDocs for the site we have not yet covered - only if we are taking the whole set, not specific assessments
		if (assessmentsToImport == null)
		{
			List<Attachment> attachments = this.attachmentService.findFiles(AttachmentService.MNEME_APPLICATION, fromContext,
					AttachmentService.DOCS_AREA);
			for (Attachment attachment : attachments)
			{
				refs.add(attachment.getReference());
			}
		}

		// import all referenced and attached documents, translating any html to local references, creating translations
		List<Translation> translations = this.attachmentService.importResources(AttachmentService.MNEME_APPLICATION, toContext,
				AttachmentService.DOCS_AREA, AttachmentService.NameConflictResolution.keepExisting, refs, AttachmentService.MNEME_THUMB_POLICY,
				AttachmentService.REFERENCE_ROOT);

		// copy each pool, with all questions, merging
		for (Pool pool : pools)
		{
			// skip if we are not needing this pool
			if ((allPoolsToImport != null) && (!allPoolsToImport.contains(pool.getId()))) continue;

			// we might just want the pool to exist, and take only some questions (those in the pool that are also in the questionsToImport set)
			boolean partial = ((partialPoolsToImport != null) && (partialPoolsToImport.contains(pool.getId())));
			Pool newPool = ((PoolServiceImpl) this.poolService).doCopyPool(toContext, pool, false, qidMap, false, translations, true,
					partial ? questionsToImport : null);

			pidMap.put(pool.getId(), newPool.getId());
		}

		// copy each assessment, unless we have a title conflict
		for (Assessment assessment : assessments)
		{
			// if we are picking specific ones and this is not one, skip it
			if ((assessmentsToImport != null) && (!assessmentsToImport.contains(assessment.getId()))) continue;

			// if we are skipping this one, skip it
			if (skippedAssessments.contains(assessment.getId())) continue;

			((AssessmentServiceImpl) this.assessmentService).doCopyAssessment(toContext, assessment, pidMap, qidMap, false, translations);
		}
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// entity producer registration (note: there is no reference root since we do no entities)
			entityManager.registerEntityProducer(this, "/mneme-NEVER");

			// messages
			if (this.bundle != null) this.messages = new ResourceLoader(this.bundle);

			M_log.info("init():");
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds =
		{ "sakai.mneme" };
		return toolIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		return false;
	}

	/**
	 * Dependency: AssessmentService.
	 * 
	 * @param service
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService service)
	{
		assessmentService = service;
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
	 * Dependency: PoolService.
	 * 
	 * @param service
	 *        The PoolService.
	 */
	public void setPoolService(PoolService service)
	{
		poolService = service;
	}

	/**
	 * Dependency: QuestionService.
	 * 
	 * @param service
	 *        The QuestionService.
	 */
	public void setQuestionService(QuestionService service)
	{
		questionService = service;
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
	 *        The ThreadLocalManager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		this.threadLocalManager = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids)
	{
		importFromSite(fromContext, toContext, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{
		// TODO: implement cleanup?
		importFromSite(fromContext, toContext, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * Check if an assessment depends on a pool or any questions in the pool.
	 * 
	 * @param assessment
	 *        The assessment to check.
	 * @param pool
	 *        The pool to check against.
	 * @return true if there is a dependency, false if not.
	 */
	protected boolean assessmentDependsOnPool(Assessment assessment, Pool pool)
	{
		for (Part part : assessment.getParts().getParts())
		{
			for (PartDetail detail : part.getDetails())
			{
				if (detail instanceof PoolDraw)
				{
					PoolDraw draw = (PoolDraw) detail;

					if (draw.getOrigPoolId().equals(pool.getId()))
					{
						return true;
					}
				}
				else if (detail instanceof QuestionPick)
				{
					QuestionPick pick = (QuestionPick) detail;
					Question question = this.questionService.getQuestion(pick.getOrigQuestionId());
					if ((question != null) && (question.getPool().getId().equals(pool.getId())))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * For the assessment, find the set of pools and questions that it uses. Consider the original (not historical) versions.
	 * 
	 * @param assessment
	 *        The assessment.
	 * @param fullPools
	 *        The set of ids of the pools the assessment draws from (so it uses the entire pool).
	 * @param partialPools
	 *        The set of ids of the pools the assessment picks specific questions from.
	 * @param questions
	 *        The set of ids of the questions that the assessment uses (picks).
	 */
	protected void findAssessmentDependencies(Assessment assessment, Set<String> fullPools, Set<String> partialPools, Set<String> questions)
	{
		for (Part part : assessment.getParts().getParts())
		{
			for (PartDetail detail : part.getDetails())
			{
				if (detail instanceof PoolDraw)
				{
					PoolDraw draw = (PoolDraw) detail;
					fullPools.add(draw.getOrigPoolId());
					// also remove it from partial in case it was there
					partialPools.remove(draw.getOrigPoolId());
				}
				else if (detail instanceof QuestionPick)
				{
					QuestionPick pick = (QuestionPick) detail;
					Question question = this.questionService.getQuestion(pick.getOrigQuestionId());
					if (question != null)
					{
						// unless we are already going to copy the full pool add it to the partial pool list and specific question list
						if (!fullPools.contains(question.getPool().getId()))
						{
							partialPools.add(question.getPool().getId());
							questions.add(question.getId());
						}
					}
				}
			}
		}
	}
}
