/**
 * 
 */
package org.sakaiproject.hierarchy.model.api;

import java.util.Set;

/**
 * @author ieb
 */
public interface Hierarchy
{
	/**
	 * Return the value associated with the column: name
	 */
	String getName();

	/**
	 * Return the value associated with the column: parent_id
	 */
	Hierarchy getParent();

	/**
	 * Set the value related to the column: parent_id
	 * 
	 * @param parent
	 *        the parent_id value
	 */
	void setParent(Hierarchy parent);

	/**
	 * Return the value associated with the column: children
	 */
	public java.util.Set getChildren();

	/**
	 * Set the value related to the column: children
	 * 
	 * @param children
	 *        the children value
	 */
	void setChildren(Set children);

	void addTochildren(Hierarchy hierarchy);

	/**
	 * Return the value associated with the column: properties
	 */
	Set getProperties();

	/**
	 * Set the value related to the column: properties
	 * 
	 * @param properties
	 *        the properties value
	 */
	void setProperties(Set properties);

	void addToproperties(HierarchyProperty hierarchyProperty);

	/**
	 * Return the value associated with the column: realm
	 */
	String getRealm();

	/**
	 * Set the value related to the column: realm
	 * 
	 * @param realm
	 *        the realm value
	 */
	void setRealm(String realm);

}
