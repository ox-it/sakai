package org.sakaiproject.hierarchy.model;

import org.sakaiproject.hierarchy.model.base.BaseHierarchyProperty;

public class HierarchyProperty extends BaseHierarchyProperty implements
		org.sakaiproject.hierarchy.model.api.HierarchyProperty
{
	private static final long serialVersionUID = 1L;

	/* [CONSTRUCTOR MARKER BEGIN] */
	public HierarchyProperty()
	{
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public HierarchyProperty(java.lang.String id)
	{
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public HierarchyProperty(java.lang.String id, java.lang.String name,
			java.lang.String propvalue)
	{

		super(id, name, propvalue);
	}

	/* [CONSTRUCTOR MARKER END] */

}