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

import java.io.IOException;
import java.util.Date;

public interface PopulatorLogWriter {

	/**
	 * Called after the logWriter has been instantiated 
	 * 
	 * @param heading
	 * @throws IOException
	 */
	public void header(String heading) throws IOException;
	
	/**
	 * Called after a successfully reading the xcri xml file.
	 * The date parameter is the generated attribute of the xcri:catalog tag.
	 * 
	 * @param generated
	 * @throws IOException
	 */
	public void heading(Date generated) throws IOException;
	
	public void footer() throws IOException;
	
	public String getIdName();
	
	public String getDisplayName();
	
	public void write(String string) throws IOException;
	
	public void flush() throws IOException;
	
	public void close() throws IOException;

}