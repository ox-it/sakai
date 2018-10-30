package org.sakaiproject.exporter.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.Validator;

import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

public class CCExport {

	private static Logger log = LoggerFactory.getLogger(CCExport.class);
	private File root;
	private long nextid = 100000;

	private HttpServletResponse response;
	private File errFile = null;
	private PrintStream errStream = null;
	private String siteId;
	private List<String> selectedFolderIds;
	private List<String> selectedFiles;
	private Map<String, ContentResource> selectedFilesToExport = new HashMap<>();

	private ContentHostingService contentService;

	public CCExport(ContentHostingService contentHostingService) {
		this.contentService = contentHostingService;
	}

	public void doExport(String siteId, List<String> selectedFolderIds, List<String> selectedFiles, HttpServletResponse httpServletResponse) {
		this.siteId = siteId;
		this.selectedFolderIds = selectedFolderIds;
		this.selectedFiles = selectedFiles;
		response = httpServletResponse;

		if (!startExport())
			return;
		if (!findSelectedFiles())
			return;

		try (OutputStream htmlOut = response.getOutputStream(); ZipPrintStream out = new ZipPrintStream(htmlOut)) {
			response.setHeader("Content-disposition", "inline; filename=sakai-export.imscc");
			response.setContentType("application/zip");

			outputSelectedFiles(out);
			outputCourseSettingsFiles(out);
			// Module not required for now.
			// outputModuleSettingsFiles(out);
			outputManifest(out);
		} catch (IOException ioe) {
			log.error("export-common-cartridge error getting output/zip stream, doExport" + ioe);
			setErrMessage("I/O error getting output stream for export file: " + ioe.getMessage());
		}
	}

	public boolean startExport() {
		try {
			root = File.createTempFile("ccexport", "root");
			if (root.exists()) {
				if (!root.delete())
					throw new IOException("unabled to delete old temp file for export");
			}
			if (!root.mkdir())
				throw new IOException("unable to make directory for export");
			errFile = new File(root, "export-errors");
			errStream = new PrintStream(errFile);
		} catch (IOException ioe) {
			log.error("export-common-cartridge error creating output files" + ioe);
			setErrMessage("I/O error creating output files: " + ioe.getMessage());
			return false;
		}
		return true;
	}

	private boolean findSelectedFiles() {
		try {
			// Add any files in the folders to the Map.
			if (selectedFolderIds.size() > 0) {
				for (String selectedFolder : selectedFolderIds) {
					List<ContentResource> folderContents = contentService.getAllResources(selectedFolder);
					for (ContentResource folderFile: folderContents) {
						selectedFilesToExport.put(folderFile.getId(), folderFile);
					}
				} 
			}

			// Add any files to the Map.
			for (String selectedFile : selectedFiles) {
				ContentResource contentFile = contentService.getResource(selectedFile);
				selectedFilesToExport.put(contentFile.getId(), contentFile);
			}

			// Add everything other than reading lists (citations) or zips to the files to be in the export.
			for (Iterator<Map.Entry<String, ContentResource>> it = selectedFilesToExport.entrySet().iterator(); it.hasNext();) {
				ContentResource contentResource = it.next().getValue();
				if ("org.sakaiproject.citation.impl.CitationList".equals(contentResource.getResourceType()) ||
					"application/zip".equals(contentResource.getContentType())) {
					it.remove();
				}
			}
		} catch (PermissionException pe) {
			log.error("export-common-cartridge permission error finding selected files" + pe);
			setErrMessage("Error finding files selected for export: " + pe.getMessage());
			return false;
		} catch (IdUnusedException ide) {
			log.error("export-common-cartridge ID unused  error finding selected files" + ide);
			setErrMessage("Error finding files selected for export: " + ide.getMessage());
			return false;
		} catch (TypeException te) {
			log.error("export-common-cartridge type error finding selected files" + te);
			setErrMessage("Error finding files selected for export.: " + te.getMessage());
			return false;
		}
		return true;
	}

	public boolean outputSelectedFiles(ZipPrintStream out) {

		String dashedFilename;
		Collection<ContentResource> contentResources = selectedFilesToExport.values();
		for (ContentResource contentResource : contentResources) {
			ZipEntry zipEntry;

			// Find the file name without the file path.
			String filename = getFileName(contentResource, true);

			// Files that are HTML or XHTML should be put in the pages area in Canvas so that they can be edited,
			// to do this add to the wiki-content area and put the appropriate entry in the manifest file.
			// Files in Pages must be in a flat file structure regardless of where they are in resources.
			// Files in web_resources will end up in Files and cannot be edited.  They can be in the same file structure as resources.

			// Change the filename to be the folder structure but replace slash with dash, this is so that duplicate file names are not lost.
			// Will only be used for HTML files.
			dashedFilename = getFilePath(contentResource, false).replace('/', '-') + filename;
			if (hasMarkUp(contentResource.getContentType())) {
				zipEntry = new ZipEntry("wiki_content/" + dashedFilename);
			} else {
				zipEntry = new ZipEntry("web_resources/" + getFilePath(contentResource, false) + filename);
			}

			zipEntry.setSize(contentResource.getContentLength());

			try (InputStream contentStream = contentResource.streamContent()) {

				out.putNextEntry(zipEntry);
				if (hasMarkUp(contentResource.getContentType())) {
					// treat html/xhtml separately. Need to convert urls to relative urls.
					String content = null;

					content = new String(contentResource.getContent());
					content = linkFixup(content);

					//  add in HTML header and footer
					out.println("<html>");
					out.println("<head>");
					out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
					out.println("<title>" + dashedFilename + "</title>");
					out.println("<meta name=\"identifier\" content=\"" + contentResource.getId() + "\"/>");
					out.println("<meta name=\"editing_roles\" content=\"teachers\"/>");
					out.println("<meta name=\"workflow_state\" content=\"unpublished\"/>");
					out.println("</head>");
					out.println("<body>");
					out.println(content);
					out.println("</body>");
					out.println("</html>");
				} else {
					IOUtils.copy(contentStream, out);
				}
				out.closeEntry();
			} catch (ServerOverloadException soe) {
				log.error("export-common-cartridge server overload error adding selected files" + soe);
				setErrMessage("Error outputting selected files: " + soe.getMessage());
				return false;
			} catch (IOException ioe) {
				log.error("export-common-cartridge I/O error adding selected files" + ioe);
				setErrMessage("Error outputting selected files: " + ioe.getMessage());
				return false;
			}
		}
		return true;
	}

	public boolean outputCourseSettingsFiles(ZipPrintStream out) {
		try {
			ZipEntry zipEntry = new ZipEntry("course_settings/course_settings.xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<course identifier=\"i56e1997d322b372fd7e6015b2f538aad\" xmlns=\"http://canvas.instructure.com/xsd/cccv1p0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://canvas.instructure.com/xsd/cccv1p0 https://canvas.instructure.com/xsd/cccv1p0.xsd\">");
			out.println("  <title>Course content created in Canvas</title>");
			out.println("  <course_code>Course</course_code>");
			out.println("  <is_public>false</is_public>");
			out.println("  <public_syllabus>false</public_syllabus>");
			out.println("  <public_syllabus_to_auth>false</public_syllabus_to_auth>");
			out.println("  <allow_student_wiki_edits>false</allow_student_wiki_edits>");
			out.println("  <allow_student_forum_attachments>false</allow_student_forum_attachments>");
			out.println("  <lock_all_announcements>true</lock_all_announcements>");
			out.println("  <default_wiki_editing_roles>teachers</default_wiki_editing_roles>");
			out.println("  <allow_student_organized_groups>true</allow_student_organized_groups>");
			out.println("  <default_view>modules</default_view>");
			out.println("  <open_enrollment>false</open_enrollment>");
			out.println("  <self_enrollment>false</self_enrollment>");
			out.println("  <license>private</license>");
			out.println("  <indexed>false</indexed>");
			out.println("  <hide_final_grade>false</hide_final_grade>");
			out.println("  <hide_distribution_graphs>false</hide_distribution_graphs>");
			out.println("  <allow_student_discussion_topics>true</allow_student_discussion_topics>");
			out.println("  <allow_student_discussion_editing>true</allow_student_discussion_editing>");
			out.println("  <show_announcements_on_home_page>false</show_announcements_on_home_page>");
			out.println("  <home_page_announcement_limit>3</home_page_announcement_limit>");
			out.println("  <enable_offline_web_export>true</enable_offline_web_export>");
			out.println("  <restrict_student_future_view>false</restrict_student_future_view>");
			out.println("  <restrict_student_past_view>false</restrict_student_past_view>");
			out.println("  <grading_standard_enabled>false</grading_standard_enabled>");
			out.println("  <storage_quota>1048576000</storage_quota>");
			out.println("  <root_account_uuid>5kBHQyhv7XPtbq84g8Y4KYbyHmrS7odURR6NriBv</root_account_uuid>");
			out.println("</course>");
			out.closeEntry();

			zipEntry = new ZipEntry("course_settings/canvas_export.txt");
			out.putNextEntry(zipEntry);
			out.println("Required so that common cartridge import into Canvas Pages and Files works.");
			out.closeEntry();
		} catch (IOException ioe) {
			log.error("export-common-cartridge IO exception outputing course settings files" + ioe);
			setErrMessage("Error outputing selected course files: " + ioe.getMessage());
			return false;
		}

		return true;
	}

	public boolean outputModuleSettingsFiles(ZipPrintStream out) {
		try {
			ZipEntry zipEntry = new ZipEntry("course_settings/module_meta.xml");
			out.putNextEntry(zipEntry);

			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<modules xmlns=\"http://canvas.instructure.com/xsd/cccv1p0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://canvas.instructure.com/xsd/cccv1p0 https://canvas.instructure.com/xsd/cccv1p0.xsd\">");
			out.println("<module identifier=\"tocomplete\">");
			out.println("<title>First module</title>");
			out.println("<workflow_state>unpublished</workflow_state>");
			out.println("<position>1</position>");
			out.println("<require_sequential_progress>false</require_sequential_progress>");
			out.println("<locked>false</locked>");
			out.println("<items>");
			out.println("</items>");
			out.println("</module>");
			out.println("</modules>");
			out.closeEntry();

			out.closeEntry();
		} catch (IOException ioe) {
			log.error("export-common-cartridge IO exception outputing course settings files" + ioe);
			setErrMessage("Error outputting module selected course files: " + ioe.getMessage());
			return false;
		}

		return true;
	}

	public boolean outputManifest(ZipPrintStream out) {
		String title = "Sakai";  // should never be used
		try {
			Site site = null;
			site = SiteService.getSite(siteId);
			title = site.getTitle();
		} catch (IdUnusedException impossible) {
			// impossible, one hopes
		}

		try {
			ZipEntry zipEntry = new ZipEntry("imsmanifest.xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

			out.println("<manifest identifier=\"i65d048afc30bea25ed17ce5063f901f6\"");
			out.println(" xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1\"");
			out.println(" xmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource\"");
			out.println(" xmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest\"");
			out.println(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			out.println(" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1");
			out.println("                      http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imscp_v1p2_v1p0.xsd");
			out.println("                      http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource");
			out.println("                      http://www.imsglobal.org/profile/cc/ccv1p1/LOM/ccv1p1_lomresource_v1p0.xsd");
			out.println("                      http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest");
			out.println("                      http://www.imsglobal.org/profile/cc/ccv1p1/LOM/ccv1p1_lommanifest_v1p0.xsd\">");
			out.println("  <metadata>");
			out.println("    <schema>IMS Common Cartridge</schema>");
			out.println("    <schemaversion>1.1.0</schemaversion>");
			out.println("    <lomimscc:lom>");
			out.println("      <lomimscc:general>");
			out.println("        <lomimscc:title>");
			out.println("          <lomimscc:string>" + StringEscapeUtils.escapeXml(title) + "</lomimscc:string>");
			out.println("        </lomimscc:title>");
			out.println("      </lomimscc:general>");
			out.println("      <lomimscc:lifeCycle>");
			out.println("        <lomimscc:contribute>");
			out.println("          <lomimscc:date>");
			out.println("            <lomimscc:dateTime>2018-06-11</lomimscc:dateTime>");
			out.println("          </lomimscc:date>");
			out.println("        </lomimscc:contribute>");
			out.println("      </lomimscc:lifeCycle>");
			out.println("      <lomimscc:rights>");
			out.println("        <lomimscc:copyrightAndOtherRestrictions>");
			out.println("          <lomimscc:value>yes</lomimscc:value>");
			out.println("        </lomimscc:copyrightAndOtherRestrictions>");
			out.println("        <lomimscc:description>");
			out.println("          <lomimscc:string>Private (Copyrighted) - http://en.wikipedia.org/wiki/Copyright</lomimscc:string>");
			out.println("        </lomimscc:description>");
			out.println("      </lomimscc:rights>");
			out.println("    </lomimscc:lom>");
			out.println("  </metadata>");
			// Module not required for now.
//			out.println("  <organizations>");
//			out.println("    <organization identifier=\"org_1\" structure=\"rooted-hierarchy\">");
//			out.println("      <item identifier=\"LearningModules\"><item identifier=\"tocomplete\"><title>First module</title></item></item>");
//			out.println("    </organization>");
//			out.println("  </organizations>");

			out.println("  <resources>");
			out.println("    <resource identifier=\"i56e1997d322b372fd7e6015b2f538aad\" type=\"associatedcontent/imscc_xmlv1p1/learning-application-resource\" href=\"course_settings/canvas_export.txt\">");
			out.println("      <file href=\"course_settings/course_settings.xml\"/>");
			out.println("      <file href=\"course_settings/canvas_export.txt\"/>");
			// Module not required for now.
			// out.println("      <file href=\"course_settings/module_meta.xml\"/>");
			out.println("    </resource>");

			Collection<ContentResource> contentResources = selectedFilesToExport.values();
			for (ContentResource contentResource : contentResources) {
				String filepath = getFilePath(contentResource, true);
				String filename = getFileName(contentResource, true);

				// See the comments above in method outputSelectedFiles.
				if (hasMarkUp(contentResource.getContentType())) {
					out.println("    <resource identifier=\"" + contentResource.getId() + "\" type=\"webcontent\" href=\"wiki_content/" + filepath.replace('/', '-') + filename + "\">");
					out.println("      <file href=\"wiki_content/" + filepath.replace('/', '-') + filename + "\"/>");
				} else {
					out.println("    <resource identifier=\"" + getResourceId() + "\" type=\"webcontent\" href=\"web_resources/" + getFilePath(contentResource, false) + filename + "\">");
					out.println("      <file href=\"web_resources/" + getFilePath(contentResource, false) + filename + "\"/>");
				}
				out.println("    </resource>");
			}

			// add error log at the very end
			String errId = getResourceId();
			out.println("    <resource href=\"cc-objects/export-errors\" identifier=\"" + errId + "\" type=\"webcontent\">");
			out.println("      <file href=\"cc-objects/export-errors\"/>");
			out.println("    </resource>");
			out.println("  </resources>");
			out.println("</manifest>");

			out.closeEntry();
			errStream.close();
			zipEntry = new ZipEntry("cc-objects/export-errors");
			out.putNextEntry(zipEntry);
			try (InputStream contentStream = new FileInputStream(errFile)) {
				IOUtils.copy(contentStream, out);
				out.closeEntry();
			}
		} catch (IOException ioe) {
			log.error("export-common-cartridge IO exception outputing manifest file" + ioe);
			setErrMessage("Error outputting manifest: " + ioe.getMessage());
			return false;
		}
		return true;
	}

	String getResourceId() {
		return "res" + (nextid++);
	}

	private boolean hasMarkUp(String mimeType) {
		return (("text/html").equals(mimeType) || ("application/xhtml+xml").equals(mimeType));
	}

	private String getFilePath(ContentResource contentResource, boolean encode) {
		// We want to find the path up until the filename, just strip off the /group/:siteid and stop at the last slash.
		String filePath = contentResource.getId().substring(contentResource.getId().lastIndexOf(siteId) + siteId.length() + 1, contentResource.getId().lastIndexOf("/") + 1);
		if (encode) {
			// use Validator class, not URLEncoder as Validator does not convert the slashes in the path.
			return Validator.escapeUrl(filePath);
		} else {
			return filePath;
		}
	}

	private String getFileName(ContentResource contentResource, boolean encode) {
		String fileName = contentResource.getId().substring(contentResource.getId().lastIndexOf("/") + 1);
		if (encode) {
			try {
				return URLEncoder.encode(fileName, "UTF-8");
			} catch (UnsupportedEncodingException uee) {
				throw new RuntimeException("export-common-cartridge UnsupportedEncodingException encoding file name: " + uee);
			}
		}
		return fileName;
	}

	// Converts links in HTML files into relative links and makes them suitable for Canvas.
	public String linkFixup(String content) {
		StringBuilder parsedContent = new StringBuilder(content);

		// Remove protocol, domain and /access/content folders from all links.
		parsedContent = makeLinksRelative(parsedContent);

		// Check which links are to html files (need to use the unencoded href but not remove the encoding) and change 
		// the folder slashes for hyphens when they are. Store the links in a map to use below.
		Map<String, Boolean> savedLinks = new HashMap<>();
		parsedContent = convertSlashToHyphen(parsedContent, savedLinks);

		// Add the correct Canvas substitution variable so that HTML files end up in Pages in Canvas and
		// everything else in the files area in Canvas.
		parsedContent = addCanvasFilePath(parsedContent, savedLinks);

		// Remove the /group/siteid from the links.
		parsedContent = removeWLPath(parsedContent);

		//Remove any encoding characters from the link text only (not the href).
		parsedContent = removeUrlEncoding(parsedContent);

		return parsedContent.toString();
	}

	private StringBuilder makeLinksRelative(StringBuilder parsedContent) {
		// Remove protocol, domain and /access/content folders from all links.
		Pattern target = Pattern.compile("(?:https?:)?(?://[-a-z0-9.]+(?::[0-9]+)?)?/access/content", Pattern.CASE_INSENSITIVE);
		Matcher matcher = target.matcher(parsedContent);
		return new StringBuilder(matcher.replaceAll(""));
	}

	private StringBuilder convertSlashToHyphen(StringBuilder parsedContent, Map<String, Boolean> savedLinks) {
		// Check which links are to html files (need to use the unencoded href but not remove the encoding) and change 
		// the folder slashes for hyphens when they are.
		Pattern target = Pattern.compile("(?:href=)+(?:\"|')?(/group/[a-z0-9-]+/)([a-z0-9/%+._-]+)(?:\"|'| )?(?:[a-z0-9=\"'])*>(?:/group/[a-z0-9-]+/)([a-z0-9/%+._-]+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = target.matcher(parsedContent);

		try {
			while (matcher.find()) {
				String urlMatch = matcher.group(1) + matcher.group(2);
				urlMatch = URLDecoder.decode(urlMatch, "UTF-8");

				Collection<ContentResource> contentResources = selectedFilesToExport.values();
				for (ContentResource cr : contentResources) {
					if (cr.getId().equals(urlMatch)) {
						if (hasMarkUp(cr.getContentType())) {
							parsedContent.replace(matcher.start(2), matcher.end(2), matcher.group(2).replace('/', '-'));
							parsedContent.replace(matcher.start(3), matcher.end(3), matcher.group(3).replace('/', '-'));
							// Add the href link to the map to use when working out the correct Canvas substitution variable.
							savedLinks.put(matcher.group(2).replace('/', '-'), true);
						} else {
							savedLinks.put(matcher.group(2), false);
						}
					}
				}
			}
		} catch (IllegalStateException ise) {
			log.error("export-common-cartridge illegal state exception in convertSlashToHyphen for matcher: " + matcher.toString() + " and content: " + parsedContent + ise);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("export-common-cartridge unsupported encoding exception in convertSlashToHyphen" + uee);
		}
		return parsedContent;
	}

	private StringBuilder addCanvasFilePath(StringBuilder parsedContent, Map<String, Boolean> savedLinks) {
		// Add the correct Canvas substitution variable so that HTML files end up in Pages in Canvas and
		// everything else in the files area in Canvas.
		try {
			for (Map.Entry<String, Boolean> link : savedLinks.entrySet()) {
				String pat = "(href=)+(\"|')?(/group/" + siteId + "/)(" + link.getKey() + ")(\"|')?([ a-z0-9=\"'-]*)(>)";
				Pattern target = Pattern.compile(pat, Pattern.CASE_INSENSITIVE);
				Matcher matcher = target.matcher(parsedContent);
				if (matcher.find()) {
					if (link.getValue()) {
						// Link is to HTML content which will be in the wiki_content directory (Pages).
						// To get links between HTML pages working in href links in Canvas the underscore has to be replaced with hyphen,
						// and spaces also have to be replaced with a hyphen.  The file ending (.html) also has to be removed.
						// Also, the filename (including the bit converted from the file path to the filename with hyphens has to be lower case.
						String filename = link.getKey();
						int periodPosition = filename.indexOf('.');
						if (periodPosition > -1) {
							filename = filename.substring(0, periodPosition);
						}
						// Change any encoding characters to hyphens in the href as Canvas requires this for HTML files.
						filename = URLDecoder.decode(filename, "UTF-8");
						filename = filename.replace(' ', '-').replace('_', '-').toLowerCase();
						parsedContent = new StringBuilder(matcher.replaceAll(matcher.group(1) + matcher.group(2) + matcher.group(3) +
								"%24WIKI_REFERENCE%24/pages/" + filename + matcher.group(5) + matcher.group(6) + matcher.group(7)));
					} else {
						// Otherwise the link is to a file in the Files area.
						parsedContent = new StringBuilder(matcher.replaceAll(matcher.group(1) + matcher.group(2) + matcher.group(3) +
								"%24IMS-CC-FILEBASE%24/" + matcher.group(4) + matcher.group(5) + matcher.group(6) + matcher.group(7)));
					}
				}
			}
		} catch (IllegalStateException ise) {
			log.error("export-common-cartridge illegal state exception in addCanvasFilePath, content: " + parsedContent + ise);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("export-common-cartridge unsupported encoding exception in addCanvasFilePath" + uee);
		}
		return parsedContent;
	}

	private StringBuilder removeWLPath(StringBuilder parsedContent) {
		// Remove the /group/siteid from the links.
		Pattern targetGroupSiteId = Pattern.compile("/group/" + siteId + "/", Pattern.CASE_INSENSITIVE);
		Matcher matcherGroupSiteId = targetGroupSiteId.matcher(parsedContent);
		return new StringBuilder(matcherGroupSiteId.replaceAll(""));
	}

	private StringBuilder removeUrlEncoding(StringBuilder parsedContent) {
		//Remove any encoding characters from the link text only (not the href)
		Pattern targetEncoding = Pattern.compile("(?:>)[a-z0-9%/._-]+[^</a>]", Pattern.CASE_INSENSITIVE);
		Matcher matcherEncoding = targetEncoding.matcher(parsedContent);
		int subsequence = 1;

		try {
			while (matcherEncoding.find(subsequence)) {
				parsedContent.replace(matcherEncoding.start(0), matcherEncoding.end(0), URLDecoder.decode(matcherEncoding.group(0), "UTF-8"));
				subsequence++;
			}
		} catch (IllegalStateException ise) {
			log.error("export-common-cartridge illegal state exception in removeUrlEncoding, content: " + parsedContent + ise);
		} catch (IllegalArgumentException iae) {
			log.error("export-common-cartridge unsupported encoding exception in removeUrlEncoding for content: " + parsedContent + iae);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("export-common-cartridge unsupported encoding exception in removeUrlEncoding" + uee);
		}
		return parsedContent;
	}

	// return base directory of file, including trailing /
	// "" if it is in home directory
	public String getParent(String s) {
		int i = s.lastIndexOf("/");
		if (i < 0) {
			return "";
		} else {
			return s.substring(0, i + 1);
		}
	}

	public String removeDotDot(String s) {
		int loopCount = 0;
		while (true) {
			int i = s.indexOf("/../");
			// prevent infinite loop
			if (i < 1 || ++loopCount > 999)
				return s;
			int j = s.lastIndexOf("/", i-1);
			if (j < 0)
				j = 0;
			else
				j = j + 1;
			s = s.substring(0, j) + s.substring(i+4);
		}
	}

	// the error messages are a problem. They won't show until the next page display
	// however errrors at this level are unusual, and we interrupt the download, so the
	// user should never see an incomplete one. Most common errors have to do with
	// problems converting for CC format. Those go into a log file that's included in
	// the ZIP, so the user will see those errors (if he knows the look)
	public static void setErrMessage(String s) {
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		if (toolSession == null) {
			log.error("Error in export-common-cartridge: CCExport " + s);
			return;
		}
		List<String> errors = (List<String>)toolSession.getAttribute("export-common-cartridge.errors");
		if (errors == null)
			errors = new ArrayList<String>();
		errors.add(s);
		toolSession.setAttribute("export-common-cartridge.errors", errors);
	}

}