package org.sakaiproject.site.tool.helper.participantlist.components;

import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.sort.AjaxFallbackOrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;

/**
 *
 * @author mweston4
 */
abstract public class IndicatingAjaxFallbackOrderByBorder  extends AjaxFallbackOrderByBorder implements IAjaxIndicatorAware
{
    private static final long serialVersionUID = 1L;
    private final AjaxIndicatorAppender indicator;

    public IndicatingAjaxFallbackOrderByBorder(String id, String property, ISortStateLocator stateLocator)
    {
        super(id, property, stateLocator);
        this.indicator = new AjaxIndicatorAppender();
        add(indicator);
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return indicator.getMarkupId();
    }
}
