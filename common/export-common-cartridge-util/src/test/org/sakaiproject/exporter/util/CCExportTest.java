package org.sakaiproject.exporter.util;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

public class CCExportTest {
	@Test
	public void addPDFViewerTest() {
		StringBuilder parsedContent = new StringBuilder("<p><a href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf\">/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf</a></p>" +
								"<p>other content</p><p><a href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf\" name=\"Some other name\" target=\"_blank\">Link to PDF</a></p>" +
								"<p><a href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf\">/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf</a></p>");

		Map<String, Boolean> savedLinks = new HashMap<>();
		savedLinks.put("TEL2017-V2.pdf", false);

		StringBuilder expectedResult = new StringBuilder("<p><a class=\"instructure_file_link instructure_scribd_file\" href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\">" +
				"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf</a></p><p>other content</p><p><a class=\"instructure_file_link instructure_scribd_file\" " +
				"href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\" name=\"Some other name\" target=\"_blank\">" +
				"Link to PDF</a></p><p><a class=\"instructure_file_link instructure_scribd_file\" " +
				"href=\"/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf?canvas_download=1&amp;canvas_qs_wrap=1\">/group/0b1cfd08-c97a-48d8-8020-5448ffa25b1e/TEL2017-V2.pdf</a></p>");

		CCExport ccExport = new CCExport(null);
		ccExport.setSiteId("0b1cfd08-c97a-48d8-8020-5448ffa25b1e");
		StringBuilder result = ccExport.addPDFViewer(parsedContent, savedLinks);
		assertEquals(expectedResult.toString(), result.toString());
	}
}
