package uk.ac.ox.oucs.sirlouie.sfx;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.ox.oucs.sirlouie.daia.Document;
import uk.ac.ox.oucs.sirlouie.daia.ResponseBean;
import uk.ac.ox.oucs.sirlouie.reply.SearObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SFXService {
	
	Client client;
	WebResource webResource;
	private static Log log = LogFactory.getLog(SFXService.class);
	
	public SFXService(String webResourceURL) {
	    
		log.debug(webResourceURL);
		client = Client.create();
		webResource = client.resource(webResourceURL);
	}
	
	public ResponseBean getResource(String id) throws Exception {
		
		log.debug("getResource");
	    MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	    params.add("sfx.response_type", "simplexml");
	    
	    String responseXML = webResource.queryParams(params).get(String.class);
	    log.debug("response ["+responseXML+"]");
	    
	    ResponseBean responseBean = new ResponseBean();
	    Collection<SearObject> beans = filterResponse(responseXML);
		
		Document document = new Document(id, null);
		document.addItems(beans);
		responseBean.addDocument(document);
	    
		return responseBean;
	}
	
	public static Collection<SearObject> filterResponse(String response) 
	throws SAXException, IOException {
		
		StringReader xmlreader = new StringReader(response);
		InputSource source = new InputSource(xmlreader);
		
		SFXXMLFilter filter = new SFXXMLFilter();
		filter.setParent(XMLReaderFactory.createXMLReader());
		filter.parse(source);
		
		return filter.getBeans();
	}

}
