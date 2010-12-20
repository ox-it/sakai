package uk.ac.ox.oucs.sirlouie;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;

import javax.ws.rs.core.MultivaluedMap;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import uk.ac.ox.oucs.sirlouie.daia.ResponseBean;
import uk.ac.ox.oucs.sirlouie.reply.SearObject;

import com.sun.jersey.api.client.*;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class PrimoService {
	
	Client client;
	WebResource webResource;
	private String nameSpaceURI = "http://www.exlibrisgroup.com/xsd/jaguar/search";
	
	public PrimoService(String webResourceURL) {
	    
		client = Client.create();
		//webResource = client.resource("http://primo-s-web-2.sers.ox.ac.uk:1701/PrimoWebServices/xservice/getit");
		webResource = client.resource(webResourceURL);
	}
	
	public ResponseBean getResource(String id) throws Exception {
		
	    MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	    params.add("institution", "OXVU1");
	    params.add("docId", id);
	    params.add("isLoggedIn", "false");
	    params.add("onCampus", "false");
	     
	    // for testing
	    //WebResource resource = webResource.queryParams(params);
	    //System.out.println("PrimoService.webResource("+resource.toString()+")");
	    
	    String responseXML = webResource.queryParams(params).get(String.class);
	    
	    // for testing 
	    /*
	    try {
	    	BufferedWriter out = new BufferedWriter(new FileWriter("response.xml"));
	    	out.write(responseXML);
	    	out.close();
	    	
	    } catch (IOException e)	{
	    	System.out.println("Exception ");
	    }
	    */
	    
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
