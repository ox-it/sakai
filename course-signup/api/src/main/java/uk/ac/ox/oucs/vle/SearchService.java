/*
 * #%L
 * Course Signup API
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

/**
 * This service handles the updating of documents in the solr server and the querying of that solr server.
 */
public interface SearchService {

	/**
	 * Perform a solr search.
	 * @param query The parameters to pass to solr server.
	 * @return A results wrapper, the calling client must call disconnect on this once it's finished with it.
	 * @throw IOException When there is an IO problem talking to the search server.
	 */
	public ResultsWrapper select(String query) throws IOException;
	
	public boolean addCourseGroup(CourseGroup group);
	
	public void deleteCourseGroup(CourseGroup group);
	
	public void deleteAll();
	
	public void tidyUp();

}
