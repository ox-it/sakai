/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2013, 2014 Etudes, Inc.
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

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.util.EscapeRefUrl;
import org.etudes.mneme.api.Answer;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentParts;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.etudes.mneme.api.Attachment;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.Part;
import org.etudes.mneme.api.Question;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.etudes.mneme.api.SubmissionService.FindAssessmentSubmissionsSort;
import org.etudes.mneme.api.TypeSpecificAnswer;
import org.etudes.mneme.api.TypeSpecificQuestion;
import org.etudes.mneme.impl.EssayQuestionImpl.SubmissionType;
import org.etudes.mneme.impl.MatchQuestionImpl.MatchQuestionPair;
import org.etudes.util.DateHelper;
import org.etudes.util.TranslationImpl;
import org.etudes.util.api.Translation;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.CopyrightException;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdLengthException;
import org.sakaiproject.exception.IdUniquenessException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.OverQuotaException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * AttachmentServiceImpl implements AttachmentService.
 */
public class AttachmentServiceImpl implements AttachmentService, EntityProducer
{
	public class LikertScaleCount
	{
		protected int count;

		protected String text;

		public LikertScaleCount(String text, int count)
		{
			this.text = text;
			this.count = count;
		}

		public int getCount()
		{
			return this.count;
		}

		public String getText()
		{
			return this.text;
		}

		public void setText(String text)
		{
			this.text = text;
		}
	}

	public class ScoreUser implements java.util.Comparator<ScoreUser>
	{
		protected Float score;
		protected String userId;

		public ScoreUser(Float score, String userId)
		{
			this.score = score;
			this.userId = userId;
		}

		public int compare(ScoreUser arg0, ScoreUser arg1)
		{
			// null (ungraded) is lower than everyone
			if (arg0.score == null) return -1;

			// everyone is higher than null
			if (arg1.score == null) return 1;

			if (arg0.score == null && arg1.score == null) return 0;

			return arg0.score.compareTo(arg1.score);
		}

		public Float getScore()
		{
			return this.score;
		}
	}

	/** A thread-local key to the List of resource bodies translated so far in the thread. */
	public final static String THREAD_TRANSLATIONS_BODY_KEY = "AttachmentService.body.translations.";

	/** A thread-local key to the List of Translations we have made so far in the thread. */
	public final static String THREAD_TRANSLATIONS_KEY = "AttachmentService.translations.";

	/** Our logger. */
	private static Log M_log = LogFactory.getLog(AttachmentServiceImpl.class);

	protected final static String PROP_THUMB = "attachment:thumb";

	protected final static String PROP_UNIQUE_HOLDER = "attachment:unique";

	/** The chunk size used when streaming (100k). */
	protected static final int STREAM_BUFFER_SIZE = 102400;

	/** Assessment service. */
	protected AssessmentService assessmentService = null;

	/** Messages bundle name. */
	protected String bundle = null;

	/** Dependency: ContentHostingService */
	protected ContentHostingService contentHostingService = null;

	/** Dependency: EntityManager */
	protected EntityManager entityManager = null;

	/** Dependency: EventTrackingService */
	protected EventTrackingService eventTrackingService = null;

	/** Dependency: IdManager. */
	protected IdManager idManager = null;

	/** Configuration: to make thumbs for images or not. */
	protected boolean makeThumbs = true;

	/** Messages. */
	protected transient InternationalizedMessages messages = null;

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SecurityService */
	protected org.sakaiproject.authz.api.SecurityService securityServiceSakai = null;

	/** Dependency: ServerConfigurationService */
	protected ServerConfigurationService serverConfigurationService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/** Dependency: SiteService */
	protected SiteService siteService = null;

	/** Dependency: SubmissionService */
	protected SubmissionService submissionService = null;

	/** Dependency: ThreadLocalManager */
	protected ThreadLocalManager threadLocalManager = null;

	/** Dependency: UserDirectoryService. */
	protected UserDirectoryService userDirectoryService = null;


	/**
	 * {@inheritDoc}
	 */
	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, FileItem file,
			boolean makeThumb, String altRef)
	{
		pushAdvisor();

		try
		{
			String name = file.getName();
			if (name != null)
			{
				name = massageName(name);
			}

			String type = file.getContentType();

			// TODO: change to file.getInputStream() for after Sakai 2.3 more efficient support
			// InputStream body = file.getInputStream();
			byte[] body = file.get();

			long size = file.getSize();

			// detect no file selected
			if ((name == null) || (type == null) || (body == null) || (size == 0))
			{
				// TODO: if using input stream, close it
				// if (body != null) body.close();
				return null;
			}

			Reference rv = addAttachment(name, name, application, context, prefix, onConflict, type, body, size, makeThumb, altRef);
			return rv;
		}
		finally
		{
			popAdvisor();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, Reference resourceRef,
			boolean makeThumb, String altRef)
	{
		// make sure we can read!
		pushAdvisor();

		try
		{
			// get an id from the reference string
			String chsId = resourceRef.getId();
			if (chsId.startsWith("/content/"))
			{
				chsId = chsId.substring("/content".length());
			}

			// make sure we can read!
			ContentResource resource = this.contentHostingService.getResource(chsId);
			String type = resource.getContentType();
			long size = resource.getContentLength();
			byte[] body = resource.getContent();
			String name = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);

			// form an id from the resource id
			String id = massageName(resource.getId());

			Reference rv = addAttachment(id, name, application, context, prefix, onConflict, type, body, size, makeThumb, altRef);
			return rv;
		}
		catch (PermissionException e)
		{
			M_log.warn("addAttachment: " + e.toString());
		}
		catch (IdUnusedException e)
		{
			M_log.warn("addAttachment: " + e.toString());
		}
		catch (TypeException e)
		{
			M_log.warn("addAttachment: " + e.toString());
		}
		catch (ServerOverloadException e)
		{
			M_log.warn("addAttachment: " + e.toString());
		}
		finally
		{
			// clear the security advisor
			popAdvisor();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, String name, byte[] body,
			String type, boolean makeThumb, String altRef)
	{
		pushAdvisor();

		try
		{
			if (name != null)
			{
				name = massageName(name);
			}

			long size = body.length;

			// detect no file selected
			if ((name == null) || (type == null) || (body == null) || (size == 0))
			{
				return null;
			}

			Reference rv = addAttachment(name, name, application, context, prefix, onConflict, type, body, size, makeThumb, altRef);
			return rv;
		}
		finally
		{
			popAdvisor();
		}
	}

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
	public List<Attachment> findFiles(String application, String context, String prefix)
	{
		return findTypes(application, context, prefix, null, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Attachment> findThumbs(String application, String context, String prefix)
	{
		return findTypes(application, context, prefix, null, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		// decide on security
		if (!checkSecurity(ref)) return null;

		// isolate the ContentHosting reference
		Reference contentHostingRef = entityManager.newReference(ref.getId());

		// setup a security advisor
		pushAdvisor();
		try
		{
			// make sure we have a valid ContentHosting reference with an entity producer we can talk to
			EntityProducer service = contentHostingRef.getEntityProducer();
			if (service == null) return null;

			// pass on the request
			return service.getEntity(contentHostingRef);
		}
		finally
		{
			// clear the security advisor
			popAdvisor();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// Since we handle security ourself, we won't support anyone else asking
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		// decide on security
		if (!checkSecurity(ref)) return null;

		// isolate the ContentHosting reference
		Reference contentHostingRef = entityManager.newReference(ref.getId());

		// setup a security advisor
		pushAdvisor();
		try
		{
			// make sure we have a valid ContentHosting reference with an entity producer we can talk to
			EntityProducer service = contentHostingRef.getEntityProducer();
			if (service == null) return null;

			// pass on the request
			return service.getEntityDescription(contentHostingRef);
		}
		finally
		{
			// clear the security advisor
			popAdvisor();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// decide on security
		if (!checkSecurity(ref)) return null;

		// for download
		if (DOWNLOAD.equals(ref.getContainer()))
		{
			if (DOWNLOAD_ALL_SUBMISSIONS_QUESTION.equals(ref.getSubType()))
			{
				ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();

				// use the site title and question ids for the file name
				String[] outerParts = StringUtil.split(ref.getId(), ".");
				String[] parts = StringUtil.split(outerParts[0], "_");
				String siteTitle = ref.getContext();
				try
				{
					Site site = this.siteService.getSite(ref.getContext());
					siteTitle = site.getTitle();
				}
				catch (IdUnusedException e)
				{
				}

				// get the name extension from the messages (i.e. "_Submissions.zip")
				String fileName = siteTitle + "_" + parts[1] + this.messages.getFormattedMessage("download_submissions_file_name", null);
				fileName = fileName.replace(' ', '_');

				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, "application/zip");
				props.addProperty("DAV:displayname", fileName);
				props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "FALSE");

				return props;
			}
			else if (EXPORT_SUMMARY.equals(ref.getSubType()))
			{
				ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();

				// use the site title and question ids for the file name
				String[] outerParts = StringUtil.split(ref.getId(), ".");
				String siteTitle = ref.getContext();
				try
				{
					Site site = this.siteService.getSite(ref.getContext());
					siteTitle = site.getTitle();
				}
				catch (IdUnusedException e)
				{
				}

				// get the name extension from the messages (i.e. "_ExportSummary.xls")
				String fileName = siteTitle + "_" + outerParts[0]+ this.messages.getFormattedMessage("export_summary_file_name", null) ;

				fileName = fileName.replace(' ', '_');

				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, "application/vnd.ms-excel");
				props.addProperty("DAV:displayname", fileName);
				props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "FALSE");

				return props;
			}
			else if (ITEM_ANALYSIS.equals(ref.getSubType()))
			{
				ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();

				// use the site title and question ids for the file name
				String[] outerParts = StringUtil.split(ref.getId(), ".");
				String siteTitle = ref.getContext();
				try
				{
					Site site = this.siteService.getSite(ref.getContext());
					siteTitle = site.getTitle();
				}
				catch (IdUnusedException e)
				{
				}

				// get the name extension from the messages (i.e. "_ItemAnalysis.xls")
				String fileName = siteTitle + "_" + outerParts[0]+ this.messages.getFormattedMessage("item_analysis_file_name", null) ;

				fileName = fileName.replace(' ', '_');

				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, "application/vnd.ms-excel");
				props.addProperty("DAV:displayname", fileName);
				props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "FALSE");

				return props;
			}
			else if (ASMT_STATS.equals(ref.getSubType()))
			{
				ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();

				// use the site title and question ids for the file name
				String[] outerParts = StringUtil.split(ref.getId(), ".");
				String siteTitle = ref.getContext();
				try
				{
					Site site = this.siteService.getSite(ref.getContext());
					siteTitle = site.getTitle();
				}
				catch (IdUnusedException e)
				{
				}

				// get the name extension from the messages (i.e. "_AsmtStats.xls")
				String fileName = siteTitle + "_" + outerParts[0]+ this.messages.getFormattedMessage("asmt_stats_file_name", null) ;

				fileName = fileName.replace(' ', '_');

				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, "application/vnd.ms-excel");
				props.addProperty("DAV:displayname", fileName);
				props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "FALSE");

				return props;
			}
			else if (ASMT_CERT.equals(ref.getSubType()))
			{
				ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();

				// get the name extension from the messages (i.e. "cert.html")
				String fileName = this.messages.getFormattedMessage("cert_file_name", null) ;
				
				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, "text/html");
				props.addProperty("DAV:displayname", fileName);
				props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "FALSE");

				return props;
			}
			else if (ASMT_EXPORT.equals(ref.getSubType()))
			{
				ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();

				String siteTitle = ref.getContext();
				try
				{
					Site site = this.siteService.getSite(ref.getContext());
					siteTitle = site.getTitle();
				}
				catch (IdUnusedException e)
				{
				}
				
				String[] outerParts = StringUtil.split(ref.getReference(), "/");
				String values = outerParts[outerParts.length - 1];
				values = values.replace(".zip", "");
				String[] ids = StringUtil.split(values, "+");
				
				// get the name extension from the messages (i.e. "someAssessments")
				Integer countAssessments = this.assessmentService.countAssessments(ref.getContext());
				String subFilename = this.messages.getFormattedMessage("download_someExport_file_name", null);
				if (countAssessments.intValue() == ids.length)
					subFilename = this.messages.getFormattedMessage("download_allExport_file_name", null);
			
				String fileName = siteTitle + "_" + subFilename;
				fileName = fileName.replace(' ', '_');

				props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, "application/zip");
				props.addProperty("DAV:displayname", fileName);
				props.addProperty(ResourceProperties.PROP_IS_COLLECTION, "FALSE");

				return props;
			}
			else
			{
				// unknown download request
				M_log.warn("getEntityResourceProperties: unknown download request: " + ref.getReference());
				return null;
			}
		}

		// else for private docs CHS ref
		else
		{
			// isolate the ContentHosting reference
			Reference contentHostingRef = entityManager.newReference(ref.getId());

			// setup a security advisor
			pushAdvisor();
			try
			{
				// make sure we have a valid ContentHosting reference with an entity producer we can talk to
				EntityProducer service = contentHostingRef.getEntityProducer();
				if (service == null) return null;

				// pass on the request
				return service.getEntityResourceProperties(contentHostingRef);
			}
			finally
			{
				// clear the security advisor
				popAdvisor();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return serverConfigurationService.getAccessUrl() + ref.getReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		final AttachmentServiceImpl service = this;

		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
					throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
			{
				// decide on security
				if (!checkSecurity(ref))
				{
					throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "access", ref.getReference());
				}

				// for download
				if (DOWNLOAD.equals(ref.getContainer()))
				{
					service.handleAccessDownload(req, res, ref, copyrightAcceptedRefs);
				}

				// for private docs, forward to CHS
				else
				{
					// isolate the ContentHosting reference
					Reference contentHostingRef = entityManager.newReference(ref.getId());

					// setup a security advisor
					pushAdvisor();
					try
					{
						// make sure we have a valid ContentHosting reference with an entity producer we can talk to
						EntityProducer service = contentHostingRef.getEntityProducer();
						if (service == null) throw new EntityNotDefinedException(ref.getReference());

						// get the producer's HttpAccess helper, it might not support one
						HttpAccess access = service.getHttpAccess();
						if (access == null) throw new EntityNotDefinedException(ref.getReference());

						// let the helper do the work
						access.handleAccess(req, res, contentHostingRef, copyrightAcceptedRefs);
					}
					finally
					{
						// clear the security advisor
						popAdvisor();
					}
				}
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Reference getReference(String refString)
	{
		Reference ref = this.entityManager.newReference(refString);
		return ref;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> harvestAttachmentsReferenced(String data, boolean normalize)
	{
		// return an insertion-ordered set
		Set<String> rv = new LinkedHashSet<String>();
		if (data == null) return rv;

		// harvest the main data
		rv.addAll(harvestAttachmentsReferenced(data, normalize, null));

		// process a set of references - initially the main data's references
		Set<String> process = new LinkedHashSet<String>();
		process.addAll(rv);

		while (!process.isEmpty())
		{
			Set<String> secondary = new LinkedHashSet<String>();
			for (String ref : process)
			{
				// check for any html
				String type = getReferencedDocumentType(ref);
				if ("text/html".equals(type))
				{
					// read the referenced html
					String secondaryData = readReferencedDocument(ref);

					// harvest it
					secondary.addAll(harvestAttachmentsReferenced(secondaryData, normalize, ref));
				}
			}

			// ignore any secondary we already have
			secondary.removeAll(rv);

			// collect the secondary
			rv.addAll(secondary);

			// process the secondary
			process.clear();
			process.addAll(secondary);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> harvestEmbedded(String reference, boolean normalize)
	{
		// return an insertion-ordered set
		Set<String> rv = new LinkedHashSet<String>();
		if (reference == null) return rv;

		// deal with %20, &amp;, and other encoded URL stuff
		if (normalize)
		{
			reference = decodeUrl(reference);
		}

		// start with this reference
		rv.add(reference);

		// process a set of references - initially the passed in reference
		Set<String> process = new LinkedHashSet<String>();
		process.addAll(rv);

		while (!process.isEmpty())
		{
			Set<String> secondary = new LinkedHashSet<String>();
			for (String ref : process)
			{
				// check for any html
				String type = getReferencedDocumentType(ref);
				if ("text/html".equals(type))
				{
					// read the referenced html
					String secondaryData = readReferencedDocument(ref);

					// harvest it
					secondary.addAll(harvestAttachmentsReferenced(secondaryData, normalize, ref));
				}
			}

			// ignore any secondary we already have
			secondary.removeAll(rv);

			// collect the secondary
			rv.addAll(secondary);

			// process the secondary
			process.clear();
			process.addAll(secondary);
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Translation> importResources(String application, String context, String prefix, NameConflictResolution onConflict,
			Set<String> resources, boolean makeThumb, String altRef)
	{
		// get our thread-local list of translations made in this thread, for this application/prefix combination
		String threadKey = THREAD_TRANSLATIONS_KEY + application + "." + prefix;
		List<Translation> threadTranslations = (List<Translation>) threadLocalManager.get(threadKey);
		if (threadTranslations == null)
		{
			threadTranslations = new ArrayList<Translation>();
			threadLocalManager.set(threadKey, threadTranslations);
		}

		// get our thread-local list of bodies translated in this thread
		threadKey = THREAD_TRANSLATIONS_BODY_KEY + application + "." + prefix;
		List<String> threadBodyTranslations = (List<String>) threadLocalManager.get(threadKey);
		if (threadBodyTranslations == null)
		{
			threadBodyTranslations = new ArrayList<String>();
			threadLocalManager.set(threadKey, threadBodyTranslations);
		}

		// collect any that may need html body translation in a second pass
		List<Reference> toTranslate = new ArrayList<Reference>();

		// collect translations
		List<Translation> rv = new ArrayList<Translation>();

		for (String refString : resources)
		{
			// if we have done this already in the thread, just skip it
			boolean skip = false;
			for (Translation imported : threadTranslations)
			{
				if (refString.equals(imported.getFrom()))
				{
					skip = true;

					// we need the translation as part of the return set
					rv.add(imported);

					break;
				}
			}
			if (skip) continue;

			Reference ref = this.entityManager.newReference(refString);

			// move the referenced resource into our docs
			Reference imported = addAttachment(application, context, prefix, onConflict, ref, makeThumb, altRef);
			if (imported != null)
			{
				// make the translation
				TranslationImpl t = new TranslationImpl(ref.getReference(), imported.getReference());
				rv.add(t);
				threadTranslations.add(t);

				// do we need a second-pass translation?
				String importedRef = imported.getReference();
				String type = getReferencedDocumentType(importedRef);
				if ("text/html".equals(type))
				{
					// check if we have done this already in the thread (Reference.equals() is not to be trusted -ggolden)
					boolean found = false;
					for (String bodyTranslatedReference : threadBodyTranslations)
					{
						if (bodyTranslatedReference.equals(importedRef))
						{
							found = true;
							break;
						}
					}

					if (!found)
					{
						toTranslate.add(imported);
						threadBodyTranslations.add(importedRef);
					}
				}
			}
			else
			{
				M_log.warn("importResources: failed to import resource: " + ref.getReference());
			}
		}

		// now that we have all the translations, update any resources that have html bodies
		for (Reference ref : toTranslate)
		{
			// translate using the full set we have so far for the thread
			translateHtmlBody(ref, threadTranslations, context);
		}

		return rv;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// register as an entity producer
			entityManager.registerEntityProducer(this, REFERENCE_ROOT);

			// messages
			if (this.bundle != null) this.messages = new ResourceLoader(this.bundle);

			M_log.info("init(): thumbs: " + this.makeThumbs);
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
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{
			// we will get null, sampleAccess, content, private, sampleAccess, <context>, test.txt
			// we will store the context, and the ContentHosting reference in our id field.
			String id = null;
			String context = null;
			String subType = null;
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);
			String container = null;

			// support for the download access
			if ((parts.length == 6) && (DOWNLOAD.equals(parts[2])))
			{
				context = parts[4];

				// set id to the requested download
				id = parts[5];

				// set the sub-type to the type of download
				subType = parts[3];

				// set the container to "download"
				container = parts[2];
			}

			// support for private docs
			else if (parts.length > 5)
			{
				context = parts[5];
				id = "/" + StringUtil.unsplit(parts, 2, parts.length - 2, "/");
			}

			ref.set(APPLICATION_ID_ROOT + MNEME_APPLICATION, subType, id, container, context);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String processMnemeUrls(String ref)
	{
		if (ref == null) return null;
		String translated = decodeUrl(ref);
		if (translated == null) return null;
		// URL encode translated
		String escaped = EscapeRefUrl.escapeUrl(translated);
		if (escaped == null) return null;
		return "/access/mneme" + escaped;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAttachment(Reference ref)
	{
		pushAdvisor();

		try
		{
			String id = entityManager.newReference(ref.getId()).getId();

			// check if this has a unique containing collection
			ContentResource resource = this.contentHostingService.getResource(id);
			ContentCollection collection = resource.getContainingCollection();

			// remove the resource
			this.contentHostingService.removeResource(id);

			// if the collection was made just to hold the attachment, remove it as well
			if (collection.getProperties().getProperty(PROP_UNIQUE_HOLDER) != null)
			{
				this.contentHostingService.removeCollection(collection.getId());
			}
		}
		catch (PermissionException e)
		{
			M_log.warn("removeAttachment: " + e.toString());
		}
		catch (ServerOverloadException e)
		{
			M_log.warn("removeAttachment: " + e.toString());
		}
		catch (InUseException e)
		{
			M_log.warn("removeAttachment: " + e.toString());

		}
		catch (IdUnusedException e)
		{
			M_log.warn("removeAttachment: " + e.toString());
		}
		catch (TypeException e)
		{
			M_log.warn("removeAttachment: " + e.toString());
		}
		finally
		{
			popAdvisor();
		}
	}

	/**
	 * Set the AssessmentService.
	 *
	 * @param service
	 *        the AssessmentService.
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
	 * Dependency: ContentHostingService.
	 *
	 * @param service
	 *        The ContentHostingService.
	 */
	public void setContentHostingService(ContentHostingService service)
	{
		contentHostingService = service;
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
	 * Set the IdManager
	 *
	 * @param IdManager
	 *        The IdManager
	 */
	public void setIdManager(IdManager idManager)
	{
		this.idManager = idManager;
	}

	/**
	 * Set the make thumbs setting
	 *
	 * @param value
	 *        the string of the boolean for the make thumbs setting.
	 */
	public void setMakeThumbs(String value)
	{
		this.makeThumbs = Boolean.valueOf(value);
	}

	/**
	 * Dependency: SecurityService.
	 *
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Dependency: SecurityService.
	 *
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityServiceSakai(org.sakaiproject.authz.api.SecurityService service)
	{
		this.securityServiceSakai = service;
	}

	/**
	 * Dependency: ServerConfigurationService.
	 *
	 * @param service
	 *        The ServerConfigurationService.
	 */
	public void setServerConfigurationService(ServerConfigurationService service)
	{
		this.serverConfigurationService = service;
	}

	/**
	 * Dependency: SessionManager.
	 *
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		this.sessionManager = service;
	}

	/**
	 * Dependency: SiteService.
	 *
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService service)
	{
		this.siteService = service;
	}

	/**
	 * Dependency: SubmissionService.
	 *
	 * @param service
	 *        The SubmissionService.
	 */
	public void setSubmissionService(SubmissionService service)
	{
		this.submissionService = service;
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
	 * Dependency: UserDirectoryService.
	 *
	 * @param service
	 *        The UserDirectoryService.
	 */
	public void setUserDirectoryService(UserDirectoryService service)
	{
		this.userDirectoryService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public String translateEmbeddedReferences(String data, List<Translation> translations)
	{
		return translateEmbeddedReferences(data, translations, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * Gets the match or choice of a match question
	 * @param choiceList A Match Question pair
	 * @param key Key to match
	 * @param isMatch True for match, false for choice
	 * @return Match or Choice
	 */
	private String fetchName(List<MatchQuestionImpl.MatchQuestionPair> choiceList, String key, boolean isMatch)
	{
		if (choiceList == null || choiceList.size() == 0) return null;
		if (isMatch)
		{
			for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
			{
				MatchQuestionImpl.MatchQuestionPair mqObj = (MatchQuestionImpl.MatchQuestionPair) chIt.next();
				if (mqObj.getId().equals(key)) return mqObj.getMatch();
			}
		}
		if (!isMatch)
		{
			for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
			{
				MatchQuestionImpl.MatchQuestionPair mqObj = (MatchQuestionImpl.MatchQuestionPair) chIt.next();
				if (mqObj.getChoiceId().equals(key)) return mqObj.getChoice();
			}
		}
		return null;
	}

	/**
	 * Creates a comma delimited string of answers
	 * @param answers Array of answers
	 * @return Comma delimited string of answers
	 */
	private String getCommaAnswers(String[] answers)
	{
		if (answers.length == 0) return null;
		StringBuffer commaAnswers = new StringBuffer();
		for (int i = 0; i < answers.length; i++)
		{
			if (answers[i] != null && answers[i].trim().length() > 0)
			{
				commaAnswers.append(answers[i].trim());
				commaAnswers.append(", ");
			}
		}
		if (commaAnswers.length() >= 3) return commaAnswers.substring(0, commaAnswers.length() - 2);
		return null;
	}

	/**
	 * Returns string with html tags removed
	 * @param htmlStr String to operate on
	 * @return String with html tags removed
	 */
	private String stripHtml(String htmlStr)
	{
		if (htmlStr == null || htmlStr.trim().length() == 0) return "";
		return htmlStr.replaceAll("\\<.*?\\>", "").trim();
	}

	/**
	 * Go through the add process, trying the various requested conflict resolution steps.
	 *
	 * @param idName
	 *        The CHS id (name part) to use.
	 * @param name
	 *        The display name.
	 * @param application
	 * @param context
	 * @param prefix
	 * @param onConflict
	 * @param type
	 * @param body
	 * @param size
	 * @param makeThumb
	 * @param altRef
	 * @return
	 */
	protected Reference addAttachment(String idName, String name, String application, String context, String prefix,
			NameConflictResolution onConflict, String type, byte[] body, long size, boolean makeThumb, String altRef)
	{
		String id = contentHostingId(idName, application, context, prefix, (onConflict == NameConflictResolution.alwaysUseFolder), altRef);
		Reference rv = doAdd(id, name, type, body, size, false, (onConflict == NameConflictResolution.rename), altRef);

		// if this failed and we need to fall back to using a folder, try again
		if ((rv == null) && (onConflict == NameConflictResolution.useFolder))
		{
			id = contentHostingId(idName, application, context, prefix, true, altRef);
			rv = doAdd(id, name, type, body, size, false, false, altRef);
		}

		// if we have not added one, and we are to use the one we have
		if (rv == null)
		{
			if (onConflict == NameConflictResolution.keepExisting)
			{
				try
				{
					// get the existing
					ContentResource existing = this.contentHostingService.getResource(id);

					// use the alternate reference
					String ref = existing.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
					rv = entityManager.newReference(ref);
				}
				catch (PermissionException e)
				{
					M_log.warn("addAttachment: " + e);
				}
				catch (IdUnusedException e)
				{
					M_log.warn("addAttachment: " + e);
				}
				catch (TypeException e)
				{
					M_log.warn("addAttachment: " + e);
				}
			}
		}
		return rv;
	}

	/**
	 * If the ref is relative, reconstruct the full ref based on where it was embedded.
	 *
	 * @param ref
	 *        The reference string.
	 * @param parentRef
	 *        The reference to the resource in which ref is embedded.
	 * @return The ref string unmodified if not relative, or the full reference to the resource if relative.
	 */
	protected String adjustRelativeReference(String ref, String parentRef)
	{
		// if no transport, not a "mailto:", and it does not start with "/", it is a relative reference
		if ((parentRef != null) && (ref != null) && (parentRef.length() > 0) && (ref.length() > 0) && (ref.indexOf("://") == -1)
				&& (!(ref.startsWith("/")) && (!(ref.toLowerCase().startsWith("mailto:")))))
		{
			// replace the part after the last "/" in the parentRef with the ref
			int pos = parentRef.lastIndexOf('/');
			if (pos != -1)
			{
				String fullRef = "/access" + parentRef.substring(0, pos + 1) + ref;
				return fullRef;
			}
		}

		return ref;
	}

	/**
	 * Assure that a collection with this name exists in the container collection: create it if it is missing.
	 *
	 * @param container
	 *        The full path of the container collection.
	 * @param name
	 *        The collection name to check and create (no trailing slash needed).
	 * @param uniqueHolder
	 *        true if the folder is being created solely to hold the attachment uniquely.
	 * @param altRef
	 *        the alternate reference for the resource.
	 */
	protected void assureCollection(String container, String name, boolean uniqueHolder, String altRef)
	{
		try
		{
			contentHostingService.getCollection(container + name + "/");
		}
		catch (IdUnusedException e)
		{
			try
			{
				ContentCollectionEdit edit = contentHostingService.addCollection(container + name + "/");
				ResourcePropertiesEdit props = edit.getPropertiesEdit();

				// set the alternate reference root so we get all requests
				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, altRef);

				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

				// mark it if it is a unique holder
				if (uniqueHolder)
				{
					props.addProperty(PROP_UNIQUE_HOLDER, PROP_UNIQUE_HOLDER);
				}

				contentHostingService.commitCollection(edit);
			}
			catch (IdUsedException e2)
			{
				// M_log.warn("init: creating our root collection: " + e2.toString());
			}
			catch (IdInvalidException e2)
			{
				M_log.warn("assureCollection: " + e2.toString());
			}
			catch (PermissionException e2)
			{
				M_log.warn("assureCollection: " + e2.toString());
			}
			catch (InconsistentException e2)
			{
				M_log.warn("assureCollection: " + e2.toString());
			}
		}
		catch (TypeException e)
		{
			M_log.warn("assureCollection(2): " + e.toString());
		}
		catch (PermissionException e)
		{
			M_log.warn("assureCollection(2): " + e.toString());
		}
	}

	/**
	 * Check security for this entity.
	 *
	 * @param ref
	 *        The Reference to the entity.
	 * @return true if allowed, false if not.
	 */
	protected boolean checkSecurity(Reference ref)
	{
		String userId = this.sessionManager.getCurrentSessionUserId();
		String context = ref.getContext();

		// for download
		if (DOWNLOAD.equals(ref.getContainer()))
		{
			// TODO: if students can download anything, check the sub-type
			// if (DOWNLOAD_ALL_SUBMISSIONS_QUESTION.equals(ref.getSubType()))
			
			// manage or grade permission for the context
			if (this.securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context)) return true;
			if (this.securityService.checkSecurity(userId, MnemeService.GRADE_PERMISSION, context)) return true;
			if (ASMT_CERT.equals(ref.getSubType()) && this.securityService.checkSecurity(userId, MnemeService.SUBMIT_PERMISSION, context)) return true;
			return false;
		}

		// for private docs
		else
		{
			String[] parts = StringUtil.split(ref.getId(), "/");
			if ((parts.length == 9) && (SUBMISSIONS_AREA.equals(parts[5])))
			{
				// manage or grade permission for the context is still a winner
				if (this.securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context)) return true;
				if (this.securityService.checkSecurity(userId, MnemeService.GRADE_PERMISSION, context)) return true;

				// otherwise, user must be submission user and have submit permission
				if (this.securityService.checkSecurity(userId, MnemeService.SUBMIT_PERMISSION, context))
				{
					Submission submission = this.submissionService.getSubmission(parts[6]);
					if (submission != null)
					{
						if (submission.getUserId().equals(userId))
						{
							return true;
						}
					}
				}

				return false;
			}

			// this is how we used to do it, without a submission id in the ref
			if (this.securityService.checkSecurity(userId, MnemeService.MANAGE_PERMISSION, context)) return true;
			if (this.securityService.checkSecurity(userId, MnemeService.SUBMIT_PERMISSION, context)) return true;
			return false;
		}
	}

	/**
	 * Compute the content hosting id.
	 *
	 * @param name
	 *        The file name.
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @param uniqueHolder
	 *        If true, a uniquely named folder is created to hold the resource.
	 * @param altRef
	 *        the alternate reference for the resource.
	 * @return
	 */
	protected String contentHostingId(String name, String application, String context, String prefix, boolean uniqueHolder, String altRef)
	{
		// form the content hosting path, and make sure all the folders exist
		String contentPath = "/private/";
		assureCollection(contentPath, application, false, altRef);
		contentPath += application + "/";
		assureCollection(contentPath, context, false, altRef);
		contentPath += context + "/";
		if ((prefix != null) && (prefix.length() > 0))
		{
			// allow multi-part prefix
			if (prefix.indexOf('/') != -1)
			{
				String[] prefixes = StringUtil.split(prefix, "/");
				for (String pre : prefixes)
				{
					assureCollection(contentPath, pre, false, altRef);
					contentPath += pre + "/";
				}
			}
			else
			{
				assureCollection(contentPath, prefix, false, altRef);
				contentPath += prefix + "/";
			}
		}
		if (uniqueHolder)
		{
			String uuid = this.idManager.createUuid();
			assureCollection(contentPath, uuid, true, altRef);
			contentPath += uuid + "/";
		}

		contentPath += name;

		return contentPath;
	}

	/**
	 * Decode the URL as a browser would.
	 *
	 * @param url
	 *        The URL.
	 * @return the decoded URL.
	 */
	protected String decodeUrl(String url)
	{
		try
		{
			// these the browser will convert when it's making the URL to send
			String processed = url.replaceAll("&amp;", "&");
			processed = processed.replaceAll("&lt;", "<");
			processed = processed.replaceAll("&gt;", ">");
			processed = processed.replaceAll("&quot;", "\"");

			// if a browser sees a plus, it sends a plus (URLDecoder will change it to a space)
			processed = processed.replaceAll("\\+", "%2b");

			// and the rest of the works, including %20 and + handling
			String decoded = URLDecoder.decode(processed, "UTF-8");

			return decoded;
		}
		catch (UnsupportedEncodingException e)
		{
			M_log.warn("decodeUrl: " + e);
		}
		catch (IllegalArgumentException e)
		{
			M_log.warn("decodeUrl: " + e);
		}

		return url;
	}

	/**
	 * Perform the add.
	 *
	 * @param id
	 *        The content hosting id.
	 * @param name
	 *        The display name.
	 * @param type
	 *        The mime type.
	 * @param body
	 *        The body bytes.
	 * @param size
	 *        The body size.
	 * @param thumb
	 *        If true, mark this as a thumb.
	 * @param renameToFit
	 *        If true, let CHS rename the file on name conflict.
	 * @param altRef
	 *        the alternate reference for the resource.
	 * @return The Reference to the added attachment.
	 */
	protected Reference doAdd(String id, String name, String type, byte[] body, long size, boolean thumb, boolean renameToFit, String altRef)
	{
		try
		{
			if (!renameToFit)
			{
				ContentResourceEdit edit = this.contentHostingService.addResource(id);
				edit.setContent(body);
				edit.setContentType(type);
				ResourcePropertiesEdit props = edit.getPropertiesEdit();

				// set the alternate reference root so we get all requests TODO:
				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, altRef);

				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);

				if (thumb)
				{
					props.addProperty(PROP_THUMB, PROP_THUMB);
				}

				this.contentHostingService.commitResource(edit);

				String ref = edit.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
				Reference reference = entityManager.newReference(ref);

				return reference;
			}
			else
			{
				String[] parts = StringUtil.split(id, "/");
				String destinationCollection = StringUtil.unsplit(parts, 0, parts.length - 1, "/") + "/";
				String destinationName = parts[parts.length - 1];

				// set the alternate reference root so we get all requests
				ResourcePropertiesEdit props = this.contentHostingService.newResourceProperties();
				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, altRef);

				// mark if a thumb
				if (thumb)
				{
					props.addProperty(PROP_THUMB, PROP_THUMB);
				}

				ContentResource uploadedResource = this.contentHostingService.addResource(destinationName, destinationCollection, 255, type, body,
						props, 0);

				// need to set the display name based on the id that it got
				ContentResourceEdit edit = this.contentHostingService.editResource(uploadedResource.getId());
				props = edit.getPropertiesEdit();
				parts = edit.getId().split("/");
				destinationName = parts[parts.length - 1];
				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, destinationName);
				this.contentHostingService.commitResource(edit);

				String ref = uploadedResource.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
				Reference reference = entityManager.newReference(ref);

				return reference;
			}
		}
		catch (PermissionException e2)
		{
			M_log.warn("addAttachment: creating our content: " + e2.toString());
		}
		catch (IdUsedException e2)
		{
		}
		catch (IdInvalidException e2)
		{
			M_log.warn("addAttachment: creating our content: " + e2.toString());
		}
		catch (InconsistentException e2)
		{
			M_log.warn("addAttachment: creating our content: " + e2.toString());
		}
		catch (ServerOverloadException e2)
		{
			M_log.warn("addAttachment: creating our content: " + e2.toString());
		}
		catch (OverQuotaException e2)
		{
			M_log.warn("addAttachment: creating our content: " + e2.toString());
		}
		catch (IdUniquenessException e)
		{
		}
		catch (IdLengthException e)
		{
		}
		catch (IdUnusedException e)
		{
			M_log.warn("addAttachment: creating our content: " + e.toString());
		}
		catch (TypeException e)
		{
			M_log.warn("addAttachment: creating our content: " + e.toString());
		}
		catch (InUseException e)
		{
			M_log.warn("addAttachment: creating our content: " + e.toString());
		}
		finally
		{
			// try
			// {
			// // TODO: if using input stream
			// if (body != null) body.close();
			// }
			// catch (IOException e)
			// {
			// }
		}

		return null;
	}

	/**
	 * If the document matches typePrefix and thumbnail requirement, add it to the attachments.
	 *
	 * @param attachments
	 *        The list of attachments.
	 * @param doc
	 *        The resource.
	 * @param typePrefix
	 *        if null, match any type, else match only the types that match this prefix.
	 * @param thumbs
	 *        if true, include only thumbs, else skip them.
	 */
	protected void filterTypes(List<Attachment> attachments, ContentResource doc, String typePrefix, boolean thumbs)
	{
		// only matching types
		if ((typePrefix == null) || (doc.getContentType().toLowerCase().startsWith(typePrefix.toLowerCase())))
		{
			// thumbs? not thumbs?
			if ((doc.getProperties().getProperty(this.PROP_THUMB) != null) == thumbs)
			{
				String ref = doc.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
				String url = doc.getUrl(ContentHostingService.PROP_ALTERNATE_REFERENCE);

				// strip the leading transport, server DNS, port, etc, to make a root-relative URL
				int pos = url.indexOf("/access");
				if (pos != -1) url = url.substring(pos);

				String escapedUrl = EscapeRefUrl.escapeRefUrl(ref, url);

				Attachment a = new AttachmentImpl(doc.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME), ref, escapedUrl,
						doc.getContentType());
				attachments.add(a);
			}
		}
	}

	/**
	 * Find all the attachments in the docs area of the application for this context. Select only those matching type.
	 *
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @param typePrefix
	 *        if null, all but the thumbs. Otherwise only those matching the prefix in mime type.
	 * @param thumbs
	 *        if true, return only thumbs, else return no thumbs.
	 * @return A List of Attachments to the attachments.
	 */
	protected List<Attachment> findTypes(String application, String context, String prefix, String typePrefix, boolean thumbs)
	{
		// permission
		pushAdvisor();

		List<Attachment> rv = new ArrayList<Attachment>();

		try
		{
			// form the content hosting path to the docs collection
			String docsCollection = "/private/";
			docsCollection += application + "/";
			docsCollection += context + "/";
			if ((prefix != null) && (prefix.length() > 0))
			{
				docsCollection += prefix + "/";
			}

			// get the members of this collection
			ContentCollection docs = contentHostingService.getCollection(docsCollection);
			List<Object> members = docs.getMemberResources();
			for (Object m : members)
			{
				if (m instanceof ContentCollection)
				{
					// get the member within
					ContentCollection holder = (ContentCollection) m;
					List<Object> innerMembers = holder.getMemberResources();
					for (Object mm : innerMembers)
					{
						if (mm instanceof ContentResource)
						{
							filterTypes(rv, (ContentResource) mm, typePrefix, thumbs);
						}
					}
				}

				else if (m instanceof ContentResource)
				{
					filterTypes(rv, (ContentResource) m, typePrefix, thumbs);
				}
			}
		}
		catch (IdUnusedException e)
		{
		}
		catch (TypeException e)
		{
		}
		catch (PermissionException e)
		{
		}
		finally
		{
			popAdvisor();
		}

		return rv;
	}

	/**
	 * Get a CHS document's mime type.
	 *
	 * @param ref
	 *        The document reference.
	 * @return The document's mime type.
	 */
	protected String getReferencedDocumentType(String ref)
	{
		// bypass security when reading the resource to copy
		pushAdvisor();

		try
		{
			// get an id from the reference string
			Reference reference = this.entityManager.newReference(ref);
			String id = reference.getId();
			if (id.startsWith("/content/"))
			{
				id = id.substring("/content".length());
			}

			try
			{
				// read the resource
				ContentResource r = this.contentHostingService.getResource(id);
				String type = r.getContentType();

				return type;
			}
			catch (IdUnusedException e)
			{
			}
			catch (TypeException e)
			{
				M_log.warn("getReferencedDocumentType: " + e.toString());
			}
			catch (PermissionException e)
			{
				M_log.warn("getReferencedDocumentType: " + e.toString());
			}
		}
		finally
		{
			popAdvisor();
		}

		return "";
	}

	/**
	 * Process the access request for a download (not CHS private docs).
	 *
	 * @param req
	 * @param res
	 * @param ref
	 * @param copyrightAcceptedRefs
	 * @throws PermissionException
	 * @throws IdUnusedException
	 * @throws ServerOverloadException
	 * @throws CopyrightException
	 */
	protected void handleAccessDownload(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
			throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
	{
		if (DOWNLOAD_ALL_SUBMISSIONS_QUESTION.equals(ref.getSubType()))
		{
			// the file name has assessment_question.zip
			String[] outerParts = StringUtil.split(ref.getId(), ".");
			String[] parts = StringUtil.split(outerParts[0], "_");

			// assessment
			Assessment assessment = this.assessmentService.getAssessment(parts[0]);
			if (assessment == null)
			{
				M_log.warn("handleAccessDownload: invalid assessment id: " + parts[0]);
				return;
			}

			// check that this is not a formal course evaluation
			if (assessment.getFormalCourseEval())
			{
				throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "access", ref.getReference());
			}

			// question
			Question question = assessment.getParts().getQuestion(parts[1]);
			if (question == null)
			{
				M_log.warn("handleAccessDownload: invalid question id: " + parts[1]);
				return;
			}

			String contentType = (String) ref.getProperties().get(ResourceProperties.PROP_CONTENT_TYPE);
			String disposition = "attachment; filename=\"" + (String) ref.getProperties().get("DAV:displayname") + "\"";

			OutputStream out = null;
			ZipOutputStream zip = null;
			try
			{
				res.setContentType(contentType);
				res.addHeader("Content-Disposition", disposition);

				out = res.getOutputStream();
				zip = new ZipOutputStream(out);

				this.submissionService.zipSubmissionsQuestion(zip, assessment, question);

				zip.flush();
			}
			catch (Throwable e)
			{
				M_log.warn("zipping: ", e);
			}
			finally
			{
				if (zip != null)
				{
					try
					{
						zip.close();
					}
					catch (Throwable e)
					{
						M_log.warn("closing zip: " + e.toString());
					}
				}
			}

			// track event
			this.eventTrackingService.post(this.eventTrackingService.newEvent(MnemeService.DOWNLOAD_SQ, ref.getReference(), false));
		}
		else if (EXPORT_SUMMARY.equals(ref.getSubType()))
		{
			// the file name has assessment_question.zip
			String[] outerParts = StringUtil.split(ref.getId(), ".");
			String[] parts = StringUtil.split(outerParts[0], "_");

			// assessment
			Assessment assessment = this.assessmentService.getAssessment(parts[0]);

			if (assessment == null)
			{
				M_log.warn("handleAccessDownload: invalid assessment id: " + parts[0]);
				return;
			}

			// check that this is not a formal course evaluation
			if (assessment.getFormalCourseEval())
			{
				throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "access", ref.getReference());
			}


			OutputStream out = null;
			try
			{
				HSSFWorkbook wb = new HSSFWorkbook();

				if (createResponsesSheet(wb, assessment) != null)
				{
					String contentType = (String) ref.getProperties().get(ResourceProperties.PROP_CONTENT_TYPE);
					String disposition = "attachment; filename=\"" + (String) ref.getProperties().get("DAV:displayname") + "\"";
					res.setContentType(contentType);
					res.addHeader("Content-Disposition", disposition);

					out = res.getOutputStream();
					wb.write(out);
					out.flush();
				}
				else
				{
					res.setContentType("text/plain; charset=UTF-8");
					out = res.getOutputStream();
					String msg = this.messages.getFormattedMessage("nosub_msg", null);
					out.write(msg.getBytes());
					out.flush();
				}
			}
			catch (Throwable e)
			{
				M_log.warn("summary spreadsheet: ", e);
			}
			finally
			{

			}

			// track event
			//this.eventTrackingService.post(this.eventTrackingService.newEvent(MnemeService.DOWNLOAD_SQ, ref.getReference(), false));
		}
		else if (ITEM_ANALYSIS.equals(ref.getSubType()))
		{
			// the file name has assessment_question.zip
			String[] outerParts = StringUtil.split(ref.getId(), ".");
			String[] parts = StringUtil.split(outerParts[0], "_");

			// assessment
			Assessment assessment = this.assessmentService.getAssessment(parts[0]);

			if (assessment == null)
			{
				M_log.warn("handleAccessDownload: invalid assessment id: " + parts[0]);
				return;
			}

			// check that this is not a formal course evaluation
			if (assessment.getFormalCourseEval())
			{
				throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "access", ref.getReference());
			}


			OutputStream out = null;
			try
			{
				HSSFWorkbook wb = new HSSFWorkbook();

				if (createItemAnalysisSheet(wb, assessment) != null)
				{
					String contentType = (String) ref.getProperties().get(ResourceProperties.PROP_CONTENT_TYPE);
					String disposition = "attachment; filename=\"" + (String) ref.getProperties().get("DAV:displayname") + "\"";
					res.setContentType(contentType);
					res.addHeader("Content-Disposition", disposition);

					out = res.getOutputStream();
					wb.write(out);
					out.flush();
				}
				else
				{
					res.setContentType("text/plain; charset=UTF-8");
					out = res.getOutputStream();
					String msg = this.messages.getFormattedMessage("nosub_msg", null);
					out.write(msg.getBytes());
					out.flush();
				}
			}
			catch (Throwable e)
			{
				M_log.warn("summary spreadsheet: ", e);
			}
			finally
			{

			}

			// track event
			//this.eventTrackingService.post(this.eventTrackingService.newEvent(MnemeService.DOWNLOAD_SQ, ref.getReference(), false));
		}
		else if (ASMT_STATS.equals(ref.getSubType()))
		{
			// the file name has assessment_question.zip
			String[] outerParts = StringUtil.split(ref.getId(), ".");
			String[] parts = StringUtil.split(outerParts[0], "_");

			// assessment
			Assessment assessment = this.assessmentService.getAssessment(parts[0]);

			if (assessment == null)
			{
				M_log.warn("handleAccessDownload: invalid assessment id: " + parts[0]);
				return;
			}

			// check that this is not a formal course evaluation
			if (assessment.getFormalCourseEval())
			{
				throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "access", ref.getReference());
			}

			List<Submission> submissions = this.submissionService.findAssessmentSubmissions(assessment,
					SubmissionService.FindAssessmentSubmissionsSort.userName_a, Boolean.FALSE, null, null, null, null);

			OutputStream out = null;
			try
			{
				HSSFWorkbook wb = new HSSFWorkbook();

				if (createAsmtStatsSheet(wb, submissions) != null)
				{
					String contentType = (String) ref.getProperties().get(ResourceProperties.PROP_CONTENT_TYPE);
					String disposition = "attachment; filename=\"" + (String) ref.getProperties().get("DAV:displayname") + "\"";
					res.setContentType(contentType);
					res.addHeader("Content-Disposition", disposition);

					out = res.getOutputStream();
					wb.write(out);
					out.flush();
				}
				else
				{
					res.setContentType("text/plain; charset=UTF-8");
					out = res.getOutputStream();
					String msg = this.messages.getFormattedMessage("nosub_msg", null);
					out.write(msg.getBytes());
					out.flush();
				}
			}
			catch (Throwable e)
			{
				M_log.warn("summary spreadsheet: ", e);
			}
			finally
			{

			}

			// track event
			//this.eventTrackingService.post(this.eventTrackingService.newEvent(MnemeService.DOWNLOAD_SQ, ref.getReference(), false));
		}
		else if (ASMT_CERT.equals(ref.getSubType()))
		{
			StringBuilder contents = new StringBuilder();
			BufferedReader input = null;
			String message = null;
			InputStream inputStream = null;

			try
			{
				inputStream = AttachmentServiceImpl.class.getClassLoader().getResourceAsStream("cert.html");

				input = new BufferedReader(new InputStreamReader(inputStream));

				String line = null;
				String siteId = ref.getContext();

				while ((line = input.readLine()) != null)
				{
					if (line.contains(".png") || line.contains(".jpg") || line.contains(".gif") || line.contains(".bmp"))
					{
						line = adjustImagePath(line, siteId);
					}
					contents.append(line);
				}

				message = contents.toString();
				String subId = ref.getId();

				if (subId != null)
				{
					Submission submission = this.submissionService.getSubmission(subId);
					if (submission != null)
					{
						String userId = submission.getUserId();
						if (userId != null)
						{
							try
							{
								User user = this.userDirectoryService.getUser(userId);
								message = message.replace("student.name", user.getFirstName() + " " + user.getLastName());
							}
							catch (UserNotDefinedException e)
							{
								M_log.warn("handleAccessDownload: " + e.toString());
							}
						}
						message = message.replace("student.score", String.valueOf(submission.getTotalScore().floatValue()));
						Assessment assmt = submission.getAssessment();
						message = message.replace("test.points", String.valueOf(assmt.getParts().getTotalPoints().floatValue()));
						message = message.replace("test.title", assmt.getTitle());
						String siteTitle = null;
						try
						{
							Site site = this.siteService.getSite(siteId);
							siteTitle = site.getTitle();
						}
						catch (IdUnusedException e)
						{
						}
						message = message.replace("course.title", siteTitle);
						DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
						Date date = new Date();
						message = message.replace("today.date", dateFormat.format(date));
					}
				}
				OutputStream out = null;

				try
				{
					res.setContentType("text/html; charset=UTF-8");
					out = res.getOutputStream();
					out.write(message.getBytes());
					out.flush();

				}
				catch (Throwable e)
				{
					M_log.warn("view certificate: ", e);
				}
				finally
				{

				}
			}
			catch (Exception e)
			{
				M_log.warn("Error in handleAccessDownload", e);
			}
			finally
			{
				if (input != null)
				{
					try
					{
						input.close();
					}
					catch (IOException e)
					{
					}
				}

				if (inputStream != null)
				{
					try
					{
						inputStream.close();
					}
					catch (IOException e)
					{

					}
				}
			}

			// track event
			// this.eventTrackingService.post(this.eventTrackingService.newEvent(MnemeService.DOWNLOAD_SQ, ref.getReference(), false));
		}
		else if (ASMT_EXPORT.equals(ref.getSubType()))
		{
			String[] outerParts = StringUtil.split(ref.getReference(), "/");
			
			ZipOutputStream zip = null;
			try
			{
				String disposition = "attachment; filename=\"" + (String) ref.getProperties().get("DAV:displayname") + "\"";

				OutputStream out = null;

				res.setContentType("application/zip");
				res.addHeader("Content-Disposition", disposition);

				out = res.getOutputStream();
				zip = new ZipOutputStream(out);
				
				String values = outerParts[outerParts.length - 1];
				values = values.replace(".zip", "");
				String[] ids = StringUtil.split(values, "+");

				this.assessmentService.exportAssessments(ref.getContext(), ids, zip);

				zip.flush();
			}
			catch (Throwable e)
			{
				M_log.warn("zipping: ", e);
			}
			finally
			{
				if (zip != null)
				{
					try
					{
						zip.close();
					}
					catch (Throwable e)
					{
						M_log.warn("closing zip: " + e.toString());
					}
				}
			}			
		}
		else
		{
			M_log.warn("handleAccessDownload: unknown request: " + ref.getReference());
		}
	}

	/**
	   * Validate image with regular expression
	   * @param image image for validation
	   * @return true valid image, false invalid image
	   */
	public String adjustImagePath(String imageStr, String siteId)
	{
		Pattern pattern;
		Matcher matcher;

		String IMAGE_PATTERN = "(\"[\\S]+(\\.(?i)(jpg|png|gif|bmp))\")";
		String toolId = null;

		try
		{
			Site site = this.siteService.getSite(siteId);
			ToolConfiguration config = site.getToolForCommonId("sakai.mneme");
			if (config != null) toolId = config.getId();
		}
		catch (IdUnusedException e)
		{
			M_log.warn("get: missing site: " + siteId);
		}

		pattern = Pattern.compile(IMAGE_PATTERN);

		matcher = pattern.matcher(imageStr);
		boolean findRes = matcher.find();
		if (!findRes) return imageStr;
		if (findRes)
		{
			StringBuffer sb = new StringBuffer(imageStr);
			sb.insert(matcher.start() + 1, "/portal/tool/" + toolId + "/icons/");
			return sb.toString();
		}
		return imageStr;
	}


	/**
	 * Collect all the attachment references in the html data:<br />
	 * Anything referenced by a src= or href=. in our content docs, or in a site content area <br />
	 * Ignore anything in a myWorkspace content area or the public content area. <br />
	 *
	 * @param data
	 *        The data string.
	 * @param normalize
	 *        if true, decode the references by URL decoding rules.
	 * @param parentRef
	 *        Reference string to the embedding (parent) resource - used to resolve relative references.
	 * @return The set of attachment references.
	 */
	protected Set<String> harvestAttachmentsReferenced(String data, boolean normalize, String parentRef)
	{
		Set<String> rv = new HashSet<String>();
		if (data == null) return rv;

		// pattern to find any src= or href= text
		// groups: 0: the whole matching text 1: src|href 2: the string in the quotes 3: the terminator character
		Pattern p = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^#\"]*)([#\"])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

		Matcher m = p.matcher(data);
		while (m.find())
		{
			if (m.groupCount() == 3)
			{
				String ref = m.group(2);

				if (ref != null) ref = ref.trim();

				// expand to a full reference if relative
				ref = adjustRelativeReference(ref, parentRef);

				// harvest any content hosting reference
				int index = ref.indexOf("/access/content/");
				if (index != -1)
				{
					// except for any in /user/ or /public/
					if (ref.indexOf("/access/content/user/") != -1)
					{
						index = -1;
					}
					else if (ref.indexOf("/access/content/public/") != -1)
					{
						index = -1;
					}
				}

				// harvest also the mneme docs references
				if (index == -1) index = ref.indexOf("/access/mneme/content/");

				// TODO: further filter to docs root and context (optional)
				if (index != -1)
				{
					// save just the reference part (i.e. after the /access);
					String refString = ref.substring(index + 7);

					// deal with %20 and other encoded URL stuff
					if (normalize)
					{
						refString = decodeUrl(refString);
					}

					rv.add(refString);
				}
			}
		}

		return rv;
	}

	/**
	 * Trim the name to only the characters after the last slash of either kind.<br />
	 * Remove junk from uploaded file names.
	 *
	 * @param name
	 *        The string to trim.
	 * @return The trimmed string.
	 */
	protected String massageName(String name)
	{
		// if there are any slashes, forward or back, take from the last one found to the right as the name
		int pos = -1;
		for (int i = name.length() - 1; i >= 0; i--)
		{
			char c = name.charAt(i);
			if ((c == '/') || (c == '\\'))
			{
				pos = i + 1;
				break;
			}
		}

		if (pos != -1)
		{
			name = name.substring(pos);
		}

		return name;
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
	 * Read a document from content hosting.
	 *
	 * @param ref
	 *        The document reference.
	 * @return The document content in a String.
	 */
	protected String readReferencedDocument(String ref)
	{
		// bypass security when reading the resource to copy
		pushAdvisor();

		try
		{
			// get an id from the reference string
			Reference reference = this.entityManager.newReference(ref);
			String id = reference.getId();
			if (id.startsWith("/content/"))
			{
				id = id.substring("/content".length());
			}

			try
			{
				// read the resource
				ContentResource r = this.contentHostingService.getResource(id);

				// get the body into a string
				byte[] body = r.getContent();
				if (body == null) return null;

				String bodyString = new String(body, "UTF-8");

				return bodyString;
			}
			catch (IOException e)
			{
				M_log.warn("readReferencedDocument: " + e.toString());
			}
			catch (IdUnusedException e)
			{
			}
			catch (TypeException e)
			{
				M_log.warn("readReferencedDocument: " + e.toString());
			}
			catch (PermissionException e)
			{
				M_log.warn("readReferencedDocument: " + e.toString());
			}
			catch (ServerOverloadException e)
			{
				M_log.warn("readReferencedDocument: " + e.toString());
			}
		}
		finally
		{
			popAdvisor();
		}

		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	protected String translateEmbeddedReferences(String data, Collection<Translation> translations, String parentRef)
	{
		if (data == null) return data;
		if (translations == null) return data;

		// pattern to find any src= or href= text
		// groups: 0: the whole matching text 1: src|href 2: the string in the quotes 3: the terminator character
		Pattern p = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^#\"]*)([#\"])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

		Matcher m = p.matcher(data);
		StringBuffer sb = new StringBuffer();

		// process each "harvested" string (avoiding like strings that are not in src= or href= patterns)
		while (m.find())
		{
			if (m.groupCount() == 3)
			{
				String ref = m.group(2);
				String terminator = m.group(3);

				if (ref != null) ref = ref.trim();

				// expand to a full reference if relative
				ref = adjustRelativeReference(ref, parentRef);

				// harvest any content hosting reference
				int index = ref.indexOf("/access/content/");
				if (index != -1)
				{
					// except for any in /user/ or /public/
					if (ref.indexOf("/access/content/user/") != -1)
					{
						index = -1;
					}
					else if (ref.indexOf("/access/content/public/") != -1)
					{
						index = -1;
					}
				}

				// harvest also the mneme docs references
				if (index == -1) index = ref.indexOf("/access/mneme/content/");

				if (index != -1)
				{
					// save just the reference part (i.e. after the /access);
					String normal = ref.substring(index + 7);

					// deal with %20, &amp;, and other encoded URL stuff
					normal = decodeUrl(normal);

					// translate the normal form
					String translated = normal;
					for (Translation translation : translations)
					{
						translated = translation.translate(translated);
					}

					// URL encode translated
					String escaped = EscapeRefUrl.escapeUrl(translated);

					// if changed, replace
					if (!normal.equals(translated))
					{
						m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "=\"" + ref.substring(0, index + 7) + escaped + terminator));
					}
				}
			}
		}

		m.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Translate the resource's body html with the translations.
	 *
	 * @param ref
	 *        The resource reference.
	 * @param translations
	 *        The complete set of translations.
	 * @param context
	 *        The context.
	 */
	protected void translateHtmlBody(Reference ref, Collection<Translation> translations, String context)
	{
		// ref is the destination ("to" in the translations) resource - we need the "parent ref" from the source ("from" in the translations) resource
		String parentRef = ref.getReference();
		for (Translation translation : translations)
		{
			parentRef = translation.reverseTranslate(parentRef);
		}

		// bypass security when reading the resource to copy
		pushAdvisor();

		try
		{
			// Reference does not know how to make the id from a private docs reference.
			String id = ref.getId();
			if (id.startsWith("/content/"))
			{
				id = id.substring("/content".length());
			}

			// get the resource
			ContentResource resource = this.contentHostingService.getResource(id);
			String type = resource.getContentType();

			// translate if we are html
			if (type.equals("text/html"))
			{
				byte[] body = resource.getContent();
				if (body != null)
				{
					String bodyString = new String(body, "UTF-8");
					String translated = translateEmbeddedReferences(bodyString, translations, parentRef);
					body = translated.getBytes("UTF-8");

					ContentResourceEdit edit = this.contentHostingService.editResource(resource.getId());
					edit.setContent(body);
					this.contentHostingService.commitResource(edit, 0);
				}
			}
		}
		catch (UnsupportedEncodingException e)
		{
			M_log.warn("translateHtmlBody: " + e.toString());
		}
		catch (PermissionException e)
		{
			M_log.warn("translateHtmlBody: " + e.toString());
		}
		catch (IdUnusedException e)
		{
			M_log.warn("translateHtmlBody: " + e.toString());
		}
		catch (TypeException e)
		{
			M_log.warn("translateHtmlBody: " + e.toString());
		}
		catch (ServerOverloadException e)
		{
			M_log.warn("translateHtmlBody: " + e.toString());
		}
		catch (InUseException e)
		{
			M_log.warn("translateHtmlBody: " + e.toString());
		}
		catch (OverQuotaException e)
		{
			M_log.warn("translateHtmlBody: " + e.toString());
		}
		finally
		{
			popAdvisor();
		}
	}

	/**
	 * Computes the number of users who got an answer correct
	 * @param userList List of user ids
	 * @param answers List of answers
	 * @return Number of user who got an answer correct
	 */
	int calculateCorrects(List<String> userList, List<Answer> answers)
	{
		if (userList == null || userList.size() == 0) return 0;
		if (answers == null || answers.size() == 0) return 0;
		int correctCount = 0;
		for (Answer answer : answers)
		{
			if (answer.getTypeSpecificAnswer() != null && answer.getTypeSpecificAnswer().getCompletelyCorrect() != null
					&& answer.getTypeSpecificAnswer().getCompletelyCorrect().booleanValue() && userList.contains(answer.getSubmission().getUserId()))
			{
				correctCount++;
			}
		}
		return correctCount;
	}

	/**
	 * Iterates through the answers for a Fill In question and returns an array
	 * with correct answers marked with asterisks
	 * @param fb FillBlanksAnswerImpl object
	 * @return String array with correct answers marked with asterisks
	 */
	String[] checkCorrectFill(FillBlanksAnswerImpl fb)
	{
		String[] answers = fb.getAnswers();
		String[] checkedFillAnswers = new String[answers.length];
		if (answers.length == 0) return checkedFillAnswers;
		for (int l = 0; l < answers.length; l++)
		{
			if (fb.correctFillAnswer(answers[l],l)) checkedFillAnswers[l] = "*"+answers[l]+"*";
			else checkedFillAnswers[l] = answers[l];
		}
		return checkedFillAnswers;
	}

	/**
	 * Checks if the key and value constitute a correct math
	 * @param choiceList List of match pair objects
	 * @param key Match key
	 * @param value Match choice
	 * @return true if the key matches choice, false otherwise
	 */
	boolean checkCorrectMatch(List choiceList, String key, String value)
	{
		if (value == null) return false;
		for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
		{
			MatchQuestionImpl.MatchQuestionPair mqObj = (MatchQuestionImpl.MatchQuestionPair) chIt.next();
			if (mqObj.getId().equals(key))
			{
				if (value.equals(mqObj.getCorrectChoiceId())) return true;
			}
		}
		return false;
	}

	HSSFWorkbook createAsmtStatsSheet(HSSFWorkbook workbook, List<Submission> submissions)
	{
		if (submissions == null || submissions.size() == 0) return null;

		Map<String, Integer> userRowMap = new HashMap();
		HSSFSheet sheet = workbook.createSheet("Submission responses");

		HSSFRow headerRow = sheet.createRow((short) 0);

		HSSFCellStyle style = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		// Printing header row and question text
		HSSFCell cell0 = headerRow.createCell((short) (0));
		cell0.setCellStyle(style);
		cell0.setCellValue(this.messages.getFormattedMessage("asmt_name", null));
		HSSFCell cell1 = headerRow.createCell((short) (1));
		cell1.setCellStyle(style);
		cell1.setCellValue(this.messages.getFormattedMessage("asmt_uname", null));
		HSSFCell cell2 = headerRow.createCell((short) (2));
		cell2.setCellStyle(style);
		cell2.setCellValue(this.messages.getFormattedMessage("asmt_started", null));
		HSSFCell cell3 = headerRow.createCell((short) (3));
		cell3.setCellStyle(style);
		cell3.setCellValue(this.messages.getFormattedMessage("asmt_finished", null));
		HSSFCell cell4 = headerRow.createCell((short) (4));
		cell4.setCellStyle(style);
		cell4.setCellValue(this.messages.getFormattedMessage("asmt_status", null));
		HSSFCell cell5 = headerRow.createCell((short) (5));
		cell5.setCellStyle(style);
		cell5.setCellValue(this.messages.getFormattedMessage("asmt_ascore", null));
		HSSFCell cell6 = headerRow.createCell((short) (6));
		cell6.setCellStyle(style);
		cell6.setCellValue(this.messages.getFormattedMessage("asmt_final", null)+" "+this.messages.getFormattedMessage("asmt_outof", null)+" "+submissions.get(0).getAssessment().getParts().getTotalPoints()+")");
		HSSFCell cell7 = headerRow.createCell((short) (7));
		cell7.setCellStyle(style);
		cell7.setCellValue(this.messages.getFormattedMessage("asmt_released", null));

		for (Submission sub : submissions)
		{
			HSSFRow row;

			int rowNum = sheet.getLastRowNum() + 1;
			row = sheet.createRow(rowNum);
			try
			{
				User user = this.userDirectoryService.getUser(sub.getUserId());
				row.createCell((short) 0).setCellValue(user.getSortName());
				row.createCell((short) 1).setCellValue(user.getDisplayId());
			}
			catch (UserNotDefinedException e)
			{
				M_log.warn("createAsmtStatsSheet: " + e.toString());
			}
			if (sub.getStartDate() != null) row.createCell((short) 2).setCellValue(formatDate(sub.getStartDate()));
			if (sub.getSubmittedDate() != null) row.createCell((short) 3).setCellValue(formatDate(sub.getSubmittedDate()));
			row.createCell((short) 4).setCellValue(getSubmissionStatus(sub));
			
			if (sub.getAnswersAutoScore() != null) row.createCell((short) 5).setCellValue(sub.getAnswersAutoScore().floatValue());
			if (sub.getTotalScore() != null) row.createCell((short) 6).setCellValue(sub.getTotalScore().floatValue());
			row.createCell((short) 7).setCellValue(sub.getIsReleased().booleanValue());

		}

		return workbook;
	}
	
	/**
	 * Returns submission status such as auto, late etc
	 * @param sub Submission object
	 * @return status such as auto, late
	 */
	private String getSubmissionStatus(Submission sub)
	{
	    if (sub.getIsComplete() && !sub.getIsCompletedLate() && !sub.getIsNonSubmit() && !sub.getIsAutoCompleted()) return " ";
		if (sub.getIsComplete() && sub.getIsCompletedLate() && !sub.getIsNonSubmit() && !sub.getIsAutoCompleted()) return this.messages.getFormattedMessage("submission-finished-date-late-list", null);
		if (sub.getIsComplete() && sub.getIsAutoCompleted() && !sub.getIsCompletedLate()) return this.messages.getFormattedMessage("submission-finished-auto", null);
		if (sub.getIsComplete() && sub.getIsAutoCompleted() && sub.getIsCompletedLate()) return this.messages.getFormattedMessage("submission-finished-auto-late", null);
		if (!sub.getIsPhantom() && !sub.getIsComplete()) return this.messages.getFormattedMessage("submission-in-progress", null);
		if (sub.getIsPhantom() || sub.getIsNonSubmit()) return this.messages.getFormattedMessage("dash", null);
		return "";
	}

	/**
	 * Creates Fill Blanks tab for answers Item Analysis
	 *
	 * @param fb_questions List of Fill Blanks questions
	 * @param workbook Workbook object
	 * @param assessment Assessment object
	 */
	void createFillBlanksTab(List<Question> fb_questions, HSSFWorkbook workbook, Assessment assessment)
	{
		if (fb_questions == null || fb_questions.size() == 0) return;

		String assmtId = assessment.getId();
		HSSFSheet sheet = null;
		HSSFRow row;

		boolean headerRowDone = false;
		for (Iterator it = fb_questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();
			Map<String, Integer> fbqMap = new HashMap<String, Integer>();

			List<Answer> answers = this.submissionService.findSubmissionAnswers(assessment, q, FindAssessmentSubmissionsSort.userName_a, null, null);
			if (answers == null || answers.size() == 0) return;

			if (!headerRowDone)
			{
				sheet = workbook.createSheet("FillBlanks");

				HSSFRow headerRow = sheet.createRow((short) 0);

				HSSFCellStyle style = workbook.createCellStyle();
				HSSFFont font = workbook.createFont();
				font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				style.setFont(font);
				// Printing header row
				HSSFCell cell0 = headerRow.createCell((short) (0));
				cell0.setCellStyle(style);
				cell0.setCellValue(this.messages.getFormattedMessage("item_analysis_question", null));

				headerRowDone = true;
			}

			for (Answer answer : answers)
			{
				TypeSpecificAnswer a = answer.getTypeSpecificAnswer();

				if (a instanceof FillBlanksAnswerImpl)
				{
					FillBlanksAnswerImpl fb = (FillBlanksAnswerImpl) a;
					String[] fbAnswers = fb.getAnswers();
					for (int i = 0; i < fbAnswers.length; i++)
					{
						String fbAnswer;
						if (fb.correctFillAnswer(fbAnswers[i],i))
							fbAnswer = "*"+fbAnswers[i]+"*";
						else
							fbAnswer = fbAnswers[i];
						if (fbqMap.get(fbAnswer) != null)
						{
							int count = fbqMap.get(fbAnswer).intValue();
							count++;
							fbqMap.put(fbAnswer, count);
						}
						else
							fbqMap.put(fbAnswer, new Integer(1));
					}
				}
			}

			int rowNum = sheet.getLastRowNum() + 1;
			row = sheet.createRow(rowNum);

			String quest_desc = stripHtml(((FillBlanksQuestionImpl) q.getTypeSpecificQuestion()).getText());
			row.createCell((short) 0).setCellValue(quest_desc);
			int j = 1;
			if (fbqMap != null && fbqMap.size() > 0)
			{
				Iterator itsec = fbqMap.entrySet().iterator();
				while (itsec.hasNext())
				{
					Map.Entry pairs = (Map.Entry) itsec.next();
					if (pairs.getValue() != null)
					{
						row.createCell((short) j).setCellValue("(" + pairs.getValue() + ") " + pairs.getKey());
						j++;
					}
				}
			}

		}
	}

	/**
	 * Creates main frequency tab for Item analysis
	 *
	 * @param questions List of questions
	 * @param workbook Workbook object
	 * @param assessment Assessment object
	 * @return True if answers exist, false if not
	 */
	boolean createFrequencyTab(List<Question> questions, HSSFWorkbook workbook, Assessment assessment)
	{
		if (questions == null || questions.size() == 0) return false;

		String assmtId = assessment.getId();
		HSSFSheet sheet = null;
		HSSFRow row;

		boolean headerRowDone = false;
		List<Submission> submissions = this.submissionService.findAssessmentSubmissions(assessment,
				SubmissionService.FindAssessmentSubmissionsSort.sdate_a, Boolean.TRUE, null, null, null, null);

		boolean answersExist = false;
		for (Iterator it = questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();
			TypeSpecificQuestion tsq = q.getTypeSpecificQuestion();
			int count = 0;

			if (!(tsq instanceof EssayQuestionImpl) && !(tsq instanceof TaskQuestionImpl))
			{
				List<Answer> allAnswers = this.submissionService.findSubmissionAnswers(assessment, q, FindAssessmentSubmissionsSort.userName_a, null,
						null);
				if (allAnswers == null || allAnswers.size() == 0) continue;
				List<Answer> answers = filterOutMultiples(allAnswers, submissions);
				answersExist = true;

				if (!headerRowDone)
				{
					sheet = workbook.createSheet("ItemAnalysis");

					HSSFRow headerRow = sheet.createRow((short) 0);

					HSSFCellStyle style = workbook.createCellStyle();
					HSSFFont font = workbook.createFont();
					font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
					style.setFont(font);
					// Printing header row
					HSSFCell cell0 = headerRow.createCell((short) (0));
					cell0.setCellStyle(style);
					cell0.setCellValue(this.messages.getFormattedMessage("item_analysis_question", null));
					HSSFCell cell1 = headerRow.createCell((short) (1));
					cell1.setCellStyle(style);
					cell1.setCellValue(this.messages.getFormattedMessage("item_analysis_pool", null));
					HSSFCell cell2 = headerRow.createCell((short) (2));
					cell2.setCellStyle(style);
					cell2.setCellValue(this.messages.getFormattedMessage("item_analysis_numbers", null));
					HSSFCell cell3 = headerRow.createCell((short) (3));
					cell3.setCellStyle(style);
					cell3.setCellValue(this.messages.getFormattedMessage("item_analysis_whole_group", null));
					HSSFCell cell4 = headerRow.createCell((short) (4));
					cell4.setCellStyle(style);
					cell4.setCellValue(this.messages.getFormattedMessage("item_analysis_upper_27", null));
					HSSFCell cell5 = headerRow.createCell((short) (5));
					cell5.setCellStyle(style);
					cell5.setCellValue(this.messages.getFormattedMessage("item_analysis_lower_27", null));
					HSSFCell cell6 = headerRow.createCell((short) (6));
					cell6.setCellStyle(style);
					cell6.setCellValue(this.messages.getFormattedMessage("item_analysis_diff_in", null));
					HSSFCell cell7 = headerRow.createCell((short) (7));
					cell7.setCellStyle(style);
					cell7.setCellValue(this.messages.getFormattedMessage("item_analysis_disc", null));
					HSSFCell cell8 = headerRow.createCell((short) (8));
					cell8.setCellStyle(style);
					cell8.setCellValue(this.messages.getFormattedMessage("item_analysis_freq", null));
					headerRowDone = true;
				}

				int rowNum = sheet.getLastRowNum() + 1;
				row = sheet.createRow(rowNum);
				String quest_desc = null;
				if (tsq instanceof FillBlanksQuestionImpl)
				{
					quest_desc = stripHtml(((FillBlanksQuestionImpl) tsq).getText());
				}
				else
				{
					quest_desc = stripHtml(q.getDescription());
				}
				row.createCell((short) 0).setCellValue(quest_desc);
				row.createCell((short) 1).setCellValue(q.getPool().getTitle());
				row.createCell((short) 2).setCellValue(answers.size());
				int numCorrects =  numberOfCorrects(answers);
				double ncPc = 0.0;
				if (numCorrects > 0) ncPc = ((double) numberOfCorrects(answers) / answers.size()) * 100;
				row.createCell((short) 3).setCellValue(roundTwoDecimals(ncPc) + "%" + "(N=" + numCorrects +")");
				List<ScoreUser> scoreUserList = createScoreUserList(answers);
				List<String> upperUserList = fetchUpperList(scoreUserList, 27);
				List<String> lowerUserList = fetchLowerList(scoreUserList, 27);
				int upCorrectCount = calculateCorrects(upperUserList, answers);
				double uppPc = 0.0;
				if (upCorrectCount > 0) uppPc = ((double) upCorrectCount / upperUserList.size()) * 100;
				row.createCell((short) 4).setCellValue(roundTwoDecimals(uppPc) + "%" + "(N=" + upCorrectCount + ")");
				int loCorrectCount = calculateCorrects(lowerUserList, answers);
				double lowPc = 0.0;
				if (loCorrectCount > 0) lowPc = ((double) loCorrectCount / lowerUserList.size()) * 100;
				row.createCell((short) 5).setCellValue(roundTwoDecimals(lowPc) + "%" + "(N=" + loCorrectCount + ")");
				double diffIdx = (uppPc + lowPc) / 2;
				double discrim = (uppPc - lowPc) / 100;
				row.createCell((short) 6).setCellValue(roundTwoDecimals(diffIdx));
				row.createCell((short) 7).setCellValue(roundTwoDecimals(discrim));
				for (Submission s : submissions)
				{
					if (s.getIsPhantom()) continue;
					if (!s.getIsComplete()) continue;

					Answer a = s.getAnswer(q);
					if (a != null)
					{
						if (!a.getIsAnswered())
						{
							count++;
						}
					}
				}
				row.createCell((short) 8).setCellValue(count);
			}
		}
		return answersExist;
	}

	/**
	 * Creates Item Analysis spreadsheet for this assessment for all question
	 * types except essays and tasks
	 * Item analysis captured in separate tabs for different question types
	 * @param workbook Workbook object
	 * @param assessment Assessment object
	 * @return Updated workbook with the item analysis info
	 */
	HSSFWorkbook createItemAnalysisSheet(HSSFWorkbook workbook, Assessment assessment)
	{
		Map<String, Integer> userRowMap = new HashMap();

		AssessmentParts part = assessment.getParts();
		if (part == null) return null;

		List<Question> mc_questions = new ArrayList<Question>();
		List<Question> tf_questions = new ArrayList<Question>();
		List<Question> fb_questions = new ArrayList<Question>();
		List<Question> ma_questions = new ArrayList<Question>();
		List<Question> ls_questions = new ArrayList<Question>();

		List<Part> parts = part.getParts();
		if (parts == null || parts.size() == 0) return null;

		List<Question> questions = new ArrayList();
		for (Iterator partIt = parts.iterator(); partIt.hasNext();)
		{
			Part partObj = (Part) partIt.next();
			List<Question> questionsUsed = partObj.getQuestionsUsed();
			questions.addAll(questionsUsed);
		}
		if (questions == null || questions.size() == 0) return null;

		boolean answersExist = createFrequencyTab(questions, workbook, assessment);
		if (!answersExist) return null;

		for (Iterator it = questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();
			TypeSpecificQuestion tsq = q.getTypeSpecificQuestion();

			if (tsq instanceof MultipleChoiceQuestionImpl)
			{
				mc_questions.add(q);
			}
			if (tsq instanceof TrueFalseQuestionImpl)
			{
				tf_questions.add(q);
			}
			if (tsq instanceof FillBlanksQuestionImpl)
			{
				fb_questions.add(q);
			}
			if (tsq instanceof MatchQuestionImpl)
			{
				ma_questions.add(q);
			}
			if (tsq instanceof LikertScaleQuestionImpl)
			{
				ls_questions.add(q);
			}
		}

		createMultipleChoiceTab(mc_questions, workbook, assessment);
		createTrueFalseTab(tf_questions, workbook, assessment);
		createFillBlanksTab(fb_questions, workbook, assessment);
		createMatchTab(ma_questions, workbook, assessment);
		createLikertScaleTab(ls_questions, workbook, assessment);
		return workbook;
	}

	/**
	 * Creates Likert Scale tab for answers Item Analysis
	 *
	 * @param ls_questions List of Likert Scale questions
	 * @param workbook Workbook object
	 * @param assessment Assessment object
	 */
	void createLikertScaleTab(List<Question> ls_questions, HSSFWorkbook workbook, Assessment assessment)
	{
		if (ls_questions == null || ls_questions.size() == 0) return;

		String assmtId = assessment.getId();
		HSSFSheet sheet = null;
		HSSFRow row;

		boolean headerRowDone = false;
		for (Iterator it = ls_questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();
			Map<String, LikertScaleCount> lsqMap = new HashMap<String, LikertScaleCount>();

			List<LikertScaleQuestionImpl.LikertScaleQuestionChoice> choiceList = ((LikertScaleQuestionImpl) q.getTypeSpecificQuestion()).getChoices();
			for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
			{
				LikertScaleQuestionImpl.LikertScaleQuestionChoice chObj = (LikertScaleQuestionImpl.LikertScaleQuestionChoice) chIt.next();
				lsqMap.put(chObj.getId(), new LikertScaleCount(chObj.getText(), 0));
			}

			List<Answer> answers = this.submissionService.findSubmissionAnswers(assessment, q, FindAssessmentSubmissionsSort.userName_a, null, null);
			if (answers == null || answers.size() == 0) return;

			if (!headerRowDone)
			{
				sheet = workbook.createSheet("LikertScale");

				HSSFRow headerRow = sheet.createRow((short) 0);

				HSSFCellStyle style = workbook.createCellStyle();
				HSSFFont font = workbook.createFont();
				font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				style.setFont(font);
				// Printing header row
				HSSFCell cell0 = headerRow.createCell((short) (0));
				cell0.setCellStyle(style);
				cell0.setCellValue(this.messages.getFormattedMessage("item_analysis_question", null));

				headerRowDone = true;
			}

			for (Answer answer : answers)
			{
				TypeSpecificAnswer a = answer.getTypeSpecificAnswer();

				if (a instanceof LikertScaleAnswerImpl)
				{
					LikertScaleAnswerImpl ls = (LikertScaleAnswerImpl) a;
					for (Iterator chItn = choiceList.iterator(); chItn.hasNext();)
					{
						LikertScaleQuestionImpl.LikertScaleQuestionChoice lqc = (LikertScaleQuestionImpl.LikertScaleQuestionChoice) chItn.next();
						if (lqc.getId().equals(ls.getAnswer()))
						{
							LikertScaleCount lscObj = (LikertScaleCount) lsqMap.get(lqc.getId());
							lscObj.count++;
						}
					}
				}

			}
			int rowNum = sheet.getLastRowNum() + 1;
			row = sheet.createRow(rowNum);
			String quest_desc = stripHtml(q.getDescription());
			row.createCell((short) 0).setCellValue(quest_desc);
			int i = 1;
			if (lsqMap != null && lsqMap.size() > 0)
			{
				Iterator itsec = lsqMap.entrySet().iterator();
				while (itsec.hasNext())
				{
					Map.Entry pairs = (Map.Entry) itsec.next();
					if (pairs.getValue() != null)
					{
						LikertScaleCount lscObj = (LikertScaleCount) pairs.getValue();
						row.createCell((short) i).setCellValue("(" + lscObj.getCount() + ") " + lscObj.getText());
						i++;
					}
				}
			}

		}
	}

	/**
	 * Creates Match tab for answers Item Analysis
	 *
	 * @param ma_questions List of Match questions
	 * @param workbook Workbook object
	 * @param assessment Assessment object
	 */
	void createMatchTab(List<Question> ma_questions, HSSFWorkbook workbook, Assessment assessment)
	{
		if (ma_questions == null || ma_questions.size() == 0) return;

		String assmtId = assessment.getId();
		HSSFSheet sheet = null;
		HSSFRow row;
		boolean headerRowDone = false;
		for (Iterator it = ma_questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();
			Map<String, Integer> maMap = new HashMap<String, Integer>();

			List<Answer> answers = this.submissionService.findSubmissionAnswers(assessment, q, FindAssessmentSubmissionsSort.userName_a, null, null);
			if (answers == null || answers.size() == 0) return;

			if (!headerRowDone)
			{
				sheet = workbook.createSheet("Match");
				HSSFRow headerRow = sheet.createRow((short) 0);

				HSSFCellStyle style = workbook.createCellStyle();
				HSSFFont font = workbook.createFont();
				font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				style.setFont(font);
				// Printing header row
				HSSFCell cell0 = headerRow.createCell((short) (0));
				cell0.setCellStyle(style);
				cell0.setCellValue(this.messages.getFormattedMessage("item_analysis_question", null));

				headerRowDone = true;
			}

			for (Answer answer : answers)
			{
				TypeSpecificAnswer a = answer.getTypeSpecificAnswer();

				if (a instanceof MatchAnswerImpl)
				{
					MatchAnswerImpl ma = (MatchAnswerImpl) a;
					Map matchMap = (LinkedHashMap) ma.getAnswer();
					Iterator it2 = matchMap.entrySet().iterator();
					List choiceList = ((MatchQuestionImpl) ma.getAnswerObject().getQuestion().getTypeSpecificQuestion()).getPairsForDelivery();
					while (it2.hasNext())
					{
						StringBuffer matchStrBuf = new StringBuffer();
						Map.Entry entry = (Map.Entry) it2.next();
						String key = (String) entry.getKey();
						String value = (String) ((Value) entry.getValue()).getValue();
						String matchVal = fetchName(choiceList, key, true);
						boolean correctMatch = checkCorrectMatch(choiceList, key, value);
						if (correctMatch) matchStrBuf.append("*");
						matchStrBuf.append(stripHtml(matchVal));
						matchStrBuf.append("->");
						String choiceVal = fetchName(choiceList, value, false);
						matchStrBuf.append(stripHtml(choiceVal));
						if (correctMatch) matchStrBuf.append("*");
						String finalStr = matchStrBuf.toString();
						if (maMap.get(finalStr) != null)
						{
							int count = maMap.get(finalStr).intValue();
							count++;
							maMap.put(finalStr, count);
						}
						else
							maMap.put(finalStr, new Integer(1));
					}

				}
			}

			int rowNum = sheet.getLastRowNum() + 1;
			row = sheet.createRow(rowNum);

			String quest_desc = stripHtml(q.getDescription());
			row.createCell((short) 0).setCellValue(quest_desc);
			int j = 1;
			if (maMap != null && maMap.size() > 0)
			{
				Iterator itsec = maMap.entrySet().iterator();
				while (itsec.hasNext())
				{
					Map.Entry pairs = (Map.Entry) itsec.next();
					if (pairs.getValue() != null)
					{
						row.createCell((short) j).setCellValue("(" + pairs.getValue() + ") " + pairs.getKey());
						j++;
					}
				}
			}

		}
	}

	/**
	 * Creates the Multiple Choice tab for answers Item Analysis
	 *
	 * @param mc_questions List of Multiple Choice questions
	 * @param workbook Workbook object
	 * @param assessment Assessment object
	 */
	void createMultipleChoiceTab(List<Question> mc_questions, HSSFWorkbook workbook, Assessment assessment)
	{
		if (mc_questions == null || mc_questions.size() == 0) return;

		String assmtId = assessment.getId();
		HSSFSheet sheet = null;
		HSSFRow row;
		String[] choiceLabels = new String[]
		{ "A", "B", "C", "D", "E", "F", "G", "H", "I" };
		int[] choiceCount = new int[9];
		boolean[] choiceCorrect = new boolean[9];

		//Determine which question has most number of choices so as to determine header column count
		int headerSize = getMaxChoices(mc_questions);

		boolean headerRowDone = false;
		//Iterate through each question
		for (Iterator it = mc_questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();

			//Fetch all submissions to the question
			List<Answer> answers = this.submissionService.findSubmissionAnswers(assessment, q, FindAssessmentSubmissionsSort.userName_a, null, null);
			if (answers == null || answers.size() == 0) return;

			//Create header row once
			if (!headerRowDone)
			{
				sheet = workbook.createSheet("MultipleChoice");
				HSSFRow headerRow = sheet.createRow((short) 0);

				HSSFCellStyle style = workbook.createCellStyle();
				HSSFFont font = workbook.createFont();
				font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				style.setFont(font);
				// Printing header row
				HSSFCell cell0 = headerRow.createCell((short) (0));
				cell0.setCellStyle(style);
				cell0.setCellValue(this.messages.getFormattedMessage("item_analysis_question", null));

				for (int i = 1; i <= headerSize; i++)
				{
					HSSFCell cell1 = headerRow.createCell((short) (i));
					cell1.setCellStyle(style);
					cell1.setCellValue(choiceLabels[i - 1]);
				}

				headerRowDone = true;
			}

			int choiceListSize = 0;
			Set<Integer> correctAnswers = ((MultipleChoiceQuestionImpl) q.getTypeSpecificQuestion()).getCorrectAnswerSet();

			List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choiceList = ((MultipleChoiceQuestionImpl) q.getTypeSpecificQuestion())
			.getChoicesAsAuthored();
			choiceListSize = choiceList.size();

			//Set the choiceCorrect array so we know which ones are correct choices
			//for this question
			int pos = 0;
			for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
			{
				MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice mq = (MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice) chIt
						.next();
				if (correctAnswers.contains(Integer.parseInt(mq.getId())))
				{
					choiceCorrect[pos] = true;
				}
				pos++;
			}

			//Iterate through each submission
			for (Answer answer : answers)
			{
				TypeSpecificAnswer a = answer.getTypeSpecificAnswer();

				if (a instanceof MultipleChoiceAnswerImpl)
				{
					MultipleChoiceAnswerImpl mc = (MultipleChoiceAnswerImpl) a;
                    String[] ansArray = mc.getAnswers();

                    //Iterate and compare answer and increment choiceCount
					for (int l = 0; l < ansArray.length; l++)
					{
						int ansPos = 0;
						for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
						{
							MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice mq = (MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice) chIt
									.next();
							if (mq.getId().equals(ansArray[l]))
							{
								choiceCount[ansPos]++;
							}
							ansPos++;
						}
					}
				}
			}
			int rowNum = sheet.getLastRowNum() + 1;
			row = sheet.createRow(rowNum);
			String quest_desc = stripHtml(q.getDescription());
			row.createCell((short) 0).setCellValue(quest_desc);
			for (int k = 1; k <= choiceListSize; k++)
			{
				if (choiceCorrect[k-1]) row.createCell((short) k).setCellValue("*"+choiceCount[k - 1]+"*");
				else row.createCell((short) k).setCellValue(choiceCount[k - 1]);
			}
			for (int k = 1; k <= choiceListSize; k++)
			{
				choiceCount[k-1] = 0;
				choiceCorrect[k-1] = false;
			}
		}
	}

	/*void createMultipleChoiceTab(List<Question> mc_questions, HSSFWorkbook workbook, Assessment assessment)
	{
		if (mc_questions == null || mc_questions.size() == 0) return;

		String assmtId = assessment.getId();
		HSSFSheet sheet = null;
		HSSFRow row;
		String[] choiceLabels = new String[]
		{ "A", "B", "C", "D", "E", "F", "G", "H", "I" };
		int[] choiceCount = new int[9];
		boolean[] choiceCorrect = new boolean[9];

		//Determine which question has most number of choices so as to determine header column count
		int headerSize = getMaxChoices(mc_questions);

		boolean headerRowDone = false;
		//Iterate through each question
		for (Iterator it = mc_questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();

			//Fetch all submissions to the question
			List<Answer> answers = this.submissionService.findSubmissionAnswers(assessment, q, FindAssessmentSubmissionsSort.userName_a, null, null);
			if (answers == null || answers.size() == 0) return;

			//Create header row once
			if (!headerRowDone)
			{
				sheet = workbook.createSheet("MultipleChoice");
				HSSFRow headerRow = sheet.createRow((short) 0);

				HSSFCellStyle style = workbook.createCellStyle();
				HSSFFont font = workbook.createFont();
				font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				style.setFont(font);
				// Printing header row
				HSSFCell cell0 = headerRow.createCell((short) (0));
				cell0.setCellStyle(style);
				cell0.setCellValue(this.messages.getFormattedMessage("item_analysis_question", null));

				for (int i = 1; i <= headerSize; i++)
				{
					HSSFCell cell1 = headerRow.createCell((short) (i));
					cell1.setCellStyle(style);
					cell1.setCellValue(choiceLabels[i - 1]);
				}

				headerRowDone = true;
			}

			int choiceListSize = 0;
			//Iterate through each submission
			for (Answer answer : answers)
			{
				TypeSpecificAnswer a = answer.getTypeSpecificAnswer();

				if (a instanceof MultipleChoiceAnswerImpl)
				{
					MultipleChoiceAnswerImpl mc = (MultipleChoiceAnswerImpl) a;
                    //Get choice list as authored
					List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choiceList = ((MultipleChoiceQuestionImpl) mc.getAnswerObject()
							.getQuestion().getTypeSpecificQuestion()).getChoicesAsAuthored();
					String[] ansArray = mc.getAnswers();
					String[] choiceArray = new String[mc.getAnswers().length];
					choiceListSize = choiceList.size();
					Set<Integer> correctAnswers = ((MultipleChoiceQuestionImpl) mc.getAnswerObject().getQuestion().getTypeSpecificQuestion()).getCorrectAnswerSet();

					for (int l = 0; l < ansArray.length; l++)
					{
						int pos = 0;
						for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
						{
							MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice mq = (MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice) chIt
									.next();
							if (mq.getId().equals(ansArray[l]))
							{
								choiceCount[pos]++;
							}
							if (correctAnswers.contains(Integer.parseInt(mq.getId())))
							{
								choiceCorrect[pos] = true;
							}
							pos++;
						}
					}
				}
			}
			int rowNum = sheet.getLastRowNum() + 1;
			row = sheet.createRow(rowNum);
			String quest_desc = stripHtml(q.getDescription());
			row.createCell((short) 0).setCellValue(quest_desc);
			for (int k = 1; k <= choiceListSize; k++)
			{
				if (choiceCorrect[k-1]) row.createCell((short) k).setCellValue("*"+choiceCount[k - 1]+"*");
				else row.createCell((short) k).setCellValue(choiceCount[k - 1]);
				choiceCount[k - 1] = 0;
			}
		}
	}*/

	/**
	 * Iterates through all question types, captures responses and creates export summary spreadsheet
	 * @param workbook Workbook object
	 * @param assessment Assessment object
	 * @return Spreadsheet with Export summary
	 */
	HSSFSheet createResponsesSheet(HSSFWorkbook workbook, Assessment assessment)
	{
		boolean isSurvey;
		if (assessment.getType() == AssessmentType.survey) isSurvey = true;
		else isSurvey = false;
		
		Map<String, Integer> userRowMap = new HashMap();
		HSSFSheet sheet = workbook.createSheet("Submission responses");

		HSSFRow headerRow = sheet.createRow((short) 0);
		AssessmentParts part = assessment.getParts();

		List<Part> parts = part.getParts();
		if (parts == null || parts.size() == 0) return null;

		List<Question> questions = new ArrayList();
		for (Iterator partIt = parts.iterator(); partIt.hasNext();)
		{
			Part partObj = (Part) partIt.next();
			List<Question> questionsUsed = partObj.getQuestionsUsed();
			questions.addAll(questionsUsed);
		}

		HSSFCellStyle style = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		// Printing header row and question text
		if (!isSurvey)
		{
			HSSFCell cell0 = headerRow.createCell((short) (0));
			cell0.setCellStyle(style);
			cell0.setCellValue(this.messages.getFormattedMessage("export_lastname", null));
			HSSFCell cell1 = headerRow.createCell((short) (1));
			cell1.setCellStyle(style);
			cell1.setCellValue(this.messages.getFormattedMessage("export_firstname", null));
			HSSFCell cell2 = headerRow.createCell((short) (2));
			cell2.setCellStyle(style);
			cell2.setCellValue(this.messages.getFormattedMessage("export_username", null));

			HSSFCell cell3 = headerRow.createCell((short) (3));
			cell3.setCellStyle(style);
			cell3.setCellValue(this.messages.getFormattedMessage("export_score", null));
		}
		else
		{
			HSSFCell cell0 = headerRow.createCell((short) (0));
			cell0.setCellStyle(style);
			cell0.setCellValue(this.messages.getFormattedMessage("export_user", null));
		}

		int i;
		if (isSurvey) i = 1;
		else i = 4;
		for (Iterator it = questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();
			String quest_desc = null;

			TypeSpecificQuestion tsq = q.getTypeSpecificQuestion();
			if (tsq instanceof FillBlanksQuestionImpl)
			{
				quest_desc = stripHtml(((FillBlanksQuestionImpl) tsq).getText());
			}
			else
			{
				if (tsq instanceof EssayQuestionImpl || tsq instanceof TaskQuestionImpl)
				{
					if (tsq instanceof EssayQuestionImpl)
					{
						EssayQuestionImpl eqi = (EssayQuestionImpl) tsq;
						if (eqi.getSubmissionType() == SubmissionType.inline) quest_desc = stripHtml(q.getDescription());
					}
					if (tsq instanceof TaskQuestionImpl)
					{
						TaskQuestionImpl tqi = (TaskQuestionImpl) tsq;
						if (tqi.getSubmissionType() == SubmissionType.inline) quest_desc = stripHtml(q.getDescription());
					}
				}
				else
				{
					quest_desc = stripHtml(q.getDescription());
				}
			}
			if (quest_desc != null)
			{
				HSSFCell cell = headerRow.createCell((short) (i++));
				cell.setCellStyle(style);
				cell.setCellValue(quest_desc);
			}
		}

		int j;
		if (isSurvey) j = 1;
		else j = 4;
		boolean answersExist = false;
		for (Iterator it = questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();
			TypeSpecificQuestion tsq = q.getTypeSpecificQuestion();
			//Only captures inline submissions for essays and tasks
			if (tsq instanceof EssayQuestionImpl || tsq instanceof TaskQuestionImpl)
			{
				if (tsq instanceof EssayQuestionImpl)
				{
					EssayQuestionImpl eqi = (EssayQuestionImpl) tsq;
					if (eqi.getSubmissionType() != SubmissionType.inline)
					{
						continue;
					}
				}
				if (tsq instanceof TaskQuestionImpl)
				{
					TaskQuestionImpl tqi = (TaskQuestionImpl) tsq;
					if (tqi.getSubmissionType() != SubmissionType.inline)
					{
						continue;
					}
				}
			}
			List<Answer> answers = this.submissionService.findSubmissionAnswers(assessment, q, Boolean.TRUE, FindAssessmentSubmissionsSort.userName_a, null, null);
			if (answers == null || answers.size() == 0)
				continue;
			else
				answersExist = true;
			for (Answer answer : answers)
			{
				HSSFRow row;
				try
				{
					String userId = answer.getSubmission().getUserId();
					String subId = answer.getSubmission().getId();
					if (userRowMap == null || userRowMap.size() == 0 || (userRowMap.get(userId + subId) == null))
					{
						int rowNum = sheet.getLastRowNum() + 1;
						row = sheet.createRow(rowNum);
						if (!isSurvey)
						{
							User user = this.userDirectoryService.getUser(userId);
							row.createCell((short) 0).setCellValue(user.getLastName());
							row.createCell((short) 1).setCellValue(user.getFirstName());
							row.createCell((short) 2).setCellValue(user.getDisplayId());
							row.createCell((short) 3).setCellValue(roundTwoDecimals(answer.getSubmission().getTotalScore().floatValue()));
						}
						else
						{
							row.createCell((short) 0).setCellValue(this.messages.getFormattedMessage("export_user", null));
						}
						 userRowMap.put(userId + subId, new Integer(rowNum));
					}
					else
					{
						row = sheet.getRow(userRowMap.get(userId + subId).intValue());
					}

					TypeSpecificAnswer a = answer.getTypeSpecificAnswer();
					if (a instanceof EssayAnswerImpl)
					{
						EssayAnswerImpl essay = (EssayAnswerImpl) a;
						row.createCell((short) j).setCellValue(stripHtml(essay.getAnswerData()));
					}
					if (a instanceof TrueFalseAnswerImpl)
					{
						TrueFalseAnswerImpl tf = (TrueFalseAnswerImpl) a;
						if (!isSurvey && tf.getCompletelyCorrect().booleanValue()) row.createCell((short) j).setCellValue("*"+tf.getAnswer()+"*");
						else row.createCell((short) j).setCellValue(tf.getAnswer());
					}
					if (a instanceof MultipleChoiceAnswerImpl)
					{
						MultipleChoiceAnswerImpl mc = (MultipleChoiceAnswerImpl) a;
						List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choiceList = ((MultipleChoiceQuestionImpl) mc.getAnswerObject()
								.getQuestion().getTypeSpecificQuestion()).getChoicesAsAuthored();
						String[] ansArray = mc.getAnswers();
						String[] choiceArray = new String[mc.getAnswers().length];
						Set<Integer> correctAnswers = ((MultipleChoiceQuestionImpl) mc.getAnswerObject().getQuestion().getTypeSpecificQuestion())
								.getCorrectAnswerSet();

						int l = 0;
						for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
						{
							MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice mq = (MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice) chIt
									.next();

							if (Arrays.asList(ansArray).contains(mq.getId()))
							{
								if (!isSurvey && correctAnswers.contains(Integer.parseInt(mq.getId())))
								{
									choiceArray[l] = "*" + stripHtml(mq.getText().trim()) + "*";
								}
								else
								{
									choiceArray[l] = stripHtml(mq.getText().trim());
								}
								l++;
							}
						}

						row.createCell((short) j).setCellValue(getCommaAnswers(choiceArray));
					}
					if (a instanceof FillBlanksAnswerImpl)
					{
						FillBlanksAnswerImpl fb = (FillBlanksAnswerImpl) a;
						row.createCell((short) j).setCellValue(stripHtml(getCommaAnswers(checkCorrectFill(fb))));
					}
					if (a instanceof LikertScaleAnswerImpl)
					{
						LikertScaleAnswerImpl ls = (LikertScaleAnswerImpl) a;
						LikertScaleQuestionImpl lsq = (LikertScaleQuestionImpl) ls.getAnswerObject().getQuestion().getTypeSpecificQuestion();
						List<LikertScaleQuestionImpl.LikertScaleQuestionChoice> choiceList = lsq.getChoices();
						for (Iterator chIt = choiceList.iterator(); chIt.hasNext();)
						{
							LikertScaleQuestionImpl.LikertScaleQuestionChoice lqc = (LikertScaleQuestionImpl.LikertScaleQuestionChoice) chIt.next();
							if (lqc.getId().equals(ls.getAnswer()))
							{
								row.createCell((short) j).setCellValue(stripHtml(lqc.getText()));
								break;
							}
						}
					}
					if (a instanceof MatchAnswerImpl)
					{
						MatchAnswerImpl ma = (MatchAnswerImpl) a;
						Map matchMap = (LinkedHashMap) ma.getAnswer();
						Iterator it2 = matchMap.entrySet().iterator();
						StringBuffer matchStrBuf = new StringBuffer();

						List choiceList = ((MatchQuestionImpl) ma.getAnswerObject().getQuestion().getTypeSpecificQuestion()).getPairsForDelivery();
						while (it2.hasNext())
						{
							Map.Entry entry = (Map.Entry) it2.next();
							String key = (String) entry.getKey();
							String value = (String) ((Value) entry.getValue()).getValue();
							String matchVal = fetchName(choiceList, key, true);
							boolean correctMatch = checkCorrectMatch(choiceList, key, value);
							if (!isSurvey && correctMatch) matchStrBuf.append("*");
							matchStrBuf.append(stripHtml(matchVal.trim()));
							matchStrBuf.append("->");
							String choiceVal = fetchName(choiceList, value, false);
							if (choiceVal == null) matchStrBuf.append(this.messages.getFormattedMessage("nosel_made", null));
							else matchStrBuf.append(stripHtml(choiceVal.trim()));
							
							if (!isSurvey && correctMatch) matchStrBuf.append("*");
							matchStrBuf.append(", ");
						}
						if (matchStrBuf.length() > 0 && matchStrBuf.charAt(matchStrBuf.length() - 2) == ',')
						{
							String matchStrBufTrim = matchStrBuf.substring(0, matchStrBuf.length() - 2);
							row.createCell((short) j).setCellValue(stripHtml(matchStrBufTrim));
						}
					}
					if (a instanceof TaskAnswerImpl)
					{
						TaskAnswerImpl ta = (TaskAnswerImpl) a;
						row.createCell((short) j).setCellValue(stripHtml(ta.getAnswerData()));
					}
				}
				catch (UserNotDefinedException e)
				{
					M_log.warn("createResponsesSheet: " + e.toString());
				}
			}
			j = j + 1;
		}
		if (!answersExist) return null;
		return sheet;
	}

	/**
	 * For list of answers, creates a list of user ids and scores
	 * @param answers List of answers
	 * @return List of ScoreUser objects
	 */
	List<ScoreUser> createScoreUserList(List<Answer> answers)
	{
		if (answers == null || answers.size() == 0) return null;
		List<ScoreUser> scoreUserList = new ArrayList<ScoreUser>();
		for (Answer answer : answers)
		{
			ScoreUser suObj = new ScoreUser(answer.getTotalScore(), answer.getSubmission().getUserId());
			scoreUserList.add(suObj);
		}
		// Collections.sort(scoreUserList);
		Collections.sort(scoreUserList, new Comparator<ScoreUser>()
		{
			public int compare(ScoreUser s1, ScoreUser s2)
			{
				if (s1.getScore() != null && s2.getScore() != null) return s1.getScore().compareTo(s2.getScore());
				if (s1.getScore() == null) return -1;
				if (s2.getScore() == null) return 1;
				return 0;
			}
		});

		return scoreUserList;
	}

	/*List<Answer> filterOutMultiples(List<Answer> allAnswers)
	{
		if (allAnswers == null || allAnswers.size() == 0) return null;
		List<Answer> answers = new ArrayList<Answer>();

		HashMap<String,Answer> userAnswerMap = new HashMap<String,Answer>();
		for (Answer ansObj : allAnswers)
		{
			Answer userAnswerObj = userAnswerMap.get(ansObj.getSubmission().getUserId());
			if ( userAnswerObj != null && userAnswerObj.getTotalScore() < ansObj.getTotalScore())
			{
				userAnswerMap.put(ansObj.getSubmission().getUserId(), ansObj);
			}
			if (userAnswerObj == null)
			{
				userAnswerMap.put(ansObj.getSubmission().getUserId(), ansObj);
			}
		}
		if (userAnswerMap != null && userAnswerMap.size() > 0)
		{
			Iterator it = userAnswerMap.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pairs = (Map.Entry)it.next();
		        answers.add((Answer)pairs.getValue());
		}
		}
		return answers;
	}*/

	/**
	 * Iterates through the answers and creates an answer set that belongs to the best submissions
	 * @param allAnswers List of answers
	 * @param submissions List of best submissions
	 * @return List of answers that are associated with the best submissions
	 */
	List<Answer> filterOutMultiples(List<Answer> allAnswers, List<Submission> submissions)
	{
		if (allAnswers == null || allAnswers.size() == 0) return null;
		List<Answer> answers = new ArrayList<Answer>();

		HashMap<String,Answer> userAnswerMap = new HashMap<String,Answer>();
		for (Answer ansObj : allAnswers)
		{
			for (Submission subObj : submissions)
			{
				if (subObj.getId().equals(ansObj.getSubmission().getId()))
				{
					answers.add(ansObj);
					break;
				}
			}
		}

		return answers;
	}

	/**
	 * Creates True False tab for answers Item Analysis
	 *
	 * @param tf_questions List of True False questions
	 * @param workbook Workbook object
	 * @param assessment Assessment object
	 */
	void createTrueFalseTab(List<Question> tf_questions, HSSFWorkbook workbook, Assessment assessment)
	{
		if (tf_questions == null || tf_questions.size() == 0) return;

		String assmtId = assessment.getId();
		HSSFSheet sheet = null;
		HSSFRow row;

		boolean headerRowDone = false;
		for (Iterator it = tf_questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();

			List<Answer> answers = this.submissionService.findSubmissionAnswers(assessment, q, FindAssessmentSubmissionsSort.userName_a, null, null);
			if (answers == null || answers.size() == 0) return;

			if (!headerRowDone)
			{
				sheet = workbook.createSheet("TrueFalse");

				HSSFRow headerRow = sheet.createRow((short) 0);

				HSSFCellStyle style = workbook.createCellStyle();
				HSSFFont font = workbook.createFont();
				font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
				style.setFont(font);
				// Printing header row
				HSSFCell cell0 = headerRow.createCell((short) (0));
				cell0.setCellStyle(style);
				cell0.setCellValue(this.messages.getFormattedMessage("item_analysis_question", null));
				HSSFCell cell1 = headerRow.createCell((short) (1));
				cell1.setCellStyle(style);
				cell1.setCellValue(this.messages.getFormattedMessage("true-header", null));
				HSSFCell cell2 = headerRow.createCell((short) (2));
				cell2.setCellStyle(style);
				cell2.setCellValue(this.messages.getFormattedMessage("false-header", null));

				headerRowDone = true;
			}

			int trueCount = 0, falseCount = 0;

			for (Answer answer : answers)
			{
				TypeSpecificAnswer a = answer.getTypeSpecificAnswer();
				if (a instanceof TrueFalseAnswerImpl)
				{
					TrueFalseAnswerImpl tf = (TrueFalseAnswerImpl) a;
					if (tf.getAnswer().equals("true")) trueCount++;
					if (tf.getAnswer().equals("false")) falseCount++;
				}
			}
			int rowNum = sheet.getLastRowNum() + 1;
			row = sheet.createRow(rowNum);
			String quest_desc = stripHtml(q.getDescription());
			row.createCell((short) 0).setCellValue(quest_desc);
			if (((TrueFalseQuestionImpl) q.getTypeSpecificQuestion()).getCorrectAnswer().equals("true")) row.createCell((short) 1).setCellValue("*"+trueCount+"*");
			else row.createCell((short) 1).setCellValue(trueCount);
			if (((TrueFalseQuestionImpl) q.getTypeSpecificQuestion()).getCorrectAnswer().equals("false")) row.createCell((short) 2).setCellValue("*"+falseCount+"*");
			else row.createCell((short) 2).setCellValue(falseCount);

		}
	}

	/**
	 * Fetches the loser portion of the ScoreUser list
	 * @param scoreUserList ScoreUser list of objects
	 * @param portion Percentage of list that we wish to fetch
	 * @return Percentage of ScoreUser list from the lower part
	 */
	List<String> fetchLowerList(List<ScoreUser> scoreUserList, int portion)
	{
		if (scoreUserList == null || scoreUserList.size() == 0) return null;
		if (portion < 1 || portion > 100) return null;
		List<String> userList = new ArrayList();
		int listSize = (scoreUserList.size() * portion) / 100;

		for (int i = 0; i < listSize; i++)
		{
			userList.add(((ScoreUser) scoreUserList.get(i)).userId);
		}
		return userList;
	}

	/**
	 * Fetches the upper portion of the ScoreUser list
	 * @param scoreUserList ScoreUser list of objects
	 * @param portion Percentage of list that we wish to fetch
	 * @return Percentage of ScoreUser list from the upper part
	 */
	List<String> fetchUpperList(List<ScoreUser> scoreUserList, int portion)
	{
		if (scoreUserList == null || scoreUserList.size() == 0) return null;
		if (portion < 1 || portion > 100) return null;
		List<String> userList = new ArrayList();
		int listSize = (scoreUserList.size() * portion) / 100;
		int bottomIndex = scoreUserList.size() - listSize;
		for (int i = scoreUserList.size() - 1; i >= bottomIndex; i--)
		{
			userList.add(((ScoreUser) scoreUserList.get(i)).userId);

		}
		return userList;
	}

	/**
	 * Returns date in specified format
	 * @param date Date object
	 * @return Date in specified format
	 */
	String formatDate(Date date)
	{
		if (date == null) return null;
		Locale userLocale = DateHelper.getPreferredLocale(null);
		TimeZone userZone = DateHelper.getPreferredTimeZone(null);
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, userLocale);
		format.setTimeZone(userZone);

		return removeSeconds(format.format(date));
	}

	/**
	 * Iterates through all Multiple Choice questions to determine which one has the max number of choices
	 * @param mc_questions List of Question objects
	 * @return Number of max choices
	 */
	int getMaxChoices(List<Question> mc_questions)
	{
		if (mc_questions == null || mc_questions.size() == 0) return 0;
		int maxChoices = 1;
		for (Iterator it = mc_questions.iterator(); it.hasNext();)
		{
			Question q = (Question) it.next();
			List<MultipleChoiceQuestionImpl.MultipleChoiceQuestionChoice> choiceList = ((MultipleChoiceQuestionImpl) q.getTypeSpecificQuestion())
					.getChoices();
			if (choiceList != null)
			{
				if (choiceList.size() > maxChoices) maxChoices = choiceList.size();
			}
		}
		return maxChoices;
	}

	/**
	 * Determines how many answers are correct
	 * @param answers List of answers
	 * @return Number of correct answers
	 */
	int numberOfCorrects(List<Answer> answers)
	{
		if (answers == null || answers.size() == 0) return 0;
		int correctCount = 0;
		for (Answer answer : answers)
		{
			if (answer.getTypeSpecificAnswer() != null && answer.getTypeSpecificAnswer().getCompletelyCorrect() != null
					&& answer.getTypeSpecificAnswer().getCompletelyCorrect().booleanValue()) correctCount++;
		}
		return correctCount;
	}

	/**
	 * Removes seconds from display
	 * @param display String to display
	 * @return String with seconds part removed
	 */
	String removeSeconds(String display)
	{
		int i = display.lastIndexOf(":");
		if ((i == -1) || ((i + 3) >= display.length())) return display;

		String rv = display.substring(0, i) + display.substring(i + 3);
		return rv;
	}

	/**
	 * Returns a double with the two decimal format
	 * @param d Number as double
	 * @return Double formatted with two decimals
	 */
	double roundTwoDecimals(double d)
	{
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
	}
}
