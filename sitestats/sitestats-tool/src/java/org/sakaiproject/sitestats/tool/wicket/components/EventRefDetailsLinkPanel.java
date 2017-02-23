package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.DetailedEvent;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.pages.BasePage;

/**
 * @author plukasew, bjones86
 */
public class EventRefDetailsLinkPanel extends Panel
{
	public final boolean resolvable;

	private final String siteID;

	public EventRefDetailsLinkPanel(String id, IModel<DetailedEvent> model, String siteID)
	{
		super(id, model);
		this.siteID = siteID;
		DetailedEventsManager dem = Locator.getFacade().getDetailedEventsManager();
		resolvable = dem.isResolvable(model.getObject().getEventId());
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		add(new WebMarkupContainer("details").setOutputMarkupPlaceholderTag(true));

		AjaxFallbackLink<DetailedEvent> moreLink = new IndicatingAjaxFallbackLink<DetailedEvent>("moreLink", (IModel<DetailedEvent>) getDefaultModel())
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				DetailedEvent de = (DetailedEvent) EventRefDetailsLinkPanel.this.getDefaultModelObject();
				EventRefDetailsPanel panel = new EventRefDetailsPanel("details", de.getEventId(), de.getEventRef(), de.getEventDate(), siteID);
				panel.setOutputMarkupId(true);
				getParent().replace(panel);
				target.add(panel);
				setVisible(false);
				target.add(this);
				target.appendJavaScript(BasePage.NO_SCROLLBAR);
			}
		};

		// localizes details link text
		moreLink.add(new Label("moreDetailsLinkText", new ResourceModel("de_resultsTable_detailsLink")).setRenderBodyOnly(true));
		moreLink.add(new AttributeAppender("class", Model.of("showEventDetails")));

		add(moreLink);
	}

	@Override
	public boolean isVisible()
	{
		return resolvable;
	}
}
