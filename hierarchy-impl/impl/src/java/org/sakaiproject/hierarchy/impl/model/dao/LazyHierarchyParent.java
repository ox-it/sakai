package org.sakaiproject.hierarchy.impl.model.dao;

import java.util.Date;
import java.util.Map;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.hierarchy.impl.HierarchyImpl;

public class LazyHierarchyParent extends HierarchyImpl {
	

	private String lazyId = null;

	private HierarchyDAO dao = null;

	private boolean loaded = false;

	public LazyHierarchyParent(HierarchyDAO dao, String lazyId) {
		this.lazyId = lazyId;
		this.dao = dao;
	}

	protected void load() {
		if (!loaded) {
			Hierarchy h = dao.findHierarchyById(lazyId);
			loaded = true; // this potentially non thread safe until the
			// copy is complete
			this.copy(h);
		}
	}
	
	public void addTochildren(Hierarchy hierarchy) {
		load();
		super.addTochildren(hierarchy);
	}

	public void addToproperties(HierarchyProperty hierarchyProperty) {
		load();
		super.addToproperties(hierarchyProperty);
	}

	public HierarchyProperty addToproperties(String name, String value) {
		load();
		return super.addToproperties(name, value);
	}

	public int compareTo(Object obj) {
		load();
		return super.compareTo(obj);
	}

	public Hierarchy getChild(String path) {
		load();
		return super.getChild(path);
	}

	public Map getChildren() {
		load();
		return super.getChildren();
	}

	public String getId() {
		return lazyId;
	}

	public Hierarchy getParent() {
		load();
		return super.getParent();
	}

	public String getPath() {
		load();
		return super.getPath();
	}

	public String getPathHash() {
		load();
		return super.getPathHash();
	}

	public Map getProperties() {
		load();
		return super.getProperties();
	}

	public HierarchyProperty getProperty(String name) {
		load();
		return super.getProperty(name);
	}

	public String getRealm() {
		load();
		return super.getRealm();
	}

	public Date getVersion() {
		load();
		return super.getVersion();
	}

	public boolean isModified() {
		load();
		return super.isModified();
	}

	public void setChildren(Map children) {
		load();
		super.setChildren(children);
	}

	public void setId(String id) {
		load();
		super.setId(id);
	}

	public void setInternalChildren(LazyHierarchyChildren children) {
		load();
		super.setInternalChildren(children);
	}

	public void setInternalParent(Hierarchy parent) {
		load();
		super.setInternalParent(parent);
	}

	public void setInternalProperties(Map properties) {
		load();
		super.setInternalProperties(properties);
	}

	public void setModified(boolean modified) {
		load();
		super.setModified(modified);
	}

	public void setParent(Hierarchy parent) {
		load();
		super.setParent(parent);
	}

	public void setPath(String path) {
		load();
		super.setPath(path);
	}

	public void setProperties(Map properties) {
		load();
		super.setProperties(properties);
	}

	public void setRealm(String realm) {
		load();
		super.setRealm(realm);
	}

	public void setVersion(Date version) {
		load();
		super.setVersion(version);
	}

	public String toString() {
		load();
		return super.toString();
	}

}
