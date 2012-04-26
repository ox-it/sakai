package uk.ac.ox.oucs.vle.contentsync;

public class ContentSyncToken {
	
	private String event;
	private String reference;
	private String context;
	
	public ContentSyncToken(String event, String reference, String context) {
		this.event = event;
		this.reference = reference;
		this.context = context;
	}
	
	public String getResourceEvent() {
		return this.event;
	}
	public void setResourceEvent(String event) {
		this.event = event;
	}
	
	public String getResourceReference() {
		return this.reference;
	}
	public void setResourceReference(String reference) {
		this.reference = reference;
	}
	
	public String getResourceContext() {
		return this.context;
	}
	public void setResourceContext(String context) {
		this.context = context;
	}

}
