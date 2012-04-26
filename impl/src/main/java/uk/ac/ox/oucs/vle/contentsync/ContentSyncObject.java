package uk.ac.ox.oucs.vle.contentsync;

import org.sakaiproject.entitybroker.util.model.EntityContent;

public class ContentSyncObject {

	private EntityContent entity;
	private String type;
		
	public ContentSyncObject(String type, EntityContent entity) {
		this.type = type;
		this.entity = entity;
	}
		
	public String getType() {
		return this.type;
	}
		
	public EntityContent getEntity() {
		return this.entity;
	}
}
