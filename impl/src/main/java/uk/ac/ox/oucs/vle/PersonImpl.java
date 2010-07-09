package uk.ac.ox.oucs.vle;

public class PersonImpl implements Person {

	private String id;
	private String displayName;
	private String email;
	
	public PersonImpl(String id, String displayName, String email) {
		this.id = id;
		this.displayName = displayName;
		this.email = email;
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
	
	
}
