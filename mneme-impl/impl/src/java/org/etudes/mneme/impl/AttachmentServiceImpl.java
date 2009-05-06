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

package org.etudes.mneme.impl;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.util.EscapeRefUrl;
import org.etudes.mneme.api.Attachment;
import org.etudes.mneme.api.AttachmentService;
import org.etudes.mneme.api.MnemeService;
import org.etudes.mneme.api.SecurityService;
import org.etudes.mneme.api.Submission;
import org.etudes.mneme.api.SubmissionService;
import org.etudes.mneme.api.Translation;
import org.etudes.mneme.api.AttachmentService.NameConflictResolution;
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
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * AttachmentServiceImpl implements AttachmentService.
 */
public class AttachmentServiceImpl implements AttachmentService, EntityProducer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(AttachmentServiceImpl.class);

	protected final static String PROP_THUMB = "attachment:thumb";

	protected final static String PROP_UNIQUE_HOLDER = "attachment:unique";

	/** The chunk size used when streaming (100k). */
	protected static final int STREAM_BUFFER_SIZE = 102400;

	/** Dependency: ContentHostingService */
	protected ContentHostingService contentHostingService = null;

	/** Dependency: EntityManager */
	protected EntityManager entityManager = null;

	/** Dependency: IdManager. */
	protected IdManager idManager = null;

	/** Configuration: to make thumbs for images or not. */
	protected boolean makeThumbs = true;

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SecurityService */
	protected org.sakaiproject.authz.api.SecurityService securityServiceSakai = null;

	/** Dependency: ServerConfigurationService */
	protected ServerConfigurationService serverConfigurationService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/** Dependency: SubmissionService */
	protected SubmissionService submissionService = null;

	/**
	 * {@inheritDoc}
	 */
	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, FileItem file)
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

			Reference rv = addAttachment(name, application, context, prefix, onConflict, type, body, size);
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
	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, Reference resourceRef)
	{
		// make sure we can read!
		pushAdvisor();

		try
		{
			// if from our docs, convert into a content hosting ref
			if (resourceRef.getType().equals(APPLICATION_ID))
			{
				resourceRef = entityManager.newReference(resourceRef.getId());
			}

			// make sure we can read!
			ContentResource resource = this.contentHostingService.getResource(resourceRef.getId());
			String type = resource.getContentType();
			long size = resource.getContentLength();
			byte[] body = resource.getContent();
			String name = resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);

			Reference rv = addAttachment(name, application, context, prefix, onConflict, type, body, size);
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
	public Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, String name, byte[] body, String type)
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

			Reference rv = addAttachment(name, application, context, prefix, onConflict, type, body, size);
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
		return findTypes(application, context, prefix, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Attachment> findImages(String application, String context, String prefix)
	{
		return findTypes(application, context, prefix, "image/");
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
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
					throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
			{
				// decide on security
				if (!checkSecurity(ref))
				{
					throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "sampleAccess", ref.getReference());
				}

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
		Set<String> rv = new HashSet<String>();
		if (data == null) return rv;

		// pattern to find any src= or href= text
		// groups: 0: the whole matching text 1: src|href 2: the string in the quotes
		Pattern p = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^\"]*)\"");

		Matcher m = p.matcher(data);
		while (m.find())
		{
			if (m.groupCount() == 2)
			{
				String ref = m.group(2);

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
						try
						{
							refString = URLDecoder.decode(refString, "UTF-8");
						}
						catch (UnsupportedEncodingException e)
						{
							M_log.warn("harvestAttachmentsReferenced: " + e);
						}
					}

					rv.add(refString);
				}
			}
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
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if (parts.length > 5)
			{
				context = parts[5];
				id = "/" + StringUtil.unsplit(parts, 2, parts.length - 2, "/");
			}

			ref.set(APPLICATION_ID, null, id, null, context);

			return true;
		}

		return false;
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
	 * {@inheritDoc}
	 */
	public String translateEmbeddedReferences(String data, List<Translation> translations)
	{
		if (data == null) return data;
		if (translations == null) return data;

		// pattern to find any src= or href= text
		// groups: 0: the whole matching text 1: src|href 2: the string in the quotes
		Pattern p = Pattern.compile("(src|href)[\\s]*=[\\s]*\"([^\"]*)\"");

		Matcher m = p.matcher(data);
		StringBuffer sb = new StringBuffer();

		// process each "harvested" string (avoiding like strings that are not in src= or href= patterns)
		while (m.find())
		{
			if (m.groupCount() == 2)
			{
				String ref = m.group(2);

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

					// deal with %20 and other encoded URL stuff
					try
					{
						normal = URLDecoder.decode(normal, "UTF-8");
					}
					catch (UnsupportedEncodingException e)
					{
						M_log.warn("harvestAttachmentsReferenced: " + e);
					}

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
						m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "=\"" + ref.substring(0, index + 7) + escaped + "\""));
					}
				}
			}
		}

		m.appendTail(sb);

		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * Go through the add process, trying the various requested conflict resolution steps.
	 * 
	 * @param name
	 * @param application
	 * @param context
	 * @param prefix
	 * @param onConflict
	 * @param type
	 * @param body
	 * @param size
	 * @return
	 */
	protected Reference addAttachment(String name, String application, String context, String prefix, NameConflictResolution onConflict, String type,
			byte[] body, long size)
	{
		String id = contentHostingId(name, application, context, prefix, (onConflict == NameConflictResolution.alwaysUseFolder));
		Reference rv = doAdd(id, name, type, body, size, false, (onConflict == NameConflictResolution.rename));

		// if this failed and we need to fall back to using a folder, try again
		if ((rv == null) && (onConflict == NameConflictResolution.useFolder))
		{
			id = contentHostingId(name, application, context, prefix, true);
			rv = doAdd(id, name, type, body, size, false, false);
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

		// TODO: we might not want a thumb (such as for submission uploads to essay/task)
		// if we added one
		else
		{
			// if it is an image
			if (type.toLowerCase().startsWith("image/"))
			{
				addThumb(rv, body);
			}
		}

		return rv;
	}

	/**
	 * Add a thumbnail for the image at this reference with this name and contents.
	 * 
	 * @param resource
	 *        The image resource reference.
	 * @param body
	 *        The image bytes.
	 * @return A reference to the thumbnail, or null if not made.
	 */
	protected Reference addThumb(Reference resource, byte[] body)
	{
		// if disabled
		if (!this.makeThumbs) return null;

		// base the thumb name on the resource name
		String[] parts = StringUtil.split(resource.getId(), "/");
		String thumbName = parts[parts.length - 1] + THUMB_SUFFIX;
		Reference ref = this.getReference(resource.getId());
		String thumbId = ref.getId() + THUMB_SUFFIX;
		try
		{
			byte[] thumb = makeThumb(body, 80, 80, 0.75f);
			Reference thumbRef = doAdd(thumbId, thumbName, "image/jpeg", thumb, thumb.length, true, false);

			return thumbRef;
		}
		catch (IOException e)
		{
			M_log.warn("addAttachment: thumbing: " + e.toString());
		}
		catch (InterruptedException e)
		{
			M_log.warn("addAttachment: thumbing: " + e.toString());
		}

		return null;
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
	 */
	protected void assureCollection(String container, String name, boolean uniqueHolder)
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
				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, AttachmentService.REFERENCE_ROOT);

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
	 * @return
	 */
	protected String contentHostingId(String name, String application, String context, String prefix, boolean uniqueHolder)
	{
		// form the content hosting path, and make sure all the folders exist
		String contentPath = "/private/";
		assureCollection(contentPath, application, false);
		contentPath += application + "/";
		assureCollection(contentPath, context, false);
		contentPath += context + "/";
		if ((prefix != null) && (prefix.length() > 0))
		{
			// allow multi-part prefix
			if (prefix.indexOf('/') != -1)
			{
				String[] prefixes = StringUtil.split(prefix, "/");
				for (String pre : prefixes)
				{
					assureCollection(contentPath, pre, false);
					contentPath += pre + "/";
				}
			}
			else
			{
				assureCollection(contentPath, prefix, false);
				contentPath += prefix + "/";
			}
		}
		if (uniqueHolder)
		{
			String uuid = this.idManager.createUuid();
			assureCollection(contentPath, uuid, true);
			contentPath += uuid + "/";
		}

		contentPath += name;

		return contentPath;
	}

	/**
	 * Perform the add.
	 * 
	 * @param id
	 *        The content hosting id.
	 * @param name
	 *        The simple file name.
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
	 * @return The Reference to the added attachment.
	 */
	protected Reference doAdd(String id, String name, String type, byte[] body, long size, boolean thumb, boolean renameToFit)
	{
		try
		{
			if (!renameToFit)
			{
				ContentResourceEdit edit = this.contentHostingService.addResource(id);
				edit.setContent(body);
				edit.setContentType(type);
				ResourcePropertiesEdit props = edit.getPropertiesEdit();

				// set the alternate reference root so we get all requests
				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, AttachmentService.REFERENCE_ROOT);

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
				props.addProperty(ContentHostingService.PROP_ALTERNATE_REFERENCE, AttachmentService.REFERENCE_ROOT);

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
	 * If the document matches typePrefix and not a thumbnail, add it to the attachments.
	 * 
	 * @param attachments
	 *        The list of attachments.
	 * @param doc
	 *        The resource.
	 * @param typePrefix
	 *        if null, match any type, else match only the types that match this prefix.
	 */
	protected void filterNonThumbTypes(List<Attachment> attachments, ContentResource doc, String typePrefix)
	{
		// only matching types
		if ((typePrefix == null) || (doc.getContentType().toLowerCase().startsWith(typePrefix.toLowerCase())))
		{
			// not thumbs
			if (doc.getProperties().getProperty(this.PROP_THUMB) == null)
			{
				String ref = doc.getReference(ContentHostingService.PROP_ALTERNATE_REFERENCE);
				String url = doc.getUrl(ContentHostingService.PROP_ALTERNATE_REFERENCE);
				String escapedUrl = EscapeRefUrl.escapeRefUrl(ref, url);

				Attachment a = new AttachmentImpl(doc.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME), ref, escapedUrl, doc
						.getContentType());
				attachments.add(a);
			}
		}
	}

	/**
	 * Find all the attachments in the docs area of the application for this context. Skip image thumbs. Select only those matching type.
	 * 
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @param typePrefix
	 *        if null, all but the thumbs. Otherwise only those matching the prefix in mime type.
	 * @return A List of Attachments to the attachments.
	 */
	protected List<Attachment> findTypes(String application, String context, String prefix, String typePrefix)
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
			List members = docs.getMemberResources();
			for (Object m : members)
			{
				if (m instanceof ContentCollection)
				{
					// get the member within
					ContentCollection holder = (ContentCollection) m;
					List innerMembers = holder.getMemberResources();
					for (Object mm : innerMembers)
					{
						if (mm instanceof ContentResource)
						{
							filterNonThumbTypes(rv, (ContentResource) mm, typePrefix);
						}
					}
				}

				else if (m instanceof ContentResource)
				{
					filterNonThumbTypes(rv, (ContentResource) m, typePrefix);
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
	 * Create a thumbnail image from the full image in the byte[], of the desired width and height and quality, preserving aspect ratio.
	 * 
	 * @param full
	 *        The full image bytes.
	 * @param width
	 *        The desired max width (pixels).
	 * @param height
	 *        The desired max height (pixels).
	 * @param quality
	 *        The JPEG quality (0 - 1).
	 * @return The thumbnail JPEG as a byte[].
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected byte[] makeThumb(byte[] full, int width, int height, float quality) throws IOException, InterruptedException
	{
		// read the image from the byte array, waiting till it's processed
		Image fullImage = Toolkit.getDefaultToolkit().createImage(full);
		MediaTracker tracker = new MediaTracker(new Container());
		tracker.addImage(fullImage, 0);
		tracker.waitForID(0);

		// get the full image dimensions
		int fullWidth = fullImage.getWidth(null);
		int fullHeight = fullImage.getHeight(null);

		// preserve the aspect of the full image, not exceeding the thumb dimensions
		if (fullWidth > fullHeight)
		{
			// full width will take the full desired width, set the appropriate height
			height = (int) ((((float) width) / ((float) fullWidth)) * ((float) fullHeight));
		}
		else
		{
			// full height will take the full desired height, set the appropriate width
			width = (int) ((((float) height) / ((float) fullHeight)) * ((float) fullWidth));
		}

		// draw the scaled thumb
		BufferedImage thumbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2D = thumbImage.createGraphics();
		g2D.drawImage(fullImage, 0, 0, width, height, null);

		// encode as jpeg to a byte array
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		BufferedOutputStream out = new BufferedOutputStream(byteStream);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
		param.setQuality(quality, false);
		encoder.setJPEGEncodeParam(param);
		encoder.encode(thumbImage);
		out.close();
		byte[] thumb = byteStream.toByteArray();

		return thumb;
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
}
