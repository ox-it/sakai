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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

//@Provider
public class SpringComponentContextProvider implements ContextResolver<Object> {

	private static final Log log = LogFactory.getLog(SpringComponentContextProvider.class);

	private ServletContext context;

	public SpringComponentContextProvider(@Context ServletContext context) {
		this.context = context;
	}
	
	public Object getContext(Class<?> type) {
		Map beans = WebApplicationContextUtils.getWebApplicationContext(context).getBeansOfType(type);
		if (!beans.isEmpty()) {
			if (beans.size() > 1) {
				log.warn("More than one match when looking for beans of type "+ type);
			}
			return beans.values().iterator().next();

		}
		return null;
	}

}
