package uk.ac.ox.oucs.vle;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class ImageResizeFilterTest extends TestCase {

	public void testFormatPNG() throws IOException {
		resizeResource("/sample.png");
	}
	
	public void testFomatJPG() throws IOException {
		resizeResource("/sample.jpg");
	}
	
	public void testFomatGIF() throws IOException {
		resizeResource("/sample.gif");
	}
	
	public void testBadData() {
		try {
			CountingInputStream in = new CountingInputStream(ImageResizeFilterTest.class.getResourceAsStream("/random.jpg"));
			ImageResizeFilter filter = new ImageResizeFilter(in, 10, 10);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IOUtils.copy(filter, out);
			fail("Should have thrown an error as it's not an image.");
		} catch (IOException ioe) {
			// Good.
		}
	}

	private void resizeResource(String resource) throws IOException {
		CountingInputStream in = new CountingInputStream(ImageResizeFilterTest.class.getResourceAsStream(resource));
		ImageResizeFilter filter = new ImageResizeFilter(in, 10, 10);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(filter, out);
		assertTrue(in.getByteCount() > out.size());
	}
}
