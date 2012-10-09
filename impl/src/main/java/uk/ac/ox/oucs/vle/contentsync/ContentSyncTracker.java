package uk.ac.ox.oucs.vle.contentsync;

import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

/**
 * This service watches for changes announced through the event system and stores interesting ones 
 * in the database.
 * 
 * @author buckett
 *
 */
public class ContentSyncTracker implements Observer {
	
	private static final String CONTENT_EVENT = ContentHostingService.REFERENCE_ROOT.substring(1);
	

	private static Log log = LogFactory.getLog(ContentSyncTracker.class);

	private EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	private ContentSyncPushServiceImpl pushService;
	public void setPushService(ContentSyncPushServiceImpl pushService) {
		this.pushService = pushService;
	}
	
	private ContentSyncService contentSyncService;
	public void setContentSyncService(ContentSyncService contentSyncService) {
		this.contentSyncService = contentSyncService;
	}

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

	private BlockingQueue<ContentSyncToken> queue = new ArrayBlockingQueue<ContentSyncToken>(1024, true);
	
	private Thread thread;
	private volatile boolean runThread = true;

	public void init() {
		if (contentSyncService.isContentSyncEnabled()) {
			log.info("Starting event listener so we sync content.");
			eventTrackingService.addLocalObserver(this);
			thread = new Thread(new ContentSyncPersister());
			thread.setDaemon(true); // Don't hold up shutdown waiting on this thread.
			thread.start();	
		} else {
			log.info("Content sync is not enabled.");
		}
	}
	
	public void destroy() {
		if (runThread) {
			try {
				runThread = false;
				thread.join(500); // Wait (timeout should fire, but not too long).
				if (thread.isAlive()) {
					log.warn("Didn't shutdown in 500ms waiting until the queue has cleared.");
					thread.join();
				}
			} catch (InterruptedException e) {
				log.warn("Got interrupted shutting down: "+ e.getMessage());
			}
		}
	}
		
	
	public void update(Observable o, Object arg) {
		 
		if (arg instanceof Event) {
			Event event = (Event)arg;
			if (event.getEvent().startsWith(CONTENT_EVENT) && (
					ContentHostingService.EVENT_RESOURCE_ADD.equals(event.getEvent()) || 
					ContentHostingService.EVENT_RESOURCE_WRITE.equals(event.getEvent()) ||
					ContentHostingService.EVENT_RESOURCE_REMOVE.equals(event.getEvent()))
				) {
				Reference ref = entityManager.newReference(event.getResource());
				postToken(event.getEvent(), ref.getId(), ref.getContext());
				
			} else if (DiscussionForumService.EVENT_FORUMS_ADD.equals(event.getEvent()) 
						|| DiscussionForumService.EVENT_FORUMS_REMOVE.equals(event.getEvent())
				){
				postToken(event.getEvent(), event.getResource(), event.getContext());
			}
			// TODO Need to deal with authz changes.
		}
	}

	private void postToken(String event, String reference, String context) {
		ContentSyncToken token = new ContentSyncToken(event, reference, context);
		try {
			queue.offer(token, 100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.warn("Failed to post item to queue: "+ token);
		}
	}

	
	/**
	 * This thread takes work off the queue and persists it in the database.
	 * @author buckett
	 *
	 */
	public class ContentSyncPersister implements Runnable {

		public void run() {

			try {
				ContentSyncToken token = null;
				do {
					// We poll with a timeout so that we don't hold up shutdown.
					token = queue.poll(100, TimeUnit.MILLISECONDS);
					if (token != null && contentSyncService.isSiteTracked(token.getResourceContext())) {
						ContentSyncTableDAO resource = new ContentSyncTableDAO();
						resource.setEvent(token.getResourceEvent());
						resource.setReference(token.getResourceReference());
						resource.setContext(token.getResourceContext());
						resource.setTimeStamp(Calendar.getInstance().getTime());
						dao.save(resource);

						// Ignore it if we don't have a push service.
						if (pushService != null) {
							pushService.broadcastToken(token);
						}
					}
				} while(runThread || !queue.isEmpty());
			} catch (InterruptedException intEx) {
				log.warn("We got interrupted.");
			} finally {
				log.info("Persiter thread stopped");
			}
		}

	}

}
