package org.sakaiproject.search.producer;

import org.sakaiproject.search.api.EntityContentProducer;

import java.io.InputStream;

/**
 * Produce indexable documents while providing a binary stream.
 * <p>
 * Providing a binary stream through a ContentProducer allows to offload the document parsing.
 * </p>
 *
 * @author Colin Hebert
 */
public interface BinaryEntityContentProducer extends EntityContentProducer {
    /**
     * Generates a binary stream for the given reference.
     *
     * @param reference reference of the document
     * @return binary stream to the content of the referenced document, null if there is nothing to stream
     */
    InputStream getContentStream(String reference);

    /**
     * Obtains the content type of the referenced document.
     * <p>
     * The content type is usually a MIME type.<br />
     * The content type can be sent to the parsing system thus avoiding guess work to determine the type of document.
     * </p>
     *
     * @param reference reference of the document
     * @return the content type of the referenced document or null if it can't be provided
     */
    String getContentType(String reference);

    /**
     * Obtains the resource name of the referenced document.
     * <p>
     * The resource name is usually a file name with an extension.<br />
     * The resource name can be used by the parsing system to determine the content type.
     * </p>
     *
     * @param reference reference of the document
     * @return the resource name of the referenced document or null if it can't be provided
     */
    String getResourceName(String reference);
}
