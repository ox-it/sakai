package uk.ac.ox.oucs.vle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.security.DigestInputStream;
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
		byte[] source = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><h1>Hello</h1>".getBytes();
		InputStream in = new ByteArrayInputStream(source);
		XMLFilter proxy = new XMLFilter(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(proxy, out);
		assertEquals(new String(source), new String(out.toByteArray()));
	}
	
	public void testLargerXML() throws IOException {
		CountingInputStream in = new CountingInputStream(XMLFilterTest.class.getResourceAsStream("/podcastingnewsfeeds.xml"));
		XMLFilter proxy = new XMLFilter(in);
		CountingOutputStream out = new CountingOutputStream(new NullOutputStream());
		IOUtils.copy(proxy, out);
		//assertEquals(in.getByteCount(), out.getByteCount());
	}
	
	public void testDoubleFilterXML() throws IOException, NoSuchAlgorithmException {
		CountingInputStream in = new CountingInputStream(XMLFilterTest.class.getResourceAsStream("/podcastingnewsfeeds.xml"));
		MessageDigest md1 = MessageDigest.getInstance("SHA1");
		MessageDigest md2 = MessageDigest.getInstance("SHA1");
		DigestInputStream proxy1 = new DigestInputStream(new XMLFilter(in), md1);
		DigestInputStream proxy2 = new DigestInputStream(new XMLFilter(proxy1), md2);
		
		CountingOutputStream out = new CountingOutputStream(new NullOutputStream());
		IOUtils.copy(proxy2, out);
		assertTrue(Arrays.equals(md1.digest(), md2.digest()));
	}

	public void testLargeFile() throws IOException {
		CountingInputStream in = new CountingInputStream(XMLFilterTest.class
				.getResourceAsStream("/users-anon.xml"));
		XMLFilter proxy = new XMLFilter(in);
		CountingOutputStream out = new CountingOutputStream(new NullOutputStream());
		IOUtils.copy(proxy, out);
	}

	public void testLargeFileNoFilter() throws IOException {
		CountingInputStream in = new CountingInputStream(XMLFilterTest.class
				.getResourceAsStream("/users-anon.xml"));
		CountingOutputStream out = new CountingOutputStream(new NullOutputStream());
		IOUtils.copy(in, out);
		assertEquals(in.getByteCount(), out.getByteCount());
	}
	
	public void testCorrectBuffering() throws IOException {
		byte[] source = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><zz><a></a><b></b></zz>".getBytes();
		InputStream in = new ByteArrayInputStream(source);
		XMLFilter proxy = new XMLFilter(in);
		byte[] buffer = new byte[4];
		int read = 0, total = 0;
		StringBuffer soFar = new StringBuffer();
		do {
			read = proxy.read(buffer, 0, 4);
			soFar.append(new String(buffer));
			for (int i = 0; i < read; i++) {
				assertEquals("At position: "+ (total + i) , source[total+i], buffer[i]);
			}
			total += read;
				
		}while (read == 4);
	}
}