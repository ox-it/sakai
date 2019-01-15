package org.sakaiproject.exporter.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
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

import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

public class CCExport {

	private static Logger log = LoggerFactory.getLogger(CCExport.class);
	private File root;

	private HttpServletResponse response;
	private String siteId;
	private Site site = null;
	private List<String> selectedFolderIds;
	private List<String> selectedFiles;
	private Map<String, ContentResource> filesToExport = new HashMap<>();

	private ContentHostingService contentService;
	private AssignmentService assignmentService;
	private MessageForumsForumManager messageForumsForumManager;
	private MessageForumsMessageManager messageForumsMessageManager;
	private ForumsExport forumsExport;
	private AssignmentsExport assignmentsExport;
	private ExportUtil exportUtil;

	private static final String CANVAS_ROOT_ACCOUNT_ID = "5kBHQyhv7XPtbq84g8Y4KYbyHmrS7odURR6NriBv";

	public CCExport(String siteId, ContentHostingService contentHostingService) throws IdUnusedException {
		this.siteId = siteId;
		this.contentService = contentHostingService;
		site = SiteService.getSite(siteId);
		this.assignmentService = ComponentManager.get(AssignmentService.class);
		this.messageForumsForumManager = ComponentManager.get(MessageForumsForumManager.class);
		this.messageForumsMessageManager = ComponentManager.get(MessageForumsMessageManager.class);

		exportUtil = new ExportUtil(siteId, contentHostingService);

		forumsExport = new ForumsExport(exportUtil, messageForumsForumManager, messageForumsMessageManager);
		assignmentsExport = new AssignmentsExport(assignmentService, exportUtil);
	}

	// Required for the tests.
	protected void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	// Export specific files chosen in the Resources tool. 
	public void doExport(List<String> selectedFolderIds, List<String> selectedFiles, HttpServletResponse httpServletResponse) {
		response = httpServletResponse;

		this.selectedFolderIds = selectedFolderIds;
		this.selectedFiles = selectedFiles;

		if (!startExport())
			return;
		if (!findSelectedFiles())
			return;

		createExportZip();
	}

	// Export all site content.
	public void doExport(HttpServletResponse httpServletResponse) {
		response = httpServletResponse;

		if (!startExport())
			return;
		if (!findAllFiles())
			return;
		if (!findForums())
			return;
		if (!findAssignments())
			return;
		createExportZip();
	}

	private void createExportZip() {
		try (OutputStream htmlOut = response.getOutputStream(); ZipPrintStream out = new ZipPrintStream(htmlOut)) {
			response.setHeader("Content-disposition", "inline; filename=sakai-export.imscc");
			response.setContentType("application/zip");

			outputSelectedFiles(out);
			forumsExport.outputAllForums(out);
			assignmentsExport.outputAssignments(out);
			outputCourseSettingsFiles(out);
			outputManifest(out);
		} catch (IOException ioe) {
			log.error("export-common-cartridge error getting output/zip stream, doExport", ioe);
			setErrMessage("I/O error getting output stream for export file: " + ioe.getMessage());
		} catch (PermissionException pe) {
			log.error("export-common-cartridge permission error finding selected files", pe);
			setErrMessage("Error creating export zip: " + pe.getMessage());
		} catch (IdUnusedException ide) {
			log.error("export-common-cartridge ID unused  error finding selected files", ide);
			setErrMessage("Error creating export zip: " + ide.getMessage());
		} catch (TypeException te) {
			log.error("export-common-cartridge type error finding selected files", te);
			setErrMessage("Error creating export zip: " + te.getMessage());
		} catch(ServerOverloadException soe) {
			log.error("export-common-cartridge type error finding selected files", soe);
			setErrMessage("Error creating export zip: " + soe.getMessage());
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
		} catch (IOException ioe) {
			log.error("export-common-cartridge error creating output files", ioe);
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
			log.error("export-common-cartridge permission error finding selected files", pe);
			setErrMessage("Error finding files selected for export: " + pe.getMessage());
			return false;
		} catch (IdUnusedException ide) {
			log.error("export-common-cartridge ID unused  error finding selected files", ide);
			setErrMessage("Error finding files selected for export: " + ide.getMessage());
			return false;
		} catch (TypeException te) {
			log.error("export-common-cartridge type error finding selected files", te);
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
		return forumsExport.getForumsInSite(siteId);
	}

	private boolean findAssignments() {
		return assignmentsExport.getAssignmentsInSite(siteId);
	}

	public boolean outputSelectedFiles(ZipPrintStream out) {
		String dashedFilename;
		Collection<ContentResource> contentResources = filesToExport.values();
		for (ContentResource contentResource : contentResources) {
			ZipEntry zipEntry;

			// Find the file name without the file path.
			String filename = exportUtil.getFileName(contentResource);

			// Files that are HTML or XHTML should be put in the pages area in Canvas so that they can be edited,
			// to do this add to the wiki-content area and put the appropriate entry in the manifest file.
			// Files in Pages must be in a flat file structure regardless of where they are in resources.
			// Files in web_resources will end up in Files and cannot be edited.  They can be in the same file structure as resources.

			// Change the filename to be the folder structure for HTML files, but replace slash with dash, this is so that duplicate file names are not lost.
			dashedFilename = exportUtil.getFilePath(contentResource.getId()).replace('/', '-') + filename;
			if (exportUtil.hasMarkUp(contentResource.getContentType())) {
				zipEntry = new ZipEntry("wiki_content/" + dashedFilename);
			} else {
				zipEntry = new ZipEntry("web_resources/" + exportUtil.getFilePath(contentResource.getId()) + filename);
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
				log.error("export-common-cartridge server overload error adding selected files", soe);
				setErrMessage("Error outputting selected files: " + soe.getMessage());
				return false;
			} catch (IOException ioe) {
				log.error("export-common-cartridge I/O error adding selected files", ioe);
				setErrMessage("Error outputting selected files: " + ioe.getMessage());
				return false;
			}
		}
		return true;
	}

	public boolean outputCourseSettingsFiles(ZipPrintStream out) {
		try {
			ZipEntry zipEntry;
			zipEntry = new ZipEntry("course_settings/course_settings.xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<course identifier=\"SubstitutedByCanvas\" xmlns=\"http://canvas.instructure.com/xsd/cccv1p0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://canvas.instructure.com/xsd/cccv1p0 https://canvas.instructure.com/xsd/cccv1p0.xsd\">");
			out.println("  <title>" + site.getTitle() + "</title>");
			out.println("  <course_code>" + site.getTitle() + "</course_code>");
			out.println("  <start_at/>");
			out.println("  <conclude_at/>");
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
			out.println("  <restrict_enrollments_to_course_dates>false</restrict_enrollments_to_course_dates>");
			out.println("  <grading_standard_enabled>false</grading_standard_enabled>");
			out.println("  <storage_quota>1048576000</storage_quota>");
			out.println("  <root_account_uuid>" + CANVAS_ROOT_ACCOUNT_ID + "</root_account_uuid>");
			out.println("</course>");
			out.closeEntry();

			zipEntry = new ZipEntry("course_settings/assignment_groups.xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<assignmentGroups xmlns=\"http://canvas.instructure.com/xsd/cccv1p0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://canvas.instructure.com/xsd/cccv1p0 https://canvas.instructure.com/xsd/cccv1p0.xsd\">");
			out.println("<assignmentGroup identifier=\"" + ExportUtil.getResourceId() + "\">");
			out.println("    <title>Assignments</title>");
			out.println("    <position>1</position>");
			out.println("    <group_weight>0.0</group_weight>");
			out.println("  </assignmentGroup>");
			out.println("</assignmentGroups>");
			out.closeEntry();

			zipEntry = new ZipEntry("course_settings/files_meta.xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<fileMeta xmlns=\"http://canvas.instructure.com/xsd/cccv1p0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://canvas.instructure.com/xsd/cccv1p0 https://canvas.instructure.com/xsd/cccv1p0.xsd\">");
			out.println("  <files>");
			for (String fileID: filesToExport.keySet()) {
				out.println("    <file identifier=\"" + fileID + "\">");
				out.println("      <locked>true</locked>");
				out.println("    </file>");
			}
			out.println("  </files>");
			out.println("</fileMeta>");
			out.closeEntry();

			zipEntry = new ZipEntry("course_settings/media_tracks.xml");
			out.putNextEntry(zipEntry);
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<media_tracks xmlns=\"http://canvas.instructure.com/xsd/cccv1p0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://canvas.instructure.com/xsd/cccv1p0 https://canvas.instructure.com/xsd/cccv1p0.xsd\">");
			out.println("</media_tracks>");
			out.closeEntry();

			zipEntry = new ZipEntry("course_settings/canvas_export.txt");
			out.putNextEntry(zipEntry);
			out.println("Required so that HTML pages import into Canvas Pages area, not files.");
			out.closeEntry();

		} catch (IOException ioe) {
			log.error("export-common-cartridge IO exception outputing course settings files", ioe);
			setErrMessage("Error outputing selected course files: " + ioe.getMessage());
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
			out.println("  <resources>");
			out.println("    <resource identifier=\"i56e1997d322b372fd7e6015b2f538aad\" type=\"associatedcontent/imscc_xmlv1p3/learning-application-resource\" href=\"course_settings/canvas_export.txt\">");
			out.println("      <file href=\"course_settings/course_settings.xml\"/>");
			out.println("      <file href=\"course_settings/assignment_groups.xml\"/>");
			out.println("      <file href=\"course_settings/files_meta.xml\"/>");
			out.println("      <file href=\"course_settings/media_tracks.xml\"/>");
			out.println("      <file href=\"course_settings/canvas_export.txt\"/>");
			out.println("    </resource>");

			Collection<ContentResource> contentResources = filesToExport.values();
			for (ContentResource contentResource : contentResources) {
				String contentResourceId = contentResource.getId();
				String filepath = exportUtil.getFilePath(contentResourceId);
				String filename = exportUtil.getFileName(contentResource);

				// See the comments above in method outputSelectedFiles.
				if (exportUtil.hasMarkUp(contentResource.getContentType())) {
					out.println("    <resource href=\"wiki_content/" + filepath.replace('/', '-') + filename + "\" identifier=\"" + contentResourceId + "\" type=\"webcontent\">");
					out.println("      <file href=\"wiki_content/" + filepath.replace('/', '-') + filename + "\"/>");
				} else {
					out.println("    <resource href=\"web_resources/" + filepath + filename + "\" identifier=\"" + contentResource.getId() + "\" type=\"webcontent\">");
					out.println("      <file href=\"web_resources/" + filepath + filename + "\"/>");
				}
				out.println("    </resource>");
			}

			// Create manifest entries for forum items (if they exist) and any forum attachments.
			forumsExport.ouputForumsToManifest(out);

			// Create manifest entries for assignment items (if they exist) and any assignment attachments.
			assignmentsExport.outputAssignmentsToManifest(out);

			out.println("  </resources>");
			out.println("</manifest>");

			out.closeEntry();
		} catch (IOException ioe) {
			log.error("export-common-cartridge IO exception outputing manifest file", ioe);
			setErrMessage("Error outputting manifest: " + ioe.getMessage());
			return false;
		} catch (TypeException te) {
			log.error("export-common-cartridge type error outputting assignments", te);
			setErrMessage("Error outputting forum attachments: " + te.getMessage());
			return false;
		} catch (PermissionException pe) {
			log.error("export-common-cartridge permission error outputting assignments", pe);
			setErrMessage("Error outputting forum attachments: " + pe.getMessage());
			return false;
		} catch (IdUnusedException ide) {
			log.error("export-common-cartridge ID unuse error outputting assignments", ide);
			setErrMessage("Error outputting forum attachments: " + ide.getMessage());
			return false;
		}
		return true;
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
			log.error("Error in export-common-cartridge: CCExport ", s);
			return;
		}
		List<String> errors = (List<String>)toolSession.getAttribute("export-common-cartridge.errors");
		if (errors == null)
			errors = new ArrayList<String>();
		errors.add(s);
		toolSession.setAttribute("export-common-cartridge.errors", errors);
	}

}