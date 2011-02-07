package uk.ac.ox.oucs.sirlouie.daia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.ox.oucs.sirlouie.reply.SearObject;

public class ResponseBean {
	
	private String id;
	private List<Document> documents = new ArrayList<Document>();
	private String error;
	
	public ResponseBean() {
	}
	
	public ResponseBean(String id) {
		this.id = id;
	}
	
	public void setError(String message) {
		this.error = message;
	}
	
	public void addSearObjects(Collection<SearObject> beans) {
		Document document = new Document(id, null);
		document.addItems(beans);
		addDocument(document);
	}
	
	public void addDocument(Document document) {
		documents.add(document);
	}
	
	/*
	 *	{
	 *	"version" : "0.5",
	 *	"schema" : "http://ws.gbv.de/daia/",
	 *	"timestamp" : "2009-06-09T15:39:52.831+02:00",
	 *	"institution" :
	 *	{
	 *	"content" : "University of Oxford",
	 *	"href" : "http://www.ox.ac.uk"
	 *	},
	 *	"document" :
	 *	[
	 *	{
	 *	"id" : "UkOxUUkOxUb15585873",
	 *	"href" : "http://library.ox.ac.uk/cgi-bin/record_display_link.pl?id=15585873",
	 *	"items" :
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
	 *	}
	 */
	
	public JSONObject toJSON() throws JSONException {
		return toJSON(null);
	}
	
	public JSONObject toJSON( String xsDateTime) throws JSONException {
		
		JSONObject json = new JSONObject();
		json.put("version", "0.5");
		json.put("schema", "http://ws.gbv.de/daia/");
		
		if (null == xsDateTime) {
			xsDateTime = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date());
		}
		json.put("timestamp", xsDateTime);
		
		JSONObject institutionData = new JSONObject();
		institutionData.put("content", "University of Oxford");
		institutionData.put("href", "http://www.ox.ac.uk");
		json.put("institution", institutionData);
		
		JSONArray documentList = new JSONArray();
		for (Document document: documents) {
			documentList.put(document.toJSON());
		}
		
		if (documentList.length() != 0) {
			json.put("document", documentList);
		}
		
		if (null != error) {
			json.put("error", error);
		}

		return json;
	}
}
