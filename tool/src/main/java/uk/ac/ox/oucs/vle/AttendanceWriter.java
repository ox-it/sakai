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
		root = new Element("attendance");
		document = new Document(root);
	}
	
	public Element writeTeachingInstance(Collection<CourseGroup> courseGroups, 
										 CourseComponent courseComponent, 
										 Collection<CourseSignup> signups) {
		
		Element teachingInstance = new Element("teachingInstance");
		teachingInstance.setAttribute("id", courseComponent.getId());
		
		for (CourseGroup courseGroup : courseGroups) {
			Element element = new Element("assessmentUnit");
			element.setAttribute("id", courseGroup.getId());
			element.setAttribute("title", courseGroup.getTitle());
			teachingInstance.addContent(element);
		}
		
		Element component = new Element("teachingComponent");
		component.setAttribute("id", courseComponent.getComponentSet());
		
		for (CourseSignup signup : signups) {
			Element attendee = new Element("signup");
			attendee.setAttribute("webauthid", signup.getUser().getWebauthId());
			attendee.setAttribute("surname", signup.getUser().getLastName());
			attendee.setAttribute("forename", signup.getUser().getFirstName());
			attendee.setAttribute("displayname", signup.getUser().getName());
			component.addContent(attendee);
		}
		
		teachingInstance.addContent(component);
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
