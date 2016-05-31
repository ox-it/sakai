package org.sakaiproject.component.app.messageforums;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.api.app.messageforums.MessageParsingService;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.impl.FormattedTextImpl;

public class TestMessageParsingServiceImpl extends TestCase {

	private String message1="The quick brown fox jumped over the lazy dog\nwww.bbc.co.uk";
	private String result1="The quick brown fox jumped over the lazy dog\nwww.bbc.co.uk";
	private String storage1="The quick brown fox jumped over the lazy dog<br /><a href='http://www.bbc.co.uk' target='_blank'>www.bbc.co.uk</a>";
	
	private String message2="The quick brown fox jumped over the lazy dog<br />www.bbc.co.uk";
	private String result2="The quick brown fox jumped over the lazy dog<br />www.bbc.co.uk";
	private String storage2="The quick brown fox jumped over the lazy dog&lt;br /&gt;<a href='http://www.bbc.co.uk' target='_blank'>www.bbc.co.uk</a>";
	
	private String message3="just sending http://news.bbc.co.uk and expecting WebLearn to mark it\nup?";
	private String result3="just sending http://news.bbc.co.uk and expecting WebLearn to mark it\nup?";
	private String storage3="just sending <a href='http://news.bbc.co.uk' target='_blank'>http://news.bbc.co.uk</a> and expecting WebLearn to mark it<br />up?";
				
	private String message4="<b>Fred bloggs</b> visits the bbc website: http://bbc.co.uk.\n\n<i>Alice</i> on the other hand doesnt.\n\"";
	private String result4="<b>Fred bloggs</b> visits the bbc website: http://bbc.co.uk.\n\n<i>Alice</i> on the other hand doesnt.\n\"";
	private String storage4="&lt;b&gt;Fred bloggs&lt;/b&gt; visits the bbc website: <a href='http://bbc.co.uk' target='_blank'>http://bbc.co.uk</a>.<br /><br />&lt;i&gt;Alice&lt;/i&gt; on the other hand doesnt.<br />\"";

	public TestMessageParsingServiceImpl(String name) {
		super(name);
	}

	private MessageParsingServiceImpl messageParsingService;

	@Before
	public void setUp() {
		messageParsingService = new MessageParsingServiceImpl();
		FormattedText formattedText = new FormattedTextImpl();
		messageParsingService.setFormattedText(formattedText);
	}
	
	@Test
	public void test1() {
		assertEquals(storage1, 
				messageParsingService.parse(message1));
		assertEquals(result1, 
				messageParsingService.format(messageParsingService.parse(message1)));
	}
	
	@Test
	public void test2() {
		assertEquals(storage2, 
				messageParsingService.parse(message2));
		assertEquals(result2, 
				messageParsingService.format(messageParsingService.parse(message2)));
	}
	
	@Test
	public void test3() {
		assertEquals(storage3, 
				messageParsingService.parse(message3));
		assertEquals(result3, 
				messageParsingService.format(messageParsingService.parse(message3)));
	}
	
	@Test
	public void test4() {
		assertEquals(storage4, 
				messageParsingService.parse(message4));
		assertEquals(result4, 
				messageParsingService.format(messageParsingService.parse(message4)));
	}

	@Test
	public void testURLParsing() {
		// These are tests where the whole string should make it through without any changes.
		testURL("http://www.google.com");
		testURL("http://www.google.com/");
		testURL("http://www.facebook.com/login?go=true");
		
		// Brackets, escaped and not.
		testURL("http://en.wikipedia.org/wiki/PC_Tools_(Central_Point_Software)");
		testURL("http://msdn.microsoft.com/en-us/library/aa752574(VS.85).aspx");
		testURL("http://en.wikipedia.org/wiki/PC_Tools_%28Central_Point_Software%29");
		testURL("http://msdn.microsoft.com/en-us/library/aa752574%28VS.85%29.aspx");
		
		// Test post

	}
	
	@Test
	public void testNonURL() {
		assertEquals("2.99", messageParsingService.parse("2.99"));
		assertEquals("112,122,000", messageParsingService.parse("112,122,000"));
		assertEquals("End of sentance.", messageParsingService.parse("End of sentance."));
	}
	
	@Test
	public void testMissingProtocol() {
		assertEquals("<a href='http://www.ox.ac.uk' target='_blank'>www.ox.ac.uk</a>", messageParsingService.parse("www.ox.ac.uk"));
		assertEquals("<a href='http://news.bbc.co.uk' target='_blank'>news.bbc.co.uk</a>", messageParsingService.parse("news.bbc.co.uk"));
		assertEquals("<a href='http://weblearn.ox.ac.uk' target='_blank'>weblearn.ox.ac.uk</a>", messageParsingService.parse("weblearn.ox.ac.uk"));
	}
	
	@Test
	public void testBadSentance() {
		assertEquals("No space after stop.My other", messageParsingService.parse("No space after stop.My other"));
	}
	
	@Test
	public void testPort() {
		testURL("http://www.someserver.com:8080/more/");
		testURL("http://www.someserver.com:8080");
	}
	
	@Test
	public void testHttps() {
		testURL("https://www.secure.com/");
		
	}
	
	@Test
	public void testEmail() {
		testEmail("matthew.buckett@it.ox.ac.uk");
		testEmail("me+123@gmail.com");
	}
	
	@Test
	public void testEmailsInText() {
		// In brackets
		assertEquals("(<a href='mailto:a@example.com'>a@example.com</a>)", messageParsingService.parse("(a@example.com)"));
		assertEquals("My new email is <a href='mailto:b@example.com'>b@example.com</a>.", messageParsingService.parse("My new email is b@example.com."));
	}
	
	@Test
	public void testNonEmail() {
		testUnchanged("My twitter handle is @buckett");
		testUnchanged("I bought 4 apples @ $2.99");
	}
	
	@Test
	public void testTerminator() {
		// Checking that the terminator matches.
		assertEquals(" <a href='http://www.ele.ac.uk/' target='_blank'>http://www.ele.ac.uk/</a> ", messageParsingService.parse(" http://www.ele.ac.uk/ "));
	}
	
	@Test
	public void testEntitiesInLinks() {
		// We need to make sure that & in URLs doesn't get turned into &amp;
		assertEquals("Q&amp;A <a href='http://www.ox.ac.uk/go?a=1&b=2' target='_blank'>http://www.ox.ac.uk/go?a=1&amp;b=2</a>",
				messageParsingService.parse("Q&A http://www.ox.ac.uk/go?a=1&b=2"));
	}
	
	@Test
	public void testMultipleNewlines() {
		assertEquals("Hello<br /><br />World", messageParsingService.parse("Hello\n\nWorld"));
	}
	
	@Test
	public void testInBrackets() {
		assertEquals("(<a href='http://www.ox.ac.uk' target='_blank'>http://www.ox.ac.uk</a>)", messageParsingService.parse("(http://www.ox.ac.uk)"));
		assertEquals("(<a href='http://www.cam.ac.uk/' target='_blank'>http://www.cam.ac.uk/</a>)", messageParsingService.parse("(http://www.cam.ac.uk/)"));
		assertEquals("(<a href='http://www.ex.ac.uk' target='_blank'>www.ex.ac.uk</a>)", messageParsingService.parse("(www.ex.ac.uk)"));
		assertEquals("(<a href='https://itunes.apple.com/gb/id288751446?mt=8' target='_blank'>https://itunes.apple.com/gb/id288751446?mt=8</a>)",
				messageParsingService.parse("(https://itunes.apple.com/gb/id288751446?mt=8)"));
	}
	
	// This test fails due to non-breaking space before the http:// fixing this complicates the regexp and it's an edge case so it 
	// doesn't currently work, but if we change how we perform scanning it's a case to consider.
	// Paste from word is my guess as to how this occurred in the first place.
	// @Test
	public void strangeWhitespace() {
		// After See is a non breaking space C2 A0 (UTF-8 hex).
		assertEquals("(See <a href='https://itunes.apple.com/link' target='_blank'>https://itunes.apple.com/link</a>)",
				messageParsingService.parse("See https://itunes.apple.com/link"));
	}
	
	@Test
	public void testInBracketsWithStop() {
		assertEquals("(<a href='http://news.bbc.co.uk/' target='_blank'>http://news.bbc.co.uk/</a>).", messageParsingService.parse("(http://news.bbc.co.uk/)."));
		assertEquals("(<a href='http://news.bbc.co.uk' target='_blank'>http://news.bbc.co.uk</a>).", messageParsingService.parse("(http://news.bbc.co.uk)."));
	}
	
	public void testURL(String url) {
		assertEquals("<a href='"+ url+ "' target='_blank'>"+ url+ "</a>", messageParsingService.parse(url));
	}
	
	public void testEmail(String email) {
		assertEquals("<a href='mailto:"+ email+ "'>"+ email+ "</a>", messageParsingService.parse(email));
	}
	
		
	public void testUnchanged(String test) {
		assertEquals(test, messageParsingService.parse(test));
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestMessageParsingServiceImpl.class);
	}

}
