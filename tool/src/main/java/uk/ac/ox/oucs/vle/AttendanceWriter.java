package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class AttendanceWriter {
	
	private OutputStream outputStream;
	private Document document;
	private Element root;
	
	public AttendanceWriter(OutputStream out) {
		outputStream = out;
		root = new Element("TeachingInstances");
		document = new Document(root);
	}
	
	public Element writeTeachingInstance(CourseComponent courseComponent, 
										 Collection<CourseSignup> signups) {
		
		Element teachingInstance = new Element("TeachingInstance");
		
		Element tid = new Element("id");
		tid.setText(courseComponent.getId());
		teachingInstance.addContent(tid);
		
		Map<String, Collection<CourseSignup>> myMap = split(signups);
		
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
	
	
	private Map<String, Collection<CourseSignup>> split(Collection<CourseSignup> signups) {
	
		// create the thing to store the sub lists
		Map<String, Collection<CourseSignup>> subs = 
				new HashMap<String, Collection<CourseSignup>>();

		// iterate through your objects
		for (CourseSignup signup : signups) {

			// fetch the list for this object's id
			Collection<CourseSignup> temp = subs.get(signup.getGroup().getId());

			if (temp == null) {
				// if the list is null we haven't seen an
				// object with this id before, so create 
				// a new list
				temp = new ArrayList<CourseSignup>();

				// and add it to the map
				subs.put(signup.getGroup().getId(), temp);
			}

			// whether we got the list from the map
			// or made a new one we need to add our
			// object.
			temp.add(signup);
		}
		
		return subs;
	}

}
