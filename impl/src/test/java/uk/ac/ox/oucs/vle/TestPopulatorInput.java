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

public class TestPopulatorInput implements PopulatorInput {

	public InputStream getInput(PopulatorContext context) {
		
		InputStream input;
		DefaultHttpClient httpclient = new DefaultHttpClient();

		try {
			URL xcri = new URL(context.getURI());
			if ("file".equals(xcri.getProtocol())) {
				input = xcri.openStream();

			} else {	
				HttpHost targetHost = new HttpHost(xcri.getHost(), xcri.getPort(), xcri.getProtocol());

				httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(targetHost.getHostName(), targetHost.getPort()),
						new UsernamePasswordCredentials(context.getUser(), context.getPassword()));

				HttpGet httpget = new HttpGet(xcri.toURI());
				HttpResponse response = httpclient.execute(targetHost, httpget);
				HttpEntity entity = response.getEntity();

				if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
					throw new IllegalStateException(
							"Invalid response ["+response.getStatusLine().getStatusCode()+"]");
				}

				input = entity.getContent();
			}
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
