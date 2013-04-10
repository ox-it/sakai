/*
 * #%L
 * Course Signup API
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
