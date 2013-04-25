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
import java.util.ArrayList;
import java.util.Collection;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.sakaiproject.component.api.ServerConfigurationService;

public class SearchServiceImpl implements SearchService {

	private static ConcurrentUpdateSolrServer solrServer;

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
	}
	
	@Override
	public void addCourseGroup(CourseGroup course) {
		
		try {
			Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
			
			for (CourseComponent component : course.getComponents()) {
				
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
			
				doc.addField("presentation_identifier", component.getPresentationId());
				
				doc.addField("presentation_start", component.getStarts());
				doc.addField("presentation_end", component.getEnds());

				doc.addField("presentation_applyFrom", component.getOpens());
				doc.addField("presentation_applyUntil", component.getCloses());

				doc.addField("presentation_venue_identifier", component.getLocation());

				doc.addField("presentation_bookingEndpoint", component.getApplyTo());
				doc.addField("presentation_memberApplyTo", component.getMemberApplyTo());
				
				doc.addField("presentation_attendanceMode", component.getAttendanceModeText());
				doc.addField("presentation_attendancePattern", component.getAttendancePatternText());
				doc.addField("presentation_studyMode", component.getTeachingDetails());
				
				docs.add(doc);
				solrServer.add(doc);
			}
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
