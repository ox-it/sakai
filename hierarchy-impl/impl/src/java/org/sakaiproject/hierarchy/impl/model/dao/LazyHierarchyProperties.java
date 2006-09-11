package org.sakaiproject.hierarchy.impl.model.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

public class LazyHierarchyProperties extends MapProxy
{

	private Hierarchy owner = null;

	private HierarchyDAO dao = null;

	public LazyHierarchyProperties(HierarchyDAO dao, Hierarchy owner)
	{
		this.owner = owner;
		this.dao = dao;
	}

	protected void load()
	{
		if (target == null)
		{
			
			List l = dao.findHierarchyProperties(owner);
			target = new HashMap();
			for (Iterator i = l.iterator(); i.hasNext();)
			{
				HierarchyProperty hp = (HierarchyProperty) i.next();
				target.put(hp.getName(), hp);
			}
		}
	}

}
