package uk.ac.ox.oucs.sirlouie.response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Item {
	
	private String id;
	private List<Message> messages = new ArrayList<Message>();
	private Department department;
	private String label;
	private List<Available> availableServices = new ArrayList<Available>();
	private List<UnAvailable> unAvailableServices = new ArrayList<UnAvailable>();
	private String storage;
	private String limitation;
	
	public Item() {	
	}
	
	public void addMesage(Message message) {
		messages.add(message);
	}
	
	public void addAvailableService(Available service) {
		availableServices.add(service);
	}
	
	public void addUnAvailableService(UnAvailable service) {
		unAvailableServices.add(service);
	}
	
	public void setId(String id) {
		this.id=id;
	}
	
	public void setDepartment(Department department) {
		this.department=department;
	}
	
	public void setLabel(String label) {
		this.label=label;
	}
	
	public void setStorage(String storage) {
		this.storage=storage;
	}
	
	public void setLimitation(String limitation) {
		this.limitation=limitation;
	}

	public Map<String, Object> toJSON() {
		
		Map <String, Object> data = new LinkedHashMap<String, Object>();
		if (null != id) {
			data.put("id", id);
		}
		for (Message message : messages) {
			data.put("message", message.toJSON());
		}
		if (null != department) {
			data.put("department", department.toJSON());
		}
		if (null != label) {
			data.put("label", label);
		}
		for (Available service : availableServices) {
			data.put("available", service.toJSON());
		}
		for (UnAvailable service : unAvailableServices) {
			data.put("unavailable", service.toJSON());
		}
		if (null != storage) {
			Map <String, Object> storageData = new LinkedHashMap<String, Object>();
			storageData.put("content", storage);
			data.put("storage", storageData);
		}
		if (null != limitation) {
			Map <String, Object> limitationData = new LinkedHashMap<String, Object>();
			limitationData.put("content", limitation);
			data.put("limitation", limitationData);
		}
		return data;
	}
}
