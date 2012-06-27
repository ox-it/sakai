package uk.ac.ox.oucs.vle.contentsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.sakaiproject.component.api.ServerConfigurationService;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

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
