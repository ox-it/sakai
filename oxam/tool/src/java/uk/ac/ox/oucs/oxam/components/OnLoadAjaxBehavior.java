package uk.ac.ox.oucs.oxam.components;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;

/**
 * Allow an AJAX event to happen on page load.
 * The problem with AjaxEventBehaviour is that you can only attach to the onload attribute
 * of the body tag, and if your body tag doesn't have a wicket:id then you can't.
 * 
 * @author buckett
 *
 */
public abstract class OnLoadAjaxBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		// Only do on non Ajax calls
		if ( AjaxRequestTarget.get() == null) {
			// Add the AJAX call to the DOM ready
			response.renderOnDomReadyJavascript(getCallbackScript().toString());
		}
	}

}
