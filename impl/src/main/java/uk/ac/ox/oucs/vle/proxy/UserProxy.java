package uk.ac.ox.oucs.vle.proxy;

import java.util.List;

public class UserProxy {

	private String id;
	private String eid;
	private String name;
	private String email;
	private List<String> units;
	
	public UserProxy(String id, String eid, String name, String email, List<String> units) {
		this.id = id;
		this.eid = eid;
		this.name = name;
		this.email = email;
		this.units = units;
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
}
