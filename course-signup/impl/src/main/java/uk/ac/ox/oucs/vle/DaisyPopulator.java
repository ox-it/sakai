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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class DaisyPopulator implements Populator {

	private final Logger log = LoggerFactory.getLogger(DaisyPopulator.class);

	/**
	 * The DAO to update our entries through.
	 */
	private CourseDAO dao;
	public void setCourseDao(CourseDAO dao) {
		this.dao = dao;
	}

	/**
	 * 
	 */
	private Populator populator;
	public void setPopulator(Populator populator) {
		this.populator = populator;
	}
	
	/**
	 * The service to supply search services
	 */
	private SearchService search;
	public void setSearchService(SearchService search) {
		this.search = search;
	}

	private NowService nowService;
	public void setNowService(NowService nowService) {
		this.nowService = nowService;
	}

	@Override
	public void update(PopulatorContext context) {

		dao.flagSelectedDaisyCourseGroups(context.getName(), nowService.getNow());
		dao.flagSelectedDaisyCourseComponents(context.getName(), nowService.getNow());

		populator.update(context);

		Collection<CourseGroupDAO> groups = dao.deleteSelectedCourseGroups(context.getName());
		for (CourseGroupDAO group : groups) {
			search.deleteCourseGroup(new CourseGroupImpl(group, null));
			try {
				context.getDeletedLogWriter().write("Deleting course ["+group.getCourseId()+" "+group.getTitle()+"]"+"\n");
			} catch (IOException e) {
				log.warn("Failed to write deleted log.", e);
			}
		}

		Collection<CourseComponentDAO> components = dao.deleteSelectedCourseComponents(context.getName());
		for (CourseComponentDAO component : components) {
			try {
				context.getDeletedLogWriter().write("Deleting component ["+component.getComponentId()+" "+component.getTitle()+"]"+"\n");
			} catch (IOException e) {
				log.warn("Failed to write deleted log.", e);
			}
		}
	}

}
