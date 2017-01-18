package uk.ac.ox.it.shoal.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.*;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import uk.ac.ox.it.shoal.SakaiApplication;
import uk.ac.ox.it.shoal.logic.SakaiProxy;

/**
 * This is our base page for our Sakai app. It sets up the containing markup and
 * top navigation. All top level pages should extend from this page so as to
 * keep the same navigation. The content for those pages will be rendered in the
 * main area below the top nav.
 * 
 * <p>
 * It also allows us to setup the API injection and any other common methods,
 * which are then made available in the other pages.
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 * 
 */
public class SakaiPage extends WebPage implements IHeaderContributor {

	private static final Log log = LogFactory.getLog(SakaiPage.class);


	@SpringBean
	private SakaiProxy sakaiProxy;

	// Any links to be added to the page.
	private RepeatingView links;

	public SakaiPage() {

		log.debug("SakaiPage()");

		links = new RepeatingView("link");
		add(links);

		Application application = getApplication();
		if (application instanceof SakaiApplication) {
			SakaiApplication sakaiApplication = (SakaiApplication) application;
			links.setVisible(sakaiApplication.isToolbarEnabled());
		}

	}

	/**
	 * Helper to clear the feedbackpanel display.
	 * 
	 * @param f
	 *            FeedBackPanel
	 */
	public void clearFeedback(FeedbackPanel f) {
		if (!f.hasFeedbackMessage()) {
			f.add(AttributeModifier.remove("class"));
		}
	}

	/**
	 * This block adds the required wrapper markup to style it like a Sakai
	 * tool. Add to this any additional CSS or JS references that you need.
	 * 
	 */
	@Override
	public void renderHead(IHeaderResponse response) {
		Application application = Application.get();
		// get Sakai skin
		String skinRepo = sakaiProxy.getSkinRepoProperty();
		String toolCSS = sakaiProxy.getToolSkinCSS(skinRepo);
		String toolBaseCSS = skinRepo + "/tool_base.css";

		// Sakai additions
		response.render(JavaScriptUrlReferenceHeaderItem.forUrl("/library/js/headscripts.js"));
		response.render(CssUrlReferenceHeaderItem.forUrl(toolBaseCSS));
		response.render(CssUrlReferenceHeaderItem.forUrl(toolCSS));

		response.render(new OnLoadHeaderItem("if (typeof setMainFrameHeight !== 'undefined'){setMainFrameHeight( window.name );}"));

		// Tool additions (at end so we can override if required)
		response.render(MetaDataHeaderItem.forMetaTag("Content-Type", "text/html; charcet=UTF-8"));
		response.render(CssReferenceHeaderItem.forReference(new CssResourceReference(getClass(),"style.css")));
		
		// Need a resource reference so that we don't have to worry about path components.
		response.render(JavaScriptReferenceHeaderItem.forReference(application.getJavaScriptLibrarySettings().getJQueryReference()));
		response.render(JavaScriptReferenceHeaderItem.forReference(new JavaScriptResourceReference(getClass(), "script.js")));
	}

	/**
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink(Link<Void> l) {
		l.add(new AttributeAppender("class", new Model<String>("current"), " "));
		l.setRenderBodyOnly(true);
		l.setEnabled(false);
	}

	protected void addLink(Class<? extends Page> clazz, String title,
			String tooltip) {
		Link<Page> link = new BookmarkablePageLink<Page>("anchor", clazz);
		link.setEnabled(!getClass().equals(clazz));
		addLink(link, title, tooltip);
	}
	
	protected void addLink(final Link<Page> link, String title,
			String tooltip) {
		WebMarkupContainer parent = new WebMarkupContainer(links.newChildId());
		links.add(parent); 
		link.add(new Label("label", new ResourceModel(title))
				.setRenderBodyOnly(true));
		if (tooltip != null) {
			link.add(new AttributeModifier("title", new ResourceModel( tooltip)));
		};
		parent.add(link);
	}
}
