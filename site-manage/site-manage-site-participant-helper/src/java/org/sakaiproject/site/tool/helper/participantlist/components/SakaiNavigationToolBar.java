package org.sakaiproject.site.tool.helper.participantlist.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigatorLabel;

/**
 * @author Nuno Fernandes (updated by Melissa Beldman - mweston4@uwo.ca)
 */
public class SakaiNavigationToolBar extends AjaxNavigationToolbar
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     * 
     * @param table data table this toolbar will be attached to
     * @param filterType
     * @param filterID
     * @param rowsPerPage
     */
    public SakaiNavigationToolBar( final SakaiDataTable table, String filterType, String filterID, int rowsPerPage )
    {
        super(table);

        // Table cell
        WebMarkupContainer span = (WebMarkupContainer) get("span");
        span.add(new AttributeModifier("colspan", new Model(String.valueOf(table.getColumns().size()))));

        // bjones86 - OWL-686 - filter
        span.add( new Filter( "filter", filterType, filterID, table ) );

        // Navigator
        span.get("navigator").replaceWith(new SakaiPagingNavigator("navigator", table).add(new AttributeAppender("class", new Model<>("tableNavbar"), " ")));
        span.get("navigatorLabel").replaceWith(new NavigatorLabel("navigatorLabel", table));
    }

    /**
     * Hides this toolbar when there is only one page in the table
     * 
     * @return 
     * @see org.apache.wicket.Component#isVisible()
     */
    @Override
    public boolean isVisible()
    {
        return true;
    }
}
