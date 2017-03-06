package org.sakaiproject.site.tool.helper.participantlist.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationIncrementLink;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;

public class SakaiPagingNavigator extends IndicatingAjaxPagingNavigator
{
    /**
     * Constructor.
     * 
     * @param id        Component's id
     * @param pageable  The pageable component the page links are referring to.
     */
    public SakaiPagingNavigator(final String id, final IPageable pageable)
    {
        super(id, pageable);
    }

    @Override
    protected void onBeforeRender()
    {
        if (get("rowNumberSelector") == null)
        {
            setDefaultModel(new CompoundPropertyModel(this));

            // Get the row number selector
            add(newRowNumberSelector(getPageable()));

            // Add additional page links
            replace(new SakaiResizingAjaxPagingNavigationLink("first", getPageable(), 0));
            replace(new SakaiResizingAjaxPagingNavigationIncrementLink("prev", getPageable(), -1));
            replace(new SakaiResizingAjaxPagingNavigationIncrementLink("next", getPageable(), 1));
            replace(new SakaiResizingAjaxPagingNavigationLink("last", getPageable(), -1));
        }

        super.onBeforeRender();
    }

    protected DropDownChoice newRowNumberSelector(final IPageable pageable)
    {
        List<String> numPerPageOptions = new ArrayList<>();

        String numOptionsStr= new ResourceModel("numParticipants.options.size").getObject();
        Integer numOptions = Integer.parseInt(numOptionsStr);

        //get a list of the dropdown selections' values
        for(int i = 1; i <= numOptions; ++i)
        {
            String optionPP = new ResourceModel("numParticipants.option." + i).getObject();
            numPerPageOptions.add(optionPP);
        }

        DropDownChoice rowNumberSelector = new DropDownChoice("rowNumberSelector", numPerPageOptions);
        rowNumberSelector.add(new AjaxFormComponentUpdatingBehavior("change")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                DataTable t = (DataTable) getPageable();
                t.setCurrentPage(0);
                target.add(t);
                FrameResizer.appendMainFrameResizeJs(target);
            }
        });

        return rowNumberSelector;
    }

    // This method is called by Wicket when it needs to know which
    // option in the paging dropdown is selected
    public String getRowNumberSelector()
    {
        // OWL-693  --plukasew
        SakaiDataTable table = (SakaiDataTable) getPageable();

        if (table.isShowAll())
        {
            return "all"; // special case for selection to show all participants
        }

        long pageSize = table.getItemsPerPage();
        return String.valueOf(pageSize);
    }

    // This method is called by Wicket when the selection
    // in the paging dropdown is changed
    public void setRowNumberSelector(String value)
    {
        // OWL-693  --plukasew
        SakaiDataTable table = (SakaiDataTable) getPageable();
        long pageSize;

        try
        {
            pageSize = Integer.parseInt(value);
            table.setShowAll(false);
        }
        catch (NumberFormatException nfe)
        {
            // "all" must have been selected
            table.setShowAll(true);
            pageSize = table.getItemCount();
        }

        // OWL-1394  --plukasew
        // rowsPerPage can't be < 1, so don't change it in this scenario
        // instead we will rely on table.isShowAll()
        if (pageSize > 0)
        {
            table.setItemsPerPage(pageSize);
        }
    }

    /**
     * Create a new PagingNavigation. May be subclassed to make use of specialized PagingNavigation.
     *
     * @param id
     * @param pageable          the pageable component
     * @param labelProvider     The label provider for the link text.
     * 
     * @return the navigation object
     */
    @Override
    protected PagingNavigation newNavigation(final String id, final IPageable pageable, final IPagingLabelProvider labelProvider)
    {
        return new PagingNavigation("navigation", pageable, labelProvider)
        {
            @Override
            public boolean isVisible()
            {
                // Hide the numbered navigation bar, eg. 1 | 2 | 3, etc
                return false;
            }
        };
    }

    /* ----------------- NESTED CLASSES --------------------------- */
    // maybe these classes could be replaced by one behaviour?

    private class SakaiResizingAjaxPagingNavigationLink extends AjaxPagingNavigationLink
    {
        public SakaiResizingAjaxPagingNavigationLink(String id, IPageable pageable, int pageNumber)
        {
            super(id, pageable, pageNumber);
        }

        @Override
        public void onClick(AjaxRequestTarget target)
        {
            super.onClick(target);
            FrameResizer.appendMainFrameResizeJs(target);
        }
    }

    private class SakaiResizingAjaxPagingNavigationIncrementLink extends AjaxPagingNavigationIncrementLink
    {
        public SakaiResizingAjaxPagingNavigationIncrementLink(String id, IPageable pageable, int increment)
        {
            super(id, pageable, increment);
        }

        @Override
        public void onClick(AjaxRequestTarget target)
        {
            super.onClick(target);
            FrameResizer.appendMainFrameResizeJs(target);
        }
    }
}
