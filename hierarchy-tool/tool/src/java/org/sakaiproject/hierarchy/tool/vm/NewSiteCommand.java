/**
 * 
 */
package org.sakaiproject.hierarchy.tool.vm;

public class NewSiteCommand {
	private String title;
	private String name;
	private String siteId;
	private Method method;
	
	public enum Method {AUTOMATIC, CUSTOM};
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public String getSiteId() {
		return siteId;
	}
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
}