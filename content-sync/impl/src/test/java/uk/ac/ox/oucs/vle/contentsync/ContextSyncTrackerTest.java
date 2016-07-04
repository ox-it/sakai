package uk.ac.ox.oucs.vle.contentsync;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;

public class ContextSyncTrackerTest {

	private EventTrackingService eventTrackingService;
	private ContentSyncPushServiceImpl contentSyncPuchServiceImpl;
	private EntityManager entityManager;
	private ContentSyncDAO contentSyncDAO;
	private ContentSyncService contentSyncService;
	
	private ContentSyncTracker tracker;

	@Before
	public void setUp() {
		eventTrackingService = mock(EventTrackingService.class);
		contentSyncPuchServiceImpl = mock(ContentSyncPushServiceImpl.class);
		entityManager = mock(EntityManager.class);
		contentSyncDAO = mock(ContentSyncDAO.class);
		contentSyncService = mock(ContentSyncService.class);
		
		tracker = new ContentSyncTracker();
		tracker.setEventTrackingService(eventTrackingService);
		tracker.setPushService(contentSyncPuchServiceImpl);
		tracker.setEntityManager(entityManager);
		tracker.setContentSyncDao(contentSyncDAO);
		tracker.setContentSyncService(contentSyncService);
		
		when(contentSyncService.isContentSyncEnabled()).thenReturn(true);

	}

	@Test
	public void testInit() {
		tracker.init();
		// Check we setup the obverver
		verify(eventTrackingService).addLocalObserver(tracker);
		tracker.destroy();
	}
	
	@Test
	public void testNewEvent() {
		Event event = mock(Event.class);
		when(event.getEvent()).thenReturn("event.id");
		tracker.update(null, event);
		verify(contentSyncDAO, never()).save(any(ContentSyncTableDAO.class));
	}
	
	@Test
	public void testContentEvent() throws Exception {
		String siteId = "mysite";
		String contentId = "/group/"+ siteId +"/example";
		String contentResource = "/content"+ contentId;
		
		Event event = mock(Event.class);
		when(event.getEvent()).thenReturn(ContentHostingService.EVENT_RESOURCE_REMOVE);
		when(event.getResource()).thenReturn(contentResource);
		
		Reference reference = mock(Reference.class);
		when(reference.getId()).thenReturn(contentId);
		when(reference.getContext()).thenReturn(siteId);
		
		when(contentSyncService.isSiteTracked(siteId)).thenReturn(true);
		when(entityManager.newReference(contentResource)).thenReturn(reference);
		
		tracker.update(null, event);

		// We don't have any saves, as we don't have the thread running.
		verify(contentSyncDAO, never()).save(any(ContentSyncTableDAO.class));
		
		tracker.init();
		tracker.destroy();
		
		verify(contentSyncDAO, times(1)).save(any(ContentSyncTableDAO.class));
	}
	
	@Test
	public void testMessageEvent() throws Exception {
		String siteId = "mysite";
		String messageResource = "/messages&forums/site/"+ siteId + "/objectId/userId";
		
		Event event = mock(Event.class);
		when(event.getEvent()).thenReturn(DiscussionForumService.EVENT_FORUMS_ADD);
		when(event.getResource()).thenReturn(messageResource);
		when(event.getContext()).thenReturn(siteId);
		
		ResourceProperties rp = mock(ResourceProperties.class);
		when(rp.getProperty("trackContent")).thenReturn("true");
		Site site = mock(Site.class);
		when(site.getProperties()).thenReturn(rp);
		when(contentSyncService.isSiteTracked(siteId)).thenReturn(true);
		
		
		tracker.update(null, event);
		
		Event ignoreEvent = mock(Event.class);
		when(ignoreEvent.getEvent()).thenReturn(DiscussionForumService.EVENT_FORUMS_READ);
		when(ignoreEvent.getResource()).thenReturn(messageResource);
		when(ignoreEvent.getContext()).thenReturn(siteId);
		
		tracker.update(null, ignoreEvent);
		
		Event ignoreSite = mock(Event.class);
		when(ignoreSite.getEvent()).thenReturn(DiscussionForumService.EVENT_FORUMS_ADD);
		when(ignoreSite.getResource()).thenReturn(messageResource);
		when(ignoreSite.getContext()).thenReturn("otherSite");
				
		tracker.update(null, ignoreSite);
		
		tracker.init();
		tracker.destroy();
		
		verify(contentSyncDAO, times(1)).save(any(ContentSyncTableDAO.class));

	}
}
