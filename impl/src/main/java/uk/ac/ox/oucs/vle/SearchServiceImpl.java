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

public class SearchServiceImpl implements SearchService {

	private static int fetchSize = 1000;
	private static String url = "http://localhost:8983/solr/core1/";
	private static HttpSolrServer solrCore;

	public void init() {
		solrCore = new HttpSolrServer(url);
	}
	
	@Override
	public void addCourseGroup(CourseGroup group) {
		// TODO Auto-generated method stub
		
	}

}
