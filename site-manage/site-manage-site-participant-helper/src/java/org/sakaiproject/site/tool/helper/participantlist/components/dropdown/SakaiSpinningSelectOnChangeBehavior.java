package org.sakaiproject.site.tool.helper.participantlist.components.dropdown;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;

/**
 *
 * @author plukasew
 */
public abstract class SakaiSpinningSelectOnChangeBehavior extends AjaxFormComponentUpdatingBehavior
{	
	public SakaiSpinningSelectOnChangeBehavior()
	{
		super("onchange");
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);
		
		AjaxCallListener listener = new SakaiSpinningSelectAjaxCallListener(getComponent().getMarkupId(), false);
		attributes.getAjaxCallListeners().add(listener);
	}
}
