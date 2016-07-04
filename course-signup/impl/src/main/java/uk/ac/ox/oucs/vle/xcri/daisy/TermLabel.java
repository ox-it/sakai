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
package uk.ac.ox.oucs.vle.xcri.daisy;

import org.xcri.Extension;

/**
 * This is the label for a term.
 * But as we only have Oxford terms and know how to map from a term code to a term label we don't need this.
 */
public class TermLabel extends DaisyElement implements Extension {
	
	/**
	 * @return the element name
	 */
	@Override
	public String getName() {
		return "termLabel";
	}
	
}
