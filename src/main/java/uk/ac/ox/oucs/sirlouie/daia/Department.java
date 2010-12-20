package uk.ac.ox.oucs.sirlouie.daia;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.oucs.sirlouie.LibraryCodes;

public class Department {

	private String id;
	private LibraryCodes codes;
	
	public Department() {	
	}
	
	public Department(String id) {
		
		this.id=id;
		codes=LibraryCodes.getInstance();
	}
	

	public Map<String, Object> toJSON() {
		
		Map <String, Object> data = new LinkedHashMap<String, Object>();
		if (null != id) {
			data.put("id", id);
		}
		if (null != codes.get(id)) {
			data.put("content", codes.get(id));
		}

		return data;
	}
}
