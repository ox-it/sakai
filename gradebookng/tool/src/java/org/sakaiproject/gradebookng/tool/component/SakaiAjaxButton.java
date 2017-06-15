package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;

/**
 * Disables the button on click, sets the standard Sakai spinner on it, and removes it/re-enables the button after the Ajax call completes.
 * 
 * @author plukasew
 */
public class SakaiAjaxButton extends AjaxButton
{
	private static final String DISABLED = "$('#%s').prop('disabled', true);";
	private static final String ENABLED = "$('#%s').prop('disabled', false);";
	private static final String SPIN = "$('#%s').addClass('spinButton');";
	private static final String STOP = "$('#%s').removeClass('spinButton');";
	private static final String DISABLE_AND_SPIN = DISABLED + SPIN;
	private static final String ENABLE_AND_STOP = ENABLED + STOP;
	
	protected boolean willRenderOnClick = false;
	
	public SakaiAjaxButton(String id) {
		super(id);
	}

	public SakaiAjaxButton(String id, Form<?> form) {
		super(id, form);
	}
	
	public SakaiAjaxButton setWillRenderOnClick(boolean value)
	{
		willRenderOnClick = value;
		return this;
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		AjaxCallListener listener = new AjaxCallListener();
		String id = getMarkupId();
		
		// on the client side, disable the button and show the spinner after click
		String before = String.format(DISABLE_AND_SPIN, id, id);
		listener.onBefore(before);

		// if the button is re-rendered the disabled property will be set by wicket and the spinner
		// class will not be on the button as wicket doesn't know about it
		if (!willRenderOnClick)
		{
			String complete = String.format(ENABLE_AND_STOP, id, id);
			listener.onComplete(complete);
		}
		
		attributes.getAjaxCallListeners().add(listener);
	}
}
