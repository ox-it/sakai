package org.sakaiproject.shortenedurl.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RandomisedUrlServiceTest {

    private RandomisedUrlService service = new RandomisedUrlService();

    @Test
    public void testEncodeNoChange() {
        assertEquals("http://www.google.com/", service.encodeUrl("http://www.google.com/"));
    }

    @Test
    public void testEncodeWithSpace() {
        assertNotEquals("http://example.com/my file.txt", service.encodeUrl("http://example.com/my file.txt"));
    }

    @Test
    public void testEncodeWithUTF8() {
        assertNotEquals("http://example.com/台灣", service.encodeUrl("http://example.com/台灣"));
    }

    @Test
    public void testEncodeWithParams() {
        assertEquals("http://example.com/?key=value", service.encodeUrl("http://example.com/?key=value"));
    }

    @Test
    public void testEncodeWithAnchor() {
        assertEquals("http://example.com/file.html#anchor", service.encodeUrl("http://example.com/file.html#anchor"));
    }

    // You can't know what to do about the next 2
//    @Test
//    public void testEncodeParamWithAmp() {
//        assertEquals("http://example.com/?param=bed%26breakfast", service.encodeUrl("http://example.com/?param=bed%26breakfast"));
//    }

    @Test
    public void testEncodeParams() {
        assertEquals("http://example.com/?param1=yes&param2=no", service.encodeUrl("http://example.com/?param1=yes&param2=no"));
    }

}
