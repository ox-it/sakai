package org.sakaiproject.content.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCopyContext;
import org.sakaiproject.content.api.ContentCopyInterceptorRegistry;
import org.sakaiproject.content.api.ContentCopyTextInterceptor;
import org.sakaiproject.content.api.ContentHostingService;

public class ContentCopyTest extends TestCase {

	private ContentCopyImpl contentCopy;
	private ContentCopyContext basicCtx;

	public void setUp() throws Exception {
		super.setUp();
		contentCopy = new ContentCopyImpl();
		ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
		when(serverConfigurationService.getServerNameAliases()).thenReturn(Arrays.asList("weblearn.ox.ac.uk"));
		contentCopy.setServerConfigurationService(serverConfigurationService);
		contentCopy.setContentHostingService(mock(ContentHostingService.class));
        ContentCopyInterceptorRegistry interceptorRegistry = mock(ContentCopyInterceptorRegistry.class);
        when(interceptorRegistry.getTextInterceptors()).thenReturn(Collections.<ContentCopyTextInterceptor>singletonList(new ContentCopySiteIdInterceptor()));
        contentCopy.setInterceptorRegistry(interceptorRegistry);
		
		contentCopy.init();
		
		basicCtx = contentCopy.createCopyContext("old", "new", true);
	}
	
	public void testConvertContent() {
		unaltered("");
		unaltered("sometext");
		unaltered("href=boo");
		unaltered("<a href='test.url");
		unaltered("<a href='value'>");
		unaltered("<a href='url'>Anchor</a><img src='other'>");
	}
	
	public void testEncodingPreserved() {
		unaltered("<a href='Hello%20World.html'>");
	}
	
	public void testRemovesHost() {
		updated("<a href='/access/content/group/file.html'>", "<a href='https://weblearn.ox.ac.uk/access/content/group/file.html'>");
	}
	
	public void testUpdatesSiteId() {
		updated("<a href='/access/content/group/new/index.html'>", "<a href='/access/content/group/old/index.html'>");	
	}
	
	public void testLeavesSiteId() {
		// Should only replace on patterns it knows about.
		unaltered("<a href='/old/old/myfile.html'>");
	}
	
	public void testAllURIParts() {
		unaltered("<a href='http://user:pass@news.bbc.co.uk:123/path/to/file.html?query=asd#fragment'>");
	}
	
	public void testWrongProtocol() {
		// Shouldn't be replaced as it's not a supported protocol
		unaltered("<a href='ftp://weblearn.ox.ac.uk/access/content/group/old/file.html'>");
	}
	
	public void testOtherHost() {
		// Shouldn't be replaced as it's on another host.
		unaltered("<a href='http://othersite.edu/access/content/group/old/file.html'>");
	}
	
	public void testNormalises() {
		updated("<a href='/myfile.html'>", "<a href='/folder/./another/../../myfile.html'>");
	}
	
	public void testIgnoreNonSiteRefs() {
		unaltered("<a href='/this/should/be/ignored'>");
		assertNull(basicCtx.popResource());
	}
	
	public void testIgnoreNewSiteRefs() {
		unaltered("<a href='/access/content/group/new/file.html'>");
		assertNull(basicCtx.popResource());
	}
	
	public void testStoresContent() {
		// Check it adds the old resource to the context.
		updated("<img src='/access/content/group/new/img.png'>", "<img src='/access/content/group/old/img.png'>");
		assertEquals("/group/old/img.png", basicCtx.popResource());
	}
	
	public void testSiteCollectionId() {
		// Check we can work out the collection ID from a resource ID.
		assertEquals("/group/site/", contentCopy.getSiteCollection("/group/site/folder/file.html"));
	}
	
	public void testSupplyingContext() {
		// Check that relative URLs work.
		assertEquals("<img src='img.png'>", contentCopy.convertContent(basicCtx, "<img src='img.png'>", null, "/access/content/group/old/somefile.html"));
		// The old resource should be copied across
		assertEquals("/group/old/img.png", basicCtx.popResource());
	}
	
	public void testPlainText() {
		// Plain text copy only happens when it's an auto generated ID.
		ContentCopyContext fullContext = contentCopy.createCopyContext("818c36fc-1b99-467e-005b-f88aa2cc3776", "new", false);
		assertEquals("My URL: /access/content/group/new/file.txt", contentCopy.convertContent(fullContext, "My URL: /access/content/group/818c36fc-1b99-467e-005b-f88aa2cc3776/file.txt", "text/plain", null));
	}
	
	public void testFailingAnnouncement() {
		// Site ID is only 35 characters to make sure we catch it on URL parsing code.
		// The problem with this one is the missing quotes
		ContentCopyContext announcementExample = contentCopy.createCopyContext("36e31e17-3384-4b2a-80a4-ce7c570a342", "******", true);
		String content = "<a href=/access/content/group/36e31e17-3384-4b2a-80a4-ce7c570a342/Picture%201.png target=\"_blank\" >/access/content/group/36e31e17-3384-4b2a-80a4-ce7c570a342/Picture%201.png</a>&nbsp";
		assertTrue(contentCopy.convertContent(announcementExample, content, "text/html", null).contains("*****"));
		
	}
	
	/**
	 * Check that when processing the source we get the correct response.
	 */
	private void updated(String expected, String source) {
		assertEquals(expected, contentCopy.convertContent(basicCtx, source, null, null));
	}

	/**
	 * Check that the supplied string isn't changed by the processing.
	 */
	private void unaltered(String expected) {
		updated(expected, expected);
	}

}
