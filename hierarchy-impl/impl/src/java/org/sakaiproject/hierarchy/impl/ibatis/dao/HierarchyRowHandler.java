package org.sakaiproject.hierarchy.impl.ibatis.dao;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.impl.HierarchyImpl;

import com.ibatis.sqlmap.client.event.RowHandler;

class HierarchyRowHandler implements RowHandler {

	private HierarchyDAO hierarchyDOA;

	private Hierarchy parent;

	HierarchyRowHandler(HierarchyDAO hierarchyDAO, Hierarchy parent) {
		this.hierarchyDOA = hierarchyDAO;
		this.parent = parent;
	}

	public void handleRow(Object object) {

		if (object instanceof HierarchyImpl) {
			HierarchyImpl hierarchy = (HierarchyImpl) object;
			hierarchy.setInternalChildren(new LazyHierarchyChildren(hierarchyDOA,
					hierarchy));
			if (parent == null) {
				if (hierarchy.getParentId() != null) {
					hierarchy.setInternalParent(new LazyHierarchyParent(hierarchyDOA,
							hierarchy.getParentId()));
				}
			} else {
				hierarchy.setInternalParent(parent);
			}
			hierarchy.setInternalProperties(new LazyHierarchyProperties(
					hierarchyDOA, hierarchy));
		}

	}
	
}
