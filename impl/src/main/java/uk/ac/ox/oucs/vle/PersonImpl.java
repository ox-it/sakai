package uk.ac.ox.oucs.vle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonImpl implements Person {

	private String id;
	private String firstName;
	private String lastName;
	private String displayName;
	private String email;
	private List<String> units;
	private String webauthId;
	private String yearOfStudy;
	private String departmentName;
	private String type;
	
	public PersonImpl(String id, String firstName, String lastName, String displayName, 
			String email, List<String> units, 
			String webauthId, String yearOfStudy, String departmentName, String type) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.displayName = displayName;
		this.email = email;
		this.units = new ArrayList<String>(units);
		this.webauthId = webauthId;
		this.yearOfStudy = yearOfStudy;
		this.departmentName = departmentName;
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
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
	
	public String getWebauthId() {
		return webauthId;
	}
	public String getYearOfStudy() {
		return yearOfStudy;
	}
	public String getDepartmentName() {
		return departmentName;
	}
	public String getType() {
		return type;
	}
}
