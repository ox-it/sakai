package uk.ac.ox.oucs.sirlouie.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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
	
	public Map<String, Object> toJSON() {
		
		Map <String, Object> data = new LinkedHashMap<String, Object>();
		if (null != service) {
			data.put("service", service);
		}
		if (null != href) {
			data.put("href", href);
		}
		if (null != expected) {
			data.put("expected", expected);
		}
		for (Message message : messages) {
			data.put("message", message.toJSON());
		}
		if (null != limitation) {
			data.put("limitation", limitation);
		}
		if (null != queue) {
			data.put("queue", queue);
		}
		return data;
	}

}
