package uk.ac.ox.oucs.sirlouie.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DaiaURI {
	
	private URI uri;
	private Map<String, String> queryMap = new HashMap<String, String>();
	// Originally the URLs contained a doc=.....
	private final String IDKEY = "doc";
	// Then the libraries changes to a different URL structure and the ID was in docId...
	private final String ALTERNATIVE_ID_KEY = "docId";

	private static Log log = LogFactory.getLog(DaiaURI.class);
	
	public DaiaURI(String uri) throws URISyntaxException, UnsupportedEncodingException {
		
		log.debug(uri);
		this.uri = new URI(URLDecoder.decode(uri, "UTF-8"));
		
		String query = this.uri.getQuery();
		if (null == query) {
			throw new URISyntaxException(uri, "invalid DaiaURI ["+uri+"]");
		}
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
		String id;
		id = queryMap.get(IDKEY);
		if (id == null || id.isEmpty()) {
			id = queryMap.get(ALTERNATIVE_ID_KEY);
		}
		return id;
	}

}
