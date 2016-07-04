package org.sakaiproject.authz.hbm;

public class DevolvedAdmin {

	private long id;
	private String realm;
	private String adminRealm;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getRealm() {
		return realm;
	}
	
	public void setRealm(String realm) {
		this.realm = realm;
	}
	
	public String getAdminRealm() {
		return adminRealm;
	}
	
	public void setAdminRealm(String adminRealm) {
		this.adminRealm = adminRealm;
	}
	
}
