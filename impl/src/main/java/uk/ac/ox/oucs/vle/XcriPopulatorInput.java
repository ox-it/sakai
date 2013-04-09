package uk.ac.ox.oucs.vle;

/*
 * #%L
 * Course Signup Implementation
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class XcriPopulatorInput implements PopulatorInput {
	
	private static final Log log = LogFactory.getLog(XcriPopulatorInput.class);
	
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
		
		InputStream input = null;
		HttpEntity entity = null;

		try {
			URL xcri = new URL(context.getURI());
			
			HttpHost targetHost = new HttpHost(xcri.getHost(), xcri.getPort(), xcri.getProtocol());

			httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(targetHost.getHostName(), targetHost.getPort()),
					new UsernamePasswordCredentials(context.getUser(), context.getPassword()));

			HttpGet httpget = new HttpGet(xcri.toURI());
			HttpResponse response = httpclient.execute(targetHost, httpget);
			entity = response.getEntity();

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

		} finally {
			if (null == input && null != entity) {
				try {
					entity.getContent().close();
				} catch (IOException e) {
					log.error("IOException ["+e+"]");
				}
			}
		}
		return input;
	}

}
