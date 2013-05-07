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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.sakaiproject.component.api.ServerConfigurationService;

public class SearchServiceImpl implements SearchService {

	private static ConcurrentUpdateSolrServer solrServer;
	private static int recentDays;
	
	/**
	 * 
	 */
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void init() {
		String url = serverConfigurationService.getString("ses.solr.server", null);
		solrServer = new ConcurrentUpdateSolrServer(url, 1000, 1);
		
		recentDays = Integer.parseInt(serverConfigurationService.getString("recent.days", "14"));
	}
	
	@Override
	public void addCourseGroup(CourseGroup course) {
		
		try {
			
			SolrInputDocument doc = new SolrInputDocument();
			
			doc.addField("provider_title", course.getDepartment());
			doc.addField("course_identifier", course.getCourseId());
			doc.addField("course_title", course.getTitle());
			doc.addField("course_description", course.getDescription());
		
			for (CourseCategory category : course.getCategories(CourseGroup.Category_Type.RDF)) {
				doc.addField("course_subject_rdf", category.getName());
			}
		
			for (CourseCategory category : course.getCategories(CourseGroup.Category_Type.RM)) {
				doc.addField("course_subject_rm", category.getName());
			}
			
			// Choose the most recent component
			
			CourseComponent chosenComponent = null;
			Set<String> bookableSet = new HashSet<String>();
			Set<String> timescaleSet = new HashSet<String>();
			Date toDay = new Date();
			Date twoWeeksAgo = new DateTime().minusWeeks(2).toDate();
			
			for (CourseComponent component : course.getComponents()) {
				
				if (component.getCreated().after(twoWeeksAgo)) {
					timescaleSet.add("New Courses");
				}
					
				if (null != component.getBaseDate()) {
				
					if (component.getBaseDate().after(toDay)) {
						bookableSet.add("Yes");
						timescaleSet.add("Current Courses");
					} else {
						bookableSet.add("No");
						timescaleSet.add("Old Courses");
					}
					
				} else {
					if (null != component.getStartsText() &&
							 !component.getStartsText().isEmpty()) {
						bookableSet.add("Yes");
						timescaleSet.add("Current Courses");
					} else {
						bookableSet.add("No");
						timescaleSet.add("Old Courses");
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
			
			for (String timescale : timescaleSet) {
				doc.addField("course_timescale", timescale);
			}
			
			if (null != chosenComponent) {
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
}
