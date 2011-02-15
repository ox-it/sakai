package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;

/**
 * This is a fast XML filter which doesn't hold the whole XML document in memory.
 * Woodstox is used as it allows simple copying of events from input to output without too much
 * object creation.
 * @author buckett
 *
 */
public class XMLFilter extends ContentFilter {
	
	// These are grabbed statically as getting them can be a little slow.
	// The newFactory methods only came in as part of JDK 6 update 18.
	// http://www.oracle.com/technetwork/java/javase/6u18-142093.html
	protected static XMLInputFactory inputFactory = XMLInputFactory.newFactory("com.ctc.wstx.stax.WstxInputFactory", null);
	protected static XMLOutputFactory outputFactory = XMLOutputFactory.newFactory("com.ctc.wstx.stax.WstxOutputFactory", null);
	
	protected XMLStreamReader2 xmlReader;
	protected XMLStreamWriter2 xmlWriter;

	public XMLFilter(InputStream in, OutputStream out) throws IOException {

		try {
			xmlReader = (XMLStreamReader2) inputFactory.createXMLStreamReader(in);
			xmlWriter = (XMLStreamWriter2) outputFactory.createXMLStreamWriter(out);
			
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	
	public void filter() throws IOException {
		try {
			while (xmlReader.hasNext()) {
				write();
				xmlReader.next();
			}
			xmlWriter.flush();
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
	/**
	 * Subclass can change this method to change the XML that passes through.
	 * @param event
	 * @throws XMLStreamException 
	 */
	public void write() throws XMLStreamException {
		xmlWriter.copyEventFromReader(xmlReader, false);
	}
}
		