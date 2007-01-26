/**
 * 
 */
package org.sakaiproject.hierarchy.api;

/**
 * @author ieb
 *
 */
public class HierarchyServiceException extends Exception
{

	/**
	 * 
	 */
	public HierarchyServiceException()
	{
		super();
	}

	/**
	 * @param arg0
	 */
	public HierarchyServiceException(String arg0)
	{
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public HierarchyServiceException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public HierarchyServiceException(Throwable arg0)
	{
		super(arg0);
	}

}
