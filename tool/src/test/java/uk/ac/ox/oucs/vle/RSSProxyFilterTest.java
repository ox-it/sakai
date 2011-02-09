package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

public class RSSProxyFilterTest extends TestCase {
	
	private ProxyService proxyService;
	
	public void setUp() {
		proxyService = new SimpleProxyService();
	}

	public void testProxyRSS() throws IOException {
		InputStream source = RSSProxyFilterTest.class.getResourceAsStream("/podcastingnewsfeeds.xml");
		RSSProxyFilter proxy = new RSSProxyFilter(source, proxyService, "1", "1");
		OutputStream output = new ByteArrayOutputStream();
		IOUtils.copy(proxy, output);
		String rss = output.toString();
		assertTrue(rss.contains(proxyService.getProxyURL(null)));
	}
	
	public void testLargeFile() throws IOException {
		InputStream source = RSSProxyFilterTest.class.getResourceAsStream("/users-anon.xml");
		RSSProxyFilter proxy = new RSSProxyFilter(source, proxyService, "1", "1");
		OutputStream output = new NullOutputStream();
		IOUtils.copy(proxy, output);
	}
}
