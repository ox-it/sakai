package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

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
	
	public Element writeTeachingInstance(Collection<CourseGroup> courseGroups, 
										 CourseComponent courseComponent, 
										 Collection<CourseSignup> signups) {
		
		Element teachingInstance = new Element("TeachingInstance");
		
		Element tid = new Element("id");
		tid.setText(courseComponent.getId());
		teachingInstance.addContent(tid);
		
		/*
		for (CourseGroup courseGroup : courseGroups) {
			Element element = new Element("assessmentUnit");
			element.setAttribute("id", courseGroup.getId());
			element.setAttribute("title", courseGroup.getTitle());
			teachingInstance.addContent(element);
		}
		*/
		
		Element students = new Element("students");
		//component.setAttribute("id", courseComponent.getComponentSet());
		
		for (CourseSignup signup : signups) {
			Element attendee = new Element("student");
			Element sid = new Element("webauth_id");
			sid.setText(signup.getUser().getWebauthId());
			attendee.addContent(sid);
			Element sst = new Element("status");
			sst.setText(signup.getStatus().name());
			attendee.addContent(sst);
			
			//attendee.setAttribute("surname", signup.getUser().getLastName());
			//attendee.setAttribute("forename", signup.getUser().getFirstName());
			//attendee.setAttribute("displayname", signup.getUser().getName());
			students.addContent(attendee);
		}
		
		teachingInstance.addContent(students);
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
}
