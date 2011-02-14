package uk.ac.ox.oucs.vle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;

public class XMLFilterTest extends TestCase {

	public void testPassesXML() throws IOException {
		byte[] source = "<?xml version='1.0' encoding='UTF-8'?><h1>Hello</h1>".getBytes();
		InputStream in = new ByteArrayInputStream(source);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLFilter proxy = new XMLFilter(in, out);
		proxy.filter();
		assertEquals(new String(source), new String(out.toByteArray()));
	}
	
	public void testLargerXML() throws IOException {
		CountingInputStream in = new CountingInputStream(XMLFilterTest.class.getResourceAsStream("/podcastingnewsfeeds.xml"));
		CountingOutputStream out = new CountingOutputStream(new NullOutputStream());
		XMLFilter proxy = new XMLFilter(in, out);
		proxy.filter();
	}
	
	public void testDoubleFilterXML() throws IOException, NoSuchAlgorithmException {
		CountingInputStream in = new CountingInputStream(XMLFilterTest.class.getResourceAsStream("/podcastingnewsfeeds.xml"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		MessageDigest md1 = MessageDigest.getInstance("SHA1");
		MessageDigest md2 = MessageDigest.getInstance("SHA1");
		DigestOutputStream proxy1 = new DigestOutputStream(out, md1);
		new XMLFilter(in,proxy1).filter();
		ByteArrayInputStream in2 = new ByteArrayInputStream(out.toByteArray());
		out.reset();
		DigestOutputStream proxy2 = new DigestOutputStream(out, md2);
		new XMLFilter(in2, proxy2).filter();
		
		assertTrue(Arrays.equals(md1.digest(), md2.digest()));
	}

	public void testLargeFile() throws IOException {
		CountingInputStream in = new CountingInputStream(XMLFilterTest.class
				.getResourceAsStream("/users-anon.xml"));
		CountingOutputStream out = new CountingOutputStream(new NullOutputStream());
		XMLFilter proxy = new XMLFilter(in,out);
		proxy.filter();
	}

	public void testLargeFileNoFilter() throws IOException {
		CountingInputStream in = new CountingInputStream(XMLFilterTest.class
				.getResourceAsStream("/users-anon.xml"));
		CountingOutputStream out = new CountingOutputStream(new NullOutputStream());
		IOUtils.copy(in, out);
		assertEquals(in.getByteCount(), out.getByteCount());
	}
	
}