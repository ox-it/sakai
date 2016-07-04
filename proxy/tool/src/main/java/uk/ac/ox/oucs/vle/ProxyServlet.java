package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * Simple proxy servlet which passes through signed request/responses.
 * Only deals with GET requests.
 * @author buckett
 *
 */
public class ProxyServlet extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	private ProxyService proxyService;
	
	private FilterFactory filterFactory;
	
	private String[] propogatedResponseHeaders = {"Expires", "Content-Type", "Cache-Control", "Content-Length"};
	// When we are filtering the response we don't want the content length.
	private String[] filteredResponseHeaders = {"Expires", "Content-Type", "Cache-Control"};
	private String[] propogatedRequestHeaders = {"Accept", "Accept-Charset", "Cache-Control"};
	private int lengthLimit = 5 * 1024 * 1024; // 5 MB
	
	public void init() {
		proxyService = (ProxyService) ComponentManager.get(ProxyService.class.getName());
		filterFactory = FilterFactory.newInstance();
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String url = request.getParameter("url");
		String signature = request.getParameter("sig");
		
		if (url != null && signature != null) {
			// Good to go.
			String goodSignature = proxyService.getSignature(url);
			if (signature.equals(goodSignature)) {
				makeRequest(new URL(url), request, response);
			} else {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Signature supplied is invalid.");
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		
	}

	private void makeRequest(URL url, HttpServletRequest request, HttpServletResponse response) {
		HttpURLConnection connection = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			// This does follow HTTP redirects, which for a proxy makes sense.
			connection = (HttpURLConnection)url.openConnection();
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);
			connection.setRequestMethod("GET");
			for(String header: propogatedRequestHeaders) {
				String value = request.getHeader(header);
				if (value != null) {
					connection.setRequestProperty(header, value);
				}
			}
			connection.connect();
			
			// Check it's not too big.
			int length = connection.getContentLength();
			if (length > lengthLimit) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "We only proxy request up to "+ lengthLimit+ " bytes");
			} else {
				in = connection.getInputStream();
				out = response.getOutputStream();
				ContentFilter filter = filterFactory.getFilter(in, out, request.getParameter("filter"));
				
				if (filter != null) {
					copyHeaders(response, connection, true);
					filter.filter();
				} else {
					copyHeaders(response, connection, true);
					IOUtils.copy(in, out);
				}
				
			}
			
		} catch (IOException ioe) {
			
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					// Ignore.
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException ioe) {
					// Ignore
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private void copyHeaders(HttpServletResponse response,
			HttpURLConnection connection, boolean filtered) throws IOException {
		response.setStatus(connection.getResponseCode());
		Map<String, List<String>> respHeaders = connection.getHeaderFields();
		for(String header: (filtered)?filteredResponseHeaders:propogatedResponseHeaders) {
			List<String> values = respHeaders.get(header);
			if (values != null) {
				String value = join(values, ", "); // Multiple values are seperated by commas
				response.setHeader(header, value);
			}
		}
	}

	/**
	 * Change all the keys on the Map to lower case.
	 * @param connection The HttpURLConnection from which to get the headers.
	 * @return A Map with all the keys in lowercase.
	 */
	private Map<String, List<String>> getHeaders(HttpURLConnection connection) {
		Map<String, List<String>> source = connection.getHeaderFields();
		Map<String, List<String>> dest = new HashMap<String, List<String>>();
		for (String key : source.keySet()) {
			List<String> values = source.get(key);
			if (key != null) {
				key = key.toLowerCase();
				// Need to check incase two headers with different case.
				if (dest.containsKey(key)) {
					if (!(values instanceof ArrayList)) {
						values = new ArrayList<String>(values);// Create new list of values including existing ones incase list doesn't support writing.
					}
					values.addAll(dest.get(key));
				}
				dest.put(key, values);
			}
		}
		return dest;
	}
	
	/**
	 * Quick utility method to joins strings with a seprator.
	 * Saves pulling in a commons.
	 * @param values
	 * @param seperator
	 * @return
	 */
	private static String join(List<String> values, String seperator) {
		StringBuilder output = new StringBuilder();
		Iterator<String> it = values.iterator();
		if (it.hasNext()) {
			output.append(it.next());
		}
		while (it.hasNext()) {
			output.append(seperator);
			output.append(it.next());
		}
		return output.toString();
	}

}
