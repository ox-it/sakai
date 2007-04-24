package org.sakaiproject.hierarchy.impl.ibatis.dao;

import java.util.Date;

import org.sakaiproject.hierarchy.api.model.Hierarchy;
import org.sakaiproject.hierarchy.api.model.HierarchyProperty;
import org.sakaiproject.hierarchy.impl.HierarchyPropertyImpl;

public class HierarchyPropertyWrapper implements HierarchyProperty {

	private HierarchyProperty wrapped;
	private String nodeId;
	private Date oldVersion;

	public HierarchyPropertyWrapper(HierarchyProperty hierarchyProperty) {
		this.wrapped = hierarchyProperty;
		this.nodeId = getNode().getId();
	}
	
	public HierarchyPropertyWrapper() {
		this.wrapped = new HierarchyPropertyImpl();
	}

	public String getId() {
		return wrapped.getId();
	}

	public String getName() {
		return wrapped.getName();
	}

	public Hierarchy getNode() {
		return wrapped.getNode();
	}

	public String getPropvalue() {
		return wrapped.getPropvalue();
	}

	public Date getVersion() {
		return wrapped.getVersion();
	}

	public boolean isModified() {
		return wrapped.isModified();
	}

	public void setId(String string) {
		wrapped.setId(string);
	}

	public void setModified(boolean b) {
		wrapped.setModified(b);
	}

	public void setName(String name) {
		wrapped.setName(name);
	}

	public void setNode(Hierarchy node) {
		wrapped.setNode(node);
	}

	public void setPropvalue(String propvalue) {
		wrapped.setPropvalue(propvalue);
	}

	public void setVersion(Date object) {
		wrapped.setVersion(object);
	}

	public String getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Date getOldVersion() {
		return oldVersion;
	}

	public void setOldVersion(Date oldVersion) {
		this.oldVersion = oldVersion;
	}
}
