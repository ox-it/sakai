package org.sakaiproject.site.api;

/**
 * Interface that should be implemented by a class wishing to handle the aliasing of sites.
 * @author buckett
 *
 */
public interface SiteAliasProvider {
	
	/**
	 * Attempt to find an alias for the site.
	 * @param siteId The site to find an alias for.
	 * @return <code>null</code> if no alias was found, otherwise the alias.
	 */
	public String lookupAlias(String siteId);

}
