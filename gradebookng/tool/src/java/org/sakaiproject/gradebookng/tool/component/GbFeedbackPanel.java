package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

/**
 * Feedback panel used and reused by GradebookNG so that the messages are styled consistently. Markup ID is automatically output.
 */
public class GbFeedbackPanel extends FeedbackPanel {

	public GbFeedbackPanel(final String id) {
		super(id);

		setOutputMarkupId(true);
	}
	
	@Override
	protected void onConfigure()
	{
		super.onConfigure();
		if (getFeedbackMessages().isEmpty())
		{
			add(AttributeModifier.remove("class"));
		}
	}

	@Override
	protected Component newMessageDisplayComponent(final String id, final FeedbackMessage message) {
		final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

		switch (message.getLevel())
		{
			case FeedbackMessage.ERROR:
			case FeedbackMessage.DEBUG:
			case FeedbackMessage.FATAL:
				add(AttributeModifier.replace("class", "messageError"));
				add(AttributeModifier.append("class", "feedback"));
				break;
			case FeedbackMessage.WARNING:
				add(AttributeModifier.replace("class", "messageWarning"));
				add(AttributeModifier.append("class", "feedback"));
				break;
			case FeedbackMessage.INFO:
				add(AttributeModifier.replace("class", "messageInformation"));
				add(AttributeModifier.append("class", "feedback"));
				break;
			case FeedbackMessage.SUCCESS:
				add(AttributeModifier.replace("class", "messageSuccess"));
				add(AttributeModifier.append("class", "feedback"));
				break;
			default:
				break;
		}

		return newMessageDisplayComponent;
	}

	/**
	 * Clear all messages from the feedback panel
	 */
	public void clear() {
		getFeedbackMessages().clear();
		this.add(AttributeModifier.remove("class"));
	}
}
