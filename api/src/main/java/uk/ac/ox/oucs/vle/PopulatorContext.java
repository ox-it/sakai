package uk.ac.ox.oucs.vle;

import java.util.Map;

public class PopulatorContext {
	
	private String uri;
	private String user;
	private String password;
	private String name;
	
	public PopulatorContext(String prefix, Map map) {
		this.uri = (String)map.get(prefix+".uri");
		this.user = (String)map.get(prefix+".username");
		this.password = (String)map.get(prefix+".password");
		this.name = (String)map.get(prefix+".name");
	}
	
	public String getURI() {
		return this.uri;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getName() {
		return this.name;
	}

}
