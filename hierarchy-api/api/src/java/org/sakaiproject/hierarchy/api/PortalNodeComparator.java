package org.sakaiproject.hierarchy.api;

import java.util.Comparator;

import org.sakaiproject.hierarchy.api.model.PortalNode;

/**
 * Sorting for Portal Nodes.
 * Effectively sorts the nodes but titles if present and then by the path.
 * @author Matthew Buckett
 *
 */
public class PortalNodeComparator implements Comparator<PortalNode> {
	public int compare(PortalNode node1, PortalNode node2) {
		String title1 = node1.getTitle();
		String title2 = node2.getTitle();
		if (title1 == null) {
			if (title2 == null) {
				// Fallback to sorting on the path.
				return node1.getPath().compareTo(node2.getPath());
			} else {
				return +1;
			}
		} else {
			if (title2 == null) {
				return -1;
			} else {
				return title1.compareTo(title2);
			}
		}
	}
}
