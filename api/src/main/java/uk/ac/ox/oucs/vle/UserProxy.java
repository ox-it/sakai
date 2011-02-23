package uk.ac.ox.oucs.vle;

import java.util.List;

/**
 * Wrapper around a normal Sakai user. 
 * @author buckett
 *
 */
public class UserProxy {

	private String id;
	private String eid;
	private String name;
	private String email;
	private List<String> units;
	private String yearOfStudy;
	private String type;
	
	public UserProxy(String id, String eid, String name, String email, 
			String yearOfStudy, String type, List<String> units) {
		this.id = id;
		this.eid = eid;
		this.name = name;
		this.email = email;
		this.units = units;
		this.yearOfStudy = yearOfStudy;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public String getEid() {
		return eid;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
	
	public List<String> getUnits() {
		return units;
	}
	
	public String getYearOfStudy() {
		return yearOfStudy;
	}
	
	public String getType() {
		return type;
	}
}
