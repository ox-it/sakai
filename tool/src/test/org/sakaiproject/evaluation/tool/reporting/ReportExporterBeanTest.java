package org.sakaiproject.evaluation.tool.reporting;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthew Buckett
 */
public class ReportExporterBeanTest{

	private ReportExporterBean exporter;

	@Before
	public void setUp() {
		exporter = new ReportExporterBean();
	}

	@Test
	public void testContentDisposition() {
		assertEquals("inline; filename=\"file.txt\"; filename*=UTF-8''file.txt",
				exporter.buildContentDisposition("file.txt"));
	}

	@Test
	public void testContentDispositionSemiColon() {
		assertEquals("inline; filename=\"start;stop.txt\"; filename*=UTF-8''start%3Bstop.txt",
				exporter.buildContentDisposition("start;stop.txt"));
	}

	@Test
	public void testContentDispositionQuotes() {
		assertEquals("inline; filename=\"start\\\"stop.txt\"; filename*=UTF-8''start%22stop.txt",
				exporter.buildContentDisposition("start\"stop.txt"));
	}

	@Test
	public void testContentDispositionUTF8() {
		// encoding hello world in greek.
		assertEquals("inline; filename=\"???? ??? ?????.txt\"; " +
				"filename*=UTF-8''%CE%93%CE%B5%CE%B9%CE%B1%20%CF%83%CE%B1%CF%82%20%CE%BA%CF%8C%CF%83%CE%BC%CE%BF.txt",
				exporter.buildContentDisposition("\u0393\u03B5\u03B9\u03B1 \u03C3\u03B1\u03C2 \u03BA\u03CC\u03C3\u03BC\u03BF.txt"));
	}
}
