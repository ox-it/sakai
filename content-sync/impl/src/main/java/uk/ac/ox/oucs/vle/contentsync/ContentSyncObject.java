package uk.ac.ox.oucs.vle.contentsync;

public class ContentSyncObject {

	private Object entity;
	private String type;
		
	public ContentSyncObject() {
	}
	
	public ContentSyncObject(String type, Object entity) {
		this.type = type;
		this.entity = entity;
	}
		
	public String getType() {
		return this.type;
	}
	
	public String getEntityType() {
		return this.entity.getClass().getSimpleName();
	}
		
	public Object getEntity() {
		return this.entity;
	}
}
