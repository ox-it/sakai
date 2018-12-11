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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.util.FormattedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

public class CCExport {

	private static Logger log = LoggerFactory.getLogger(CCExport.class);
	private File root;

	private HttpServletResponse response;
	private File errFile = null;
	private PrintStream errStream = null;
	private String siteId;
	private Site site = null;
	private List<String> selectedFolderIds;
	private List<String> selectedFiles;
	private Map<String, ContentResource> filesToExport = new HashMap<>();
	private Map<String, Resource> forumMap = new HashMap<>();
	private Map<String, Resource> fileMap = new HashMap<>();

	private ContentHostingService contentService;
	private MessageForumsForumManager messageForumsForumManager;
	private MessageForumsMessageManager messageForumsMessageManager;
	private ForumsExport forumsExport;

	private ExportUtil exportUtil;

	public CCExport(String siteId, ContentHostingService contentHostingService) throws IdUnusedException {
		this.siteId = siteId;
		this.contentService = contentHostingService;
		site = SiteService.getSite(siteId);

		this.messageForumsForumManager = ComponentManager.get(MessageForumsForumManager.class);
		this.messageForumsMessageManager = ComponentManager.get(MessageForumsMessageManager.class);

		exportUtil = new ExportUtil(siteId);

		forumsExport = new ForumsExport(exportUtil, messageForumsForumManager, messageForumsMessageManager);


	}

	// Required for the tests.
	protected void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	public void doExport(List<String> selectedFolderIds, List<String> selectedFiles, HttpServletResponse httpServletResponse) {
		response = httpServletResponse;
		// Export specific files chosen in the Resources tool. 
		this.selectedFolderIds = selectedFolderIds;
		this.selectedFiles = selectedFiles;

		if (!startExport())
			return;
		if (!findSelectedFiles())
			return;

		createExportZip();
	}

	public void doExport(HttpServletResponse httpServletResponse) {
		response = httpServletResponse;

		// Export all site content.
		if (!startExport())
			return;
		if (!findAllFiles())
			return;
		if (!findForums())
			return;
		
		createExportZip();
	}

	private void createExportZip() {
		try (OutputStream htmlOut = response.getOutputStream(); ZipPrintStream out = new ZipPrintStream(htmlOut)) {
			response.setHeader("Content-disposition", "inline; filename=sakai-export.imscc");
			response.setContentType("application/zip");

			outputSelectedFiles(out);
			outputAllForums(out);
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
			// Add any files in the chosen folders to the files to be in the export.
			if (selectedFolderIds.size() > 0) {
				for (String selectedFolder : selectedFolderIds) {
					List<ContentResource> folderContents = contentService.getAllResources(selectedFolder);
					for (ContentResource folderFile: folderContents) {
						filesToExport.put(folderFile.getId(), folderFile);
					}
				}
			}

			// Add any chosen files to the files to be in the export.
			for (String selectedFile : selectedFiles) {
				ContentResource contentFile = contentService.getResource(selectedFile);
				filesToExport.put(contentFile.getId(), contentFile);
			}

			filesToExport = exportUtil.removeReadingLists(filesToExport);

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

	private boolean findAllFiles() {
		// Finds all content in the Resources tool and adds to the filesToExport map.
		List<ContentResource> allResources = contentService.getAllResources(contentService.getSiteCollection(siteId));
		
		for (ContentResource contentResource: allResources) {
			filesToExport.put(contentResource.getId(), contentResource);
		}

		filesToExport = exportUtil.removeReadingLists(filesToExport);
		return true;
	}

	private boolean findForums() {
		// Find the topics (ForumItems) in the forum.
		forumMap = forumsExport.getForumsInSite(siteId);
		if (!forumMap.isEmpty()) {
			findForumAttachments();
		}
		return true;
	}

	private boolean findForumAttachments() {
		// Find the attachments on forums and topics.
		fileMap = forumsExport.getAttachmentsInSite();
		return true;
	}

	public boolean outputSelectedFiles(ZipPrintStream out) {
		String dashedFilename;
		Collection<ContentResource> contentResources = filesToExport.values();
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
			dashedFilename = exportUtil.getFilePath(contentResource.getId(), false).replace('/', '-') + filename;
			if (exportUtil.hasMarkUp(contentResource.getContentType())) {
				zipEntry = new ZipEntry("wiki_content/" + dashedFilename);
			} else {
				zipEntry = new ZipEntry("web_resources/" + exportUtil.getFilePath(contentResource.getId(), false) + filename);
			}

			zipEntry.setSize(contentResource.getContentLength());

			try (InputStream contentStream = contentResource.streamContent()) {

				out.putNextEntry(zipEntry);
				if (exportUtil.hasMarkUp(contentResource.getContentType())) {
					// treat html/xhtml separately. Need to convert urls to relative urls.
					String content = null;

					content = new String(contentResource.getContent());
					content = exportUtil.linkFixup(content, filesToExport);

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

	public boolean outputAllForums(ZipPrintStream out) {
		// Output forum topics into the cc-objects directory.
		try {
			for (Map.Entry<String, Resource> entry: forumMap.entrySet()) {
				Topic topic = messageForumsForumManager.getTopicByIdWithAttachments(Long.parseLong(entry.getValue().getSakaiId()));
				String text = topic.getExtendedDescription();  // html
				if (text == null || text.trim().equals("")) {
					text = topic.getShortDescription();
					if (text != null) {
						text = FormattedText.convertPlaintextToFormattedText(text);
					}
				}

				ZipEntry zipEntry = new ZipEntry(entry.getValue().location);
				out.putNextEntry(zipEntry);
				out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

				out.println("<topic xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3\"");
				out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p3/imsdt_v1p3 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imsdt_v1p3.xsd\">");

				out.println("  <title>" + entry.getValue().getTitle() + "</title>");
				out.println("  <text texttype=\"text/html\"><div>" + text + "</div></text>");
				if (!entry.getValue().getDependencies().isEmpty()) {
					out.println("  <attachments>");
					for (String dependancy: entry.getValue().getDependencies()) {
						out.println("    <attachment href=\"" + fileMap.get(dependancy).getLocation() + "\"/>");
					}
					out.println("  </attachments>");
				}
				out.println("</topic>");
				out.closeEntry();
			}
		} catch (IOException ioe) {
			log.error("export-common-cartridge I/O error outputting forums" + ioe);
			setErrMessage("Error outputting forums: " + ioe.getMessage());
			return false;
		}

		// Output any forum attachments into the attachments directory.
		try {
			for (Map.Entry<String, Resource> entry : fileMap.entrySet()) {
				ZipEntry zipEntry = new ZipEntry(entry.getValue().location);

				String s = entry.getValue().getSakaiId();
				ContentResource contentResource = contentService.getResource(s);
				if (!exportUtil.isLink(contentResource)) {
					// Attachment is a file.
					try (InputStream contentStream = contentResource.streamContent()) {
						out.putNextEntry(zipEntry);
						IOUtils.copy(contentStream, out);
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
			}
		} catch (TypeException te) {
			log.error("export-common-cartridge type error outputting forum attachments" + te);
			setErrMessage("Error outputting forum attachments: " + te.getMessage());
			return false;
		} catch (PermissionException pe) {
			log.error("export-common-cartridge permission error outputting forum attachments" + pe);
			setErrMessage("Error outputting forum attachments: " + pe.getMessage());
			return false;
		} catch (IdUnusedException ide) {
			log.error("export-common-cartridge ID unuse error outputting forum attachments" + ide);
			 setErrMessage("Error outputting forum attachments: " + ide.getMessage());
			 return false;
		}
		return true;

	}

	public boolean outputCourseSettingsFiles(ZipPrintStream out) {
		try {
			ZipEntry zipEntry = new ZipEntry("course_settings/course_settings.xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<course identifier=\"i56e1997d322b372fd7e6015b2f538aad\" xmlns=\"http://canvas.instructure.com/xsd/cccv1p0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://canvas.instructure.com/xsd/cccv1p0 https://canvas.instructure.com/xsd/cccv1p0.xsd\">");
			out.println("  <title>" + site.getTitle() + "</title>");
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
		try {
			ZipEntry zipEntry = new ZipEntry("imsmanifest.xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<manifest identifier=\"i65d048afc30bea25ed17ce5063f901f6\"");
			out.println("xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imscp_v1p1\"");
			out.println("xmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p3/LOM/resource\"");
			out.println("xmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p3/LOM/manifest\"");
			out.println("xmlns:cpx=\"http://www.imsglobal.org/xsd/imsccv1p3/imscp_extensionv1p2\"");
			out.println(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			out.println(" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p3/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imscp_v1p2_v1p0.xsd"); 
			out.println("                      http://www.imsglobal.org/xsd/imsccv1p3/imscp_extensionv1p2 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_cpextensionv1p2_v1p0.xsd");
			out.println("                      http://ltsc.ieee.org/xsd/imsccv1p3/LOM/resource http://www.imsglobal.org/profile/cc/ccv1p3/LOM/ccv1p3_lomresource_v1p0.xsd");
			out.println("                      http://ltsc.ieee.org/xsd/imsccv1p3/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p3/LOM/ccv1p3_lommanifest_v1p0.xsd\">");
			out.println("  <metadata>");
			out.println("    <schema>IMS Common Cartridge</schema>");
			out.println("    <schemaversion>1.3.0</schemaversion>");
			out.println("    <lomimscc:lom>");
			out.println("      <lomimscc:general>");
			out.println("        <lomimscc:title>");
			out.println("          <lomimscc:string>" + StringEscapeUtils.escapeXml(site.getTitle()) + "</lomimscc:string>");
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

			// Modules not required.
//			out.println("  <organizations>");
//			out.println("    <organization identifier=\"org_1\" structure=\"rooted-hierarchy\">");
//			out.println("      <item identifier=\"LearningModules\"><item identifier=\"tocomplete\"><title>First module</title></item></item>");
//			out.println("    </organization>");
//			out.println("  </organizations>");

			out.println("  <resources>");

			out.println("    <resource identifier=\"i56e1997d322b372fd7e6015b2f538aad\" type=\"associatedcontent/imscc_xmlv1p3/learning-application-resource\">");
			out.println("      <file href=\"course_settings/course_settings.xml\"/>");
			// out.println("      <file href=\"course_settings/module_meta.xml\"/>");
			out.println("    </resource>");

			Collection<ContentResource> contentResources = filesToExport.values();
			for (ContentResource contentResource : contentResources) {
				String contentResourceId = contentResource.getId();
				String filepath = exportUtil.getFilePath(contentResourceId, true);
				String filename = getFileName(contentResource, true);

				// See the comments above in method outputSelectedFiles.
				if (exportUtil.hasMarkUp(contentResource.getContentType())) {
					out.println("    <resource href=\"wiki_content/" + filepath.replace('/', '-') + filename + "\" identifier=\"" + contentResourceId + "\" type=\"webcontent\">");
					out.println("      <file href=\"wiki_content/" + filepath.replace('/', '-') + filename + "\"/>");
				} else {
					out.println("    <resource href=\"web_resources/" + exportUtil.getFilePath(contentResourceId, false) + filename + "\" identifier=\"" + exportUtil.getResourceId() + "\" type=\"webcontent\">");
					out.println("      <file href=\"web_resources/" + exportUtil.getFilePath(contentResourceId, false) + filename + "\"/>");
				}
				out.println("    </resource>");
			}

			// Output forum attachments and forum items (if they exist) to the manifest.
			for (Map.Entry<String, Resource> forumAttachment: fileMap.entrySet()) {
				out.println("    <resource href=\"" + forumAttachment.getValue().getLocation() + "\" identifier=\"" + forumAttachment.getValue().getResourceId() + "\" type=\"webcontent\">");
				out.println("      <file href=\"" + forumAttachment.getValue().getLocation() + "\"/>");
				out.println("    </resource>");
			}
			
			for (Map.Entry<String, Resource> forumTopic: forumMap.entrySet()) {
				out.println("    <resource identifier=\"" + forumTopic.getValue().getResourceId() + "\" type=\"imsdt_xmlv1p3\">");
				out.println("      <file href=\"" + forumTopic.getValue().getLocation() + "\"/>");
				for (String dependency: forumTopic.getValue().getDependencies()) {
					out.println("      <dependency identifierref=\"" + dependency + "\"/>");
				}
				out.println("    </resource>");
			}

			// add error log at the very end
			String errId = exportUtil.getResourceId();
			out.println("    <resource identifier=\"" + errId + "\" type=\"webcontent\">");
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