package uk.ac.ox.oucs.vle.contentsync;

import java.util.Date;

import org.sakaiproject.event.api.Event;

/**
 * This is a quick test implementation of Event.
 *
 */
public class FakeEvent implements Event {

	private String context;
	private String event;
	private String resource;
	private String sessionId;
	private String userId;
	private Date eventTime;
	private boolean modify;
	private int priority;
	
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public Date getEventTime() {
		return eventTime;
	}
	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}
	public boolean getModify() {
		return modify;
	}
	public void setModify(boolean modify) {
		this.modify = modify;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}

}
