package org.sakaiproject.hierarchy.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNodeDao;
import org.sakaiproject.memory.api.Cache;

import static org.mockito.Mockito.*;
import static org.sakaiproject.hierarchy.api.PortalHierarchyService.EVENT_MODIFY;
import static org.sakaiproject.hierarchy.impl.PortalHierarchyServiceImpl.SiteToNodeObserver;

/**
 * @author Matthew Buckett
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSiteToNodeObserver {

	@Mock
	private Cache cache;

	@Mock
	private PortalPersistentNodeDao dao;

	private SiteToNodeObserver observer;

	@Before
	public void setUp() {
		observer = new SiteToNodeObserver(cache, dao);
	}

	@Test
	public void testUpdateNull() {
		observer.update(null, null);
	}

	@Test
	public void testUpdate() {
		Event event = mockEvent("/portalnode/1", EVENT_MODIFY, true);
		PortalPersistentNode node = mockNode("siteId");
		when(dao.findById("1")).thenReturn(node);

		observer.update(null, event);

		verify(cache, atLeastOnce()).remove("siteId");
	}

	@Test
	public void testUpdateNotInDb() {
		Event event = mockEvent("/portalnode/1", EVENT_MODIFY, true);
		PortalPersistentNode node = mockNode("siteId");
		when(dao.findById("1")).thenReturn(null);

		observer.update(null, event);

		verify(cache, never()).remove("siteId");

	}

	@Test
	public void testUpdateNotSiteNode() {
		Event event = mockEvent("/portalnode/1", EVENT_MODIFY, true);
		PortalPersistentNode node = mockNode(null);

		observer.update(null, event);

		verify(cache, never()).remove(any());
	}

	private Event mockEvent(String resource, String event, boolean modify) {
		Event mock = mock(Event.class);
		when(mock.getModify()).thenReturn(modify);
		when(mock.getEvent()).thenReturn(event);
		when(mock.getResource()).thenReturn(resource);
		return mock;
	}

	private PortalPersistentNode mockNode(String siteId) {
		PortalPersistentNode node = mock(PortalPersistentNode.class);
		when(node.getSiteId()).thenReturn(siteId);
		return node;
	}

}
