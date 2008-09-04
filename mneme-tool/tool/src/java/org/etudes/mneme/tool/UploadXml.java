/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008 Etudes, Inc.
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

import org.apache.commons.fileupload.FileItem;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;

/**
 * Upload handles file uploads of XML documents, parsing the text into a DOM.
 */
public class UploadXml
{
	/** The uploaded file parsed into a DOM. */
	protected Document upload = null;

	/**
	 * Construct.
	 */
	public UploadXml()
	{
	}

	/**
	 * Access the uploaded DOM.
	 * 
	 * @return The uploaded file reference, or null if there was not an upload.
	 */
	public Document getUpload()
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
		try
		{
			// parse into a doc
			this.upload = Xml.readDocumentFromStream(file.getInputStream());
		}
		catch (IOException e)
		{
		}
	}
}
