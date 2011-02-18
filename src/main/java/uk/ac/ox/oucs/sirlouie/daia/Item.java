package uk.ac.ox.oucs.sirlouie.daia;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Item {
	
	private String id;
	private List<Message> messages = new ArrayList<Message>();
	private Department department;
	private String label;
	private List<Available> availableServices = new ArrayList<Available>();
	private List<UnAvailable> unAvailableServices = new ArrayList<UnAvailable>();
	private String storage;
	private String limitation;
	private String href;
	
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
	
	public void setHref(String href) {
		this.href=href;
	}

	public JSONObject toJSON() throws JSONException {
		
		JSONObject json = new JSONObject();
		if (null != id) {
			json.put("id", id);
		}
		if (null != href) {
			json.put("href", href);
		}
		for (Message message : messages) {
			json.put("message", message.toJSON());
		}
		if (null != department) {
			json.put("department", department.toJSON());
		}
		if (null != label) {
			json.put("label", label);
		}
		
		JSONArray availableList = new JSONArray();
		for (Available service : availableServices) {
			availableList.put(service.toJSON());
		}
		
		if (availableList.length() != 0) {
			json.put("available", availableList);
		}
		
		JSONArray unavailableList = new JSONArray();
		for (UnAvailable service : unAvailableServices) {
			unavailableList.put(service.toJSON());
		}
		
		if (unavailableList.length() != 0) {
			json.put("unavailable", unavailableList);
		}
		
		if (null != storage) {
			JSONObject storageData = new JSONObject();
			storageData.put("content", storage);
			json.put("storage", storageData);
		}
		if (null != limitation) {
			JSONObject limitationData = new JSONObject();
			limitationData.put("content", limitation);
			json.put("limitation", limitationData);
		}
		return json;
	}
}
