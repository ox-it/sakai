package org.sakaiproject.exporter.util;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.content.api.ContentResource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExportUtilTest {

	private ExportUtil exportUtil;
	private Map<String, Boolean> links;
	private Map<String, ContentResource> files;
	String siteId;

	@Before
	public void setUp() {
		siteId = "0b1cfd08-c97a-48d8-8020-5448ffa25b1e";
		exportUtil = new ExportUtil(siteId);

		links = new HashMap<>();
		files = new HashMap<>();
	}

	@Test
	public void testNoUrls() {
		// Check that when there are no URLs we don't change things.
		StringBuilder source = new StringBuilder("<p><a href='/someUrl'>Link</a></p>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertEquals(source.toString(), result.toString());
	}

	@Test
	public void testSingleUrl() {
		links.put("file.pdf", false);

		StringBuilder source = new StringBuilder("<a href='/group/" + siteId + "/file.pdf'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		String expected = "<a class=\"instructure_file_link instructure_scribd_file\" href='/group/" + siteId + "/file.pdf?canvas_download=1&amp;canvas_qs_wrap=1'>File</a>";
		assertEquals(expected, result.toString());
	}

	@Test
	public void testTwoUrls() {
		links.put("file.pdf", false);

		StringBuilder source = new StringBuilder("<a href='/group/" + siteId + "/file.pdf'>File</a>" +
				"<a href='/group/" + siteId + "/file.pdf'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.stringContainsInOrder(Arrays.asList("scribd_file", "scribd_file")));
	}

	@Test
	public void testMultipleUrlsSameFile() {
		links.put("TEL2017-V2.pdf", false);
		StringBuilder source = new StringBuilder("<p><a href=\"/group/" + siteId + "/TEL2017-V2.pdf\">/group/" + siteId + "/TEL2017-V2.pdf</a></p>" +
				"<p>other content</p><p><a href=\"/group/" + siteId + "/TEL2017-V2.pdf\" name=\"Some other name\" target=\"_blank\">Link to PDF</a></p>" +
				"<p><a href=\"/group/" + siteId + "/TEL2017-V2.pdf\">/group/" + siteId + "/TEL2017-V2.pdf</a></p>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);

		String expected = "<p><a class=\"instructure_file_link instructure_scribd_file\" href=\"/group/" + siteId + "/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\">" +
				"/group/" + siteId + "/TEL2017-V2.pdf</a></p><p>other content</p><p><a class=\"instructure_file_link instructure_scribd_file\" " +
				"href=\"/group/" + siteId + "/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\" name=\"Some other name\" target=\"_blank\">" +
				"Link to PDF</a></p><p><a class=\"instructure_file_link instructure_scribd_file\" " +
				"href=\"/group/" + siteId + "/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\">/group/" + siteId + "/TEL2017-V2.pdf</a></p>";
		assertEquals(expected, result.toString());
	}

	@Test
	public void testFolderLink() {
		links.put("folder/file.pdf", true);

		StringBuilder source = new StringBuilder("<a href='/group/" + siteId + "/folder/file.pdf'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/" + siteId + "/folder/file.pdf?canvas_download"));
	}

	@Test
	public void testDifferentSite() {
		links.put("file.pdf", false);
		StringBuilder source = new StringBuilder("<a href='/group/otherId/file.pdf'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertEquals("<a href='/group/otherId/file.pdf'>File</a>", result.toString());
	}

	@Test
	public void testNonPDF() {
		links.put("file.txt", false);
		StringBuilder source = new StringBuilder("<a href='/group/" + siteId + "/file.txt'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertEquals("<a href='/group/" + siteId + "/file.txt'>File</a>", result.toString());
	}

	@Test
	public void testUpperCase() {
		links.put("file.PDF", false);
		StringBuilder source = new StringBuilder("<a href='/group/" + siteId + "/file.PDF'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/" + siteId + "/file.PDF?canvas_download"));
	}

	@Test
	public void testSpaceInFile() {
		links.put("file%20name.pdf", false);
		StringBuilder source = new StringBuilder("<a href='/group/" + siteId + "/file%20name.pdf'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/" + siteId + "/file%20name.pdf?canvas_download"));
	}

	@Test
	public void testAdditionalAttributes() {
		links.put("file.pdf", false);
		StringBuilder source = new StringBuilder("<a href='/group/" + siteId + "/file.pdf' title='Hello &amp; Bye'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/" + siteId + "/file.pdf?canvas_download"));
	}

	@Test
	public void testMoreCharacters() {
		links.put("file.pdf", false);
		StringBuilder source = new StringBuilder("<a href='/group/" + siteId + "/file.pdf' title='+++'>File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/" + siteId + "/file.pdf?canvas_download"));
	}

	//@Test
	// Not sure if we should bother here
	public void testExistingClass() {
		links.put("file.pdf", false);
		StringBuilder source = new StringBuilder("<a class='myclass' href='/group/" + siteId + "/file.pdf' >File</a>");
		StringBuilder result = exportUtil.addPDFViewer(source, links);
		assertEquals("<a class=\"myclass instructure_file_link instructure_scribd_file\"" +
				" href='/group/" + siteId + "/file.pdf?canvas_download=1&amp;canvas_qs_wrap=1' >File</a>", result.toString());
	}

	@Test
	public void testFilePathAnotherSite() {
		String path = "/group/wrongSiteId/file.pdf";
		String result = exportUtil.getFilePath(path, false);
		assertEquals(result, path); 
	}

	@Test
	public void testFilePathFlat() {
		String path = "/group/" + siteId + "/file.pdf";
		String result = exportUtil.getFilePath(path, true);
		assertEquals("", result);
	}

	@Test
	public void testFilePathEscaped() {
		String path = "/group/" + siteId + "/dot&dash/ürm/file.pdf";
		String result = exportUtil.getFilePath(path, true);
		assertEquals("dot%26dash/%C3%BCrm/", result);
	}

	@Test
	public void testRemoveReadingLists() {
		ContentResource cr = mock(ContentResource.class);
		files.put("IsCitation", cr);

		when(cr.getResourceType()).thenReturn("org.sakaiproject.citation.impl.CitationList");
		assertEquals(0, exportUtil.removeReadingLists(files).size());
	}
}