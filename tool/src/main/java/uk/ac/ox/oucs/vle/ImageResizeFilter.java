package uk.ac.ox.oucs.vle;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageResizeFilter extends ContentFilter {

	/**
	 * The maximum size in pixel we will output an image of. This is to
	 * prevent someone attempting to eat up all the heap by making requests for large images.
	 */
	public static final int MAX_SIZE = 1500;

	private InputStream in;
	private OutputStream out;
	private int width;
	private int height;

	/**
	 * Filter an image, changing it's size. See {@link #MAX_SIZE} for the upper limit.
	 * @param in The input stream to read the image from.
	 * @param out The output stream to write the image to.
	 * @param width The desired width of the output image.
	 * @param height The desired height of the output image.
	 */
	public ImageResizeFilter(InputStream in, OutputStream out, int width, int height) {
		this.in = in;
		this.out = out;
		// Bound the size to be 1 or greater and MAX_SIZE or less.
		this.width = Math.max(Math.min(width, MAX_SIZE), 1);
		this.height = Math.max(Math.min(height, MAX_SIZE), 1);
	}
	
	public void filter()  throws IOException {

		// Don't use ImageIO.read() so we know what format we're dealing with.
		ImageInputStream imageStream = ImageIO.createImageInputStream(in);
		Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
		if (!readers.hasNext()) {
			throw new IOException("Unknown format.");
		}
		ImageReader reader = readers.next();
		String format = reader.getFormatName();
		reader.setInput(imageStream, true);
		BufferedImage image = reader.read(0);
		reader.dispose();

		BufferedImage buffer = getScaledInstance(image, width, height,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
		ImageIO.write(buffer, format, out);
	}

	/**
	 * Convenience method that returns a scaled instance of the provided
	 * {@code BufferedImage}.
	 * 
	 * From http://today.java.net/pub/a/today/2007/04/03/perils-of-image-
	 * getscaledinstance.html
	 * 
	 * @param img
	 *            the original image to be scaled
	 * @param targetWidth
	 *            the desired width of the scaled instance, in pixels
	 * @param targetHeight
	 *            the desired height of the scaled instance, in pixels
	 * @param hint
	 *            one of the rendering hints that corresponds to
	 *            {@code RenderingHints.KEY_INTERPOLATION} (e.g.
	 *            {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
	 *            {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
	 *            {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
	 * @param higherQuality
	 *            if true, this method will use a multi-step scaling technique
	 *            that provides higher quality than the usual one-step technique
	 *            (only useful in downscaling cases, where {@code targetWidth}
	 *            or {@code targetHeight} is smaller than the original
	 *            dimensions, and generally only when the {@code BILINEAR} hint
	 *            is specified)
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	public BufferedImage getScaledInstance(BufferedImage img, int targetWidth,
			int targetHeight, Object hint, boolean higherQuality) {
		int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage) img;
		int w = img.getWidth();
		int h = img.getHeight();

		do {
			w = adjustSize(w, targetWidth, higherQuality);
			h = adjustSize(h, targetHeight, higherQuality);

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		} while (w != targetWidth || h != targetHeight);

		return ret;
	}

	/**
	 * Attempts to bring the image closer to the target size.
	 *
	 * @param current The current size of the image.
	 * @param target The target size.
	 * @param higherQuality If true then we do multiple re-size operations.
	 * @return The new size. The returned value will always be different to the passed value until they are the
	 * same.
	 */
	private int adjustSize(int current, int target, boolean higherQuality) {
		if (current > target) {
			if (higherQuality) {
				// Use multi-step technique: start with original size, then
				// scale down in multiple passes with drawImage()
				// until the target size is reached
				current /= 2;
			} else {
				// Use one-step technique: scale directly from original
				// size to target size with a single drawImage() call
				current = target;
			}
		}
		// Catch when the image needs to grow in size.
		if (current < target) {
			current = target;
		}
		return current;
	}

}
