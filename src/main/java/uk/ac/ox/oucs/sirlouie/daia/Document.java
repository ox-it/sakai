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
				if (null != library.getLibrary()) {
					item.setDepartment(new Department(library.getLibrary()));
				}
				items.add(item);
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
		json.put("href", href);
		
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
