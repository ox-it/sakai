package org.sakaiproject.portal.charon.handlers;

import java.util.Properties;

import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;


/**
 * Whole point of this class is to pass through the site as well as the placement to the tool so we can work out the skin.
 * @see AdoptedSitePage
 *
 */
public class AdoptedToolConfiguration implements ToolConfiguration {

	private AdoptedSitePage sitePage;
	private ToolConfiguration original;
	
	public AdoptedToolConfiguration (AdoptedSitePage sitePage, ToolConfiguration original) {
		this.sitePage = sitePage;
		this.original = original;
	}
	
	public SitePage getContainingPage() {
		return sitePage;
	}

	public String getLayoutHints() {
		return original.getLayoutHints();
	}

	public String getPageId() {
		return original.getLayoutHints();
	}

	public int getPageOrder() {
		return original.getPageOrder();
	}

	public String getSiteId() {
		return sitePage.node.getSite().getId();
	}

	public String getSkin() {
		return sitePage.getSkin();
	}

	public void moveDown() {
		throw new UnsupportedOperationException();
	}

	public void moveUp() {
		throw new UnsupportedOperationException();
	}

	public int[] parseLayoutHints() {
		return original.parseLayoutHints();
	}

	public void setLayoutHints(String hints) {
		throw new UnsupportedOperationException();
	}

	public Properties getConfig() {
		return original.getConfig();
	}

	public String getContext() {
		return original.getContext();
	}

	public String getId() {
		return original.getId()+ ":"+ sitePage.node.getId();
	}

	public Properties getPlacementConfig() {
		return original.getPlacementConfig();
	}

	public String getTitle() {
		return original.getTitle();
	}

	public Tool getTool() {
		return original.getTool();
	}

	public String getToolId() {
		return original.getToolId();
	}

	public void save() {
		throw new UnsupportedOperationException();
	}

	public void setTitle(String title) {
		throw new UnsupportedOperationException();
	}

	public void setTool(String toolId, Tool tool) {
		throw new UnsupportedOperationException();
	}

}
