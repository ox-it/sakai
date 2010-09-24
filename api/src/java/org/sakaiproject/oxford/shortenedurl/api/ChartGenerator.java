package org.sakaiproject.oxford.shortenedurl.api;

/**
 * Interface to handle the generation of charts via Google Charts
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ChartGenerator {

	public byte[] generateQRCode(String s, int height, int width);
}
