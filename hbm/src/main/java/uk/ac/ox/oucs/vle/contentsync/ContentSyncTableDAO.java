package uk.ac.ox.oucs.vle.contentsync;

import java.util.Date;

public class ContentSyncTableDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String event;
    private String reference;
    private String context;
    private Date timestamp;
    
    public ContentSyncTableDAO() {
    }
    
    public ContentSyncTableDAO(String id) {
    	this.id = id;
    }
    
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEvent() {
        return this.event;
    }
    
    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getReference() {
        return this.reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public String getContext() {
        return this.context;
    }
    
    public void setContext(String context) {
        this.context = context;
    }
    
    public Date getTimeStamp() {
        return this.timestamp;
    }
    
    public void setTimeStamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
