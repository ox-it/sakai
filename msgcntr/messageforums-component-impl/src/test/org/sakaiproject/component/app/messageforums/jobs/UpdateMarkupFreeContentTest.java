package org.sakaiproject.component.app.messageforums.jobs;

import static org.junit.Assert.*;

import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.api.app.messageforums.MessageParsingService;
import org.sakaiproject.component.app.messageforums.MessageParsingServiceImpl;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.impl.FormattedTextImpl;

public class UpdateMarkupFreeContentTest {

	private UpdateMarkupFreeContent updateContent;

	@Before
	public void setUp() throws Exception {
		MessageParsingServiceImpl messageParsingService = new MessageParsingServiceImpl();
		FormattedText formattedText = new FormattedTextImpl();
		messageParsingService.setFormattedText(formattedText);
		updateContent = new UpdateMarkupFreeContent();
		updateContent.setMessageParsingService(messageParsingService);
	}

	@Test
	public void testSimple() {
		assertEquals("",updateContent.updateBody(""));
		assertEquals("hello", updateContent.updateBody("hello"));
		assertEquals("hello", updateContent.updateBody("<b>hello</b>"));
	}
	
	@Test
	public void testComplex() {
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<br />line 2"));
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<br/>line 2"));
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<br>line 2"));
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<BR>line 2"));
		assertEquals("line 1<br />line 2", updateContent.updateBody("line 1<br clear='all'>line 2"));
	}
	
	@Test
	public void testHTMLEntities() {
		assertEquals("\"", updateContent.updateBody("\""));
		assertEquals("&amp;", updateContent.updateBody("&amp;"));
		assertEquals("", updateContent.updateBody("&lt;&gt;"));
	}
	
	@Test 
	public void testLinks() {
		assertEquals("<a href='http://news.bbc.co.uk' target='_blank'>http://news.bbc.co.uk</a>",
				updateContent.updateBody("<a href='http://news.bbc.co.uk'>http://news.bbc.co.uk</a>"));
		assertEquals("<a href='http://www.google.com/news/' target='_blank'>http://www.google.com/news/</a>",
				updateContent.updateBody("http://www.google.com/news/"));
	}
	
	@Test
	public void testAnchors() {
		assertEquals("Anchor <a href='http://www.ox.ac.uk/' target='_blank'>http://www.ox.ac.uk/</a>",
				updateContent.updateBody("<a title='hello'>Anchor</a> <a href='http://www.ox.ac.uk/'>http://www.ox.ac.uk/</a>"));
	}
	
	@Test
	public void testNewlines() {
		assertEquals("Hello All,<br /><br />I would like to start", updateContent.updateBody("Hello All,\n<div><br/>\nI would like to start"));
	}
	
	@Test
	public void testMoreContent() {
		String original = loadResource("post-original.txt");
		String filtered = loadResource("post-filtered.txt");
		assertEquals(filtered, updateContent.updateBody(original));
	}
	
	@Test
	public void testPostWithBr() {
		String original = loadResource("post-with-br-original.txt");
		String filtered = loadResource("post-with-br-filtered.txt");
		assertEquals(filtered, updateContent.updateBody(original));
	}
	
	@Test
	public void testMultipleBr() {
		assertEquals("<br /><br />", updateContent.updateBody("<br />   \n  <br />"));
		assertEquals("<br />word<br />", updateContent.updateBody("<br />word<br />"));
	}
	
	@Test
	public void testEscapeWordContent() {
		assertEquals("<br /><br />", updateContent.updateBody("&lt;!--[if gte mso 9]&gt;&lt;xml&gt;\n" + 
				"&lt;o:OfficeDocumentSettings&gt;\n" + 
				"&lt;o:AllowPNG /&gt;\n" + 
				"&lt;/o:OfficeDocumentSettings&gt;"));
	}
	
	@Test
	public void testEntitiesInLinks() {
		// We need to make sure that & in URLs doesn't get turned into &amp;
		assertEquals("Q&amp;A <a href='http://www.ox.ac.uk/go?a=1&b=2' target='_blank'>http://www.ox.ac.uk/go?a=1&amp;b=2</a>",
				updateContent.updateBody("Q&A http://www.ox.ac.uk/go?a=1&b=2"));
	}
	
	@Test
	public void testAnchorWithText() {
		assertEquals("Google (<a href='http://www.google.com/' target='_blank'>http://www.google.com/</a>)",
				updateContent.updateBody("<a href='http://www.google.com/'>Google</a>"));
	}

	private String loadResource(String resource) {
		return new Scanner(getClass().getResourceAsStream(resource)).useDelimiter("\\A").next();
	}

}
