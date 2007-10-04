package org.sakaiproject.hierarchy.impl.ibatis.dao;

import java.util.Date;
import java.util.Map;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;

/**
 * Adds some extra information needed when doing updates.
 * @author buckett
 *
 */
public class HierarchyWrapper implements Hierarchy {

	private Hierarchy hierarchy;
	private String parentId;
	private Date oldVersion;
	

	public HierarchyWrapper(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public void addTochildren(Hierarchy hierarchy) {
		hierarchy.addTochildren(hierarchy);
	}

	public void addToproperties(HierarchyProperty hierarchyProperty) {
		hierarchy.addToproperties(hierarchyProperty);
	}

	public HierarchyProperty addToproperties(String string, String value) {
		return hierarchy.addToproperties(string, value);
	}

	public Hierarchy getChild(String childPath) {
		return hierarchy.getChild(childPath);
	}

	public Map getChildren() {
		return hierarchy.getChildren();
	}

	public String getId() {
		return hierarchy.getId();
	}

	public Hierarchy getParent() {
		return hierarchy.getParent();
	}

	public String getPath() {
		return hierarchy.getPath();
	}

	public String getPathHash() {
		return hierarchy.getPathHash();
	}

	public Map getProperties() {
		return hierarchy.getProperties();
	}

	public HierarchyProperty getProperty(String string) {
		return hierarchy.getProperty(string);
	}

	public String getRealm() {
		return hierarchy.getRealm();
	}

	public Date getVersion() {
		return hierarchy.getVersion();
	}

	public boolean isModified() {
		return hierarchy.isModified();
	}

	public void setChildren(Map children) {
		hierarchy.setChildren(children);
	}

	public void setId(String string) {
		hierarchy.setId(string);
	}

	public void setModified(boolean b) {
		hierarchy.setModified(b);
	}

	public void setParent(Hierarchy parent) {
		hierarchy.setParent(parent);
	}

	public void setProperties(Map properties) {
		hierarchy.setProperties(properties);
	}

	public void setRealm(String realm) {
		hierarchy.setRealm(realm);
	}

	public void setVersion(Date date) {
		hierarchy.setVersion(date);
	}

	public Date getOldVersion() {
		return oldVersion;
	}

	public void setOldVersion(Date oldVersion) {
		this.oldVersion = oldVersion;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
}
