package org.sakaiproject.hierarchy.model;

import org.sakaiproject.hierarchy.model.base.BaseHierarchy;

public class Hierarchy extends BaseHierarchy implements
		org.sakaiproject.hierarchy.model.api.Hierarchy
{
	private static final long serialVersionUID = 1L;

	/* [CONSTRUCTOR MARKER BEGIN] */
	public Hierarchy()
	{
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Hierarchy(java.lang.String id)
	{
		super(id);
	}

	/**
	 * Constructor for required fields
	 */
	public Hierarchy(java.lang.String id, java.lang.String nodeid,
			java.lang.String name, java.lang.String realm)
	{

		super(id, nodeid, name, realm);
	}

	/* [CONSTRUCTOR MARKER END] */

}