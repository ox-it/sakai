package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom.JDOMException;
import org.xcri.exceptions.InvalidElementException;

public class XcriPopulatorInput implements PopulatorInput {
	
	DefaultHttpClient httpclient;
	
	public void init() {
		 httpclient = new DefaultHttpClient();
	}
	
	public void destroy() {
		// When HttpClient instance is no longer needed,
		// shut down the connection manager to ensure
		// immediate deallocation of all system resources
		httpclient.getConnectionManager().shutdown();
	}

	public InputStream getInput(PopulatorContext context) 
	throws PopulatorException {
		
		InputStream input;

		try {
			URL xcri = new URL(context.getURI());
			
			HttpHost targetHost = new HttpHost(xcri.getHost(), xcri.getPort(), xcri.getProtocol());

			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(targetHost.getHostName(), targetHost.getPort()),
					new UsernamePasswordCredentials(context.getUser(), context.getPassword()));

			HttpGet httpget = new HttpGet(xcri.toURI());
			HttpResponse response = httpclient.execute(targetHost, httpget);
			HttpEntity entity = response.getEntity();

			if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
	    		throw new PopulatorException("Invalid Response ["+response.getStatusLine().getStatusCode()+"]");
			}

			input = entity.getContent();

		} catch (MalformedURLException e) {
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (IllegalStateException e) {
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (IOException e) {
			throw new PopulatorException(e.getLocalizedMessage());

		} catch (URISyntaxException e) {
			throw new PopulatorException(e.getLocalizedMessage());

		}
		
		return input;
	}

}
