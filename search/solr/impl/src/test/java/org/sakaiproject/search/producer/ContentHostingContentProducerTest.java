package org.sakaiproject.search.producer;

import org.junit.Test;

import java.io.Reader;

import static org.junit.Assert.assertEquals;

/**
 * Created by buckett on 13/11/2014.
 */
public class ContentHostingContentProducerTest {

    @Test
    public void extractFilename() {
        ContentHostingContentProducer cp = new BinaryContentHostingContentProducer();
        assertEquals("file.txt", cp.getFileName("/group/content/file.txt"));
        assertEquals("directory", cp.getFileName("/group/content/directory/"));
        assertEquals("directory", cp.getFileName("/group/content/directory"));
        assertEquals(null, cp.getFileName("file"));
    }
}
