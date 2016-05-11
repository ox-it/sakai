package org.sakaiproject.authz.devolved.devolvedadmintool.tool.producers;

import java.util.Comparator;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.Site;

/**
 * Just sorts all entities that are sites based on titles.
 * @author buckett
 *
 */
public class SiteTitleSorter implements Comparator<Entity> {

	public int compare(Entity o1, Entity o2) {
		if (o1 instanceof Site && o2 instanceof Site) {
			Site s1 = (Site)o1;
			Site s2 = (Site)o2;
			return s1.compareTo(s2);
		}
		if (o1 == null) {
			return (o2 == null)?0:-1; 
		}
		if (o2 == null) {
			return (o1 == null)?0:1;
		}
		return o1.getId().compareTo(o2.getId());
	}

}
