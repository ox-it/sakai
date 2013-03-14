package org.sakaiproject.hierarchy.tool.vm;

import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

/**
 * Very similar to UrlBasedViewResolver but if it doesn't seem to be a redirect
 * or forward view let another view resolver have a go.
 * 
 * @author buckett
 * 
 */
public class RedirectViewResolver extends UrlBasedViewResolver implements Ordered {

	private int order;

	/**
	 * Don't need to specify class as we only ever deal with redirect and
	 * forward prefixes.
	 */
	protected Class getViewClass() {
		return AbstractView.class;
	}

	/**
	 * Overridden to implement check for "redirect:" prefix.
	 * <p>
	 * Not possible in loadView, as overridden loadView versions in subclasses
	 * might rely on the superclass always creating instances of the required
	 * view class.
	 * 
	 * @see #loadView
	 * @see #requiredViewClass
	 */
	protected View createView(String viewName, Locale locale) throws Exception {
		// Check for special "redirect:" prefix.
		if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
			String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length());
			return new RedirectView(redirectUrl, isRedirectContextRelative(), isRedirectHttp10Compatible());
		}
		// Check for special "forward:" prefix.
		if (viewName.startsWith(FORWARD_URL_PREFIX)) {
			String forwardUrl = viewName.substring(FORWARD_URL_PREFIX.length());
			return new InternalResourceView(forwardUrl);
		}
		return null;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
