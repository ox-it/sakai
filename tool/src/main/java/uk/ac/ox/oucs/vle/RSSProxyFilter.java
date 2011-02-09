package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class RSSProxyFilter extends XMLFilter {

	private XMLEventFactory factory;
	private ProxyService proxyService;
	
	private String newWidth;
	private String newHeight;

	public RSSProxyFilter(InputStream in, ProxyService proxy, String width, String height) throws IOException {
		super(in);
		factory = XMLEventFactory.newInstance();
		this.proxyService = proxy;
		this.newWidth = width;
		this.newHeight = height;
	}
	
	@Override
	public void write(XMLEvent event) throws XMLStreamException {
		if (event.isStartElement()) {
			StartElement elementEvent = (StartElement)event;
			if ("thumbnail".equals(elementEvent.getName().getLocalPart())) {
				final Iterator<Attribute>origAttrs = elementEvent.getAttributes();
				Iterator<Attribute> replAttrs = new Iterator<Attribute>() {
					
					public boolean hasNext() {
						return origAttrs.hasNext();
					}

					public Attribute next() {
						Attribute next = origAttrs.next();
						String name = next.getName().getLocalPart();
						if ("url".equals(name)) {
							return factory.createAttribute(next.getName(), proxyUrl(next.getValue()));
						} else if ("height".equals(name)) {
							return factory.createAttribute(next.getName(), height(next.getValue()));
						} else if ("width".equals(name)) {
							return factory.createAttribute(next.getName(), width(next.getValue()));
						} else {
							return next;
						}
					}

					public void remove() {
						origAttrs.remove();
						
					}
				};
				StartElement replacement = factory.createStartElement(elementEvent.getName(), replAttrs, elementEvent.getNamespaces());
				xmlWriter.add(replacement);
			} else { 
				xmlWriter.add(event);
			}
		} else {
			xmlWriter.add(event);
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
