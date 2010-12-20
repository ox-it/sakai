package uk.ac.ox.oucs.sirlouie.daia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Available {
	
	private String service;
	private String href;
	private String delay;
	private List<Message> messages = new ArrayList<Message>();
	private String limitation;
	
	public Available() {	
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
	
	public Map<String, Object> toJSON() {
		
		Map <String, Object> data = new LinkedHashMap<String, Object>();
		if (null != service) {
			data.put("service", service);
		}
		if (null != href) {
			data.put("href", href);
		}
		if (null != delay) {
			data.put("delay", delay);
		}
		for (Message message : messages) {
			data.put("message", message.toJSON());
		}
		if (null != limitation) {
			data.put("limitation", limitation);
		}
		return data;
	}

}
