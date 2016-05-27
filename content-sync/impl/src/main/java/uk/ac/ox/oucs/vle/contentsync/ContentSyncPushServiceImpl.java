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
import java.util.Vector;

import org.codehaus.jackson.map.ObjectMapper;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

public class ContentSyncPushServiceImpl implements ContentSyncPushService {

	/**
	 * 
	 */
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	/**
	 * 
	 * @return
	 */
	private String getUrbanAirshipKey() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("urbanAirship.key", "urbanAirshipKey");
		}
		return "EJTX_a4gQaOtxWGZI9kqnw";
	}
	
	/**
	 * 
	 * @return
	 */
	private String getUrbanAirshipSecret() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("urbanAirship.password", "urbanAirshipPassword");
		}
		return "VoR3Dgf1TumjSJH6Jp-jYA";
	}
	
	/**
	 * 
	 * @return
	 */
	private String getUrbanAirshipMasterSecret() {
		if (null != serverConfigurationService) {
			return serverConfigurationService.getString("urbanAirship.password", "urbanAirshipPassword");
		}
		return "fOtMB12aQFyh8o3-W5vZlA";
	}
	
	/**
	 * POST /api/push/
	 * @param jsonBodyString
	 * @return
	 */
	private boolean push(URL url, String jsonBodyString) {
		
		try {
	        
	        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        connection.setRequestMethod("POST");
	        connection.setDoOutput(true);
	        connection.setConnectTimeout(12000);
	        
	        String appKey = getUrbanAirshipKey();
	        String appMasterSecret = getUrbanAirshipMasterSecret();

	        String authString = appKey + ":" + appMasterSecret;
	        String authStringBase64 = Base64.encode(authString.getBytes("UTF-8"));
	        authStringBase64 = authStringBase64.trim();
	        
	        connection.setRequestProperty("Content-type", "application/json; charset:utf-8");
	        connection.setRequestProperty("Authorization", "Basic " + authStringBase64);
	        
	        OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
	        osw.write(new String(jsonBodyString.getBytes("UTF-8"),"UTF-8"));
	        osw.close();
	        
	        int responseCode = connection.getResponseCode();
	        String responseMessage = connection.getResponseMessage();
	        if (responseCode == 200) {
	        	return true;
	        }
	        
	    } catch (ProtocolException e) {
	    	e.printStackTrace();
	    	
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
	    
	    return false;
	}
	
	/**
	 * POST /api/push/
	 * @param jsonBodyString
	 * @return
	 */
	public boolean push(String jsonBodyString) {
		
		try {
	        URL url = new URL("https://go.urbanairship.com/api/push/");
	        return push(url, jsonBodyString);
	        
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    }
	    
	    return false;
	}
	
	/**
	 * POST /api/push/batch/
	 * @param jsonBodyString
	 * @return
	 */
	public boolean batch(String jsonBodyString) {
		
	    try {
	        URL url = new URL("https://go.urbanairship.com/api/push/batch/");
	        return push(url, jsonBodyString);
	         
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    }
	    
	    return false;
	}
	
	/**
	 * POST /api/push/broadcast/
	 * @param jsonBodyString
	 * @return
	 */
	public boolean broadcast(String jsonBodyString) {
		
	    try {
	        URL url = new URL("https://go.urbanairship.com/api/push/broadcast/");
	        return push(url, jsonBodyString);
	        
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	    }
	    
	    return false;
	}
	
	/**
	 * GET /api/device_tokens/<device_token>/ 
	 */
	public void alias() {	
	}
	
	/**
	 * PUT URL:https://go.urbanairship.com/api/push/scheduled/alias/<your alias>
	 */
	public void changeAliasSchedule() {
	}
	
	/**
	 * DELETE URL:https://go.urbanairship.com/api/push/scheduled/alias/<your alias>
	 */
	public void removeAliasSchedule() {
	}
	
	public void setAliasSchedule() {
	}
	
	/**
	 * GET /api/device_tokens/count/
	 * @return
	 */
	private Map<String,Object> get(URL url) {
		
		try {
	        
	        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	        connection.setRequestMethod("GET");
	        connection.setDoInput(true);
	        connection.setConnectTimeout(12000);
	        
	        String appKey = getUrbanAirshipKey();
	        String appMasterSecret = getUrbanAirshipMasterSecret();

	        String authString = appKey + ":" + appMasterSecret;
	        String authStringBase64 = Base64.encode(authString.getBytes("UTF-8"));
	        authStringBase64 = authStringBase64.trim();
	        
	        connection.setRequestProperty("Content-type", "application/json; charset:utf-8");
	        connection.setRequestProperty("Authorization", "Basic " + authStringBase64);
	        
	        int responseCode = connection.getResponseCode();
	        String responseMessage = connection.getResponseMessage();
	        if (responseCode != 200) {
	        	return Collections.EMPTY_MAP;
	        }
	        
	        InputStreamReader isr = new InputStreamReader(
	        		connection.getInputStream());
	        BufferedReader in = new BufferedReader(isr);

	        String responseString = "";
	        String jsonTxt = "";

	        while ((responseString = in.readLine()) != null) {
	        	jsonTxt = jsonTxt + responseString;
	        }
	        
	        ObjectMapper mapper = new ObjectMapper();
	        Map<String,Object> userData = mapper.readValue(jsonTxt, Map.class);
	        
	        return userData;
	        
	    } catch (ProtocolException e) {
	    	e.printStackTrace();
	    	
	    } catch (IOException e1) {
	        e1.printStackTrace();
	    }
	    
	    return Collections.EMPTY_MAP;
	}
	
	/**
	 * GET /api/device_tokens/
	 * @return
	 */
	public Collection deviceTokens() {	
		
		try {
			URL url = new URL("https://go.urbanairship.com/api/device_tokens/");
			Map<String,Object> userData = get(url);
			
		} catch (MalformedURLException e) {
	        e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}
	
	/**
	 * GET /api/device_tokens/count/
	 * @return
	 */
	public Integer deviceTokensCount() {
		
		try {
			URL url = new URL("https://go.urbanairship.com/api/device_tokens/count/");
			Map<String,Object> userData = get(url);
			if (userData.containsKey("device_tokens_count")) {
				return (Integer)userData.get("device_tokens_count");
			}
			
		} catch (MalformedURLException e) {
	        e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * GET /api/device_tokens/count/
	 * @return
	 */
	public Integer activeDeviceTokensCount() {
		
		try {
			URL url = new URL("https://go.urbanairship.com/api/device_tokens/count/");
			Map<String,Object> userData = get(url);
			if (userData.containsKey("active_device_tokens_count")) {
				return (Integer)userData.get("active_device_tokens_count");
			}
			
		} catch (MalformedURLException e) {
	        e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * GET /api/push/stats/?start=<timestamp>&end=<timestamp>
	 * @return
	 */
	public Collection stats() {
		return Collections.EMPTY_LIST;
	}
	
	
	/**
	 * 
	 */
	private ContentSyncSessionBean contentSyncSessionBean;
	public void setContentSyncSessionBean(ContentSyncSessionBean contentSyncSessionBean) {
		this.contentSyncSessionBean = contentSyncSessionBean;
	}
	
	public void broadcastToken(ContentSyncToken token) {
		if (token.getResourceEvent().startsWith(ContentHostingService.REFERENCE_ROOT.substring(1))) {
			broadcast("{\"aps\":{\"badge\":\"+1\"}}");
		}
		
		if (DiscussionForumService.EVENT_FORUMS_TOPIC_ADD.equals(token.getResourceEvent())) {	
			broadcast("{\"aps\":{\"badge\":\"+1\"}}");
		}
		if (DiscussionForumService.EVENT_FORUMS_TOPIC_REMOVE.equals(token.getResourceEvent())) {	
			broadcast("{\"aps\":{\"badge\":\"+1\"}}");
		}
		
		if (DiscussionForumService.EVENT_FORUMS_REMOVE.equals(token.getResourceEvent())) {
			long id = contentSyncSessionBean.getTopicId(
					new Long(parseReference(token.getResourceReference())));
			if (0 != id) {
				broadcast("{\"message\":\""+token.getResourceEvent()+"\",\"title\":\""+id+"\"}");
			}
		}
		
		if (DiscussionForumService.EVENT_FORUMS_ADD.equals(token.getResourceEvent())) {
			long id = contentSyncSessionBean.getTopicId(
					new Long(parseReference(token.getResourceReference())));
			if (0 != id) {
				broadcast("{\"message\":\""+token.getResourceEvent()+"\",\"title\":\""+id+"\"}");
			}
		}
	}
	private static String REFERENCE_ROOT = Entity.SEPARATOR + "forums";
	
	public long parseReference(String reference) {
		// Where was this copied from?
		if (reference.startsWith(REFERENCE_ROOT)) {
			// /syllabus/siteid/syllabusid
			String[] parts = split(reference, Entity.SEPARATOR);

			String id = null;

			if (parts.length > 5) {
				id = parts[5];
			}

			return new Long(id);
		}

		return 0;
	}
	
	protected String[] split(String source, String splitter) {
		// hold the results as we find them
		Vector rv = new Vector();
		int last = 0;
		int next = 0;
		do {
			// find next splitter in source
			next = source.indexOf(splitter, last);
			if (next != -1)	{
				// isolate from last thru before next
				rv.add(source.substring(last, next));
				last = next + splitter.length();
			}
		}
		while (next != -1);
		if (last < source.length())	{
			rv.add(source.substring(last, source.length()));
		}

		// convert to array
		return (String[]) rv.toArray(new String[rv.size()]);

	} // split
	
}
