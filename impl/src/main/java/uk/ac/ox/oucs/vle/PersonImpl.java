package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonImpl implements Person {

	private String id;
	private String displayName;
	private String email;
	private List<String> units;
	
	public PersonImpl(String id, String displayName, String email, List<String> units) {
		this.id = id;
		this.displayName = displayName;
		this.email = email;
		this.units = new ArrayList<String>(units);
	}
	
	public String getId() {
		return id;
	}
	public String getName() {
		return displayName;
	}
	public String getEmail() {
		return email;
	}

	public List<String> getUnits() {
		return Collections.unmodifiableList(units);
	}
	
	
}
