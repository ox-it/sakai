package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonImpl implements Person {

	private String id;
	private String displayName;
	private String email;
	private List<String> units;
	
	private String yearOfStudy;
	private String type;
	
	public PersonImpl(String id, String displayName, String email, List<String> units, 
			String yearOfStudy, String type) {
		this.id = id;
		this.displayName = displayName;
		this.email = email;
		this.units = new ArrayList<String>(units);
		this.yearOfStudy = yearOfStudy;
		this.type = type;
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
	
	public String getYearOfStudy() {
		return yearOfStudy;
	}
	public String getType() {
		return type;
	}
	
}
