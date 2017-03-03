package org.sakaiproject.site.tool.helper.participantlist.components;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.navigation.paging.IPageable;

/**
 *
 * @author mweston4
 */
public class IndicatingAjaxPagingNavigator extends AjaxPagingNavigator implements IAjaxIndicatorAware
{
    private static final long serialVersionUID = 1L;
    private final AjaxIndicatorAppender indicator;

    public IndicatingAjaxPagingNavigator(String id, IPageable pageable)
    {
        super(id, pageable);
        indicator = new AjaxIndicatorAppender();
        add(indicator);
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return indicator.getMarkupId();
    }
}
