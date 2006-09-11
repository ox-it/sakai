/**
 * 
 */
package org.sakaiproject.hierarchy.api.model;

import java.util.Date;

/**
 * @author ieb
 */
public interface HierarchyProperty
{

	/**
	 * Return the value associated with the column: name
	 */
	String getName();

	/**
	 * Set the value related to the column: name
	 * 
	 * @param name
	 *        the name value
	 */
	void setName(String name);

	/**
	 * Return the value associated with the column: propvalue
	 */
	String getPropvalue();

	/**
	 * Set the value related to the column: propvalue
	 * 
	 * @param propvalue
	 *        the propvalue value
	 */
	void setPropvalue(String propvalue);

	String getId();
	
	Hierarchy getNode();

	/** 
	 * Has the hierarchy been modified.
	 * @return
	 */
	boolean isModified();
	

	/**
	 * set the modified flag
	 * @param b
	 */
	void setModified(boolean b);

	Date getVersion();

	void setId(String string);

	void setVersion(Date object);

	void setNode(Hierarchy node);

}
