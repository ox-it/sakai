package uk.ac.ox.oucs.vle;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;

public class ImageResizeFilterTest extends TestCase {

	public void testFormatPNG() throws IOException {
		resizeResource("/sample.png", 10, 10, true);
	}
	
	public void testFomatJPG() throws IOException {
		resizeResource("/sample.jpg", 10, 10, true);
	}
	
	public void testFomatGIF() throws IOException {
		resizeResource("/sample.gif", 10, 10, true);
	}

	public void testIncreaseSize() throws IOException {
		// We had a bug where upsizing the image resulted in a non-ending loop.
		resizeResource("/sample.png", 1000, 1000, false);
	}

	public void testNoChange() throws IOException {
		// Check things work if we don't actually need to change anything.
		// The size changes, but I'm not too fussed about that. Just that we don't end up in a loop.
		resizeResource("/sample.png", 233, 194, true);
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

	private void resizeResource(String resource, int width, int height, boolean shrink) throws IOException {
		CountingInputStream in = new CountingInputStream(ImageResizeFilterTest.class.getResourceAsStream(resource));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageResizeFilter filter = new ImageResizeFilter(in, out, width, height);
		filter.filter();
		assertEquals(shrink, in.getByteCount() > out.size());
	}
}
