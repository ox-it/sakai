package uk.ac.ox.oucs.vle.proxy;

import java.util.Date;

/**
 * Simple test class for storing emails.
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
