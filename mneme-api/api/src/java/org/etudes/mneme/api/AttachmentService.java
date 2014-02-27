/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2013 Etudes, Inc.
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

package org.etudes.mneme.api;

import java.util.List;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.etudes.util.api.Translation;
import org.sakaiproject.entity.api.Reference;

/**
 * AttachmentService manages attachments.
 */
public interface AttachmentService
{
	/** How to resolve name conflicts when adding an attachment. */
	public enum NameConflictResolution
	{
		alwaysUseFolder, keepExisting, rename, useFolder
	}

	/**
	 * The root of type string for applications: add MNENE_APPLICATION or as appropriate to match the Reference type value.
	 */
	static final String APPLICATION_ID_ROOT = "sakai:";

	/** Prefix for the MnemeDocs area. */
	static final String DOCS_AREA = "docs";

	/** For download (such as all submissions) request references. */
	static final String DOWNLOAD = "download";

	/** The download all submissions for an assignment question download request string. */
	public final static String DOWNLOAD_ALL_SUBMISSIONS_QUESTION = "sq";
	
	/** Prefix for Export Summary */
	public final static String EXPORT_SUMMARY = "ex";
	
	/* Prefix for Item Analysis */
	public final static String ITEM_ANALYSIS = "it";
	
	/* Prefix for Assessment Export */
	public final static String ASMT_EXPORT = "ae";
	
	/* Prefix for Assessment Stats */
	public final static String ASMT_STATS = "as";

	/* Prefix for Certificate */
	public final static String ASMT_CERT = "ce";


	/** Application code for Mneme in ContentHosting's private area. */
	static final String MNEME_APPLICATION = "mneme";

	/** In Mneme, we want thumb resources created for attached images. */
	final static boolean MNEME_THUMB_POLICY = true;

	/** This string starts the references to uploaded resources. */
	static final String REFERENCE_ROOT = "/mneme";

	/** Prefix for the submissions upload area in MnemeDocs: in 1.1 and later, expect another path component with the submission id. */
	static final String SUBMISSIONS_AREA = "submissions";;

	/** The text added to the main URL to form the thumb-nail URL. */
	public final static String THUMB_SUFFIX = "_thumb.jpg";

	/**
	 * Add an attachment from an uploaded file.
	 * 
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @param onConflict
	 *        What to do if we have a file with this name already.
	 * @param file
	 *        The attachment file.
	 * @param makeThumb
	 *        if true, make a thumb resource for image types.
	 * @param altRef
	 *        The alternate reference for the resource.
	 * @return The Reference to the added attachment.
	 */
	Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, FileItem file, boolean makeThumb,
			String altRef);

	/**
	 * Add an attachment from a reference to a resource in ContentHosting.
	 * 
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @param onConflict
	 *        What to do if we have a file with this name already.
	 * @param resource
	 *        The Reference to the resource in ContentHosting.
	 * @param makeThumb
	 *        if true, make a thumb resource for image types.
	 * @param altRef
	 *        The alternate reference for the resource.
	 * @return The Reference to the added attachment.
	 */
	Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, Reference resource,
			boolean makeThumb, String altRef);

	/**
	 * Add an attachment from an uploaded file.
	 * 
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @param onConflict
	 *        What to do if we have a file with this name already.
	 * @param name
	 *        The attachment file name.
	 * @param body
	 *        The attachment body bytes.
	 * @param type
	 *        The attachment file mime type.
	 * @param makeThumb
	 *        if true, make a thumb resource for image types.
	 * @param altRef
	 *        The alternate reference for the resource.
	 * @return The Reference to the added attachment.
	 */
	Reference addAttachment(String application, String context, String prefix, NameConflictResolution onConflict, String name, byte[] body,
			String type, boolean makeThumb, String altRef);

	/**
	 * Find all the attachments in the docs area of the application for this context. Skip image thumbs.
	 * 
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @return A List of Attachments to the attachments.
	 */
	List<Attachment> findFiles(String application, String context, String prefix);

	/**
	 * Find all the image attachments in the docs area of the application for this context. Skip image thumbs.
	 * 
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @return A List of Attachments to the image type attachments.
	 */
	List<Attachment> findImages(String application, String context, String prefix);

	/**
	 * Find all the thumb images made for attachments in the docs area of the application for this context.
	 * 
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @return A List of Attachments to the attachments.
	 */
	List<Attachment> findThumbs(String application, String context, String prefix);

	/**
	 * Form a Reference object from a reference string.
	 * 
	 * @param refString
	 *        The reference string.
	 * @return The Reference object.
	 */
	Reference getReference(String refString);

	/**
	 * Collect all the attachment references in the html data:<br />
	 * Anything referenced by a src= or href=. in our content docs, or in a site content area <br />
	 * Ignore anything in a myWorkspace content area or the public content area. <br />
	 * If any are html, repeat the process for those to harvest their embedded references.
	 * 
	 * @param data
	 *        The data string.
	 * @param normalize
	 *        if true, decode the references by URL decoding rules.
	 * @return The set of attachment references.
	 */
	Set<String> harvestAttachmentsReferenced(String data, boolean normalize);

	/**
	 * Collect all the references in resource referenced, if it is html data
	 * 
	 * @param data
	 *        The data string.
	 * @param normalize
	 *        if true, decode the references by URL decoding rules.
	 * @return The set of attachment references.
	 */
	Set<String> harvestEmbedded(String reference, boolean normalize);

	/**
	 * Import a set of resources from references to resources in ContentHosting, translating any html as we import.
	 * 
	 * @param application
	 *        The application prefix for the collection in private.
	 * @param context
	 *        The context associated with the attachment.
	 * @param prefix
	 *        Any prefix path for within the context are of the application in private.
	 * @param onConflict
	 *        What to do if we have a file with this name already.
	 * @param resources
	 *        The References to the resources in ContentHosting to import.
	 * @param makeThumb
	 *        if true, make a thumb resource for image types.
	 * @param altRef
	 *        The alternate reference for the resource.
	 * @return a Translation list for each imported resource from its source to its imported location in this context.
	 */
	List<Translation> importResources(String application, String context, String prefix, NameConflictResolution onConflict, Set<String> resources,
			boolean makeThumb, String altRef);

	/**
	 * Return a relative url to an item that has been added to Mneme's content hosting section
	 * 
	 * @param ref
	 *        Resource id of item
	 * @return Url of item
	 * 
	 */
	String processMnemeUrls(String ref);

	/**
	 * Remove this attachment.
	 * 
	 * @param ref
	 *        The attachment reference.
	 */
	void removeAttachment(Reference ref);

	/**
	 * Translate any embedded attachment references in the html data, based on the set of translations.<br />
	 * Uses the same rules to find the references as harvestAttachmentsReferenced.
	 * 
	 * @param data
	 *        The html data.
	 * @param translations
	 *        The translations.
	 * @return The translated data.
	 */
	String translateEmbeddedReferences(String data, List<Translation> translations);
}
