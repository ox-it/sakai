package org.sakaiproject.hierarchy.impl;

import java.util.Comparator;
import java.util.Date;

import org.sakaiproject.hierarchy.impl.portal.dao.PortalPersistentNode;

/**
 * Simple comparator that puts the newest node first.
 * 
 * @author Matthew Buckett
 */
public class OldestFirstComparator implements Comparator<PortalPersistentNode> {
	private static final Date EPOCH = new Date(0);

	public int compare(PortalPersistentNode o1, PortalPersistentNode o2) {
		Date o1Created = o1.getCreated();
		Date o2Created = o2.getCreated();
		if (o1Created == null) {
			o1Created = EPOCH;
		}
		if (o2Created == null) {
			o2Created = EPOCH;
		}
		return o1Created.compareTo(o2Created);
	}
}