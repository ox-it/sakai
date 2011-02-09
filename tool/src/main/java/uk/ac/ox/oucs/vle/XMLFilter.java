package uk.ac.ox.oucs.vle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.input.NullInputStream;

public class XMLFilter extends ContentFilter {

	private ByteArrayOutputStream out;
	
	private InputStream buffer;
	
	protected XMLEventReader xmlReader;
	protected XMLEventWriter xmlWriter;

	public XMLFilter(InputStream in) throws IOException {
		this.out = new ByteArrayOutputStream();
		this.buffer = new ByteArrayInputStream(new byte[0]);

		try {
			xmlReader = XMLInputFactory.newInstance().createXMLEventReader(in, "UTF-8");
			xmlWriter = XMLOutputFactory.newInstance().createXMLEventWriter(out, "UTF-8");
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}

	
	public int readMore(int requested) throws IOException {

		XMLEvent event;
		try {
			if (buffer.available() > 0 || buffer.read() >= 0) {
				throw new IllegalStateException("Shouldn't readMore when data is still in the buffer.");
			}
			while (xmlReader.hasNext() && out.size() < requested) {
				event = xmlReader.nextEvent();
				write(event);
				xmlWriter.flush(); // So the out holder is updated.
			}
			buffer = new ByteArrayInputStream(out.toByteArray());
			int size = out.size();
			out.reset();
			return size;
		} catch (XMLStreamException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public int read() throws IOException {
		int data = buffer.read();
		if (data < 0) {
			readMore(1);
			data = buffer.read();
		}
		return data;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int totalRead = 0;

		do {
			int read = buffer.read(b, off+totalRead, len-totalRead);
			if (read < 1) {
				if (readMore(len-totalRead)> 0) {
					continue;
				} else {
					if (totalRead == 0) {
						totalRead = read;
					}
					break;
				}
			}
			totalRead += read;
		} while (totalRead < len); // Stop when we've read enough bytes.

		return totalRead;

	}
	
	/**
	 * Subclass can change this method to change the XML that passes through.
	 * @param event
	 * @throws XMLStreamException 
	 */
	public void write(XMLEvent event) throws XMLStreamException {
		xmlWriter.add(event);
	}
}
		