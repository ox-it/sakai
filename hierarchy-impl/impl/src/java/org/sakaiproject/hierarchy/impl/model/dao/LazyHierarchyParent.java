package org.sakaiproject.hierarchy.impl.model.dao;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.impl.HierarchyImpl;


public class LazyHierarchyParent extends
		HierarchyImpl
{
	private String lazyId = null;
	private HierarchyDAO dao = null;

	private boolean loaded = false;

	public LazyHierarchyParent(HierarchyDAO dao,String lazyId)
	{
		this.lazyId = lazyId;
		this.dao = dao;
	}

	protected void load()
	{
		if (!loaded)
		{
			Hierarchy h = (Hierarchy) dao.findHierarchyById(lazyId);
			loaded = true; // this potentially non thread safe until the
			// copy is complete
			this.copy(h);
		}
	}
}
