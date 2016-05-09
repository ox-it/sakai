package edu.amc.sakai.user;

/**
 * Imutable provided group.
 * @author buckett
 *
 */
public class ProvidedGroup {

	private final String id;
	private final String dn;
	private final String role;
	
	public ProvidedGroup(String id, String dn, String role) {
		this.id = id;
		this.dn = dn;
		this.role = role;
	}

	public String getId() {
		return id;
	}

	public String getDn() {
		return dn;
	}

	public String getRole() {
		return role;
	}
	
	public String toString() {
		return "id: "+ id+ 
			" dn: "+ dn+
			" role: "+ role;
	}

}
