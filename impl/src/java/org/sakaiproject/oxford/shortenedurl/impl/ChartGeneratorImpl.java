package org.sakaiproject.oxford.shortenedurl.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.sakaiproject.oxford.shortenedurl.api.ChartGenerator;

/**
 * Implementation of {@link org.sakaiproject.oxford.shortenedurl.api.ChartGenerator}
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public class ChartGeneratorImpl implements ChartGenerator {

	private final String CHART_URL="http://chart.apis.google.com/chart";
	private static Log log = LogFactory.getLog(OxfordShortenedUrlServiceImpl.class);

	
	public byte[] generateQRCode(String s, int height, int width) {
		
		String size = Integer.toString(height) + "x" + Integer.toString(width);
		
		Map<String,String> params = new HashMap<String,String>();
		
		params.put("cht", "qr");
		params.put("chs", size);
		params.put("chl", s);
		
		return doGet(CHART_URL, params);
	}

	
	
	/**
	 * Make a GET request and append the Map of parameters onto the query string.
	 * @param address		the fully qualified URL to make the request to
	 * @param parameters	the Map of parameters, ie key,value pairs
	 * @return	byte[] of the content
	 */
	private byte[] doGet(String address, Map<String, String> parameters){
		try {
			
			List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
			
			for (Map.Entry<String,String> entry : parameters.entrySet()) {
				queryParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			
			URI uri = URIUtils.createURI(null, address, -1, null, URLEncodedUtils.format(queryParams, "UTF-8"), null);
			
			log.debug(uri.toString());
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(address);
			HttpResponse response = httpclient.execute(httpget);
			
			//check reponse code
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() != 200) {
				log.error("Error requesting URL. Status: " + status.getStatusCode() + ", reason: " + status.getReasonPhrase());
				return null;
			}
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return EntityUtils.toByteArray(entity);
			}
		
		} catch (Exception e) {
			log.error(e.getClass() + ":" + e.getMessage());
		} 
		return null;
	}
	
	
	
}
