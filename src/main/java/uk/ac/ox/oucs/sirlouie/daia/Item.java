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
	private String libcode;
	private String libname;
	private String itemshelf;
	private String itemdesc;
	private String itemtype;
	private String availability;
	private int availableitems;
	private int totalitems;
	private String mapurl;

	public Item() {	
	}
	
	public String getId() {
		return id;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public Department getDepartment() {
		return department;
	}

	public String getLabel() {
		return label;
	}

	public List<Available> getAvailableServices() {
		return availableServices;
	}

	public List<UnAvailable> getUnAvailableServices() {
		return unAvailableServices;
	}

	public String getStorage() {
		return storage;
	}

	public String getLimitation() {
		return limitation;
	}

	public String getHref() {
		return href;
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

	public int getTotalitems() {
		return totalitems;
	}

	public void setTotalitems(int totalitems) {
		this.totalitems = totalitems;
	}

	public int getAvailableitems() {
		return availableitems;
	}

	public void setAvailableitems(int availableitems) {
		this.availableitems = availableitems;
	}

	public String getItemtype() {
		return itemtype;
	}

	public void setItemtype(String itemtype) {
		this.itemtype = itemtype;
	}

	public String getItemdesc() {
		return itemdesc;
	}

	public void setItemdesc(String itemdesc) {
		this.itemdesc = itemdesc;
	}

	public String getItemshelf() {
		return itemshelf;
	}

	public void setItemshelf(String itemshelf) {
		this.itemshelf = itemshelf;
	}

	public String getLibname() {
		return libname;
	}

	public void setLibname(String libname) {
		this.libname = libname;
	}

	public String getLibcode() {
		return libcode;
	}

	public void setLibcode(String libcode) {
		this.libcode = libcode;
	}

	public String getMapurl() {
		return mapurl;
	}

	public void setMapurl(String mapurl) {
		this.mapurl = mapurl;
	}

	public String getAvailability() {
		return availability;
	}

	public void setAvailability(String availability) {
		this.availability = availability;
	}

	public JSONObject toJSON() throws JSONException {
		
		JSONObject json = new JSONObject();
		if (null != id) {
			json.put("id", id);
		}
		// The library system was returning  OVP for the URL, which is presented as a relative URL
		// and doesn't work. So only output if it looks sensible.
		if (null != href && href.startsWith("http")) {
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
		if (null != libcode) {
			json.put("libcode", libcode);
		}
		if (null != libname) {
			json.put("libname", libname);
		}
		if (null != itemshelf) {
			json.put("itemshelf", itemshelf);
		}
		if (null != itemdesc) {
			json.put("itemdesc", itemdesc);
		}
		if (null != itemtype) {
			json.put("itemtype", itemtype);
		}
		json.put("availableitems", availableitems);
		json.put("totalitems", totalitems);
		if (null != mapurl) {
			json.put("mapurl", mapurl);
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
