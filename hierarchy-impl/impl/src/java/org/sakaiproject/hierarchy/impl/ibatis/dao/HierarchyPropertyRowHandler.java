package org.sakaiproject.hierarchy.impl.ibatis.dao;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

import com.ibatis.sqlmap.client.event.RowHandler;

public class HierarchyPropertyRowHandler implements RowHandler {

	private Hierarchy node;

	HierarchyPropertyRowHandler(Hierarchy parent) {
		this.node = parent;
	}
	
	public void handleRow(Object arg0) {
		if (arg0 instanceof HierarchyProperty) {
			HierarchyProperty property = (HierarchyProperty) arg0;
			property.setNode(node);
		}
	}

}
