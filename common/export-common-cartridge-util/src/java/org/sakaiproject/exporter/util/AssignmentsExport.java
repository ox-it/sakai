package org.sakaiproject.exporter.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.util.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;

public class AssignmentsExport {
	private AssignmentService assignmentService;
	private ExportUtil exportUtil;
	private Map<String, AssignmentResource> assignmentMap = new HashMap<>();
	private Map<String, Resource> attachmentMap = new HashMap<>();
	
	public AssignmentsExport(AssignmentService assignmentService, ExportUtil exportUtil) {
		this.assignmentService = assignmentService;
		this.exportUtil = exportUtil;
	}

	public boolean getAssignmentsInSite(String siteId) {
		AssignmentResource res;
		Iterator<Assignment> assignmentIterator = assignmentService.getAssignmentsForContext(siteId);
		while (assignmentIterator.hasNext()) {
			Assignment assignment = assignmentIterator.next();

			String deleted = assignment.getProperties().getProperty(ResourceProperties.PROP_ASSIGNMENT_DELETED);
			if ((deleted == null || "".equals(deleted)) && !assignment.getDraft()) {
				AssignmentContent content = assignment.getContent();
				List<Reference> attachments = content.getAttachments();

				res = new AssignmentResource();
				res.setResourceId(ExportUtil.getResourceId());
				res.setSakaiId(assignment.getId());
				res.setTitle(assignment.getTitle());
				int slash = res.getSakaiId().indexOf("/");
				res.setLocation("attachments/" + res.getSakaiId().substring(slash + 1) + "/assignmentpage.html");
				res.setDependencies(new HashSet<>());
				res.setInstructions(content.getInstructions());

				int typeOfGrade = content.getTypeOfGrade();

				// in Sakai only point-based goes to gradebook.
				// in CC we have gradeable with optional point value
				// I've chosen to specify a point value only for question with a point value
				switch (typeOfGrade) {
					case 3: res.setMaxPoints(content.getMaxGradePoint() / 10.0);
						res.setForPoints(true);
					case 2:
					case 4:
					case 5: res.setGradable(true);
				}

				int typeOfSubmission = content.getTypeOfSubmission();
				res.setAllowText(typeOfSubmission == 1 || typeOfSubmission == 3);
				res.setAllowFile(typeOfSubmission == 2 || typeOfSubmission == 3);

				for (Reference ref : attachments) {
					// We want the path of the assignment without the filename and then add the filename of the attachment.
					String lastAtom = ref.getId().substring(ref.getId().lastIndexOf("/") + 1);
					String sakaiId = ref.getId();
					Resource attachmentResource = exportUtil.getResource(sakaiId, "attachments/" + assignment.getId() + "/" + lastAtom, null);
					res.getDependencies().add(ref.getId());
					attachmentMap.put(ref.getId(), attachmentResource);
				}
				assignmentMap.put(res.getSakaiId(), res);
			}
		}
		return true;
	}

	// Assignments are not part of the core IMS common cartridge format (not a learning object) but can be added as an extension.
	// This requires one to put the instructions in an HTML file together with any relative references to attachments, this is put in the attachments folder.
	// Any assignment attachments that are not just URLs are also put in the correct attachments folder.
	public boolean outputAssignments(ZipPrintStream out) throws IOException, PermissionException, IdUnusedException, TypeException, ServerOverloadException {
		for (Map.Entry<String, AssignmentResource> entry: assignmentMap.entrySet()) {
			AssignmentResource assigmentResource = entry.getValue();
			String title = assigmentResource.getTitle();
			//String instructions = bean.relFixup(contents.instructions, resource);
			String instructions = assigmentResource.getInstructions(); // need to sort out links in html.
			Set<String> attachments = assigmentResource.getDependencies();

			ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());
			out.putNextEntry(zipEntry);
			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
			out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
			out.println("<body>");
			if (instructions != null && !instructions.trim().equals("")) {
				out.println("<div>" + instructions + "</div>");
			}
			if (!attachments.isEmpty()) {
				out.println(outputAttachments(assigmentResource, attachments, "../../"));
			}
			out.println("</body>");
			out.println("</html>");
			out.closeEntry();
		}

		for (Map.Entry<String, Resource> entry: attachmentMap.entrySet()) {
			String s = entry.getValue().getSakaiId();
			if (!exportUtil.isLink(s)) {
				// Attachment is a file, not a URL.
				ZipEntry zipEntry = new ZipEntry(entry.getValue().getLocation());
				ContentResource contentResource = exportUtil.getContentResource(s);
				try (InputStream contentStream = contentResource.streamContent()) {
					out.putNextEntry(zipEntry);
					IOUtils.copy(contentStream, out);
					out.closeEntry();
				}
			}
		}

		outputXMLFiles(out);
		return true;
	}

	// Output a chunk of HTML for any attachments for the assignment. If the attachment is in fact a URL then add this to the HTML chunk.
	private String outputAttachments(Resource assignmentResource, Set<String> attachments, String prefix) throws PermissionException, IdUnusedException, TypeException, ServerOverloadException {
		StringBuilder sb = new StringBuilder("<ul>");

		for (String sakaiId: attachments) {
			if (sakaiId.startsWith("/content/")) {
				sakaiId = sakaiId.substring("/content".length());
			}

			String url = null;
			// if it is a URL, need the URL rather than copying the file
			if (!sakaiId.startsWith("///")) {
				ContentResource res = exportUtil.getContentResource(sakaiId);
				if (exportUtil.isLink(res)) {
					url = new String(res.getContent());
				}
			}

			String location = attachmentMap.get(sakaiId).getLocation();
			int lastSlash = sakaiId.lastIndexOf("/");
			String lastAtom = sakaiId.substring(lastSlash + 1);

			// assumption here is that if the user entered a URL, it's in valid syntax
			// if we generate it from file location, it needs to be escaped
			if (url != null) {
				sb.append("<li><a href=\"" + url + "\">" + StringEscapeUtils.escapeHtml(url) + "</a></li>");
			} else {
				url = prefix + Validator.escapeUrl(location);  // else it's in the normal site content
				url = url.replaceAll("//", "/");
				sb.append("<li><a href=\"" + url + "\">" + StringEscapeUtils.escapeHtml(lastAtom) + "</a></li><br/>");
			}
			sb.append("</ul>");
		}

		return sb.toString();
	}

	// This creates an XML file for each assignment in the cc-objects directory.
	private boolean outputXMLFiles(ZipPrintStream out) throws IOException, PermissionException, IdUnusedException, TypeException, ServerOverloadException {

		for (Map.Entry<String, AssignmentResource> entry : assignmentMap.entrySet()) {
			AssignmentResource assignmentResource = entry.getValue();

			String title = assignmentResource.getTitle();

			// relFixup is for stuff that's in an actual HTML file, fixup for stuff in an XML descriptor
			//String instructions = bean.fixup(contents.instructions, resource);
			String instructions = assignmentResource.getInstructions(); // need to fixup html

			Set<String> attachments = assignmentResource.getDependencies();

			// the spec doesn't allow URLs in attachments, so if any of our attachments are URLs,
			// put the attachments as a list inside the instructions
			boolean useAttachments = (attachments.size() > 0);
			for (String sakaiId : attachments) {
				if (sakaiId.startsWith("/content/"))
					sakaiId = sakaiId.substring("/content".length());

				// if it is a URL, need the URL rather than copying the file
				if (exportUtil.isLink(exportUtil.getContentResource(sakaiId))) {
					useAttachments = false;
					break;
				}
			}
			ZipEntry zipEntry = new ZipEntry("cc-objects/" + assignmentResource.getResourceId() + ".xml");
			out.putNextEntry(zipEntry);

			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<assignment xmlns=\"http://www.imsglobal.org/xsd/imscc_extensions/assignment\"");
			out.println("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			out.println("     xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imscc_extensions/assignment http://www.imsglobal.org/profile/cc/cc_extensions/cc_extresource_assignmentv1p0_v1p0.xsd\"");
			out.println("  identifier=\"AssigmentId\">");

			if (title == null || title.length() == 0) {
				title = "Assignment";
			}
			out.println("<title>" + StringEscapeUtils.escapeXml(title) + "</title>");
			if (useAttachments || attachments.size() == 0) {
				out.println("  <text texttype=\"text/html\">" + instructions + "</text>");
			} else {
				out.println("  <text texttype=\"text/html\">" + StringEscapeUtils.escapeXml("<div>") + instructions + " " + StringEscapeUtils.escapeXml(outputAttachments(assignmentResource, attachments, "../") + "</div>") + "</text>");
			}
			// spec requires an instructor text even though we don't normally have one.
			out.println("<instructor_text texttype=\"text/plain\"></instructor_text>");
			out.println("<gradable" + (assignmentResource.isForPoints() ? (" points_possible=\"" + assignmentResource.getMaxPoints() + "\"") : "") + ">" +
					assignmentResource.isGradable() + "</gradable>");

			if (useAttachments) {
				out.println("  <attachments>");

				for (String sakaiId : attachments) {
					if (sakaiId.startsWith("/content/"))
						sakaiId = sakaiId.substring("/content".length());

					String URL = null;
					// if it is a URL, need the URL rather than copying the file
					ContentResource res = exportUtil.getContentResource(sakaiId);

					if (exportUtil.isLink(res)) {
						URL = new String(res.getContent());
					}

					String location = attachmentMap.get(sakaiId).getLocation();
					int lastSlash = sakaiId.lastIndexOf("/");
					String lastAtom = sakaiId.substring(lastSlash + 1);

					if (URL != null) {
						out.println("    <attachment href=\"" + StringEscapeUtils.escapeXml(URL) + "\" role=\"All\" />");
					} else {
						URL = "../" + location;  // else it's in the normal site content
						URL = URL.replaceAll("//", "/");
						out.println("    <attachment href=\"" + StringEscapeUtils.escapeXml(location) + "\" role=\"All\" />");
					}
				}
				out.println("  </attachments>");
			}
			out.println("  <submission_formats>");
			// our text input is HTML
			if (assignmentResource.isAllowText())
				out.println("    <format  type=\"html\" />");
			// file input allows both file and URL
			if (assignmentResource.isAllowFile()) {
				out.println("    <format  type=\"file\" />");
				out.println("    <format  type=\"url\" />");
			}
			out.println("  </submission_formats>");

			out.println("</assignment>");
			out.closeEntry();
		}
		return true;
	}

	public void outputAssignmentsToManifest(ZipPrintStream out) throws IOException, PermissionException, IdUnusedException, TypeException {
		// All attachments (not URL links) are placed first and linked to via the identifier attribute. 
		for (Map.Entry<String, Resource> entry: attachmentMap.entrySet()) {
			Resource resource = entry.getValue();
			if (!exportUtil.isLink(resource.getSakaiId())) {
				String use = "";
				if (resource.getUse() != null) {
					use = " intendeduse=\"" + resource.getUse() + "\"";
				}
				out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(resource.getLocation()) + "\" identifier=\"" + resource.getResourceId() + "\" type=\"webcontent\"" + use + ">");
				out.println("      <file href=\"" + StringEscapeUtils.escapeXml(resource.getLocation()) + "\"/>");
				out.println("    </resource>");
			}
		}

		String variantId = null;
		// Each assignment has one resource element for the extra assignmentpage.html and a dependency element (if there is an attachment), 
		// and another resource element with the cc-objects xml assignment config file.
		for (Map.Entry<String, AssignmentResource> entry: assignmentMap.entrySet()) {
			AssignmentResource assignmentResource = entry.getValue();
			out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(assignmentResource.getLocation()) + "\" identifier=\"" + assignmentResource.getResourceId() + "\" type=\"webcontent\" intendeduse=\"assignment\">");
			out.println("      <file href=\"" + StringEscapeUtils.escapeXml(assignmentResource.getLocation()) + "\"/>");
			for (String sakaiId: assignmentResource.getDependencies()) {
				Resource attachmentResource = attachmentMap.get(sakaiId);
				out.println("      <dependency identifierref=\"" + attachmentResource.getResourceId() + "\"/>");
			}
			variantId = ExportUtil.getResourceId();
			out.println("      <cpx:variant identifier=\"" + ExportUtil.getResourceId() + "\" identifierref=\"" + variantId + "\">");
			out.println("        <cpx:metadata/>");
			out.println("      </cpx:variant>");
			out.println("    </resource>");

			String xmlHref = "cc-objects/" + assignmentResource.getResourceId() + ".xml";
			out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(xmlHref) + "\" identifier=\"" + variantId + "\" type=\"assignment_xmlv1p0\">");
			out.println("      <file href=\"" + StringEscapeUtils.escapeXml(xmlHref) + "\"/>");
			for (String sakaiId: assignmentResource.getDependencies()) {
				Resource attachmentResource = attachmentMap.get(sakaiId);
				out.println("      <dependency identifierref=\"" + attachmentResource.getResourceId() + "\"/>");
			}
			out.println("    </resource>");
		}
	}
}
