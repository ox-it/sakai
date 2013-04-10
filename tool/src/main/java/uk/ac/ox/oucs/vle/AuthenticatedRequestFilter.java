package uk.ac.ox.oucs.vle;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * Simple filter which requires the user to be authenticated before allowing the request through.
 * This is so that if people use the webapp URL they can't search for people and perform other actions.
 * @author buckett
 *
 */
public class AuthenticatedRequestFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (response instanceof HttpServletResponse) {
			if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
				HttpServletResponse httpResponse = (HttpServletResponse)response;
				httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN); // Don't want to prompt for HTTP authentication.
			} else {
				chain.doFilter(request, response);
			}
		}

	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void destroy() {
	}

}
