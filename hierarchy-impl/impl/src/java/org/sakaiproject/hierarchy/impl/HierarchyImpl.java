package org.sakaiproject.hierarchy.impl;

// BaseValueObjectImports

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

//BaseValueObjectClassComments

/**
 * This is an object that contains data related to the hierarchy_nodes table. Do
 * not modify this class because it will be overwritten if the configuration
 * file related to this class is modified.
 * 
 * @hibernate.class table="hierarchy_nodes"
 */

// BaseValueObjectClassDefinitions
public class HierarchyImpl implements Serializable, Comparable, Hierarchy
{

	private static final long serialVersionUID = 1L;


	protected void load()
	{
		// this is not a proxy so dont load anything
	}

	// Custom BaseValueObjectStaticProperties

	public static String REF = "Hierarchy";

	public static String PROP_REALM = "realm";

	public static String PROP_NODEID = "nodeid";

	public static String PROP_PARENT = "parent";

	public static String PROP_NAME = "path";
	
	public static String PROP_PATH_HASH = "pathhash";
	
	public static String PROP_ID = "id";
	
	
	protected void copy(Hierarchy source) {
		this.setId(source.getId());
		this.setPath(source.getPath());
		this.setPathHash(source.getPathHash());
		this.setParent(source.getParent());
		this.setRealm(source.getRealm());
		this.setVersion(source.getVersion());
		this.setChildren(source.getChildren());
		this.setProperties(source.getProperties());
	}

	// BaseValueObjectConstructor

	// constructors
	public HierarchyImpl()
	{
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public HierarchyImpl(String id)
	{
		this.setId(id);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public HierarchyImpl(String id, String pathhash, String path, String realm)
	{

		this.setId(id);
		this.setPathHash(pathhash);
		this.setPath(path);
		this.setRealm(realm);
		initialize();
	}

	protected void initialize()
	{
	}

	// Custom BaseValueObjectVariableDefinitions

	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private String id;

	Date version;

	// fields
	private String pathhash;

	private String path;

	private String realm;

	// many to one
	private Hierarchy parent = null;

	// collections
	private Map children = new HashMap();

	private Map properties = new HashMap();

	private boolean modified = false;

	// BaseValueObjectGetterIdGetterSetter

	/**
	 * Return the unique identifier of this class
	 * 
	 * @hibernate.id generator-class="uuid.hex" column="id"
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Set the unique identifier of this class
	 * 
	 * @param id
	 *        the new ID
	 */
	public void setId(String id)
	{
		modified = true;
		this.id = id;
		this.hashCode = Integer.MIN_VALUE;
	}

	// BaseValueObjectGetterSetter

	/**
	 * Return the value associated with the column: version
	 */
	public Date getVersion()
	{
		return version;
	}

	/**
	 * Set the value related to the column: version
	 * 
	 * @param version
	 *        the version value
	 */
	public void setVersion(Date version)
	{
		modified = true;
		this.version = version;
	}

	/**
	 * Return the value associated with the column: nodeid
	 */
	public java.lang.String getPathHash()
	{
		return pathhash;
	}

	/**
	 * Set the value related to the column: nodeid
	 * 
	 * @param nodeid
	 *        the nodeid value
	 */
	public void setPathHash(String pathhash)
	{
		modified = true;
		this.pathhash = pathhash;
	}

	/**
	 * Return the value associated with the column: name
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * Set the value related to the column: name
	 * 
	 * @param name
	 *        the name value
	 */
	public void setPath(String path)
	{
		modified = true;
		this.path = path;
	}

	/**
	 * Return the value associated with the column: realm
	 */
	public String getRealm()
	{
		return realm;
	}

	/**
	 * Set the value related to the column: realm
	 * 
	 * @param realm
	 *        the realm value
	 */
	public void setRealm(String realm)
	{
		modified = true;
		this.realm = realm;
	}

	/**
	 * Return the value associated with the column: parent_id
	 */
	public Hierarchy getParent()
	{
		return parent;
	}

	/**
	 * Set the value related to the column: parent_id
	 * 
	 * @param parent
	 *        the parent_id value
	 */
	public void setParent(Hierarchy parent)
	{
		modified = true;
		this.parent = parent;
	}

	/**
	 * Return the value associated with the column: children
	 */
	public Map getChildren()
	{
		return children;
	}

	/**
	 * Set the value related to the column: children
	 * 
	 * @param children
	 *        the children value
	 */
	public void setChildren(Map children)
	{
		modified = true;
		this.children = children;
		for ( Iterator i = this.children.values().iterator(); i.hasNext(); ) {
			Hierarchy h = (Hierarchy) i.next();
			h.setParent(this);
		}
	}

	public void addTochildren(Hierarchy hierarchy)
	{
		modified = true;
		if (null == getChildren()) setChildren(new HashMap());
		getChildren().put(hierarchy.getPath(),hierarchy);
		hierarchy.setParent(this);
	}

	/**
	 * Return the value associated with the column: properties
	 */
	public Map getProperties()
	{
		return properties;
	}

	/**
	 * Set the value related to the column: properties
	 * 
	 * @param properties
	 *        the properties value
	 */
	public void setProperties(Map properties)
	{
		modified = true;
		this.properties = properties;
		for ( Iterator i = this.properties.values().iterator(); i.hasNext(); ) {
			HierarchyProperty h = (HierarchyProperty) i.next();
			h.setNode(this);
		}
	}

	public void addToproperties(HierarchyProperty hierarchyProperty)
	{
		modified = true;
		if (null == getProperties()) setProperties(new HashMap());
		getProperties().put(hierarchyProperty.getName(),hierarchyProperty);
		hierarchyProperty.setNode(this);
	}

	// BaseValueObjectEqualityMethods

	/*
	 * public boolean equals (Object obj) { if (null == obj) return false; if
	 * (!(obj instanceof org.sakaiproject.hierarchy.model.Hierarchy)) return
	 * false; else { org.sakaiproject.hierarchy.model.Hierarchy hierarchy =
	 * (org.sakaiproject.hierarchy.model.Hierarchy) obj; if (null ==
	 * this.getId() || null == hierarchy.getId()) return false; else return
	 * (this.getId().equals(hierarchy.getId())); } } public int hashCode () { if
	 * (Integer.MIN_VALUE == this.hashCode) { if (null == this.getId()) return
	 * super.hashCode(); else { String hashStr = this.getClass().getName() + ":" +
	 * this.getId().hashCode(); this.hashCode = hashStr.hashCode(); } } return
	 * this.hashCode; }
	 */

	public int compareTo(Object obj)
	{
		int hashCmp = hashCode() - obj.hashCode();
		if (hashCmp == 0) return 0;
		if (hashCmp < 0) return -1;
		return 1;
	}

	// CustomBaseValueObjectToString

	public String toString()
	{
		return super.toString();
	}
	
	
	public Hierarchy getChild(String path)
	{
		return (Hierarchy) children.get(path);
	}

	public HierarchyProperty  getProperty(String name)
	{
		return (HierarchyProperty) properties.get(name);
	}

	public void setModified(boolean modified ) {
		this.modified  = modified;
	}
	public boolean isModified() {
		return modified;
	}

	// BaseValueObjectCustomContents
}