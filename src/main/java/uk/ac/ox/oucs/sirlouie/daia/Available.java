package uk.ac.ox.oucs.sirlouie.daia;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class Available {
	
	private String service;
	private String href;
	private String delay;
	private List<Message> messages = new ArrayList<Message>();
	private String limitation;
	
	public Available() {	
	}
	
	public Available(String service) {
		this.service=service;
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
	
	public void setDelay(String delay) {
		this.delay=delay;
	}
	
	public void setLimitation(String limitation) {
		this.limitation=limitation;
	}
	
	public JSONObject toJSON() throws JSONException {
		
		JSONObject json = new JSONObject();
		if (null != service) {
			json.put("service", service);
		}
		if (null != href) {
			json.put("href", href);
		}
		if (null != delay) {
			json.put("delay", delay);
		}
		for (Message message : messages) {
			json.put("message", message.toJSON());
		}
		if (null != limitation) {
			json.put("limitation", limitation);
		}
		return json;
	}

}
