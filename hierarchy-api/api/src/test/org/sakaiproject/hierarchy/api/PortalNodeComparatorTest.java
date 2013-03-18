package org.sakaiproject.hierarchy.api;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.sakaiproject.hierarchy.api.model.PortalNode;

public class PortalNodeComparatorTest {


	@Test
	public void testSimple() {
		PortalNode node1 = mockNode("Node 1 Title", "/node1");
		PortalNode node2 = mockNode("Node 2 Title", "/node2");
		PortalNode node3 = mockNode("Node 3 Title", "/node3");
		
		List<PortalNode> nodes = Arrays.asList(new PortalNode[]{node2, node3, node1});
		Collections.sort(nodes, new PortalNodeComparator());
		
		assertArrayEquals(new PortalNode[]{node1, node2, node3}, nodes.toArray());
	}
	
	@Test
	public void testMissingTitle() {
		PortalNode node1 = mockNode("Node 1 Title", "/node1");
		PortalNode node2 = mockNode(null, "/node2");
		PortalNode node3 = mockNode("Node 3 Title", "/node3");
		
		List<PortalNode> nodes = Arrays.asList(new PortalNode[]{node2, node3, node1});
		Collections.sort(nodes, new PortalNodeComparator());
		
		assertArrayEquals(new PortalNode[]{node1, node3, node2}, nodes.toArray());
	}
	
	@Test
	public void testMissingNoTitles() {
		PortalNode node1 = mockNode(null, "/node1");
		PortalNode node2 = mockNode(null, "/node2");
		PortalNode node3 = mockNode(null, "/node3");
		
		List<PortalNode> nodes = Arrays.asList(new PortalNode[]{node2, node3, node1});
		Collections.sort(nodes, new PortalNodeComparator());
		
		assertArrayEquals(new PortalNode[]{node1, node2, node3}, nodes.toArray());
	}

	private PortalNode mockNode(String title, String path) {
		PortalNode node = mock(PortalNode.class);
		when(node.getTitle()).thenReturn(title);
		when(node.getPath()).thenReturn(path);
		return node;
	}

}
