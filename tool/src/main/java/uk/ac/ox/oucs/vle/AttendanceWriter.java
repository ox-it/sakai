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
	
	public Element writeTeachingInstance(CourseComponent courseComponent, 
										 Collection<CourseSignup> signups) {
		
		Element teachingInstance = new Element("TeachingInstance");
		
		Element tid = new Element("id");
		tid.setText(courseComponent.getId());
		teachingInstance.addContent(tid);
		
		Element students = new Element("students");
		
		for (CourseSignup signup : signups) {
			Element attendee = new Element("student");
			Person person = signup.getUser();
			
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
