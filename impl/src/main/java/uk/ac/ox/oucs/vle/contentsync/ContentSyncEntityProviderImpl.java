package uk.ac.ox.oucs.vle.contentsync;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.entity.DecoratedMessage;
import org.sakaiproject.api.app.messageforums.entity.DecoratedTopicInfo;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
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
	implements EntityProvider, AutoRegisterEntityProvider, PropertyProvideable, 
				RESTful, RequestStorable, RequestAware, ActionsExecutable {

	private static final Log log = LogFactory.getLog(ContentSyncEntityProviderImpl.class);
	
	  public final static String ENTITY_PREFIX = "content_sync";

	
	private static final String PARAMETER_TIMESTAMP = "timestamp";

	/**
	 * The DAO to update our entries through.
	 */
	private ContentSyncDAO dao;
	public void setContentSyncDao(ContentSyncDAO dao) {
		this.dao = dao;
	}
	
	private ContentSyncService contentSyncService;
	public void setContentSyncService(ContentSyncService contentSyncService) {
		this.contentSyncService = contentSyncService;
	}

	/**
	 * The DAO to update our entries through.
	 */
	private ContentHostingService contentService;
	public void setContentHostingService(ContentHostingService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * 
	 */
	private DiscussionForumManager forumManager;
	public void setForumManager(DiscussionForumManager forumManager) {
		this.forumManager = forumManager;
	}
	
	/**
	 * 
	 */
	private UIPermissionsManager uiPermissionsManager;
	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
		this.uiPermissionsManager = uiPermissionsManager;
	}
	
	/**
  	 * 
  	 */
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
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
        if (userId == null || userId == "") {
            throw new SecurityException(
            "This action is not accessible to anon and there is no current user.");
        }
		
    	Map<String, Object> parameters = getQueryMap((String)params.get("queryString"));
    	Date timestamp = getTime((String)parameters.get(PARAMETER_TIMESTAMP));
    	
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
		
		if (!contentSyncService.isSiteTracked(context)) {
			throw new RuntimeException("Tracking content isn't enabled on this site.");
		}

		Collection<ContentSyncTableDAO> collection = dao.findResourceTrackers(context, timestamp);
		
		Collection<ContentSyncObject> syncObjects = new ArrayList<ContentSyncObject>();
		
		for (ContentSyncTableDAO contentSync : collection) {

			Object entity = null;
			
			try {
				String entityId = contentService.getReference(contentSync.getReference());
				Reference reference = EntityManager.newReference(entityId);
				
				if (resourcesEntity(contentSync.getEvent())) {
					// This logs warning if the user doesn't have permission to access it which
					// is a little crazy
					ContentEntity content = (ContentEntity)contentService.getEntity(reference);
					 // If the entity has been deleted
					if (null == content) {
						if ("content.delete".equals(contentSync.getEvent())) {
							EntityContent thisEntity = new EntityContent();
							thisEntity.setResourceId(reference.getId());
							entity = thisEntity;
						} else {
							// If we have the creation and deletion of a file inside the date range then the
							// new event won't be able to find it's data so just skip outputting.
							continue;
						}
					} else {
						entity = EntityDataUtils.getResourceDetails(content);
					}
					
				} else if (forumsMessageEntity(contentSync.getEvent(), reference.getId())) {
					entity = getMessageEntity(reference, userId);
				
				} else if (forumsTopicEntity(contentSync.getEvent(), reference.getId())) {
					entity = getTopicEntity(reference, userId);
					
				} else {
					/*
					Do Something else [forums.reviseforum:/group/forums/site/d0c31496-d5b9-41fd-9ea9-349a7ac3a01a/Forum/2/11a2a3d7-73bc-4939-95ab-581d5ce2158a]
					Do Something else [forums.reviseforum:/group/forums/site/d0c31496-d5b9-41fd-9ea9-349a7ac3a01a/Forum/2/11a2a3d7-73bc-4939-95ab-581d5ce2158a]
					Do Something else [forums.revisetopic:/group/site/d0c31496-d5b9-41fd-9ea9-349a7ac3a01a/Topic/64/11a2a3d7-73bc-4939-95ab-581d5ce2158a]
					*/
					log.info("Don't know how to handle ["+contentSync.getEvent()+":"+ reference.getId()+"]");
				}
				
			} catch(Exception e) {
				entity = null;
			}
			
			if (null != entity) {
				syncObjects.add(new ContentSyncObject(contentSync.getEvent(), entity));
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
		return new ContentSyncObject();
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
	private Date getTime(String timestamp) {
		
		try {
			
			if (null != timestamp) {
				DateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				Date date = format.parse(timestamp);
				return date;
			}
		
		} catch (ParseException e) {
			return new Date(2020,12,31);
		}
		return null;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private long getMessageId(String id) {
		String[] strings = id.split("/");
		for (int i = 0; i<strings.length; i++) {
			if ("Message".equals(strings[i])) {
				return new Long(strings[i+1]);
			}
		}
		return new Long(0);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	private long getTopicId(String id) {
		String[] strings = id.split("/");
		for (int i = 0; i<strings.length; i++) {
			if ("Topic".equals(strings[i])) {
				return new Long(strings[i+1]);
			}
		}
		return new Long(0);
	}
	
	/**
	 * 
	 * @param message
	 * @return
	 */
	private DecoratedMessage getMessageEntity(Reference reference, String userId) {
		
		DecoratedMessage dMessage = null;
		
		Message message = forumManager.getMessageById(getMessageId(reference.getId()));
		if (null == message) {
			throw new IllegalArgumentException("IdUnusedException in Resource Entity Provider");
		}
		if (null == message.getTopic()) {
			throw new IllegalArgumentException("IdInvalidException in Resource Entity Provider");
		}
		
		if (!message.getDeleted()){
			  
			List<String> attachments = new ArrayList<String>();
			if(message.getHasAttachments()){
				for(Attachment attachment : (List<Attachment>) message.getAttachments()){
					attachments.add(attachment.getAttachmentName());
				}
			}

			Map msgIdReadStatusMap = forumManager.getReadStatusForMessagesWithId(
					Collections.singletonList(message.getId()), userId);
			Boolean readStatus = (Boolean)msgIdReadStatusMap.get(message.getId());
			if(readStatus == null)
				readStatus = Boolean.FALSE;

			dMessage = new DecoratedMessage(
							message.getId(), message.getTopic().getId(), message.getTitle(),
							message.getBody(), "" + message.getModified().getTime(),
							attachments, Collections.EMPTY_LIST, 
							message.getAuthor(), getProfileImageURL(message.getAuthorId()),
							message.getInReplyTo() == null ? null : message.getInReplyTo().getId(),
									"" + message.getCreated().getTime(), readStatus.booleanValue(), "", "");
		}
		
		return dMessage;
	}
	
	private DecoratedTopicInfo getTopicEntity(Reference reference, String userId) {
		
		DecoratedTopicInfo dTopic = null;
		
		DiscussionTopic topic = forumManager.getTopicById(getTopicId(reference.getId()));
		if (null == topic) {
			throw new IllegalArgumentException("IdUnusedException in Resource Entity Provider");
		}
		
		DiscussionForum forum = forumManager.getForumById(topic.getBaseForum().getId());
	    if (forum == null) {
	    	  throw new IllegalArgumentException("Invalid entity for creation, no forum found");
	    }
	    String siteId = forumManager.getContextForForumById(forum.getId());
		
		int unreadMessages = 0;
		int totalMessages = 0;
		
		if (topic.getDraft().equals(Boolean.FALSE)) {
			
			if (uiPermissionsManager.isRead(topic.getId(), topic.getDraft(), forum.getDraft(), userId, siteId)) {
				/*
				if (!topic.getModerated().booleanValue()
						|| (topic.getModerated().booleanValue() && 
							uiPermissionsManager.isModeratePostings(topic.getId(), forum.getLocked(), forum.getDraft(), topic.getLocked(), topic.getDraft(), userId, siteId))){

					unreadMessages = getMessageManager().findUnreadMessageCountByTopicIdByUserId(topic.getId(), userId);										
				
				} else {	
					// b/c topic is moderated and user does not have mod perm, user may only
					// see approved msgs or pending/denied msgs authored by user
					unreadMessages = getMessageManager().findUnreadViewableMessageCountByTopicIdByUserId(topic.getId(), userId);
				}
			
				totalMessages = getMessageManager().findViewableMessageCountByTopicIdByUserId(topic.getId(), userId);
			    */
				dTopic = new DecoratedTopicInfo(
					topic.getId(), topic.getTitle(), unreadMessages, totalMessages, "", 
					topic.getBaseForum().getId(), topic.getShortDescription(), "");
				
			} else {
				throw new SecurityException("Could not get entity, permission denied: " + topic);
			}
			
		}
		
		return dTopic;
	}
	
	/**
	 * 
	 * @param event
	 * @return
	 */
	private boolean resourcesEntity(String event) {
		if (event.startsWith("content.")) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param event
	 * @param id
	 * @return
	 */
	private boolean forumsMessageEntity(String event, String id) {
		if (event.startsWith("forums.") && getMessageId(id) > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param event
	 * @param id
	 * @return
	 */
	private boolean forumsTopicEntity(String event, String id) {
		if (event.startsWith("forums.") && getTopicId(id) > 0) {
			return true;
		}
		return false;
	}
	
	private String getProfileImageURL(String authorId) {
	  
	    if (null == authorId || authorId.trim().length() == 0 ) {
		    return null;
	    }	
	    StringBuffer sb = new StringBuffer();
	    sb.append(serverConfigurationService.getServerUrl());
	    sb.append("/direct/profile/");
	    sb.append(authorId);
	    sb.append("/image/thumb");
	    return sb.toString();
	}
}
