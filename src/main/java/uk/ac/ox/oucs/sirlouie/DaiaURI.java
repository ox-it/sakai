package uk.ac.ox.oucs.sirlouie;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class DaiaURI {
	
	private URI uri;
	private Map<String, String> queryMap = new HashMap<String, String>();
	private String IDKEY = "doc";
	
	public DaiaURI(String uri) throws URISyntaxException, UnsupportedEncodingException {
		this.uri = new URI(URLDecoder.decode(uri, "UTF-8"));
		
		String[] params = this.uri.getQuery().split("&");  
		     
		for (String param : params) {   
		    String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
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
