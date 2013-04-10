package uk.ac.ox.oucs.vle;

import java.util.Map;

public class PopulatorContext {
	
	private String uri;
	private String user;
	private String password;
	private String name;
	
	private PopulatorLogWriter deletedLogWriter;
	private PopulatorLogWriter errorLogWriter;
	private PopulatorLogWriter infoLogWriter;
	
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
	
	public void setDeletedLogWriter(PopulatorLogWriter deletedLogWriter) {
		this.deletedLogWriter = deletedLogWriter;
	}
	
	public PopulatorLogWriter getDeletedLogWriter() {
		return deletedLogWriter;
	}

	public void setErrorLogWriter(PopulatorLogWriter errorLogWriter) {
		this.errorLogWriter = errorLogWriter;
	}
	
	public PopulatorLogWriter getErrorLogWriter() {
		return errorLogWriter;
	}
	
	public void setInfoLogWriter(PopulatorLogWriter infoLogWriter) {
		this.infoLogWriter = infoLogWriter;
	}
	
	public PopulatorLogWriter getInfoLogWriter() {
		return infoLogWriter;
	}
}
