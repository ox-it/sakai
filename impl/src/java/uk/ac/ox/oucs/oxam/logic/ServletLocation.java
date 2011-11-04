package uk.ac.ox.oucs.oxam.logic;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

public class ServletLocation implements ServletContextAware, Location {

	private ServletContext servletContext;
	
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.oxam.logic.Location#getPrefix()
	 */
	public String getPrefix() {
		// Only in servlet 2.5, have to hardcode in servlet 2.4
		return servletContext.getContextPath();
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.ox.oucs.oxam.logic.Location#getPath(java.lang.String)
	 */
	public String getPath(String path) {
		return servletContext.getRealPath(path);
	}

}
