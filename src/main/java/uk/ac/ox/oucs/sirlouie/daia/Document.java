package uk.ac.ox.oucs.sirlouie.daia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	
	/*
	public Document(String id, Collection<SearObject> beans) {
		this.id = id;
		if (!beans.isEmpty()) {
			this.href = beans.iterator().next().getURL();
		}
		
		for (SearObject bean : beans) {
			
			if (bean instanceof SearLibrary) {
				SearLibrary library = (SearLibrary)bean;
				Item item = new Item();
				item.setStorage(library.getCollection());
				item.setDepartment(new Department(library.getLibrary()));
				items.add(item);
			}
			
			if (bean instanceof SearError) {
				SearError serror = (SearError)bean;
				Message error = new Message(null, serror.getMessage(), Integer.toString(serror.getCode()));
				errors.add(error);
			}
		}
		
	}
	*/
	
	public void addItems(Collection<SearObject> beans) {
		
		if (null == this.href && !beans.isEmpty()) {
			this.href = beans.iterator().next().getURL();
		}
		
		for (SearObject bean : beans) {
			
			if (bean instanceof SearLibrary) {
				SearLibrary library = (SearLibrary)bean;
				Item item = new Item();
				item.setStorage(library.getCollection());
				item.setDepartment(new Department(library.getLibrary()));
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

	public Map<String, Object> toJSON() {
		
		Map <String, Object> data = new LinkedHashMap<String, Object>();
		data.put("id", id);
		data.put("href", href);
		
		List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
		for (Item item: items) {
			itemList.add(item.toJSON());
		}
		
		if (!itemList.isEmpty()) {
			data.put("item", itemList);
		}
		
		for (Message error: errors) {
			data.put("error", error.toJSON());
		}
		
		return data;
	}
}
