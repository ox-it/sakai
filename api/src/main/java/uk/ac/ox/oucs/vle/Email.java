package uk.ac.ox.oucs.vle;

import java.util.Date;

/**
 * Simple test class for storing emails which is used when debugging.
 * 
 * @author buckett
 *
 */
public class Email {

	private String to;
	private String subject;
	private String body;
	private Date created;
	
	public Email(String to, String subject, String body) {
		this.to = to;
		this.subject = subject;
		this.body = body;
		this.created = new Date();
	}

	public String getTo() {
		return to;
	}

	public String getSubject() {
		return subject;
	}

	public String getBody() {
		return body;
	}

	public Date getCreated() {
		return created;
	}
}
