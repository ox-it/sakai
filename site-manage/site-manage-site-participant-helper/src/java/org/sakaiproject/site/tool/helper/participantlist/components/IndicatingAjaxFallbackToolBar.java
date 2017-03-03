package org.sakaiproject.site.tool.helper.participantlist.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.MarkupContainer;

/**
 *
 * @author mweston4
 */
public class IndicatingAjaxFallbackToolBar extends AjaxFallbackHeadersToolbar
{
    private static final long serialVersionUID = 1L;

    public IndicatingAjaxFallbackToolBar(DataTable<?> table, ISortStateLocator stateLocator)
    {
        super(table, stateLocator);
    }

    @Override
    protected WebMarkupContainer newSortableHeader(final String borderId, final String property, final ISortStateLocator locator)
    {
        return new IndicatingAjaxFallbackOrderByBorder(borderId, property, locator)
        {
            @Override
            protected void onAjaxClick(AjaxRequestTarget target)
            {
                MarkupContainer tableAndIndicatorContainer = getTable().getParent().getParent();
                target.addComponent(tableAndIndicatorContainer);
            }
        };
    }
}
