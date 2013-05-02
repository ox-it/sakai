package org.sakaiproject.hierarchy.impl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNodeDao;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

public abstract class BasePortalHierarchyService {

	PortalPersistentNode node = new PortalPersistentNode();

	@Mock
	protected SiteService siteService;
	@Mock
	protected MemoryService memoryService;
	@Mock
	protected FunctionManager functionManager;
	@Mock
	protected EventTrackingService eventTrackingService;
	@Mock
	protected SessionManager sessionManager;
	@Mock
	protected SecurityService securityService;
	@Mock
	protected PortalPersistentNodeDao dao;

	@InjectMocks
	protected PortalHierarchyServiceImpl phs;
	

	@Before
	public void setUp() {
		// Disable caching
		Cache emptyCache = mock(Cache.class);
		when(memoryService.newCache(anyString())).thenReturn(emptyCache);
		when(memoryService.newCache(anyString(), anyString())).thenReturn(emptyCache);
		// And init us.
		phs.init();
	}
}
