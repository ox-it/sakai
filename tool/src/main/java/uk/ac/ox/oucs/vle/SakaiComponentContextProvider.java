/*
 * #%L
 * Course Signup Webapp
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

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * Allows to component manager references to be "injected" into resources without an explicit dependency.
 * @author buckett
 */
@Provider
public class SakaiComponentContextProvider implements ContextResolver<Object> {
	
	private static final Log log = LogFactory.getLog(SakaiComponentContextProvider.class);
	
	public Object getContext(Class<?> type) {
		try {
			return (Object) ComponentManager.get(type.getName());
		} catch (NoClassDefFoundError ncdfe) {
			log.warn("Failed to find Sakai component manager");
			return null;
		}
	}

}
