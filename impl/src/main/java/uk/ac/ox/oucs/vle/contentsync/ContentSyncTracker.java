package uk.ac.ox.oucs.vle.contentsync;

import java.sql.Timestamp;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;

/**
 * When a site is updated we check that all the pages have aliases.
 * We want to keep existing aliases as they may have been sent to someone in an email.
 * If the title changes though we should generate a new alias.
 * Should sort by date as newest alias should be used.
 * @author buckett
 *
 */
public class ContentSyncTracker implements Observer {
	
	private static Log log = LogFactory.getLog(ContentSyncTracker.class);
	
	/**
	 * 
	 */
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	/**
	 * The Entity Manager
	 */
	private EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	/**
	 * 
	 */
	private EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}
	
	/**
	 * The DAO to update our entries through.
	 */
	private ContentSyncDAO dao;
	public void setContentSyncDao(ContentSyncDAO dao) {
		this.dao = dao;
	}

	private BlockingQueue<ContentSyncToken> queue = new ArrayBlockingQueue(1, true);
	
	protected static String EVENT_RESOURCE_DONE = "content.done";

	public void init() {
		eventTrackingService.addLocalObserver(this);
		new Thread(new ContentSyncPersister(queue, dao)).start();
	}
	
	public void destroy() {
		try {
			queue.put(new ContentSyncToken(EVENT_RESOURCE_DONE, "DONE", null));
		} catch (InterruptedException e) {
			System.out.println("Interrupted! " + 
	                "Last one out, turn out the lights!");
		}
	}
		
	
	public void update(Observable o, Object arg) {
		 
		if (arg instanceof Event) {
			Event event = (Event)arg;
			
			if (ContentHostingService.EVENT_RESOURCE_ADD.equals(event.getEvent()) || 
				ContentHostingService.EVENT_RESOURCE_READ.equals(event.getEvent()) ||
				ContentHostingService.EVENT_RESOURCE_WRITE.equals(event.getEvent()) ||
				ContentHostingService.EVENT_RESOURCE_REMOVE.equals(event.getEvent())) {
				
				Reference ref = entityManager.newReference(event.getResource());
				
				try {
					queue.put(new ContentSyncToken(event.getEvent(), ref.getId(), ref.getContext()));
					
				} catch (InterruptedException e) {
					System.out.println("Interrupted! " + 
			                "Last one out, turn out the lights!");
				}
				
			} else if (DiscussionForumService.EVENT_FORUMS_ADD.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_FORUMS_FORUM_ADD.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_FORUMS_FORUM_REMOVE.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_FORUMS_FORUM_REVISE.equals(event.getEvent()) ||
					   
					   DiscussionForumService.EVENT_FORUMS_GRADE.equals(event.getEvent()) ||
				  //   DiscussionForumService.EVENT_FORUMS_READ.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_FORUMS_REMOVE.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_FORUMS_RESPONSE.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_FORUMS_REVISE.equals(event.getEvent()) ||
					   
					   DiscussionForumService.EVENT_FORUMS_TOPIC_ADD.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_FORUMS_TOPIC_REMOVE.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE.equals(event.getEvent()) ||
					   
					   DiscussionForumService.EVENT_MESSAGES_ADD.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_MESSAGES_FOLDER_ADD.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_MESSAGES_FOLDER_REMOVE.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_MESSAGES_FOLDER_REVISE.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_MESSAGES_FORWARD.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_MESSAGES_READ.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_MESSAGES_REMOVE.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_MESSAGES_RESPONSE.equals(event.getEvent()) ||
					   DiscussionForumService.EVENT_MESSAGES_UNREAD.equals(event.getEvent())) {
				
				try {
					queue.put(new ContentSyncToken(event.getEvent(), event.getResource(), event.getContext()));
					
				} catch (InterruptedException e) {
					System.out.println("Interrupted! " + 
			                "Last one out, turn out the lights!");
				}
				
			}
			
		}
	}
}
