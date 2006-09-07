/**
 * 
 */
package org.sakaiproject.hierarchy.model.api;

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

}
