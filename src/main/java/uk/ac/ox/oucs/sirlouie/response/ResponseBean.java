package uk.ac.ox.oucs.sirlouie.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;

import uk.ac.ox.oucs.sirlouie.reply.SearLibrary;
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
		if (!beans.isEmpty()) {
			addDocument(new Document(id, beans));
		}
		
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
	
	public Map <String, Object> toJSON() {
		return toJSON(null);
	}
	
	public Map <String, Object> toJSON( String xsDateTime) {
		
		Map <String, Object> jsonData = new LinkedHashMap<String, Object>();
		jsonData.put("version", "0.5");
		jsonData.put("schema", "http://ws.gbv.de/daia/");
		
		if (null == xsDateTime) {
			xsDateTime = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(new Date());
		}
		jsonData.put("timestamp", xsDateTime);
		
		Map <String, String> institutionData = new LinkedHashMap<String, String>();
		institutionData.put("content", "University of Oxford");
		institutionData.put("href", "http://www.ox.ac.uk");
		jsonData.put("institution", institutionData);
		
		List<Map<String, Object>> documentList = new ArrayList<Map<String, Object>>();
		
		for (Document document: documents) {
			documentList.add(document.toJSON());
		}
		
		if (!documentList.isEmpty()) {
			jsonData.put("document", documentList);
		}
		
		if (null != error) {
			jsonData.put("error", error);
		}

		return jsonData;
	}
	
}
