package uk.ac.ox.oucs.vle;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.BasicAuth;

/**
 * Simple filter which requires the user to be authenticated before allowing the request through.
 * This is so that if people use the webapp URL they can't search for people and perform other actions.
 * @author buckett
 *
 */
public class BasicAuthenticatedRequestFilter implements Filter {
	
	protected BasicAuth basicAuth = null;

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		if (response instanceof HttpServletResponse) {
			basicAuth.doLogin((HttpServletRequest) request);
			if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
				HttpServletResponse httpResponse = (HttpServletResponse)response;
				
				String uiService = ServerConfigurationService.getString("ui.service");
				httpResponse.addHeader("WWW-Authenticate", "Basic realm=\"" + uiService + "\"");
				
				httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED); // Don't want to prompt for HTTP authentication.
				return;
			} else {
				chain.doFilter(request, response);
			}
		}
	}

	public void init(FilterConfig filterConfig) throws ServletException {
		basicAuth = new BasicAuth();
		basicAuth.init();
	}

	public void destroy() {
	}

}
