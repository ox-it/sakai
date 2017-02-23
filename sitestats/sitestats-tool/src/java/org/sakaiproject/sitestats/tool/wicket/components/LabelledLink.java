package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 * @author plukasew
 * @param <T>
 */
public abstract class LabelledLink<T> extends Panel
{
	public LabelledLink(final String id, final IModel<?> labelModel, final IModel<T> model)
	{
		super(id);
		Link<T> link = new Link<T>("link", model)
		{
			@Override
			public void onClick()
			{
				LabelledLink.this.onClick();
			}
		};

		link.add(new Label("label", labelModel));
		add(link);
	}

	protected abstract void onClick();
}
