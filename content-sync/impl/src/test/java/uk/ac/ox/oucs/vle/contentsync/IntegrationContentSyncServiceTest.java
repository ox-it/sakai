package uk.ac.ox.oucs.vle.contentsync;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * This test checks the bean wiring.
 * @author buckett
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/db-hibernate.xml", "classpath:/content-sync.xml", "classpath:/content-sync-support.xml"})
@Transactional
public class IntegrationContentSyncServiceTest extends Assert {

	@Autowired
	@Qualifier("uk.ac.ox.oucs.vle.contentsync.ContentSyncDAO")
	private ContentSyncDAO contentSyncDAO;

	@Autowired
	private ContentSyncTracker contentSyncTracker;

	@Autowired
	private SiteService siteService;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private ServerConfigurationService serverConfigurationService;

	@Before
	public void onSetUp() throws Exception {
		contentSyncTracker.destroy();
		when(serverConfigurationService.getBoolean(eq(ContentSyncService.CONTENT_SYNC_ENABLED), anyBoolean())).thenReturn(true);
		// The original init happened before we mocked out the config.
		contentSyncTracker.init();
	}
	
	/**
	 * This is to test that our transactions are working correctly.
	 */
	@Test
	public void testInsertAndRetrieve() {
		// We put an entry in.
		createTestDAO();

		// We check it comes back out.
		List<ContentSyncTableDAO> found = contentSyncDAO.findResourceTrackers("test", new Date(0));
		assertEquals(1, found.size());
		assertNotNull(found.get(0).getId());
	}

	@Test
	public void testQueue() throws Exception {
		// Mock the site interactions
		Site site = mock(Site.class);
		ResourceProperties properties = mock(ResourceProperties.class);
		when(properties.getProperty(ContentSyncService.TRACK_CONTENT)).thenReturn("true");
		when(site.getProperties()).thenReturn(properties);
		when(siteService.getSite(anyString())).thenReturn(site);
		
		Reference ref = mock(Reference.class);
		when(ref.getContext()).thenReturn("context");
		when(ref.getId()).thenReturn("id");
		when(entityManager.newReference(anyString())).thenReturn(ref);
		
		FakeEvent event = new FakeEvent();
		event.setContext("context");
		event.setResource("/some/resource");
		event.setUserId("userId");
		event.setEvent(ContentHostingService.EVENT_RESOURCE_ADD);
		event.setModify(true);
		
		contentSyncTracker.update(null, event);
		
		// This will clear the queue.
		contentSyncTracker.destroy();
		
		assertEquals(1,contentSyncDAO.findResourceTrackers("context", new Date(0)).size());
		
		// Restart things.
		contentSyncTracker.init();
	}

	private void createTestDAO() {
		ContentSyncTableDAO entry = new ContentSyncTableDAO();
		entry.setContext("test");
		entry.setEvent("test.event");
		entry.setReference("/test/reference");
		entry.setTimeStamp(new Date());
		contentSyncDAO.save(entry);
	}

}
