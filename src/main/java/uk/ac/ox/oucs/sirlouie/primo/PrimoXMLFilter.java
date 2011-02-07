package uk.ac.ox.oucs.sirlouie.primo;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import uk.ac.ox.oucs.sirlouie.reply.SearError;
import uk.ac.ox.oucs.sirlouie.reply.SearLibrary;
import uk.ac.ox.oucs.sirlouie.reply.SearLink;
import uk.ac.ox.oucs.sirlouie.reply.SearObject;

public class PrimoXMLFilter extends XMLFilterImpl {
	
	private String nameSpaceURI;
	private String tempVal;
	
	private SearLibrary searLibrary;
	
	private Collection<SearObject> beans = new ArrayList<SearObject>();
	
	public PrimoXMLFilter(String nameSpaceURI) {
		super();
		this.nameSpaceURI = nameSpaceURI;
	} 
	
	public Collection<SearObject> getBeans() {
		return beans;
	}
	
	
	@Override
	public void characters(char[] ch, int start, int length) 
	throws SAXException {
		
		StringBuffer sb = new StringBuffer();
		for (int i=start; i<start+length; i++) {
			switch (ch[i]) {
            	case '\n': break;
            	case '\r': break;
            	case '\f': break;
            	case '\t': break;
            	default: sb.append(ch[i]);
			}
		}
			
		//System.out.println("PrimoXMLFilter.characters ["+start+":"+length+":"+sb.toString()+"]");
		tempVal = new String(sb.toString());
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) 
	throws SAXException {
		
		//log.debug("startElement ["+uri+":"+localName+"]");
		
		tempVal="";
		
		if (uri.equals(nameSpaceURI) && localName.equals("ERROR")) {
			SearError searError = new SearError();
			for (int i=0; i<atts.getLength(); i++) {
				if (atts.getLocalName(i).equals("MESSAGE")) {
					searError.setMessage(atts.getValue(i));
				} else if (atts.getLocalName(i).equals("CODE")) {
					searError.setCode(Integer.parseInt(atts.getValue(i)));
				}
			}
			beans.add(searError);
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("LIBRARY")) {
			searLibrary = new SearLibrary();
			
		} 
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		
		//log.debug("endElement ["+qName+":"+localName+"]");
		
		if (uri.equals(nameSpaceURI) && localName.equals("ERROR")) {
			//throw new SAXException(searError.getMessage());
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("LIBRARY")) {
			beans.add(searLibrary);
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("institution")) {
			searLibrary.setInstitution(tempVal);
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("library")) {
			searLibrary.setLibrary(tempVal);
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("status")) {
			searLibrary.setStatus(tempVal);
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("collection")) {
			searLibrary.setCollection(tempVal);
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("callNumber")) {
			searLibrary.setCallNumber(tempVal);
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("url")) {
			searLibrary.setURL(tempVal);
			
		} else if (uri.equals(nameSpaceURI) && localName.equals("linktorsrc")) {
			SearLink searLink = new SearLink();
			searLink.setHref(tempVal);
			beans.add(searLink);
			
		}
	}

}
