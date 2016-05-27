package uk.ac.ox.oucs.vle.contentsync;

import java.util.Collection;

public interface ContentSyncPushService {
	
	boolean push(String jsonBodyString);
	
	boolean batch(String jsonBodyString);
	
	boolean broadcast(String jsonBodyString);
	
	void alias();
	
	void changeAliasSchedule();
	
	void removeAliasSchedule();
	
	void setAliasSchedule();
	
	Collection deviceTokens();
	
	Integer deviceTokensCount();
	
	Integer activeDeviceTokensCount();
	
	Collection stats();

}
