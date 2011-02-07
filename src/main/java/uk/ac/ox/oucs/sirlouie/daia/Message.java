package uk.ac.ox.oucs.sirlouie.daia;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {

	private String lang;
	private String content;
	private String errno;
	
	public Message() {	
	}
	
	public Message(String lang, String content, String errno) {
		this.lang=lang;
		this.content=content;
		this.errno=errno;
	}
	

	public JSONObject toJSON() throws JSONException {
		
		JSONObject json = new JSONObject();
		if (null != lang) {
			json.put("lang", lang);
		}
		if (null != content) {
			json.put("content", content);
		}
		if (null != errno) {
			json.put("errno", errno);
		}
		return json;
	}
}
