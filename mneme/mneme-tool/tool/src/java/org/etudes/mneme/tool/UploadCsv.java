/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/mneme/trunk/mneme-tool/tool/src/java/org/etudes/mneme/tool/UploadCsv.java $
 * $Id: UploadCsv.java 9578 2014-12-18 03:27:47Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2014 Etudes, Inc.
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
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.fileupload.FileItem;

/**
 * Upload handles file uploads of XML documents, parsing the text into a DOM.
 */
public class UploadCsv
{
	/** The uploaded file. */
	protected String contents = null;

	/** The file name extension. */
	protected String extension = null;

	/** The file name. */
	protected String name = null;

	/** The parsed file. */
	protected List<CSVRecord> records = null;

	/**
	 * Construct.
	 */
	public UploadCsv()
	{
	}

	/**
	 * @return The uploaded file, as a string.
	 */
	public String getContents()
	{
		return this.contents;
	}

	/**
	 * @return The file name extension.
	 */
	public String getExtension()
	{
		return this.extension;
	}

	/**
	 * @return The file name.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @return The uploaded file, as a list of parsed CSV records.
	 */
	public List<CSVRecord> getRecords()
	{
		return this.records;
	}

	/**
	 * Accept a file upload from the user.
	 * 
	 * @param file
	 *        The file.
	 */
	public void setUpload(FileItem file)
	{
		this.name = file.getName();
		this.extension = (this.name  != null && this.name .lastIndexOf('.') != -1) ? this.name .substring(this.name .lastIndexOf('.') + 1) : null;
		this.contents = file.getString();

		parse();
	}

	/**
	 * Parse the contents into CSV records.
	 */
	protected void parse()
	{
		try
		{
			Reader in = new StringReader(this.contents);
			CSVParser parser = new CSVParser(in, CSVFormat.RFC4180);
			this.records = parser.getRecords();
			parser.close();
		}
		catch (IOException e)
		{
		}
		finally
		{

		}
	}
}
