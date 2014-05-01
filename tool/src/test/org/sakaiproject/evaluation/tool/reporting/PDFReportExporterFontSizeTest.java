package org.sakaiproject.evaluation.tool.reporting;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PDFReportExporterFontSizeTest {

	// Accuracy of floating point comparisons.
	private static final double DELTA = 1e-6;
	private PDFReportExporter exporter;

	@Before
	public void setUp() {
		exporter = new PDFReportExporter();

	}

	@Test
	public void testSimpleFontSize() {
		assertEquals(14.0f, exporter.calculateFontSize("<div style='font-size: large; color: blue'>Hello</div>"), DELTA);
	}

	@Test
	public void testJustDefaultFontSize() {
		assertEquals(10.0f, exporter.calculateFontSize("<div>Hello</div>"), DELTA);
	}

	@Test
	public void testFontSizeNumeric() {
		// If we have a font style but can't understand it we have a slightly larger font.
		assertEquals(12.0f, exporter.calculateFontSize("<div style='font-size: 12pt;'>Hello</div>"), DELTA);
	}

	@Test
	public void testFontSizeNoSemiColon() {
		assertEquals(14.0f, exporter.calculateFontSize("<div style='font-size: large'>Hello</div>"), DELTA);
	}

	@Test
	public void testFontSizeNoSpavce() {
		assertEquals(14.0f, exporter.calculateFontSize("<div style='font-size:large;'>Hello</div>"), DELTA);
	}

}
