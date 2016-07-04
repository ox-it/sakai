package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.output.NullOutputStream;

public class RSSProxyFilterTest extends TestCase {
	
	private ProxyService proxyService;
	
	public void setUp() {
		proxyService = new SimpleProxyService();
	}

	public void testProxyRSSNullOut() throws IOException {
		InputStream source = RSSProxyFilterTest.class.getResourceAsStream("/podcastingnewsfeeds.xml");
		OutputStream output = new NullOutputStream();
		RSSProxyFilter proxy = new RSSProxyFilter(source, output, proxyService, "1", "1");
		proxy.filter();
	}
	
	public void testProxyRSS() throws IOException {
		InputStream source = RSSProxyFilterTest.class.getResourceAsStream("/podcastingnewsfeeds.xml");
		OutputStream output = new ByteArrayOutputStream();
		RSSProxyFilter proxy = new RSSProxyFilter(source, output, proxyService, "1", "1");
		proxy.filter();
		String rss = output.toString();
		assertTrue(rss.contains(proxyService.getProxyURL(null)));
	}
	
	public void testLargeFile() throws IOException {
		InputStream source = RSSProxyFilterTest.class.getResourceAsStream("/users-anon.xml");
		OutputStream output = new NullOutputStream();
		RSSProxyFilter proxy = new RSSProxyFilter(source, output, proxyService, "1", "1");
		proxy.filter();
	}
}
