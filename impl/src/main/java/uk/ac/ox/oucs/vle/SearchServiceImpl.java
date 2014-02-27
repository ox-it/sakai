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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.apache.solr.common.SolrInputDocument;

public class SearchServiceImpl implements SearchService {

	private static final Log log = LogFactory.getLog(SearchServiceImpl.class);
	
	private ConcurrentUpdateSolrServer solrServer;
	private String solrUrl;
	private BaseDateComparator baseDateComparator = new BaseDateComparator();

	public void setSolrUrl(String solrUrl) {
		this.solrUrl = solrUrl;
	}

	public void init() {
		// Check things are going to work.
		try {
			String url = getSolrUrl();
			log.info("Search using solr on: "+ url);
		} catch (IllegalStateException e) {
			log.error(e.getMessage());
		}
	}

	protected String getSolrUrl() {
		if (solrUrl == null || solrUrl.trim().length() == 0) {
			throw new IllegalStateException("No Solr Server configured for SES, set ses.solr.server");
		}
		// Append trailing slash if there isn't one.
		return solrUrl + ((solrUrl.endsWith("/"))?"":"/");

	}

	protected SolrServer getSolrServer() {
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
	public boolean addCourseGroup(CourseGroup course) {
		try {
			
			SolrInputDocument doc = new SolrInputDocument();
			
			doc.addField("provider_title", course.getDepartment());
			doc.addField("course_muid", course.getMuid());
			doc.addField("course_identifier", course.getCourseId());
			doc.addField("course_title", course.getTitle());
			doc.addField("course_description", course.getDescription());
			// We add the hidden courses to the index even though we don't return them as this way if we wanted to use
			// Solr as the source of all data we can still lookup details of hidden courses.
			doc.addField("course_hidden", course.getHideGroup());
		
			for (CourseCategory category : course.getCategories(CourseGroup.CategoryType.RDF)) {
				doc.addField("course_subject_rdf", category.getName());
			}
		
			for (CourseCategory category : course.getCategories(CourseGroup.CategoryType.RM)) {
				doc.addField("course_subject_rm", category.getName());
			}

			// These need splitting into 2 facets.
			for (CourseCategory category : course.getCategories(CourseGroup.CategoryType.VITAE)) {
				// We could keep these separate in the database by using 2 CategoryTypes but that
				// feels a little too much like overengineering.
				if (category.getCode().length() == 1) {
					doc.addField("course_subject_vitae_domain", category.getName());
				} else if (category.getCode().length() == 2) {
					doc.addField("course_subject_vitae_subdomain", category.getName());
				} else {
					log.warn("Unsupported vitae code: "+ category.getCode()+ " on course "+ course.getMuid());
				}
			}
			
			// This wasn't updated to Researcher Training in WL-3217 because it doesn't appear to be used anywhere.
			doc.addField("course_class", "Graduate Training");

			// Choose the most recent component
			CourseComponent chosenComponent = null;
			
			for (CourseComponent component : course.getComponents()) {
				if (null == chosenComponent) {
					chosenComponent = component;
					continue;
				}
				if (baseDateComparator.compare(chosenComponent, component) < 0) {
					chosenComponent = component;
				}
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
				doc.addField("course_basedate", BaseDateComparator.getBaseDate(chosenComponent));
			}
			getSolrServer().add(doc);
			return true;

		} catch (SolrServerException e) {
			log.error(e.getLocalizedMessage() + " whilst processing course [" + course.getCourseId() + ":" + course.getTitle() +"]", e);
			
		} catch (IOException e) {
			log.error(e.getLocalizedMessage() + " whilst processing course [" + course.getCourseId() + ":" + course.getTitle() +"]", e);
		}
		return false;
		
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
