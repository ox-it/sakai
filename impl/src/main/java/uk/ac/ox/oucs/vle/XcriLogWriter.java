package uk.ac.ox.oucs.vle;

/*
 * #%L
 * Course Signup Implementation
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class XcriLogWriter extends OutputStreamWriter implements PopulatorLogWriter {
		
	private String name;
	
	public XcriLogWriter(OutputStream arg0, String name) {
		super(arg0);
		this.name = name;
	}
	
	public void header(String heading) throws IOException {
		Calendar cal = Calendar.getInstance();
		this.write("<html><head></head><body>"+"<h3>"+ heading+" ");
		this.write(
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime()));
		this.write("</h3>");
	}
	
	public void heading(Date generated) throws IOException {
		if (null != generated) {
			this.write("<h3>Using the XCRI file generated on ");
			this.write(
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(generated));
			this.write("</h3>");
		}
		this.write("<pre>");
	}
	
	public void footer() throws IOException {
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.write("</pre>");
		this.write("<h3>Log completed at ");
		this.write(sdf.format(cal.getTime()));
		this.write("</h3>");
		this.write("</body></html>");
	}
	
	public String getIdName() {
		return name+"Log.html";
	}
	
	public String getDisplayName() {
		return getIdName();
	}

}