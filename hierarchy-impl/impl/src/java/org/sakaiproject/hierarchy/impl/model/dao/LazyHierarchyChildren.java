package org.sakaiproject.hierarchy.impl.model.dao;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.hierarchy.api.model.Hierarchy;

public class LazyHierarchyChildren extends MapProxy
{

	private Hierarchy parent = null;
	private HierarchyDAO dao = null;
	public LazyHierarchyChildren(HierarchyDAO dao,Hierarchy parent)
	{
		this.parent = parent;
		this.dao = dao;
	}
	protected void load()
	{
		if (target == null)
		{
			List l = dao.findHierarchyByParent(parent);
			target = new HashMap();
			for (Iterator i = l.iterator(); i.hasNext();)
			{
				Hierarchy hp = (Hierarchy) i.next();
				target.put(hp.getPath(), hp);
			}
			
		}
	}

}
