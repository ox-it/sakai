package org.sakaiproject.sitestats.tool.wicket.components;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef.ResolutionType;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableEventRefDetailsModel;

/**
 * @author plukasew, bjones86
 */
public class EventRefDetailsPanel extends Panel
{
	public EventRefDetailsPanel(String id, String eventType, String eventRef, Date eventDate, String siteID)
	{
		super(id, new LoadableEventRefDetailsModel(eventType, eventRef, eventDate, siteID));
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		List<ResolvedRef> detailsList = (List<ResolvedRef>) getDefaultModelObject();
		if (detailsList.isEmpty())
		{
			//OWLTODO: localize
			detailsList = Collections.singletonList(ResolvedRef.newText("Error", "Unable to find details for this event."));
		}
		add(new ListView<ResolvedRef>("detailsList", detailsList)
		{
			@Override
			protected void populateItem(ListItem<ResolvedRef> item)
			{
				ResolvedRef ref = item.getModelObject();
				// OWLTODO: localize

				item.add(new Label("key", Model.of(ref.getKey())).setRenderBodyOnly(true));
				Fragment frag = buildDetailsFragment(ref);
				item.add(frag);
			}
		});
	}

	private Fragment buildDetailsFragment(ResolvedRef rr)
	{
		Fragment frag = null;
		if (ResolutionType.TEXT.equals(rr.getType()))
		{
			frag = new Fragment("details", "text", this);
			frag.add(new Label("displayValue", rr.getDisplayValue()).setRenderBodyOnly(true));
		}
		else if (ResolutionType.LINK.equals(rr.getType()))
		{
			frag = new Fragment("details", "link", this);
			ExternalLink displayLink = new ExternalLink("displayLink", rr.getUrl(), rr.getDisplayValue());
			displayLink.add(new AttributeModifier("target", "_blank"));
			displayLink.add(new AttributeModifier("rel", "noreferrer"));
			frag.add(displayLink);
		}

		return frag;
	}
}
