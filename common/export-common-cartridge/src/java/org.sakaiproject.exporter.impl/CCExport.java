package org.sakaiproject.exporter.impl;

import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
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

	boolean doBank = false;

	class Resource {
		String sakaiId;
		String resourceId;
		String location;
		String use;
		String title;
		String contentType;
		String url;
		boolean isbank;
		String filename;
		boolean isHtml;
		Set<String> dependencies;
		private ContentResource cr;
		void  setContentResource(ContentResource cr) {this.cr = cr;} 
		ContentResource getContentResource() {return cr;}
	
	}

	// map of all file resource to be included in cartridge
	Map<String, Resource> fileMap = new HashMap<String, Resource>();
	Map<Long, Resource> poolMap = new HashMap<Long, Resource>();
	Resource samigoBank = null;

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
			outputModuleSettingsFiles(out);
			
			
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

	String getResourceId () {
       	return "res" + (nextid++);
    }

	String getResourceIdPeek () {
       	return "res" + nextid;
    }

	public void setIntendeduse (String sakaiId, String intendeduse) {
		Resource ref = fileMap.get(sakaiId);
		if (ref == null)
			return;
		ref.use = intendeduse;
	}

	String getLocation(String sakaiId) {
		Resource ref = fileMap.get(sakaiId);
		if (ref == null)
			return null;
		return ref.location;
	}

	public Resource addSelectedFile(ContentResource contentResource, String location) {
		return addSelectedFile(contentResource, location, null);
	}

	public Resource addSelectedFile(ContentResource contentResource, String location, String use) {
		Resource res = new Resource();
		res.sakaiId = contentResource.getId();
		res.resourceId = getResourceId();
		res.location = location;
		res.dependencies = new HashSet<String>();
		res.use = use;
		res.contentType = contentResource.getContentType();
		res.isbank = false;
		res.cr = contentResource;

		// Work out the filename, filetype and see if this is HTML. If so, we need to scan it.
		String filepath = contentResource.getId();
		int lastdot = filepath.lastIndexOf(".");
		int lastslash = filepath.lastIndexOf("/");
		String extension = "";
		if (lastdot >= 0 && lastdot > lastslash)
			extension = filepath.substring(lastdot + 1);

		// update the resource map entry with the filename.
		res.filename = filepath.substring(lastslash + 1);

		String mimeType = null;
		mimeType = contentResource.getContentType();
		boolean isHtml = false;
		if (mimeType != null && (mimeType.startsWith("http") || mimeType.equals("")))
			mimeType = null;
		if (mimeType != null && (mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml"))
				|| mimeType == null && (extension.equals("html") || extension.equals("htm"))) {
			isHtml = true;
		}
		res.isHtml = isHtml;
		fileMap.put(contentResource.getId(), res);
		return res;
	}

	private boolean findSelectedFiles() {
		try {
			if (selectedFolderIds.size() > 0) {
				for (String selectedFolder : selectedFolderIds) {
					List<ContentResource> folderContents = contentService.getAllResources(selectedFolder);
					for (ContentResource folderFile: folderContents) {
						addSelectedFile(folderFile, folderFile.getId());
					}
				}
			}
			for (String selectedFile : selectedFiles) {
				addSelectedFile(contentService.getResource(selectedFile), selectedFile);
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
		for (Map.Entry<String, Resource> entry : fileMap.entrySet()) {
			Resource resource = entry.getValue();

			ZipEntry zipEntry;

			if (resource.isHtml) {
				zipEntry = new ZipEntry("wiki_content/" + resource.filename);
			} else {
				zipEntry = new ZipEntry("web_resources/" + resource.filename);
			}

			zipEntry.setSize(resource.getContentResource().getContentLength());

			try (InputStream contentStream = resource.getContentResource().streamContent()) {

				out.putNextEntry(zipEntry);
				if (resource.isHtml) {
					// treat html separately. Need to convert urls to relative
					String content = null;

					content = new String(resource.getContentResource().getContent());

					// relFixup needs work
					content = relFixup(content, entry.getValue());

					//  add in HTML header and footer
					out.println("<html>");
					out.println("<head>");
					out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
					out.println("  <title>" + StringEscapeUtils.escapeXml(resource.filename.substring(0, resource.filename.indexOf('.')))+ "</title>");
					out.println("  <meta name=\"identifier\" content=\"" + entry.getValue().resourceId + "\"/>");

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

	public String removeDotDot(String s) {
		while (true) {
			int i = s.indexOf("/../");
			if (i < 1)
				return s;
			int j = s.lastIndexOf("/", i-1);
			if (j < 0)
				j = 0;
			else
			j = j + 1;
			s = s.substring(0, j) + s.substring(i+4);
		}
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
			out.println("some Canvas rubbish");
			
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
		} catch (Exception impossible) {
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
	
			out.println("  <organizations>");
			out.println("    <organization identifier=\"org_1\" structure=\"rooted-hierarchy\">");
			out.println("      <item identifier=\"LearningModules\"><item identifier=\"tocomplete\"><title>First module</title></item></item>");
			out.println("    </organization>");
			out.println("  </organizations>");
	
			String qtiid = null;
			String bankid = null;
			String topicid = null;
			String linkid = null;
			String usestr = "";
	
			qtiid = "imsqti_xmlv1p2/imscc_xmlv1p3/assessment";
			bankid = "imsqti_xmlv1p2/imscc_xmlv1p3/question-bank";
			topicid = "imsdt_xmlv1p3";
			linkid = "imswl_xmlv1p3";
			usestr = " intendeduse=\"assignment\"";
	
			out.println("  <resources>");
			out.println("    <resource identifier=\"i56e1997d322b372fd7e6015b2f538aad\" type=\"associatedcontent/imscc_xmlv1p1/learning-application-resource\" href=\"course_settings/canvas_export.txt\">");
			out.println("      <file href=\"course_settings/course_settings.xml\"/>");
			out.println("      <file href=\"course_settings/canvas_export.txt\"/>");
			out.println("      <file href=\"course_settings/module_meta.xml\"/>");
			out.println("      <file href=\"course_settings/assignment_groups.xml\"/>");
			out.println("      <file href=\"course_settings/files_meta.xml\"/>");
			out.println("      <file href=\"course_settings/media_tracks.xml\"/>");
			out.println("      <file href=\"course_settings/canvas_export.txt\"/>");
			out.println("    </resource>");
			for (Map.Entry<String, Resource> entry: fileMap.entrySet()) {
				String use = "";
				if (entry.getValue().use != null) {
					use = " intendeduse=\"" + entry.getValue().use + "\"";
				}
				String type = "webcontent";
				if (entry.getValue().isHtml) {
					out.println("    <resource identifier=\"" + entry.getValue().resourceId + "\" type=\"webcontent\"" + use + " href=\"wiki_content/" + StringEscapeUtils.escapeXml(entry.getValue().filename) + "\">");
					out.println("      <file href=\"wiki_content/" + StringEscapeUtils.escapeXml(entry.getValue().filename) + "\"/>");
				} else {
					out.println("    <resource identifier=\"" + entry.getValue().resourceId + "\" type=\"webcontent\"" + use + " href=\"web_resources/" + StringEscapeUtils.escapeXml(entry.getValue().filename) + "\">");
					out.println("      <file href=\"web_resources/" + StringEscapeUtils.escapeXml(entry.getValue().filename) + "\"/>");
				}
				for (String d: entry.getValue().dependencies) {
					out.println("      <dependency identifierref=\"" + d + "\"/>");
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

	// turns the links into relative links
	// fixups will get a list of offsets where fixups were done, for loader to reconstitute HTML
	public String relFixup (String s, Resource resource, StringBuilder fixups) {
		StringBuilder ret = new StringBuilder();
		String sakaiIdBase = "/group/" + siteId;
		// I'm matching against /access/content/group not /access/content/group/SITEID, because SITEID can in some installations
		// be user chosen. In that case there could be escaped characters, and the escaping in HTML URL's isn't unique. 
		Pattern target = Pattern.compile("(?:https?:)?(?://[-a-zA-Z0-9.]+(?::[0-9]+)?)?/access/content(/group/)|http://lessonbuilder.sakaiproject.org/", Pattern. CASE_INSENSITIVE);
		Matcher matcher = target.matcher(s);
		// technically / isn't allowed in an unquoted attribute, but sometimes people
		// use sloppy HTML	
		Pattern wordend = Pattern.compile("[^-a-zA-Z0-9._:/]");
		int index = 0;
		while (true) {
			if (!matcher.find()) {
				ret.append(s.substring(index));
				break;
			}
			String sakaiId = null;
			int start = matcher.start();
			if (matcher.start(1) >= 0) { // matched /access/content...
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
			// do the mapping. resource.location is a relative URL of the page we're looking at
			// sakaiid is the URL of the object, starting /group/
			String base = getParent(resource.location);
			String thisref = sakaiId.substring(sakaiIdBase.length()+1);
			String relative = relativize(thisref, base);
			ret.append(s.substring(index, start));
			// we're now at start of URL. save it for fixup list
			if (fixups != null) {
				if (fixups.length() > 0)
					fixups.append(",");
				fixups.append("" + ret.length());
			}
			// and now add the new relative URL
			ret.append(relative.toString());
			index = sakaiend;  // start here next time
			} else { // matched http://lessonbuilder.sakaiproject.org/
				int last = matcher.end(); // should be start of an integer
				int endnum = s.length();  // end of the integer
				for (int i = last; i < s.length(); i++) {
					if ("0123456789".indexOf(s.charAt(i)) < 0) {
						endnum = i;
						break;
					}
				}
				String numString = s.substring(last, endnum);
				if (numString.length() >= 1) {
					Long itemId = new Long(numString);
					if (sakaiId.startsWith(sakaiIdBase)) {
						ret.append(s.substring(index, start));
						String base = getParent(resource.location);
						String thisref = sakaiId.substring(sakaiIdBase.length()+1);
						String relative = relativize(thisref, base);
						// we're now at start of URL. save it for fixup list
						if (fixups != null) {
							if (fixups.length() > 0)
							fixups.append(",");
							fixups.append("" + ret.length());
						}
						// and now add the new relative URL
						ret.append(relative);
						if (s.charAt(endnum) == '/')
							endnum++;
						index = endnum;
					}
				}
			}
			if (sakaiId != null) {
				Resource r = fileMap.get(sakaiId);
				if (r != null) {
					resource.dependencies.add(r.resourceId);
				}
			}
		}
		if (fixups != null && fixups.length() > 0) {
			return ("<!--fixups:" + fixups.toString() + "-->" + ret.toString());
		}
		return ret.toString();
	}

	public String relFixup (String s, Resource resource) {
		return relFixup(s, resource, null);
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

	// return relative path to target from base
	// base is assumed to be "" or ends in /
	public String relativize(String target, String base) {
	if (base.equals(""))
	    return target;
	if (target.startsWith(base))
	    return target.substring(base.length());
	else {
		// get parent directory of base directory.
		// base directory ends in /
		int i = base.lastIndexOf("/", base.length()-2);
		if (i < 0) {
			base = "";
		} else {
			base = base.substring(0, i + 1); // include /
		}
		return "../" + relativize(target, base);
	}
	}
}