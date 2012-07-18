package uk.ac.ox.oucs.vle.contentsync;

import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;

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
	
	private static String REFERENCE_ROOT = Entity.SEPARATOR + "forums";
	
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
	private DiscussionForumManager discussionForumManager;
	public void setDiscussionForumManager(DiscussionForumManager discussionForumManager) {
		this.discussionForumManager = discussionForumManager;
	}
	
	/**
	 * 
	 */
	private EventTrackingService eventTrackingService;
	public void setEventTrackingService(EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}
	
	private ContentSyncPushService pushService;
	public void setPushService(ContentSyncPushService pushService) {
		this.pushService = pushService;
	}
	
	/**
	 * 
	 */
	private ContentSyncSessionBean contentSyncSessionBean;
	public void setContentSyncSessionBean(ContentSyncSessionBean contentSyncSessionBean) {
		this.contentSyncSessionBean = contentSyncSessionBean;
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
	
	Thread thread;

	public void init() {
		eventTrackingService.addLocalObserver(this);
		thread = new Thread(new ContentSyncPersister());
		thread.start();
	}
	
	public void destroy() {
		try {
			queue.put(new ContentSyncToken(EVENT_RESOURCE_DONE, "DONE", null));
			thread.stop();
		} catch (InterruptedException e) {
			System.out.println("Interrupted! " + 
	                "Last one out, turn out the lights!");
		}
	}
		
	
	public void update(Observable o, Object arg) {
		 
		if (arg instanceof Event) {
			Event event = (Event)arg;
			if 
				/*
				(ContentHostingService.EVENT_RESOURCE_ADD.equals(event.getEvent()) || 
				ContentHostingService.EVENT_RESOURCE_READ.equals(event.getEvent()) ||
				ContentHostingService.EVENT_RESOURCE_WRITE.equals(event.getEvent()) ||
				ContentHostingService.EVENT_RESOURCE_REMOVE.equals(event.getEvent())) 
				*/
				(event.getEvent().startsWith(ContentHostingService.REFERENCE_ROOT.substring(1))) {
				
				Reference ref = entityManager.newReference(event.getResource());
				
				try {
					queue.put(new ContentSyncToken(
							event.getEvent(), ref.getId(), ref.getContext()));
					
				} catch (InterruptedException e) {
					System.out.println("Interrupted! " + 
			                "Last one out, turn out the lights!");
				}
				
			} else if (DiscussionForumService.EVENT_FORUMS_ADD.equals(event.getEvent()) 
				  //   DiscussionForumService.EVENT_FORUMS_FORUM_ADD.equals(event.getEvent()) ||
				  //   DiscussionForumService.EVENT_FORUMS_FORUM_REMOVE.equals(event.getEvent()) ||
				  //   DiscussionForumService.EVENT_FORUMS_FORUM_REVISE.equals(event.getEvent()) ||
					   
				  //   DiscussionForumService.EVENT_FORUMS_GRADE.equals(event.getEvent()) ||
				  //   DiscussionForumService.EVENT_FORUMS_READ.equals(event.getEvent()) ||
					   || DiscussionForumService.EVENT_FORUMS_REMOVE.equals(event.getEvent()) 
				  //   DiscussionForumService.EVENT_FORUMS_RESPONSE.equals(event.getEvent()) ||
				  //   DiscussionForumService.EVENT_FORUMS_REVISE.equals(event.getEvent()) ||
					   
				  //   || DiscussionForumService.EVENT_FORUMS_TOPIC_ADD.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_FORUMS_TOPIC_REMOVE.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE.equals(event.getEvent()) 
					   
				  //   || DiscussionForumService.EVENT_MESSAGES_ADD.equals(event.getEvent())
				  //   || DiscussionForumService.EVENT_MESSAGES_FOLDER_ADD.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_MESSAGES_FOLDER_REMOVE.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_MESSAGES_FOLDER_REVISE.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_MESSAGES_FORWARD.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_MESSAGES_READ.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_MESSAGES_REMOVE.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_MESSAGES_RESPONSE.equals(event.getEvent()) 
				  //   || DiscussionForumService.EVENT_MESSAGES_UNREAD.equals(event.getEvent())) 
			)
			{
				
				try {
					queue.put(new ContentSyncToken(
							event.getEvent(), event.getResource(), event.getContext()));
					
				} catch (InterruptedException e) {
					System.out.println("Interrupted! " + 
			                "Last one out, turn out the lights!");
				}
			
			} else if (event.getEvent().startsWith(AuthzGroupService.REFERENCE_ROOT.substring(1))) {
				
			}
			
		}
	}
	
	public class ContentSyncPersister implements Runnable {
		
		public void run() {
			
			try {
		        ContentSyncToken token = null;
		        while (!((token = queue.take())
		        		.getResourceEvent().equals(ContentSyncTracker.EVENT_RESOURCE_DONE))) {
		        	
		        	ContentSyncTableDAO resource = new ContentSyncTableDAO();
		        	resource.setEvent(token.getResourceEvent());
		        	resource.setReference(token.getResourceReference());
		        	resource.setContext(token.getResourceContext());
		        	resource.setTimeStamp(Calendar.getInstance().getTime());
		        	dao.save(resource);
		        	
		        	if (token.getResourceEvent().startsWith(ContentHostingService.REFERENCE_ROOT.substring(1))) {
		        		pushService.broadcast("{\"aps\":{\"badge\":\"+1\"}}");
		        	}
		        	
		        	if (DiscussionForumService.EVENT_FORUMS_TOPIC_ADD.equals(token.getResourceEvent())) {	
		        		pushService.broadcast("{\"aps\":{\"badge\":\"+1\"}}");
		        	}
		        	if (DiscussionForumService.EVENT_FORUMS_TOPIC_REMOVE.equals(token.getResourceEvent())) {	
		        		pushService.broadcast("{\"aps\":{\"badge\":\"+1\"}}");
		        	}
		        	
		        	if (DiscussionForumService.EVENT_FORUMS_REMOVE.equals(token.getResourceEvent())) {
		        		long id = contentSyncSessionBean.getTopicId(
		        				new Long(parseReference(token.getResourceReference())));
		        		if (0 != id) {
		        			pushService.broadcast("{\"message\":\""+token.getResourceEvent()+"\",\"title\":\""+id+"\"}");
		        		}
		        	}
		        	
		        	if (DiscussionForumService.EVENT_FORUMS_ADD.equals(token.getResourceEvent())) {
		        		long id = contentSyncSessionBean.getTopicId(
		        				new Long(parseReference(token.getResourceReference())));
		        		if (0 != id) {
		        			pushService.broadcast("{\"message\":\""+token.getResourceEvent()+"\",\"title\":\""+id+"\"}");
		        		}
		        	}
		        	
		        	/*
		        	if (DiscussionForumService.EVENT_FORUMS_RESPONSE.equals(token.getResourceEvent())) {
		        		// new Thread or Message
		        		long id = contentSyncSessionBean.getTopicId(
		        				new Long(parseReference(token.getResourceReference())));
		        		if (0 != id) {
		        			pushService.push("{\"tags\":[\""+id+"\"],\"aps\":{\"badge\":\"+1\"}}");
		        		}
		        	}
		        	*/
		        }
		            
		    } catch (InterruptedException intEx) {
		        System.out.println("Interrupted! " + 
		                "Last one out, turn out the lights!");
		    }
		}
		
		public long parseReference(String reference) {
			
			if (reference.startsWith(REFERENCE_ROOT)) {
				// /syllabus/siteid/syllabusid
				String[] parts = split(reference, Entity.SEPARATOR);

				String subType = null;
				String context = null;
				String id = null;
				String container = null;

				if (parts.length > 3) {
					// the site/context
					context = parts[3];

					// the subType
					if (parts.length > 4) {
						subType = parts[4];
						
						if (parts.length > 5) {
							id = parts[5];
						}
					}
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

}
