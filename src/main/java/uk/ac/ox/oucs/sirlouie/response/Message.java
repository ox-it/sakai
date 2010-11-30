package uk.ac.ox.oucs.sirlouie.response;

import java.util.LinkedHashMap;
import java.util.Map;

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
	

	public Map<String, Object> toJSON() {
		
		Map <String, Object> data = new LinkedHashMap<String, Object>();
		if (null != lang) {
			data.put("lang", lang);
		}
		if (null != content) {
			data.put("content", content);
		}
		if (null != errno) {
			data.put("errno", errno);
		}
		return data;
	}
}
