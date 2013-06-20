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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationErrors {

	private Map<String, List<String>> errors = null;
	
	public boolean hasErrors() {
	 return errors != null;
	}
	
	protected Map<String, List<String>>getErrors() {
		if(errors == null) {
			errors = new HashMap<String,List<String>>();
		}
		return errors;
	}
	
	public void addError(String field, String error) {
		List<String> messages = getErrors().get(field);
		if (messages == null) {
			getErrors().put(field, Collections.singletonList(error));
		} else {
			if (messages.size() == 1) {
				messages = new ArrayList<String>(messages);
				getErrors().put(field, messages);
			} else {
				messages.add(error);
			}
		}
	}
	
	public List<String> getErrors(String field) {
		if (hasErrors() && getErrors().containsKey(field)) {
			return getErrors().get(field);
		}
		return Collections.emptyList();
	}
	
	public List<String> getAllErrors() {
		if (hasErrors()) {
			ArrayList<String> allErrors = new ArrayList<String>(getErrors().size());
			for (List<String> errorList: getErrors().values()) {
				allErrors.addAll(errorList);
			}
			return allErrors;
		}
		return Collections.emptyList();
	}
}
