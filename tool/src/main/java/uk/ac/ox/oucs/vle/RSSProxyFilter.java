package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

public class RSSProxyFilter extends XMLFilter {

	private ProxyService proxyService;
	
	private String newWidth;
	private String newHeight;
	
	private String thumbnail = "thumbnail";

	public RSSProxyFilter(InputStream in, OutputStream out, ProxyService proxy, String width, String height) throws IOException {
		super(in,out);
		this.proxyService = proxy;
		this.newWidth = width;
		this.newHeight = height;
	}
	
	@Override
	public void write() throws XMLStreamException {
		if (xmlReader.isStartElement()) {
			
			// Would be nice if there was a faster way to know if this was the matching element.
			if (thumbnail.equals(xmlReader.getName().getLocalPart())) {				
				xmlWriter.writeStartElement(xmlReader.getPrefix(), xmlReader.getLocalName(), null);
				for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
						String name = xmlReader.getAttributeLocalName(i);
						String value = xmlReader.getAttributeValue(i);
						if ("url".equals(name)) {
							xmlWriter.writeAttribute(name, proxyUrl(value));
						} else if ("height".equals(name)) {
							xmlWriter.writeAttribute(name, height(value));
						} else if ("width".equals(name)) {
							xmlWriter.writeAttribute(name, width(value));
						} else {
							xmlWriter.writeAttribute(name, value);
						}
					}
			} else { 
				super.write();
			}
		} else {
			super.write();
		}
	}
	
	public String height(String original) {
		if (newHeight != null) {
			return escapeAttributes(newHeight);
		}
		return original;
	}
	

	public String width(String original) {
		if (newWidth != null) {
			return escapeAttributes(newWidth);
		}
		return original;
	}
	
	
	public String proxyUrl(String original) {
		return proxyService.getProxyURL(original) + "&filter=imageResize("+newWidth+ ","+newHeight+")";
	}
	
	private String escapeAttributes(String source) {
		StringBuilder output = new StringBuilder(source.length());
		
		for(int i = 0; i < source.length(); i++) {
			char c = source.charAt(i);
			switch(c) {
				case '&':
					output.append("&amp;");
					break;
				case '<':
					output.append("&lt;");
					break;
				case '>':
					output.append("&gt;");
					break;
				case '"':
					output.append("&quot;");
					break;
				case '\'':
					output.append("&#39;");
					break;
				default:
					output.append(c);
			}
		}
		return output.toString();
	}

}
