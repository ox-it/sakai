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
	private String firstname;
	private String lastname;
	private String displayname;
	private String email;
	private List<String> units;
	private String webauthId;
	private String ossId;
	private String yearOfStudy;
	private String type;
	private String primaryOrgUnit;
	private String degreeProgram;
	
	public UserProxy(String id, String eid, 
			String firstname, String lastname, String name, String email, 
			String webauthId, String ossId, String yearOfStudy, String type,
			String primaryOrgUnit, String degreeProgram, List<String> units) {
		this.id = id;
		this.eid = eid;
		this.firstname = firstname;
		this.lastname = lastname;
		this.displayname = name;
		this.email = email;
		this.units = units;
		this.yearOfStudy = yearOfStudy;
		this.webauthId = webauthId;
		this.ossId = ossId;
		this.type = type;
		this.primaryOrgUnit = primaryOrgUnit;
		this.degreeProgram = degreeProgram;
	}

	public String getId() {
		return id;
	}

	public String getEid() {
		return eid;
	}

	public String getFirstName() {
		return firstname;
	}
	
	public String getLastName() {
		return lastname;
	}
	
	public String getDisplayName() {
		return displayname;
	}

	public String getEmail() {
		return email;
	}
	
	public String getOssId() {
		return ossId;
	}
	
	public List<String> getUnits() {
		return units;
	}
	
	public String getWebauthId() {
		return webauthId;
	}
	
	public String getYearOfStudy() {
		return yearOfStudy;
	}
	
	public String getType() {
		return type;
	}
	
	public String getPrimaryOrgUnit() {
		return primaryOrgUnit;
	}
	
	public String getDegreeProgram() {
		return degreeProgram;
	}
}
