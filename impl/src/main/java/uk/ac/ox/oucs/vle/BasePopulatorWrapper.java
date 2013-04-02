package uk.ac.ox.oucs.vle;

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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

abstract class BasePopulatorWrapper implements PopulatorWrapper {

	private static final Log log = LogFactory.getLog(BasePopulatorWrapper.class);

	abstract void runPopulator(PopulatorContext context) throws IOException;
	
	/**
	 * 
	 */
	public void update(PopulatorContext context) 
	throws PopulatorException{

		try {
			runPopulator(context);

		} catch (IllegalStateException e) {
			log.error("IllegalStateException ["+context.getURI()+"]", e);

		} catch (IOException e) {
			log.error("IOException ["+context.getURI()+"]", e);

		}

	}

}
