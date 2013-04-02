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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * We need todo this from another bean so the transaction proxy works.
 * @author buckett
 *
 */
public class PopulatorInit {

	private Populator populator;
	private static final Log log = LogFactory.getLog(PopulatorInit.class);

	public void setPopulator(Populator populator) {
		this.populator = populator;
	}
	
	public void init() {
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("config.properties"));
			PopulatorContext context = new PopulatorContext("xcri.oxcap.populator", properties);
			populator.update(context);
			
		} catch (IOException e) {
			log.error("IOException [config.properties]", e);
		}
	}
}
