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
package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.sakaiproject.component.api.ServerConfigurationService;

public class SearchServiceImpl implements SearchService {

	private static final Log log = LogFactory.getLog(SearchServiceImpl.class);
	
	private ConcurrentUpdateSolrServer solrServer;
	private String solrUrl;

	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	public void init() {
		// Check things are going to work.
		try {
			log.info("Search using solr on: "+ getSolrUrl());
		} catch (IllegalStateException e) {
			log.error(e.getMessage());
		}
	}

	private String getSolrUrl() {
		if (solrUrl == null || solrUrl.trim().length() == 0) {
			throw new IllegalStateException("No Solr Server configured for SES, set ses.solr.server");
		}
		// Append trailing slash if there isn't one.
		return solrUrl + ((!solrUrl.endsWith("/"))?"":"/");

	}

	private SolrServer getSolrServer() {
		if (solrServer == null) {
			solrServer = new ConcurrentUpdateSolrServer(getSolrUrl(), 1000, 1);
		}
		return solrServer;
	}
	
	@Override
	public SearchResultsWrapper select(String query) throws IOException {
		HttpURLConnection connection = null;
		try {
			URL serverAddress = new URL(getSolrUrl()+"select?"+query);
			connection = (HttpURLConnection)serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.connect();
		
		} catch (MalformedURLException e) {
			log.error(e.getLocalizedMessage());
		}
		return new SearchResultsWrapper(connection);
	}
	
	@Override
	public void addCourseGroup(CourseGroup course) {
		
		try {
			
			SolrInputDocument doc = new SolrInputDocument();
			
			doc.addField("provider_title", course.getDepartment());
			doc.addField("course_muid", course.getMuid());
			doc.addField("course_identifier", course.getCourseId());
			doc.addField("course_title", course.getTitle());
			doc.addField("course_description", course.getDescription());
		
			for (CourseCategory category : course.getCategories(CourseGroup.Category_Type.RDF)) {
				doc.addField("course_subject_rdf", category.getName());
			}
		
			for (CourseCategory category : course.getCategories(CourseGroup.Category_Type.RM)) {
				doc.addField("course_subject_rm", category.getName());
			}
			
			doc.addField("course_class", "Graduate Training");
			
			// Choose the most recent component
			
			CourseComponent chosenComponent = null;
			Set<Date> baseDateSet = new HashSet<Date>();
			
			for (CourseComponent component : course.getComponents()) {
					
				if (null != component.getBaseDate()) {
				
					baseDateSet.add(component.getBaseDate());
					
				} else {
					if (null != component.getStartsText() &&
							 !component.getStartsText().isEmpty()) {
						baseDateSet.add(new Date(Long.MAX_VALUE));
					} else {
						baseDateSet.add(new Date(0));
					}
				}
					
				if (null == chosenComponent) {
					chosenComponent = component;
					continue;
				}
				if (null != component.getBaseDate()) {
					if (null != chosenComponent.getBaseDate()) {
						if (component.getBaseDate().after(chosenComponent.getBaseDate())) {
							chosenComponent = component;
						}
					} else {
						chosenComponent = component;
					}
				}
			}
			
			for (Date baseDate : baseDateSet) {
				doc.addField("course_basedate", baseDate);
			}
			
			if (null != chosenComponent) {	
				doc.addField("course_delivery", attendanceMode(chosenComponent));
				doc.addField("course_bookable", chosenComponent.getBookable());
			
				doc.addField("course_teaching_start", chosenComponent.getStarts());
				doc.addField("course_teaching_end", chosenComponent.getEnds());

				doc.addField("course_signup_open", chosenComponent.getOpens());
				doc.addField("course_signup_close", chosenComponent.getCloses());
				
				doc.addField("course_signup_opentext", chosenComponent.getOpensText());
				doc.addField("course_signup_closetext", chosenComponent.getClosesText());

				doc.addField("course_created", chosenComponent.getCreated());
				
			}
			getSolrServer().add(doc);
			
		} catch (SolrServerException e) {
			log.error(e.getLocalizedMessage() + " whilst processing course [" + course.getCourseId() + ":" + course.getTitle() +"]", e);
			
		} catch (IOException e) {
			log.error(e.getLocalizedMessage() + " whilst processing course [" + course.getCourseId() + ":" + course.getTitle() +"]", e);
			
		}
		
	}
	
	@Override
	public void deleteCourseGroup(CourseGroup course) {
		
		try {
			getSolrServer().deleteById(Integer.toString(course.getMuid()));
			
		} catch (SolrServerException e) {
			log.error(e.getLocalizedMessage() + " whilst processing course [" + course.getCourseId() + ":" + course.getTitle() +"]", e);
			
		} catch (IOException e) {
			log.error(e.getLocalizedMessage() + " whilst processing course [" + course.getCourseId() + ":" + course.getTitle() +"]", e);
			
		}
	}
	
	@Override
	public void deleteAll() {
		
		try {
			String query = "*:*";
			getSolrServer().deleteByQuery(query);
			
		} catch (SolrServerException e) {
			log.error(e.getLocalizedMessage(), e);
			
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
			
		}
	}
	
	@Override
	public void tidyUp() {
		
		try {
			getSolrServer().commit();
			getSolrServer().optimize();
			
		} catch (SolrServerException e) {
			log.error(e.getLocalizedMessage(), e);
			
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
			
		}
	}

	private String attendanceMode(CourseComponent component) {
		if ("CM".equals(component.getAttendanceMode())) {
			return "Face to face";
		}
		if ("ON".equals(component.getAttendanceMode())) {
			return "Online";
		}
		return component.getAttendanceModeText();
	}
}
