package uk.ac.ox.oucs.sirlouie.daia;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class UnAvailable {
	
	private String service;
	private String href;
	private String expected;
	private List<Message> messages = new ArrayList<Message>();
	private String limitation;
	private String queue;
	
	public UnAvailable() {	
	}
	
	public void addMesage(Message message) {
		messages.add(message);
	}
	
	public void setService(String service) {
		this.service=service;
	}
	
	public void setHref(String href) {
		this.href=href;
	}
	
	public void setExpected(String expected) {
		this.expected=expected;
	}
	
	public void setLimitation(String limitation) {
		this.limitation=limitation;
	}
	
	public void setQueue(String queue) {
		this.queue=queue;
	}
	
	public JSONObject toJSON() throws JSONException {
		
		JSONObject json = new JSONObject();
		if (null != service) {
			json.put("service", service);
		}
		if (null != href) {
			json.put("href", href);
		}
		if (null != expected) {
			json.put("expected", expected);
		}
		for (Message message : messages) {
			json.put("message", message.toJSON());
		}
		if (null != limitation) {
			json.put("limitation", limitation);
		}
		if (null != queue) {
			json.put("queue", queue);
		}
		return json;
	}

}
