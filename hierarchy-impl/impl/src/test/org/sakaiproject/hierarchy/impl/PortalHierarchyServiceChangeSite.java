package org.sakaiproject.hierarchy.impl;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.hierarchy.api.PortalHierarchyService;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNodeDao;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * This just aims to test the new caching.
 * 
 * @author Matthew Buckett
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class PortalHierarchyServiceChangeSite {

	PortalPersistentNode node = new PortalPersistentNode();

	@Mock
	private SiteService siteService;
	@Mock
	private MemoryService memoryService;
	@Mock
	private FunctionManager functionManager;
	@Mock
	private EventTrackingService eventTrackingService;
	@Mock
	private SessionManager sessionManager;
	@Mock
	private SecurityService securityService;
	@Mock
	private PortalPersistentNodeDao dao;

	@InjectMocks
	private PortalHierarchyServiceImpl phs;

	@Before
	public void setUp() {
		// Disable caching
		Cache emptyCache = mock(Cache.class);
		when(memoryService.newCache(anyString())).thenReturn(emptyCache);
		when(memoryService.newCache(anyString(), anyString())).thenReturn(emptyCache);
		// And init us.
		phs.init();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChangeSiteOnRedirect() throws PermissionException {
		PortalPersistentNode persistentNode = new PortalPersistentNode();
		persistentNode.setRedirectUrl("http://example.com");
		when(dao.findById(anyString())).thenReturn(persistentNode);

		phs.changeSite("id", "newSiteId");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChangeSiteNotFound() throws PermissionException {
		when(dao.findById(anyString())).thenReturn(null);

		phs.changeSite("id", "newSiteId");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testChangeSiteNewSiteMissing() throws Exception {
		PortalPersistentNode node = new PortalPersistentNode();
		when(dao.findById(anyString())).thenReturn(node);
		when(siteService.getSite(anyString())).thenThrow(new IdUnusedException("newSiteId"));
		phs.setSiteService(siteService);
		phs.setDao(dao);

		phs.changeSite("id", "newSiteId");
	}

	@Test(expected = PermissionException.class)
	public void testChangeSiteNotManageCurrent() throws Exception {
		PortalPersistentNode node = new PortalPersistentNode();
		when(dao.findById(anyString())).thenReturn(node);
		Site site = mock(Site.class);
		when(siteService.getSite(anyString())).thenReturn(site);
		when(site.getReference()).thenReturn("/site/siteId");
		Session session = mock(Session.class);
		when(sessionManager.getCurrentSession()).thenReturn(session);
		when(session.getUserEid()).thenReturn("eid");

		phs.changeSite("id", "newSiteId");
	}

	@Test(expected = PermissionException.class)
	public void testChangeSiteNotManageNew() throws Exception {
		PortalPersistentNode node = new PortalPersistentNode();
		when(dao.findById(anyString())).thenReturn(node);
		Site site = mock(Site.class);
		when(siteService.getSite(anyString())).thenReturn(site);
		when(site.getReference()).thenReturn("/site/siteId");
		Session session = mock(Session.class);
		when(sessionManager.getCurrentSession()).thenReturn(session);
		when(session.getUserEid()).thenReturn("eid");
		when(securityService.unlock(PortalHierarchyService.SECURE_MODIFY, "/site/siteId")).thenReturn(true);

		phs.changeSite("id", "newSiteId");
	}

	@Test
	public void testChangeSite() throws Exception {
		PortalPersistentNode node = new PortalPersistentNode();
		node.setSiteId("siteId");
		when(dao.findById(anyString())).thenReturn(node);
		Site siteNew = mock(Site.class);
		Site siteOld = mock(Site.class);
		when(siteService.getSite("siteId")).thenReturn(siteOld);
		when(siteService.getSite("newSiteId")).thenReturn(siteNew);
		when(siteOld.getReference()).thenReturn("/site/siteId");
		when(siteNew.getReference()).thenReturn("/site/newSiteId");
		when(siteService.siteReference("siteId")).thenReturn("/site/siteId");
		Session session = mock(Session.class);
		when(sessionManager.getCurrentSession()).thenReturn(session);
		when(session.getUserEid()).thenReturn("eid");
		when(securityService.unlock(PortalHierarchyService.SECURE_MODIFY, "/site/siteId")).thenReturn(true);
		when(securityService.unlock(SiteService.SECURE_UPDATE_SITE, "/site/newSiteId")).thenReturn(true);

		phs.changeSite("id", "newSiteId");

		verify(dao, times(1)).save(node);
		verify(eventTrackingService, times(1)).post(any(Event.class));

	}

}
