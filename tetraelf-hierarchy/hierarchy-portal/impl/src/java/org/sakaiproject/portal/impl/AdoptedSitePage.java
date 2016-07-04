package org.sakaiproject.portal.impl;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.hierarchy.api.model.PortalNodeSite;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The adopted site page allows a page from another site to be displayed in
 * the current site.
 * <p>
 * This is used in the hierarchy to display the management tools form the hierarchy
 * site in the current site.
 * @author buckett
 *
 */
public class AdoptedSitePage implements SitePage {
	
	private static final long serialVersionUID = 1L;
	
	private SitePage original;
	Site newParent;

	public AdoptedSitePage(Site newParent, SitePage original) {
		this.newParent = newParent;
		this.original = original;
	}

	public ToolConfiguration addTool() {
		throw new UnsupportedOperationException();
	}

	public ToolConfiguration addTool(Tool reg) {
		throw new UnsupportedOperationException();
	}

	public ToolConfiguration addTool(String toolId) {
		throw new UnsupportedOperationException();
	}

	public Site getContainingSite() {
		// Don't return the new site or people can subvert portal based security.
		return original.getContainingSite();
	}

	public int getLayout() {
		return original.getLayout();
	}

	public String getLayoutTitle() {
		return original.getLayoutTitle();
	}

	public int getPosition() {
		return original.getPosition();
	}

	public String getSiteId() {
		return original.getId();
	}

	public String getSkin() {
		return newParent.getSkin();
	}

	public String getTitle() {
		return original.getTitle();
	}
	
	public boolean getTitleCustom() {
		return original.getTitleCustom();
	}

	@Override
	public boolean isHomePage() {
		return original.isHomePage();
	}

	@Override
	public boolean getHomeToolsTitleCustom(String toolId) {
		return original.getHomeToolsTitleCustom(toolId);
	}

	@Override
	public void setHomeToolsTitleCustom(String toolId) {
		original.setHomeToolsTitleCustom(toolId);
	}

	public ToolConfiguration getTool(String id) {
		ToolConfiguration toolConfig = original.getTool(id);
		return (toolConfig == null)?null:new AdoptedToolConfiguration(this, toolConfig);
	}

	public List getTools() {
		final List tools = original.getTools();
		return new AbstractList() {

			@Override
			public Object get(int index) {
				ToolConfiguration toolConfig = (ToolConfiguration) tools.get(index);
				return (toolConfig == null)?null:new AdoptedToolConfiguration(AdoptedSitePage.this, toolConfig);
			}

			@Override
			public int size() {
				return tools.size();
			}
			
		};
	}

	public List getTools(int col) {
		final List tools = original.getTools(col);
		return new AbstractList() {

			@Override
			public Object get(int index) {
				ToolConfiguration toolConfig = (ToolConfiguration) tools.get(index);
				return (toolConfig == null)?null:new AdoptedToolConfiguration(AdoptedSitePage.this, toolConfig);
			}

			@Override
			public int size() {
				return tools.size();
			}
			
		};
	}

	public Collection getTools(String[] toolIds) {
		final Collection tools = original.getTools(toolIds);
		return new AbstractCollection() {

			@Override
			public Iterator iterator() {
				final Iterator it = tools.iterator();
				return new Iterator() {

					public boolean hasNext() {
						return it.hasNext();
					}

					public Object next() {
						ToolConfiguration toolConfig = (ToolConfiguration) it.next();
						return (toolConfig == null)?null:new AdoptedToolConfiguration(AdoptedSitePage.this, toolConfig);
					}

					public void remove() {
						throw new UnsupportedOperationException(); 
					}
					
				};
			}

			@Override
			public int size() {
				return tools.size();
			}
			
		};
	}

	public boolean isPopUp() {
		return original.isPopUp();
	}

	public void moveDown() {
		throw new UnsupportedOperationException();
	}

	public void moveUp() {
		throw new UnsupportedOperationException();

	}

	public void removeTool(ToolConfiguration tool) {
		throw new UnsupportedOperationException();
	}

	public void setLayout(int layout) {
		throw new UnsupportedOperationException();
	}

	public void setPopup(boolean popup) {
		throw new UnsupportedOperationException();
	}

	public void setPosition(int pos) {
		throw new UnsupportedOperationException();
	}

	public void setTitle(String title) {
		throw new UnsupportedOperationException();
	}
	
	public void setTitleCustom(boolean custom) {
		throw new UnsupportedOperationException();
	}

	public ResourcePropertiesEdit getPropertiesEdit() {
		return original.getPropertiesEdit();
	}

	public boolean isActiveEdit() {
		return false;
	}

	public String getId() {
		return original.getId();
	}

	public ResourceProperties getProperties() {
		return original.getProperties();
	}

	public String getReference() {
		return original.getReference();
	}

	public String getReference(String rootProperty) {
		return original.getReference(rootProperty);
	}

	public String getUrl() {
		return original.getUrl();
	}

	public String getUrl(String rootProperty) {
		return original.getUrl();
	}

	public Element toXml(Document doc, Stack stack) {
		return original.toXml(doc, stack);
	}

	public void setupPageCategory(String toolId) {
		original.setupPageCategory(toolId);
	}

	public void localizePage() {
		original.localizePage();
		
	}

}
