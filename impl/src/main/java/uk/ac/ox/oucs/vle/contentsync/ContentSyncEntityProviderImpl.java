package uk.ac.ox.oucs.vle.contentsync;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.entitybroker.util.model.EntityContent;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.cover.UserDirectoryService;

public class ContentSyncEntityProviderImpl 
	implements ContentSyncEntityProvider, AutoRegisterEntityProvider, PropertyProvideable, 
				RESTful, RequestStorable, RequestAware, ActionsExecutable {
	
	private static final String PARAMETER_TIMESTAMP = "timestamp";

	/**
	 * The DAO to update our entries through.
	 */
	private ContentSyncDAO dao;
	public void setContentSyncDao(ContentSyncDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * The DAO to update our entries through.
	 */
	private ContentHostingService contentService;
	public void setContentHostingService(ContentHostingService contentService) {
		this.contentService = contentService;
	}
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	public String getPropertyValue(String reference, String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getProperties(String reference) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPropertyValue(String reference, String name, String value) {
		// TODO Auto-generated method stub
		
	}

	public List<String> findEntityRefs(String[] prefixes, String[] name,
			String[] searchValue, boolean exactMatch) {
		// TODO Auto-generated method stub
		return null;
	}
	
    @EntityCustomAction(action="resources", viewKey=EntityView.VIEW_LIST)
	public Collection<ContentSyncObject> getResources(EntityView view, Map<String, Object> params) 
			throws EntityPermissionException {
		
		// This is all more complicated because entitybroker isn't very flexible and announcements can only be loaded once you've got the
		// channel in which they reside first.
    	
    	String userId = UserDirectoryService.getCurrentUser().getId();
        if (userId == null) {
            throw new SecurityException(
            "This action is not accessible to anon and there is no current user.");
        }
		
    	Map<String, Object> parameters = getQueryMap((String)params.get("queryString"));
    	Time timestamp = getTime((String)parameters.get(PARAMETER_TIMESTAMP));
    	
		String[] segments = view.getPathSegments();
		String context = segments[segments.length-1];
		
		// Frig to ensure user urls contain the full userId
		if ("user".equals(segments[2])) {
			segments[3] = userId;
		}
		
		StringBuffer entityUrl = new StringBuffer();
		for (int i=2; i<segments.length; i++) {
			entityUrl.append("/"+segments[i]);
		}
		entityUrl.append("/");

		Collection<ContentSyncTableDAO> collection = dao.findResourceTrackers(context, timestamp);
		
		Collection<ContentSyncObject> syncObjects = new ArrayList<ContentSyncObject>();
		//ContentSyncObject syncObject = new ContentSyncObject();
		
		for (ContentSyncTableDAO contentSync : collection) {

			ContentEntity entity = null;
			try {
				String entityId = contentService.getReference(contentSync.getReference());
				Reference reference = EntityManager.newReference(entityId);
				entity = (ContentEntity)contentService.getEntity(reference);
			} catch(Exception e) {	
			}
			
			if (null != entity) {
				syncObjects.add(
						new ContentSyncObject(
								contentSync.getEvent(), EntityDataUtils.getResourceDetails(entity)));
			}	
		}
		
		return syncObjects;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getSampleEntity() {
		return new EntityContent();
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}

	public Object getEntity(EntityReference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getHandledOutputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}

	public String[] getHandledInputFormats() {
		return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
	}

	public void setRequestGetter(RequestGetter requestGetter) {
		// TODO Auto-generated method stub
		
	}

	public void setRequestStorage(RequestStorage requestStorage) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 * @param queryString
	 * @return
	 */
	private Map<String, Object> getQueryMap(String queryString) {
		
		Map<String, Object> params = new HashMap<String, Object>();
		if (null != queryString && !queryString.isEmpty()) {
			String[] strings = queryString.split("&");
			for (int i=0; i<strings.length; i++) {
				String parameter = strings[i];
				int j = parameter.indexOf("=");
				params.put(parameter.substring(0, j), parameter.substring(j+1));
			}
		}
		return params;
	}
	
	/**
	 * 
	 * @param timestamp  use formatter A: yyyyMMddHHmmssSSS
	 * @return
	 */
	private Time getTime(String timestamp) {
		
		try {
			
			if (null != timestamp) {
				DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				Date date = format.parse(timestamp);
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				
				return TimeService.newTime(c.getTimeInMillis());
			}
		
		} catch (ParseException e) {
			return TimeService.newTimeGmt("20201231235959999");
		}
		return null;
	}
	
}
