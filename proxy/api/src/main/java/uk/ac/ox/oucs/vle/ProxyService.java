package uk.ac.ox.oucs.vle;

/**
 * This is a very simple service for creating URLs which will proxy
 * content from other sites through the current domain.
 * @author buckett
 *
 */
public interface ProxyService {

	/**
	 * Gets the proxy URL for the supplied URL.
	 * The returned URL doesn't include the protocol or hostname so it can be put be
	 * embedded in a page be it over http or https.
	 * If you want a full URL use {@link org.sakaiproject.component.api.ServerConfigurationService#getServerUrl()}
	 * with and append the result of this method.
	 * This method should <bold>never</bold> be directly exposed over the web.
	 * @param url The URL of some content on another site, it should already be URL encoded if it needs it,
	 * for example spaces should already be encoded as %20. 
	 * @return A URL on the current host which will get the content from the remote site and return it.
	 */
	public String getProxyURL(String url);
	
	/**
	 * Get the valid signature for the supplied URL.
	 * This also allows you to check if a signature is valid.
	 * @param url The URL.
	 * @return The signature for the URL.
	 */
	public String getSignature(String url);
}
