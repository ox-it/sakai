package org.sakaiproject.authz.devolved.devolvedadmintool.entitybrokerproviders.model;

public class ManagedOwner {
	private String userId;
	private String userDisplayName;
	private String userEntityURL;

	public ManagedOwner(String userId, String displayName) {
		if (userId.startsWith("/user/") && userId.length() > 6) {
			userId = userId.substring(6);
		}
		this.userId = userId;
		this.userDisplayName = displayName;
		this.userEntityURL = "/direct/user/" + userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}

	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	public String getUserEntityURL() {
		return userEntityURL;
	}

	public void setUserEntityURL(String userEntityURL) {
		this.userEntityURL = userEntityURL;
	}

}