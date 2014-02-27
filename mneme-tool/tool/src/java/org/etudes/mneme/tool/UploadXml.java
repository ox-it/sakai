/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012, 2013 Etudes, Inc.
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.fileupload.FileItem;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;

/**
 * Upload handles file uploads of XML documents, parsing the text into a DOM.
 */
public class UploadXml
{
	/** The uploaded file parsed into a DOM. */
	protected Document upload = null;
	protected String unzipLocation = "";

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
			String fileName = file.getName();
			String extension = (fileName != null && fileName.lastIndexOf('.') != -1) ? fileName.substring(fileName.lastIndexOf('.') + 1) : null;
			// parse into a doc
			if ("xml".equals(extension))
			{
				this.upload = Xml.readDocumentFromStream(file.getInputStream());
				this.unzipLocation = "";
			}
			else if ("zip".equals(extension))
			{
				this.unzipLocation = unpackageZipFile(fileName, file);
				this.upload = Xml.readDocument(unzipLocation + File.separator + "imsmanifest.xml");
			}

		}
		catch (IOException e)
		{
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Get the unzip location
	 * 
	 * @return
	 */
	public String getUnzipLocation()
	{
		return unzipLocation;
	}

	/**
	 * Delete a file or directory and all its files
	 * 
	 * @param delfile
	 *        File object
	 */
	public void deleteFiles(File delfile)
	{
		if (delfile.isDirectory())
		{
			File files[] = delfile.listFiles();
			int i = files.length;
			while (i > 0)
				deleteFiles(files[--i]);

			delfile.delete();
		}
		else
			delfile.delete();
	}

	/**
	 * Unzip the QTI file
	 * 
	 * @param fileName_M
	 * @param backupFile_M
	 * @return
	 * @throws Exception
	 */
	private String unpackageZipFile(String fileName_M, FileItem backupFile_M) throws Exception
	{
		String packagingdirpath = ServerConfigurationService.getString("moodle.importDir", "");
		FileOutputStream out = null;
		try
		{
			String actFileName;
			// write the uploaded zip file to disk
			if (fileName_M.indexOf('\\') != -1)
				actFileName = fileName_M.substring(fileName_M.lastIndexOf('\\') + 1);
			else
				actFileName = fileName_M.substring(fileName_M.lastIndexOf('/') + 1);

			// write zip file first
			File baseDir = new File(packagingdirpath + File.separator + "importQTI");
			if (!baseDir.exists()) baseDir.mkdirs();

			File outputFile = new File(packagingdirpath + File.separator + "importQTI" + File.separator + actFileName.replace(' ', '_'));
			if (outputFile.exists()) outputFile.delete();
			out = new FileOutputStream(outputFile);
			byte buf[] = backupFile_M.get();
			out.write(buf);
			out.close();

			// unzipping dir
			File unzippeddir = new File(packagingdirpath + File.separator + "importQTI" + File.separator
					+ actFileName.substring(0, actFileName.lastIndexOf('.')));
			if (unzippeddir.exists()) deleteFiles(unzippeddir);
			if (!unzippeddir.exists()) unzippeddir.mkdirs();
			String unzippedDirpath = unzippeddir.getAbsolutePath();
			// unzip files
			unZipFile(outputFile, unzippedDirpath);
			if (outputFile.exists()) outputFile.delete();
			return unzippedDirpath;
		}
		finally
		{
			if (out != null) out.close();
		}
	}

	/**
	 * unzip the file and write to disk
	 * 
	 * @param zipfile
	 * @param dirpath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void unZipFile(File zipfile, String dirpath) throws Exception
	{
		FileInputStream fis = new FileInputStream(zipfile);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null)
		{
			if (entry.isDirectory())
			{

			}
			else if (entry.getName().lastIndexOf('\\') != -1)
			{
				String filenameincpath = entry.getName();

				String actFileNameIncPath = dirpath;

				while (filenameincpath.indexOf('\\') != -1)
				{
					String subFolName = filenameincpath.substring(0, filenameincpath.indexOf('\\'));

					File subfol = new File(actFileNameIncPath + File.separator + subFolName);
					if (!subfol.exists()) subfol.mkdirs();

					actFileNameIncPath = actFileNameIncPath + File.separator + subFolName;

					filenameincpath = filenameincpath.substring(filenameincpath.indexOf('\\') + 1);
				}

				String filename = entry.getName().substring(entry.getName().lastIndexOf('\\') + 1);
				unzip(zis, actFileNameIncPath + File.separator + filename);
			}
			else if (entry.getName().lastIndexOf('/') != -1)
			{
				String filenameincpath = entry.getName();

				String actFileNameIncPath = dirpath;

				while (filenameincpath.indexOf('/') != -1)
				{
					String subFolName = filenameincpath.substring(0, filenameincpath.indexOf('/'));
					File subfol = new File(actFileNameIncPath + File.separator + subFolName);
					if (!subfol.exists()) subfol.mkdirs();

					actFileNameIncPath = actFileNameIncPath + File.separator + subFolName;

					filenameincpath = filenameincpath.substring(filenameincpath.indexOf('/') + 1);
				}

				String filename = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
				unzip(zis, actFileNameIncPath + File.separator + filename);
			}
			else
				unzip(zis, dirpath + File.separator + entry.getName());
		}
		fis.close();
		zis.close();
	}

	/**
	 * write zip file to disk
	 * 
	 * @param zis
	 * @param name
	 * @throws IOException
	 */
	private void unzip(ZipInputStream zis, String name) throws Exception
	{
		BufferedOutputStream dest = null;
		int count;
		byte data[] = new byte[2048];
		try
		{
			// write the files to the disk
			FileOutputStream fos = new FileOutputStream(name);
			dest = new BufferedOutputStream(fos, 2048);
			while ((count = zis.read(data, 0, 2048)) != -1)
			{
				dest.write(data, 0, count);
			}
			dest.flush();
			fos.close();
		}
		catch (FileNotFoundException e)
		{
			throw e;
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (dest != null) dest.close();
			}
			catch (IOException e1)
			{
			}
		}
	}
}
