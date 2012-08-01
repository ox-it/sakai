package org.sakaiproject.hierarchy.tool.vm;

/**
 * When deleting a site we might want to remove the site as well.
 * This command object is to track if we want to remove the site.
 * @author buckett
 *
 */
public class DeleteSiteCommand {

	/**
	 * Should we also delete the site.
	 */
	private boolean deleteSite;

	public boolean isDeleteSite() {
		return deleteSite;
	}

	public void setDeleteSite(boolean deleteSite) {
		this.deleteSite = deleteSite;
	}
	
}
