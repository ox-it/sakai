/**
 * 
 */
package org.sakaiproject.hierarchy.api.model;

import java.util.Map;

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
	public Map getChildren();

	/**
	 * Set the value related to the column: children
	 * 
	 * @param children
	 *        the children value
	 */
	void setChildren(Map children);

	void addTochildren(Hierarchy hierarchy);

	/**
	 * Return the value associated with the column: properties
	 */
	Map getProperties();

	/**
	 * Set the value related to the column: properties
	 * 
	 * @param properties
	 *        the properties value
	 */
	void setProperties(Map properties);

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

	Hierarchy getChild(String testPath);

	HierarchyProperty getProperty(String string);

	String getId();

}
