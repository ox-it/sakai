/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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

package org.etudes.mneme.tool;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;
import org.etudes.mneme.api.AttachmentService;
import org.sakaiproject.entity.api.Reference;

/**
 * Upload handles file uploads into MnemeDocs.
 */
public class Upload
{
	/** Dependency: AttachmentService. */
	protected AttachmentService attachmentService = null;

	/** The context. */
	protected String context = null;

	/** The content path prefix. */
	protected String prefix = null;

	/** True if we want to store the upload in a unique holding folder. */
	protected boolean uniqueHolder = false;

	/** The uploaded file reference. */
	protected Reference upload = null;

	/**
	 * Construct.
	 * 
	 * @param context
	 *        The context.
	 * @param prefix
	 *        The content path prefix.
	 * @param uniqueHolder
	 *        True if we want to store the upload in a unique holding folder.
	 * @param attachmentService
	 *        the AttachmentService dependency.
	 */
	public Upload(String context, String prefix, boolean uniqueHolder, AttachmentService attachmentService)
	{
		this.context = context;
		this.prefix = prefix;
		this.uniqueHolder = uniqueHolder;
		this.attachmentService = attachmentService;
	}

	/**
	 * Access the uploaded file reference.
	 * 
	 * @return The uploaded file reference, or null if there was not an upload.
	 */
	public Reference getUpload()
	{
		return this.upload;
	}

	/**
	 * Accept a file upload from the user.
	 * 
	 * @param file
	 *        The file.
	 */
	public void setUpload(FileItem file)
	{
		Reference reference = this.attachmentService.addAttachment(AttachmentService.MNEME_APPLICATION, this.context, this.prefix, this.uniqueHolder,
				file);
		if (reference != null)
		{
			this.upload = reference;
		}
	}
}
