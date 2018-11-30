package org.sakaiproject.exporter.util;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CCExportTest {

	private CCExport export;
	private Map<String, Boolean> links;

	@Before
	public void setUp() {
		export = new CCExport(null);
		export.setSiteId("siteId");
		links = new HashMap<>();
	}

	@Test
	public void addPDFViewerTest() {
		StringBuilder parsedContent = new StringBuilder("<p><a href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf\">/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf</a></p>" +
								"<p>other content</p><p><a href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf\" name=\"Some other name\" target=\"_blank\">Link to PDF</a></p>" +
								"<p><a href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf\">/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf</a></p>");

		links.put("TEL2017-V2.pdf", false);

		StringBuilder expectedResult = new StringBuilder("<p><a class=\"instructure_file_link instructure_scribd_file\" href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\">" +
				"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf</a></p><p>other content</p><p><a class=\"instructure_file_link instructure_scribd_file\" " +
				"href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\" name=\"Some other name\" target=\"_blank\">" +
				"Link to PDF</a></p><p><a class=\"instructure_file_link instructure_scribd_file\" " +
				"href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\">/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf</a></p>");

		export.setSiteId("0b1cfd08-c97a-48d8-8020-5448ffa25b1e");
		StringBuilder result = export.addPDFViewer(parsedContent, links);
		assertEquals(expectedResult.toString(), result.toString());
	}


	@Test
	public void testNoUrls() {
		// Check that when there are no URLs we don't change things.
		StringBuilder source = new StringBuilder("<p><a href='/someUrl'>Link</a></p>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertEquals(source.toString(), result.toString());
	}

	@Test
	public void testSingleUrl() {
		links.put("file.pdf", false);

		StringBuilder source = new StringBuilder("<a href='/group/siteId/file.pdf'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		String expected = "<a class=\"instructure_file_link instructure_scribd_file\" href='/group/siteId/file.pdf?canvas_download=1&amp;canvas_qs_wrap=1'>File</a>";
		assertEquals(expected, result.toString());
	}

	@Test
	public void testMultipleUrls() {
		links.put("file.pdf", false);

		StringBuilder source = new StringBuilder("<a href='/group/siteId/file.pdf'>File</a>" +
				"<a href='/group/siteId/file.pdf'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.stringContainsInOrder(Arrays.asList("scribd_file", "scribd_file")));
	}

	@Test
	public void testFolderLink() {
		links.put("folder/file.pdf", true);

		StringBuilder source = new StringBuilder("<a href='/group/siteId/folder/file.pdf'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/siteId/folder/file.pdf?canvas_download"));
	}

	@Test
	public void testDifferentSite() {
		links.put("file.pdf", false);
		StringBuilder source = new StringBuilder("<a href='/group/otherId/file.pdf'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertEquals("<a href='/group/otherId/file.pdf'>File</a>", result.toString());
	}

	@Test
	public void testNonPDF() {
		links.put("file.txt", false);
		StringBuilder source = new StringBuilder("<a href='/group/siteId/file.txt'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertEquals("<a href='/group/siteId/file.txt'>File</a>", result.toString());
	}

	@Test
	public void testUpperCase() {
		links.put("file.PDF", false);
		StringBuilder source = new StringBuilder("<a href='/group/siteId/file.PDF'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/siteId/file.PDF?canvas_download"));
	}

	@Test
	public void testSpaceInFile() {
		links.put("file%20name.pdf", false);
		StringBuilder source = new StringBuilder("<a href='/group/siteId/file%20name.pdf'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/siteId/file%20name.pdf?canvas_download"));
	}

	//@Test
	// This one fails and should be fixed.
	public void testAdditionalAttributes() {
		links.put("file.pdf", false);
		StringBuilder source = new StringBuilder("<a href='/group/siteId/file.pdf' title='Hello &amp; Bye'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/siteId/file.pdf?canvas_download"));
	}

	//@Test
	// This one fails and should be fixed.
	public void testMoreCharacters() {
		links.put("file.pdf", false);
		StringBuilder source = new StringBuilder("<a href='/group/siteId/file.pdf' title='+++'>File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertThat(result.toString(), Matchers.containsString("/group/siteId/file.pdf?canvas_download"));
	}

	//@Test
	// Not sure if we should bother here
	public void testExistingClass() {
		links.put("file.pdf", false);
		StringBuilder source = new StringBuilder("<a class='myclass' href='/group/siteId/file.pdf' >File</a>");
		StringBuilder result = export.addPDFViewer(source, links);
		assertEquals("<a class=\"myclass instructure_file_link instructure_scribd_file\"" +
				" href='/group/siteId/file.pdf?canvas_download=1&amp;canvas_qs_wrap=1' >File</a>", result.toString());
	}

}
