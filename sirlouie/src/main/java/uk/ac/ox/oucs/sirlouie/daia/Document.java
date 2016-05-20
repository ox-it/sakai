package uk.ac.ox.oucs.sirlouie.daia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.ox.oucs.sirlouie.reply.SearError;
import uk.ac.ox.oucs.sirlouie.reply.SearLibrary;
import uk.ac.ox.oucs.sirlouie.reply.SearLink;
import uk.ac.ox.oucs.sirlouie.reply.SearObject;

public class Document {
	
	private String id;
	private String href;
	private List<Item> items = new ArrayList<Item>();
	private List<Message> errors = new ArrayList<Message>();
	
	// empty constructor
	public Document() {		
	}
	
	public Document(String id, String href) {
		this.id = id;
		this.href = href;
	}
	
	public String getId() {
		return id;
	}

	public String getHref() {
		return href;
	}

	public List<Item> getItems() {
		return items;
	}

	public List<Message> getErrors() {
		return errors;
	}

	public void addItems(Collection<SearObject> beans) {
		
		if (null == this.href && !beans.isEmpty()) {
			this.href = beans.iterator().next().getURL();
		}

		for (SearObject bean : beans) {
			
			if (bean instanceof SearLibrary) {
				SearLibrary library = (SearLibrary)bean;
				Item item = new Item();
				item.setId(library.getId());
				item.setHref(library.getURL());
				item.setLabel(library.getLabel());
				item.setStorage(library.getCollection());
				item.setLibcode(library.getLibrary());
				item.setLibname(library.getLibraryName());
				item.setItemshelf(library.getCallNumber());
				item.setItemdesc(library.getDescription());
				item.setItemtype(library.getType());
				if (library.getAvailability()!=null && (library.getAvailability().equals("Available") || library.getAvailability().equals("Confined") || library.getAvailability().equals("Closed Stack"))){
					item.setAvailableitems(1);
				}
				item.setTotalitems(1);
				item.setAvailability(library.getAvailability());
				item.setMapurl("");
				Available service = new Available("loan");
				service.setHref(library.getAvailableURL());
				item.addAvailableService(service);
				if (null != library.getLibrary()) {
					item.setDepartment(new Department(library.getLibrary()));
				}

				boolean alreadyExists = false;
				for (Item existingItem : items) {
					// If the item (= library, shelfmark, description, status) already exists,
					if (item.getLibcode()!=null && item.getLibcode().equals(existingItem.getLibcode()) &&
						item.getItemshelf()!=null && item.getItemshelf().equals(existingItem.getItemshelf()) &&
						item.getItemdesc()!=null && item.getItemdesc().equals(existingItem.getItemdesc()) &&
						item.getItemtype()!=null && item.getItemtype().equals(existingItem.getItemtype())){

						// Add to total items
						existingItem.setTotalitems(existingItem.getTotalitems() + 1);
						// Add to available items if Available
						existingItem.setAvailableitems(existingItem.getAvailableitems() + item.getAvailableitems());
						alreadyExists = true;
					}
				}
				if (!alreadyExists){
					items.add(item);
				}
			}
			
			if (bean instanceof SearLink) {
				SearLink link = (SearLink)bean;
				Item item = new Item();
				item.setHref(link.getHref());
				items.add(item);
			}
			
			if (bean instanceof SearError) {
				SearError serror = (SearError)bean;
				Message error = new Message(null, serror.getMessage(), Integer.toString(serror.getCode()));
				errors.add(error);
			}
		}
	}
	
	public void addItem(Item item) {
		items.add(item);
	}
	
	/*
	 * "document" :
	 *	[
	 *	{
	 *	"id" : "UkOxUUkOxUb15585873",
	 *	"href" : "http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=15585873",
	 *	"item" :
	 *	[
	 *	  {
	 *	  "department":
	 *	  {
	 *	  "id" : "RSL",
	 *	  "content" : "Radcliffe Science Library"
	 *	  },
	 *	  "storage" :  {  "content" : "Level 2"  }
	 *	  },
	 *	  {
	 *	  "department":
	 *	  {
	 *	  "id" : "SCCL",
	 *	  "content" : "St Cross College Library"
	 *	  },
	 *	  "storage" :  {  "content" : "Main Libr"  }
	 *	  }
	 *	  ]
	 *	}
	 *	]
	 */

	public JSONObject toJSON() throws JSONException {
		
		JSONObject json = new JSONObject();
		json.put("id", id);
		// We started getting bad URLs back and this prevents us from outputting bad linkes.
		if (href != null && href.startsWith("http")) {
			json.put("href", href);
		}
		
		JSONArray itemList = new JSONArray();
		for (Item item: items) {
			itemList.put(item.toJSON());
		}
		
		if (itemList.length() != 0) {
			json.put("item", itemList);
		}
		
		for (Message error: errors) {
			json.put("error", error.toJSON());
		}
		
		return json;
	}
}
