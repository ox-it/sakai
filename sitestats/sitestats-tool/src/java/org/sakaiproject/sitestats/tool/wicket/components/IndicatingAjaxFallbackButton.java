package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

/**
 * @author plukasew
 */
public class IndicatingAjaxFallbackButton extends AjaxFallbackButton implements IAjaxIndicatorAware
{
	private final AjaxIndicatorAppender indicator = new AjaxIndicatorAppender();

	public IndicatingAjaxFallbackButton(String id, Form<?> form)
	{
		super(id, form);
		add(indicator);
	}

	public IndicatingAjaxFallbackButton(String id, IModel<String> model, Form<?> form)
	{
		super(id, model, form);
		add(indicator);
	}

	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return indicator.getMarkupId();
	}
}
