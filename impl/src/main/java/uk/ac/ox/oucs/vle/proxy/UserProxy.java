package uk.ac.ox.oucs.vle.proxy;

public class UserProxy {

	private String id;
	private String eid;
	private String name;
	private String email;
	
	public UserProxy(String id, String eid, String name, String email) {
		this.id = id;
		this.eid = eid;
		this.name = name;
		this.email = email;
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
}
