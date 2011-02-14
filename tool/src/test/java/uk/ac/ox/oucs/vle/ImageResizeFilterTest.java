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
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageResizeFilter filter = new ImageResizeFilter(in, out, 10, 10);
			filter.filter();
			fail("Should have thrown an error as it's not an image.");
		} catch (IOException ioe) {
			// Good.
		}
	}

	private void resizeResource(String resource) throws IOException {
		CountingInputStream in = new CountingInputStream(ImageResizeFilterTest.class.getResourceAsStream(resource));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageResizeFilter filter = new ImageResizeFilter(in, out, 10, 10);
		filter.filter();
		assertTrue(in.getByteCount() > out.size());
	}
}
