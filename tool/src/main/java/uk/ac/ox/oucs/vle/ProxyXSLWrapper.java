package uk.ac.ox.oucs.vle;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * Class to make it easier to call into Java from XSLT.
 * But this doesn't make testing very easy.
 * @author buckett
 *
 */
public class ProxyXSLWrapper {

	public static ProxyService proxyService = (ProxyService) ComponentManager.getInstance().get(ProxyService.class);
	
	/**
	 * Get the URL.
	 * @param url
	 * @return
	 */
	public static String proxy(String url) {
		return proxyService.getProxyURL(url);
	}
}
