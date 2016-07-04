/*
 * #%L
 * Course Signup Webapp
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
package uk.ac.ox.oucs.vle.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import uk.ac.ox.oucs.vle.*;

public class AttendanceWriter {
	
	private OutputStream outputStream;
	private Document document;
	private Element root;
	
	public AttendanceWriter(OutputStream out) {
		outputStream = out;
		root = new Element("TeachingInstances");
		document = new Document(root);
	}
	
	public Element writeTeachingInstance(CourseComponentExport courseComponent) {
		
		Element teachingInstance = new Element("TeachingInstance");
		
		Element tid = new Element("id");
		tid.setText(courseComponent.getComponent().getPresentationId());
		teachingInstance.addContent(tid);
		
		Map<String, Collection<CourseSignup>> myMap = split(courseComponent);
		
		Iterator it = myMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        
	        Element group = new Element("AssessmentUnit");
	        Element aid = new Element("id");
			aid.setText((String)pairs.getKey());
			group.addContent(aid);
		
			Element students = new Element("students");
		
			for (CourseSignup signup : (Collection<CourseSignup>)pairs.getValue()) {
			
				Person person = signup.getUser();
				
				if (null != person) {
				
					Element attendee = new Element("student");
			
					Element sid = new Element("webauth_id");
					sid.setText(person.getWebauthId());
					attendee.addContent(sid);
			
					Element sod = new Element("ossid");
					if (null == person.getOssId()) {
						sod.setText("null");
					} else {
						sod.setText(person.getOssId());
					}
					attendee.addContent(sod);
			
					Element ssn = new Element("name");
					ssn.setText(person.getName());
					attendee.addContent(ssn);
			
					Element sst = new Element("status");
					sst.setText(signup.getStatus().name());
					attendee.addContent(sst);
					
					Element ssr = new Element("reason");
					ssr.setText(signup.getNotes());
					attendee.addContent(ssr);
			
					students.addContent(attendee);
				}
			}
			group.addContent(students);
			teachingInstance.addContent(group);
	    }
		
		root.addContent(teachingInstance);
		return teachingInstance;
	}

	public void close() {
		try {
			XMLOutputter outp = new XMLOutputter();
			outp.setFormat(Format.getPrettyFormat());
			outp.output(document, outputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String toString() {
		XMLOutputter outp = new XMLOutputter();
		outp.setFormat(Format.getPrettyFormat());
		return outp.outputString(document);
	}
	
	
	private Map<String, Collection<CourseSignup>> split(CourseComponentExport componentExport) {
	
		// create the thing to store the sub lists
		Map<String, Collection<CourseSignup>> subs = 
				new HashMap<String, Collection<CourseSignup>>();

		// iterate through your objects
		for (CourseSignupExport signup : componentExport.getSignups()) {

			// fetch the list for this object's id
			String courseId = signup.getGroup().getCourseId();
			Collection<CourseSignup> temp = subs.get(courseId);

			if (temp == null) {
				// if the list is null we haven't seen an
				// object with this id before, so create 
				// a new list
				temp = new ArrayList<CourseSignup>();

				// and add it to the map
				subs.put(courseId, temp);
			}

			// whether we got the list from the map
			// or made a new one we need to add our
			// object.
			temp.add(signup.getSignup());
		}
		
		return subs;
	}

}
