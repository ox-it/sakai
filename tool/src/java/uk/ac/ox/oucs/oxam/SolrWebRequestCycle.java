package uk.ac.ox.oucs.oxam;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import uk.ac.ox.oucs.oxam.pages.SolrDownPage;

import java.io.IOException;

/**
 * This should be used in applications that want sensible error handling of Solr
 * errors. When a SolrServerException is hit a sensible error page is displayed.
 *
* @author Matthew Buckett
*/
public class SolrWebRequestCycle extends WebRequestCycle {

	public SolrWebRequestCycle(WebApplication application, Request request, Response response) {
		super(application, (WebRequest) request, (WebResponse) response);
	}

	@Override
	public Page onRuntimeException(Page page, RuntimeException e) {
		// This has already been logged by org.apache.wicket.RequestCycle
		SolrServerException cause = (SolrServerException) findCause(e, SolrServerException.class);
		if (cause instanceof SolrServerException) {
			// If we have problems talking to Solr then show a custom page.
			SolrServerException solrCause = (SolrServerException)cause;
			if (solrCause.getRootCause() instanceof IOException) {
				return new SolrDownPage(solrCause);
			}
		}
		throw e;
	}

	private Throwable findCause(Throwable t, Class clazz) {
		Throwable cause = t;
		while(cause != null && ! cause.getClass().isAssignableFrom(clazz)) {
			cause = cause.getCause();
		}
		return cause;
	}
}
