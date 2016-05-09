package org.sakaiproject.entitybroker.providers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.entitybroker.util.model.EntityContent;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.UserDirectoryService;

public class ContentHostingProvider extends AbstractEntityProvider 
	implements CoreEntityProvider, RESTful, ActionsExecutable, RequestAware {
	
	private static final Log log = LogFactory.getLog(ContentHostingProvider.class);

	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService)
	{	this.contentHostingService = contentHostingService;
	}
	
	private EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager)
	{	this.entityManager = entityManager;
	}
	
	public static String PREFIX = "content";
    public String getEntityPrefix() {
        return PREFIX;
    }
    
    private static final String PARAMETER_DEPTH = "depth";
    private static final String PARAMETER_TIMESTAMP = "timestamp";

	/**
	 * 
	 * @param entity
	 * @return
	 */
    private Comparator getComparator(ContentEntity entity) {
	
		boolean hasCustomSort = false;
		try	{
			hasCustomSort = entity.getProperties().getBooleanProperty(
					ResourceProperties.PROP_HAS_CUSTOM_SORT);
	
		} catch(Exception e) {
			// ignore -- let value of hasCustomSort stay false
		}
		
		if(hasCustomSort) {
			return contentHostingService.newContentHostingComparator(
					ResourceProperties.PROP_CONTENT_PRIORITY, true);
		} else {
			return contentHostingService.newContentHostingComparator(
					ResourceProperties.PROP_DISPLAY_NAME, true);
		}
	}
	
	/**
	 * 
	 * @param entity The entity to load details of.
	 * @param currentDepth How many collections we have already processed
	 * @param requestedDepth The maximum number depth of the tree to scan.
	 * @param timeStamp All returned details must be newer than this timestamp.
	 * @return EntityContent containing details of all resources the user can access.
	 * <code>null</code> is returned if the current user isn't allowed to access the resource.
	 */
	private EntityContent getResourceDetails(	
			ContentEntity entity, int currentDepth, int requestedDepth, Time timeStamp) {
		boolean allowed = (entity.isCollection()) ?
				contentHostingService.allowGetCollection(entity.getId()) :
				contentHostingService.allowGetResource(entity.getId());
		if (!allowed) {
			// If the user isn't allowed to see this we return null.
			return null;
		}
		EntityContent tempRd = EntityDataUtils.getResourceDetails(entity);
		
		// If it's a collection recurse down into it.
		if ((requestedDepth > currentDepth) && entity.isCollection()) {
				
			ContentCollection collection = (ContentCollection)entity;
			// This is all members, no permission check has been done yet.
			List<ContentEntity> contents = collection.getMemberResources();
			
			Comparator comparator = getComparator(entity);
			if (null != comparator) {
				Collections.sort(contents, comparator);
			}
			
			for (Iterator<ContentEntity> i = contents.iterator(); i.hasNext();) {
				ContentEntity content = i.next();
				EntityContent resource = getResourceDetails(content, currentDepth+1, requestedDepth, timeStamp);

				if (resource != null && resource.after(timeStamp)) {
					tempRd.addResourceChild(resource);
				}
			}
		}
		
		return tempRd;
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
				
				return TimeService.newTimeGmt(format.format(date));
			}
		
		} catch (ParseException e) {
			return TimeService.newTimeGmt("20201231235959999");
		}
		return null;
	}
    
    @EntityCustomAction(action="resources", viewKey=EntityView.VIEW_LIST)
	public List<EntityContent> getResources(EntityView view, Map<String, Object> params) 
			throws EntityPermissionException {
		
		// This is all more complicated because entitybroker isn't very flexible and announcements can only be loaded once you've got the
		// channel in which they reside first.
    	
    	String userId = developerHelperService.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
            "This action is not accessible to anon and there is no current user.");
        }
		
    	Map<String, Object> parameters = getQueryMap((String)params.get("queryString"));
    	Time timeStamp = getTime((String)parameters.get(PARAMETER_TIMESTAMP));
    	
    	int requestedDepth = 1;
    	int currentDepth = 0;
    	if (parameters.containsKey(PARAMETER_DEPTH)) {
    		if ("all".equals((String)parameters.get(PARAMETER_DEPTH))) {
    			requestedDepth = Integer.MAX_VALUE;
    		} else {
    			requestedDepth = Integer.parseInt((String)parameters.get(PARAMETER_DEPTH));
    		}
    	}
    	
		String[] segments = view.getPathSegments();
		
		StringBuffer resourceId = new StringBuffer();
		for (int i=2; i<segments.length; i++) {
			resourceId.append("/"+segments[i]);
		}
		resourceId.append("/");
	
		Reference reference = entityManager.newReference(
				ContentHostingService.REFERENCE_ROOT+resourceId.toString());
		
		// We could have used contentHostingService.getAllEntities(id) bit it doesn't do 
		// permission checks on any contained resources (documentation wrong).
		// contentHostingService.getAllResources(String id) does do permission checks
		// but it doesn't include collections in it's returned list.
		// Also doing the recursion ourselves means that we don't loads lots of entities
		// when the depth of recursion is low.
		ContentCollection collection= null;
		try {
			collection = contentHostingService.getCollection(reference.getId());
			
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("IdUnusedException in Resource Entity Provider");
			
		} catch (TypeException e) {
			throw new IllegalArgumentException("TypeException in Resource Entity Provider");
			
		} catch (PermissionException e) {
			throw new SecurityException("PermissionException in Resource Entity Provider");
		}
		
		List<EntityContent> resourceDetails = new ArrayList<EntityContent>();
		if (collection!=null) {
			EntityContent resourceDetail = getResourceDetails(collection, currentDepth, requestedDepth, timeStamp);
			if (resourceDetail != null) {
				resourceDetails.add(resourceDetail);
			} else {
				log.error("Initial permission check passed but subsequent permission check failed on "+ reference.getId());
			}
		}
		return resourceDetails;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getSampleEntity() {
		// TODO Auto-generated method stub
		return null;
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

	public boolean entityExists(String id) {
		// TODO Auto-generated method stub
        return false;
	}
}
