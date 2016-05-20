package uk.ac.ox.oucs.sirlouie.sfx;

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

public class SFXXMLFilter extends XMLFilterImpl {
	
	private StringBuffer tempVal;
	
	private SearLibrary searLibrary;
	
	private Collection<SearObject> beans = new ArrayList<SearObject>();
	
	private static Log log = LogFactory.getLog(SFXXMLFilter.class);
	
	public SFXXMLFilter() {
		super();
	} 
	
	/**
	 * Filter the collection of beans
	 * remove objects without a url
	 * @return
	 */
	public Collection<SearObject> getBeans() {
		
		Collection<SearObject> myBeans = new ArrayList<SearObject>();
		for (SearObject bean : beans) {
			
			if (!validString(bean.getURL())) {
				continue;
			}
			
			if (bean instanceof SearLibrary) {
				SearLibrary library = (SearLibrary)bean;
				
				if (!"getFullTxt".equals(library.getType())) {
					continue;
				}
			}
			
			myBeans.add(bean);
		}
		return myBeans;
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
			
		//log.debug(".characters ["+start+":"+length+":"+sb.toString()+"]");
		tempVal.append(new String(sb.toString()));
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) 
	throws SAXException {
		
		//log.debug("startElement ["+localName+"]");
		tempVal=new StringBuffer();
		
		if (localName.equals("ERROR")) {
			SearError searError = new SearError();
			for (int i=0; i<atts.getLength(); i++) {
				if (atts.getLocalName(i).equals("MESSAGE")) {
					searError.setMessage(atts.getValue(i));
				} else if (atts.getLocalName(i).equals("CODE")) {
					searError.setCode(Integer.parseInt(atts.getValue(i)));
				}
			}
			beans.add(searError);
			
		} else if (localName.equals("target")) {
			searLibrary = new SearLibrary();
			
		} 
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) 
	throws SAXException {
		
		//log.debug("endElement ["+qName+":"+localName+"]");
		
		if (localName.equals("ERROR")) {
			//throw new SAXException(searError.getMessage());
			
		} else if (localName.equals("target")) {
			beans.add(searLibrary);
			
		} else if (localName.equals("target_service_id")) {
			searLibrary.setId(tempVal.toString());
			
		} else if (localName.equals("target_public_name")) {
			searLibrary.setLabel(tempVal.toString());
			
		} else if (localName.equals("target_name")) {
		} else if (localName.equals("service_type")) {
			searLibrary.setType(tempVal.toString());
			
		} else if (localName.equals("parser")) {
		} else if (localName.equals("parse_param")) {
		} else if (localName.equals("proxy")) {
		} else if (localName.equals("crossref")) {
		} else if (localName.equals("note")) {
		} else if (localName.equals("authentication")) {
		} else if (localName.equals("char_set")) {
		} else if (localName.equals("displayer")) {
			
		} else if (localName.equals("target_url")) {
			searLibrary.setURL(tempVal.toString());
			searLibrary.setAvailableURL(tempVal.toString());
			
		} else if (localName.equals("linktorsrc")) {
			SearLink searLink = new SearLink();
			searLink.setHref(tempVal.toString());
			beans.add(searLink);
		}
	}
	
	public static boolean validString(String s) {
		if (null == s || s.trim().isEmpty()) {
			return false;
		}
		return true;
	}

}
