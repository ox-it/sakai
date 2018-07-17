package org.sakaiproject.exporter.impl;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ComponentManager;
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
	private String rootPath;
	long nextid = 1;

	private HttpServletResponse response;
	private File errFile = null;
	private PrintStream errStream = null;
	private String siteId;
	private List<String> selectedFolderIds;
	private List<String> selectedFiles;
	private org.sakaiproject.content.api.ContentHostingService contentService;

	List<ContentResource> selectedFilesToExport = new ArrayList<>();

	public void doExport(String siteId, List<String> selectedFolderIds, List<String> selectedFiles, HttpServletResponse httpServletResponse) {
		this.siteId = siteId;
		this.selectedFolderIds = selectedFolderIds;
		this.selectedFiles = selectedFiles;
		response = httpServletResponse;
		contentService = ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);

		if (! startExport())
			return;
		if (! findSelectedFiles())
			return;

		try (OutputStream htmlOut = response.getOutputStream(); ZipPrintStream out = new ZipPrintStream(htmlOut)){
			response.setHeader("Content-disposition", "inline; filename=sakai-export.imscc");
			response.setContentType("application/zip");

			outputSelectedFiles (out);
			outputCourseSettingsFiles(out);
			// Module not required for now.
			// outputModuleSettingsFiles(out);
			outputManifest (out);
			out.close();
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
			if (selectedFolderIds.size() > 0) {
				for (String selectedFolder : selectedFolderIds) {
					List<ContentResource> folderContents = contentService.getAllResources(selectedFolder);
					for (ContentResource folderFile: folderContents) {
						selectedFilesToExport.add(folderFile);
					}
				}
			}
			for (String selectedFile : selectedFiles) {
				selectedFilesToExport.add(contentService.getResource(selectedFile));
			}
		} catch (PermissionException pe) {
			log.error("export-common-cartridge permission error finding selected files" + pe);
			setErrMessage("Error finding files selected for export: " + pe.getMessage());
			return false;
		} catch(IdUnusedException ide) {
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

	public boolean outputSelectedFiles (ZipPrintStream out) {
		for (ContentResource contentResource: selectedFilesToExport) {
			ZipEntry zipEntry;

			// Includes the file path and name, starting with /
			String filename = getFileName(contentResource, true);

			// Files that are HTML or XHTML should be put in the pages area in Canvas so that they can be edited,
			// to do this add to the wiki-content area and put the appropriate entry in the manifest file.
			// Files in Pages must be in a flat file structure regardless of where they are in resources.
			// Files in web_resources will end up in Files and cannot be edited.  They can be in the same file structure as resources.
			if (hasMarkUp(contentResource.getContentType())) {
				zipEntry = new ZipEntry("wiki_content/" + filename);
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
					content = relFixup(content, contentResource);

					//  add in HTML header and footer
					out.println("<html>");
					out.println("<head>");
					out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
					out.println("  <title>" + StringEscapeUtils.escapeXml(filename) + "</title>");
					out.println("  <meta name=\"identifier\" content=\"" + getResourceId() + "\"/>");

					out.println("  <meta name=\"editing_roles\" content=\"teachers\"/>");
					out.println("  <meta name=\"workflow_state\" content=\"unpublished\"/>");
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
			out.println(" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1 ");
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

			for (ContentResource contentResource: selectedFilesToExport) {
				String filename = getFileName(contentResource, true);

				// See the comments above in method outputSelectedFiles.
				if (hasMarkUp(contentResource.getContentType())) {
					out.println("    <resource identifier=\"" + getResourceId() + "\" type=\"webcontent\" href=\"wiki_content/" + filename + "\">");
					out.println("      <file href=\"wiki_content/" + filename + "\"/>");
				} else {
					out.println("    <resource identifier=\"" + getResourceId() + "\" type=\"webcontent\" href=\"web_resources/" + getFilePath(contentResource, false) + filename + "\">");
					out.println("      <file href=\"web_resources/" + getFilePath(contentResource, false) + filename + "\"/>");
				}
				out.println("    </resource>");
			}

			// add error log at the very end
			String errId = getResourceId();
			out.println(("    <resource href=\"cc-objects/export-errors\" identifier=\"" + errId + "\" type=\"webcontent\">\n      <file href=\"cc-objects/export-errors\"/>\n    </resource>"));
			out.println("  </resources>\n</manifest>");

			out.closeEntry();
			errStream.close();
			zipEntry = new ZipEntry("cc-objects/export-errors");
			out.putNextEntry(zipEntry);
			try (InputStream contentStream = new FileInputStream(errFile)){
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

	String getResourceId () {
		return "res" + (nextid++);
	}

	private boolean hasMarkUp(String mimeType){
		if (("text/html").equals(mimeType) || ("application/xhtml+xml").equals(mimeType) ||
				("application/xml").equals(mimeType) || ("text/xml").equals(mimeType)) {
			return true;
		}
		return false;
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
				log.error("export-common-cartridge UnsupportedEncodingException encoding file name: " + uee);
			}
		}
		return fileName;
	}

	// turns the links into relative links
	// fixups will get a list of offsets where fixups were done, for loader to reconstitute HTML
	public String relFixup (String s, ContentResource contentResource) {
		StringBuilder ret = new StringBuilder();
		String sakaiIdBase = "/group/" + siteId;
		// I'm matching against /access/content/group not /access/content/group/SITEID, because SITEID can in some installations
		// be user chosen. In that case there could be escaped characters, and the escaping in HTML URL's isn't unique. 
		Pattern target = Pattern.compile("(?:https?:)?(?://[-a-zA-Z0-9.]+(?::[0-9]+)?)?/access/content(/group/)", Pattern. CASE_INSENSITIVE);
		Matcher matcher = target.matcher(s);
		// technically / isn't allowed in an unquoted attribute, but sometimes people
		// use sloppy HTML
		Pattern wordend = Pattern.compile("[^-a-zA-Z0-9._:/]");
		int index = 0;
		int loopCount = 0;
		while (true) {
			// prevent infinite loop
			if (!matcher.find() || ++loopCount > 999) {
				ret.append(s.substring(index));
				break;
			}
			String sakaiId = null;
			int start = matcher.start();

			// make sure it's the right siteid. This approach will get it no matter
			// how the siteid is url encoded
			int startsite = matcher.end(1);
			int last = s.indexOf("/", startsite);
			if (last < 0)
				continue;
			String sitepart = null;
			try {
				sitepart = URLDecoder.decode(s.substring(startsite, last), "UTF-8");
			} catch (Exception e) {
				log.info("decode failed in CCExport " + e);
			}
			if (!siteId.equals(sitepart))
				continue;

			int sakaistart = matcher.start(1); //start of sakaiid, can't find end until we figure out quoting

			// need to find sakaiend. To do that we need to find the close quote
			int sakaiend = 0;
			char quote = s.charAt(start-1);
			if (quote == '\'' || quote == '"')  // quoted, this is easy
				sakaiend = s.indexOf(quote, sakaistart);
			else { // not quoted. find first char not legal in unquoted attribute
				Matcher wordendMatch = wordend.matcher(s);
				if (wordendMatch.find(sakaistart)) {
					sakaiend = wordendMatch.start();
				}
				else
					sakaiend = s.length();
			}
			try {
				sakaiId = removeDotDot(URLDecoder.decode(s.substring(sakaistart, sakaiend), "UTF-8"));
			} catch (Exception e) {
				log.info("Exception in CCExport URLDecoder " + e);
			}

			ret.append(s.substring(index, start));
			ret.append(sakaiId.substring(sakaiIdBase.length()+1));
			index = sakaiend;  // start here next time
		}
		return ret.toString();
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