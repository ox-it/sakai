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
