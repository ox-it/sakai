package org.sakaiproject.exporter.util;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExportUtil {
	private final String siteId;
	private static Logger log = LoggerFactory.getLogger(CCExport.class);
	private static long nextid = 100000;
	
	private ContentHostingService contentService;

	public ExportUtil(String siteId, ContentHostingService contentService) {
		this.siteId = siteId;
		this.contentService = contentService;
	}

	public Resource getResource(String sakaiId, String location) {
		return getResource(sakaiId, location, null);
	}

	public Resource getResource(String sakaiId, String location, String use) {
		Resource res = new Resource();
		res.setSakaiId(sakaiId);
		res.setResourceId(getResourceId());
		res.setLocation(location);
		res.setDependencies(new HashSet<>());
		res.setUse(use);
		res.setIslink(false);
		res.setIsbank(false);
		return res;
	}

	String getFilePath(String contentResourceId) {
		// Find the path up until the filename without the /group/:siteid or the file name (may result in an empty string). 
		// Just return the path if the content resource ID is in another site.
		if (contentResourceId.contains(siteId)) {
			String filePath = contentResourceId.substring(contentResourceId.indexOf(siteId) + siteId.length() + 1, contentResourceId.lastIndexOf("/") + 1);
			return filePath;
		} else {
			return contentResourceId;
		}
	}

	String getFileName(ContentResource contentResource) {
		String fileName = contentResource.getId().substring(contentResource.getId().lastIndexOf("/") + 1);
		return fileName;
	}

	// Converts links in HTML files into relative links and makes them suitable for Canvas.
	String linkFixup(String content,  Map<String, ContentResource> selectedFilesToExport) {
		StringBuilder parsedContent = new StringBuilder(content);

		// Remove protocol, domain and /access/content folders from all links.
		parsedContent = makeLinksRelative(parsedContent);

		// Check which links are to html files (need to use the unencoded href but not remove the encoding) and change 
		// the folder slashes for hyphens when they are. Store the links in a map to use below.
		Map<String, Boolean> savedLinks = new HashMap<>();
		parsedContent = convertSlashToHyphen(parsedContent, savedLinks, selectedFilesToExport);

		// Add the things required so that Canvas displays the 'Preview the file' icon next to the filename and it
		// displays the file preview window correctly.
		parsedContent = addPDFViewer(parsedContent, savedLinks);

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

	private StringBuilder convertSlashToHyphen(StringBuilder parsedContent, Map<String, Boolean> savedLinks,  Map<String, ContentResource> selectedFilesToExport) {
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
			log.error("export-common-cartridge illegal state exception in convertSlashToHyphen for matcher: " + matcher.toString() + " and content: " + parsedContent, ise);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("export-common-cartridge unsupported encoding exception in convertSlashToHyphen" + uee);
		}
		return parsedContent;
	}


	StringBuilder addPDFViewer(StringBuilder parsedContent, Map<String, Boolean> savedLinks) {
		// Add the CSS class and URL parameters so that Canvas displays the 'Preview the file' icon next to the filename.
		for (Map.Entry<String, Boolean> linkPath : savedLinks.entrySet()) {
			if (linkPath.getKey().toLowerCase().contains(".pdf")) {

				String pat = "(href=)+(\"|')?(/group/" + siteId + "/)(" + linkPath.getKey() + ")(\"|')?([_ a-z0-9=\"'&;+-]*)(>)";
				Pattern target = Pattern.compile(pat, Pattern.CASE_INSENSITIVE);
				Matcher matcher = target.matcher(parsedContent);

				String urlAddition = "?canvas_download=1&amp;canvas_qs_wrap=1";
				String cssAddition = "class=\"instructure_file_link instructure_scribd_file\" ";
				while (matcher.find()) {
					parsedContent.insert(matcher.end(4), urlAddition);
					parsedContent.insert(matcher.start(), cssAddition);
					// Reset the region to search as have increased the length of the search string (parsedContent).
					matcher.region(matcher.end() + urlAddition.length() + cssAddition.length(), parsedContent.length());
				}
			}
		}
		return parsedContent;
	}

	private StringBuilder addCanvasFilePath(StringBuilder parsedContent, Map<String, Boolean> savedLinks) {
		// Add the correct Canvas substitution variable so that HTML files end up in Pages in Canvas and
		// everything else in the files area in Canvas.
		try {
			for (Map.Entry<String, Boolean> link : savedLinks.entrySet()) {
				String pat = "(href=)+(\"|')?(/group/" + siteId + "/)(" + link.getKey() + ")(\"|')?([_ a-z0-9=\"'-]*)(>)";
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
			log.error("export-common-cartridge illegal state exception in addCanvasFilePath, content: " + parsedContent, ise);
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
			log.error("export-common-cartridge illegal state exception in removeUrlEncoding, content: " + parsedContent, ise);
		} catch (IllegalArgumentException iae) {
			log.error("export-common-cartridge unsupported encoding exception in removeUrlEncoding for content: " + parsedContent, iae);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("export-common-cartridge unsupported encoding exception in removeUrlEncoding", uee);
		}
		return parsedContent;
	}

	public String removeSakaiForumPath(String sakaiPath) {

		Pattern target = Pattern.compile("/attachment/[-a-z0-9]+/Forums/[-a-z0-9]+/", Pattern.CASE_INSENSITIVE);
		Matcher matcher = target.matcher(sakaiPath);
		return matcher.replaceAll("");
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

	ContentResource getContentResource(String sakaiId) throws PermissionException, IdUnusedException, TypeException{
		return contentService.getResource(sakaiId);
	}

	boolean isLink(String sakaiId) throws PermissionException, IdUnusedException, TypeException {
		ContentResource contentResource = contentService.getResource(sakaiId);
		return isLink(contentResource);
	}

	boolean isLink(ContentResource r) {
		return r.getResourceType().equals("org.sakaiproject.content.types.urlResource") ||
				r.getContentType().equals("text/url");
	}

	boolean hasMarkUp(String mimeType) {
		return (("text/html").equals(mimeType) || ("application/xhtml+xml").equals(mimeType));
	}

	Map<String, ContentResource> removeReadingLists(Map<String, ContentResource> filesToExport) {
		// Remove any reading lists (citations)from the files to be in the export.
		for (Iterator<Map.Entry<String, ContentResource>> it = filesToExport.entrySet().iterator(); it.hasNext(); ) {
			ContentResource contentResource = it.next().getValue();
			if ("org.sakaiproject.citation.impl.CitationList".equals(contentResource.getResourceType())) {
				it.remove();
			}
		}
		return filesToExport;
	}

	static String getResourceId() {
		return "res" + (nextid++);
	}
}
