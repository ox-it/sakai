package uk.ac.ox.oucs.sirlouie.daia;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.ox.oucs.sirlouie.utils.LibraryCodes;

public class Department {

	private String id;
	private LibraryCodes codes;
	
	public Department() {	
	}
	
	public Department(String id) {
		
		this.id=id;
		codes=LibraryCodes.getInstance();
	}
	

	public JSONObject toJSON() throws JSONException {
		
		JSONObject json = new JSONObject();
		if (null != id) {
			json.put("id", id);
		}
		if (null != codes.get(id)) {
			json.put("content", codes.get(id));
		}

		return json;
	}
}
