package org.sakaiproject.hierarchy.impl;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.hierarchy.impl.PortalHierarchyServiceImpl.DerivedPathCache;
import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;
import org.sakaiproject.memory.api.Cache;

/**
 * @author Matthew Buckett
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDerivedPathCache {

	DerivedPathCache derivedPathCache;

	@Mock
	Cache cache;

	@Before
	public void setUp() {
		derivedPathCache = new DerivedPathCache(cache);
	}

	@Test
	public void testCachePutBadValues() {
		derivedPathCache.notifyCachePut("key", "payload");
		verify(cache, never()).put(any(), any());
	}

	@Test
	public void testCachePutNullPayload() {
		derivedPathCache.notifyCachePut("key", null);
		verify(cache, never()).put(any(), any());
	}

	@Test
	public void testCachePut() {
		PortalPersistentNode node = mock(PortalPersistentNode.class);
		when(node.getPath()).thenReturn("path");
		when(node.getId()).thenReturn("id");
		derivedPathCache.notifyCachePut("id", node);
		verify(cache, times(1)).put("path", "id");
	}

	@Test
	public void testCacheClear() {
		derivedPathCache.notifyCacheClear();
		verify(cache, times(1)).clear();
	}

	@Test
	public void testCacheRemoveBadValues() {
		derivedPathCache.notifyCacheRemove("key", "payload");
		verify(cache, never()).remove(any());
	}

	@Test
	public void testCacheRemoveNullPayload() {
		derivedPathCache.notifyCacheRemove("key", null);
		verify(cache, never()).remove(any());
	}

	@Test
	public void testCacheRemove() {
		PortalPersistentNode node = mock(PortalPersistentNode.class);
		when(node.getPath()).thenReturn("path");
		derivedPathCache.notifyCacheRemove("key", node);
		verify(cache, times(1)).remove("path");
	}
}
