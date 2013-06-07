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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;
import org.sakaiproject.component.api.ServerConfigurationService;

public class SearchServiceImpl implements SearchService {

	private static final Log log = LogFactory.getLog(SearchServiceImpl.class);
	
	private static ConcurrentUpdateSolrServer solrServer;
	private String solrUrl;
	/**
	 * 
	 */
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void init() {
		solrUrl = serverConfigurationService.getString("ses.solr.server", null);
		solrServer = new ConcurrentUpdateSolrServer(solrUrl, 1000, 1);
		
		Integer.parseInt(serverConfigurationService.getString("recent.days", "14"));
	}
	
	public void close() {
		
	}
	
	@Override
	public SearchResultsWrapper select(String query) {
		HttpURLConnection connection = null;
		try {
			URL serverAddress = new URL(solrUrl+"select?"+query);
			connection = (HttpURLConnection)serverAddress.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.connect();
		
		} catch (MalformedURLException e) {
			log.error(e.getLocalizedMessage());
			
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
			
		} 
		
		return new SearchResultsWrapper(connection);
	}
	
	@Override
	public void addCourseGroup(CourseGroup course) {
		
		try {
			
			SolrInputDocument doc = new SolrInputDocument();
			
			doc.addField("provider_title", course.getDepartment());
			doc.addField("course_muid", new Integer(course.getMuid()).toString());
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
			Set<String> bookableSet = new HashSet<String>();
			Set<String> timeframeSet = new HashSet<String>();
			Date toDay = new Date();
			Date twoWeeksAgo = new DateTime().minusWeeks(2).toDate();
			
			for (CourseComponent component : course.getComponents()) {
				
				if (component.getCreated().after(twoWeeksAgo)) {
					timeframeSet.add("New Courses");
				}
					
				if (null != component.getBaseDate()) {
				
					if (component.getBaseDate().after(toDay)) {
						bookableSet.add("Yes");
						timeframeSet.add("Current Courses");
					} else {
						bookableSet.add("No");
						timeframeSet.add("Old Courses");
					}
					
				} else {
					if (null != component.getStartsText() &&
							 !component.getStartsText().isEmpty()) {
						bookableSet.add("Yes");
						timeframeSet.add("Current Courses");
					} else {
						bookableSet.add("No");
						timeframeSet.add("Old Courses");
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
			
			for (String timeframe : timeframeSet) {
				doc.addField("course_timeframe", timeframe);
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

				doc.addField("course_basedate", chosenComponent.getBaseDate());
				
			}
			solrServer.add(doc);
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void deleteCourseGroup(CourseGroup course) {
		
		try {
			solrServer.deleteById(new Integer(course.getMuid()).toString());
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteAll() {
		
		try {
			String query = new String("*:*");
			solrServer.deleteByQuery(query);
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void tidyUp() {
		
		try {
			solrServer.commit();
			solrServer.optimize();
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
