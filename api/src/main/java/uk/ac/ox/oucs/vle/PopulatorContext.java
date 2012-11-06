package uk.ac.ox.oucs.vle;

public class PopulatorContext {
	
	private String uri;
	private String user;
	private String password;
	private String name;
	
	public PopulatorContext() {
		
	}
	
	public String getURI() {
		return this.uri;
	}
	public void setURI(String uri) {
		this.uri = uri;
	}
	
	public String getUser() {
		return this.user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() {
		return this.password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
