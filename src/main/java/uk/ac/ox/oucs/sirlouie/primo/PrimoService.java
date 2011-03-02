package uk.ac.ox.oucs.sirlouie.primo;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.ox.oucs.sirlouie.daia.ResponseBean;
import uk.ac.ox.oucs.sirlouie.reply.SearObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class PrimoService {
	
	Client client;
	WebResource webResource;
	private String nameSpaceURI = "http://www.exlibrisgroup.com/xsd/jaguar/search";
	private static Log log = LogFactory.getLog(PrimoService.class);
	
	public PrimoService(String webResourceURL) {
	    
		log.debug(webResourceURL);
		client = Client.create();
		webResource = client.resource(webResourceURL);
	}
	
	public ResponseBean getResource(String id) throws Exception {
		
		log.debug("getResource ["+id+"]");
	    MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	    params.add("institution", "OXVU1");
	    params.add("docId", id);
	    params.add("isLoggedIn", "false");
	    params.add("onCampus", "false");
	     
	    WebResource query = webResource.queryParams(params);
	    System.out.println("Query ["+query.getURI().toString()+"]");
	    
	    String responseXML = webResource.queryParams(params).get(String.class);
	    //log.debug("response ["+responseXML+"]");
	    
	    ResponseBean responseBean = new ResponseBean(id);
	    Collection<SearObject> beans = filterResponse(nameSpaceURI, responseXML);
		responseBean.addSearObjects(beans);
	    
		return responseBean;
	}
	
	public static Collection<SearObject> filterResponse(String nameSpaceURI, String response) 
	throws SAXException, IOException {
		
		StringReader xmlreader = new StringReader(response);
		InputSource source = new InputSource(xmlreader);
		
		PrimoXMLFilter filter = new PrimoXMLFilter(nameSpaceURI);
		filter.setParent(XMLReaderFactory.createXMLReader());
		filter.parse(source);
		
		return filter.getBeans();
	}

}
