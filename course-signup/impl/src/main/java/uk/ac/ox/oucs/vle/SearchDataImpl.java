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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SearchDataImpl implements Module {

	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}
	
	private static final Log log = LogFactory.getLog(SearchDataImpl.class);
	
	/**
	 * The searchService.
	 */
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	public void update() {

		int indexed = 0, failed = 0;
		try {
			searchService.deleteAll();
		
			List<CourseGroupDAO> groupDaos = dao.findAllGroups();
		
			for(CourseGroupDAO groupDao: groupDaos) {
				if (searchService.addCourseGroup(new CourseGroupImpl(groupDao, null))) {
					indexed++;
				} else {
					failed++;
				}
			}
		
		} finally {
			
			searchService.tidyUp();
		}
		
		log.info("Completed update of Solr Search Data, indexed: "+ indexed+ " failed: "+ failed);
	}
	
}
