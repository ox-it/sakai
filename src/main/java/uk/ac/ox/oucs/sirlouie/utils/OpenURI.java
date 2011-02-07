package uk.ac.ox.oucs.sirlouie.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OpenURI {
	
	private URI uri;
	private Map<String, String> queryMap = new HashMap<String, String>();
	private String IDKEY = "doc";
	private static Log log = LogFactory.getLog(OpenURI.class);
	
	public OpenURI(String uri) throws URISyntaxException, UnsupportedEncodingException {
		
		log.debug(uri);
		this.uri = new URI(URLDecoder.decode(uri, "UTF-8"));
		
		String query = this.uri.getQuery();
		if (null == query) {
			throw new URISyntaxException(uri, "invalid DaiaURI ["+uri+"]");
		}
		String[] params = this.uri.getQuery().split("&");  
		for (String param : params) {   
		    String name = param.split("=")[0]; 
		    String value = null;
		    
		    try {
		    	value = param.split("=")[1]; 
		    	
		    } catch (Exception e) {
		    	
		    }
		    queryMap.put(name, value);  
		}
	}
	
	public URI getURI() {
		return uri;
	}
	
	public String getDoc() {
		if (queryMap.containsKey(IDKEY)) {
			return queryMap.get(IDKEY);
		}
	
		return null;
	}

}
