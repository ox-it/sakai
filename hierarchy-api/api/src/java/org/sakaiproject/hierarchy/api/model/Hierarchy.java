/**
 * 
 */
package org.sakaiproject.hierarchy.api.model;

import java.util.Date;
import java.util.Map;

/**
 * @author ieb
 */
public interface Hierarchy
{
	/**
	 * Return the value associated with the column: name
	 */
	String getPath();
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

	/**
	 * Add or update a property. If the property name already exists then update the 
	 * value with the value of the supplied property.
	 * @param hierarchyProperty The new or existing hierarchyProperty.
	 */
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

	Hierarchy getChild(String childPath);

	/**
	 * Get the property.
	 * @param String key. 
	 * @return The HierarchyProperty or <code>null</code> if the property doesn't exist.
	 */
	HierarchyProperty getProperty(String string);

	String getId();

	/** 
	 * set the modified flag that controls saving operations
	 * @param b
	 */
	void setModified(boolean b);

	/**
	 * Has the hierarchy been modified
	 * @return
	 */
	boolean isModified();

	String getPathHash();

	Date getVersion();

	void setVersion(Date date);
	
	void setId(String string);

	HierarchyProperty addToproperties(String string, String value);

}
