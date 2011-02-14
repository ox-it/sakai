package uk.ac.ox.oucs.vle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.sakaiproject.component.impl.MockCompMgr;

/**
 * These tests aren't used.
 * @author buckett
 *
 */
public class XMLTransformTestNot extends TestCase {
	
	private ProxyService proxyService;
	
	public void setUp() {
		proxyService = new SimpleProxyService();
	}

	public void testProxyRSS() throws IOException {
		InputStream source = XMLTransformTestNot.class.getResourceAsStream("/podcastingnewsfeeds.xml");
		OutputStream output = new ByteArrayOutputStream();
		XMLTransformFilter proxy = new XMLTransformFilter(source, output, new SimpleProxyService());
		proxy.filter();
	}
	
	public void testLargeFile() throws IOException {
		InputStream source = XMLTransformTestNot.class.getResourceAsStream("/users-anon.xml");
		OutputStream output = new NullOutputStream();
		XMLTransformFilter proxy = new XMLTransformFilter(source, output, new SimpleProxyService());
		proxy.filter();
	}
}
