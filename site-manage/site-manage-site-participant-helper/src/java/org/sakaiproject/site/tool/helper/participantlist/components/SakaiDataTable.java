package org.sakaiproject.site.tool.helper.participantlist.components;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.site.tool.helper.participantlist.model.Participant;
import org.sakaiproject.site.tool.helper.participantlist.providers.ParticipantsProvider;

public class SakaiDataTable extends AjaxFallbackDefaultDataTable
{
    private static final long serialVersionUID = 1L;
    private boolean showAll;

    /**
     * Constructor
     * 
     * @param id            component id
     * @param columns       array of columns
     * @param dataProvider  data provider
     * @param rowsPerPage   number of rows per page
     */
    public SakaiDataTable( String id, final List<IColumn<Participant, String>> columns, ISortableDataProvider dataProvider, int rowsPerPage )
    {
        super(id, columns, dataProvider, rowsPerPage);

        ((RepeatingView) get("topToolbars:toolbars")).removeAll();
        ((RepeatingView) get("bottomToolbars:toolbars")).removeAll();

        ParticipantsProvider provider = (ParticipantsProvider) dataProvider;

        addTopToolbar( new SakaiNavigationToolBar( this, provider.getFilterType(), provider.getFilterID(), rowsPerPage ) );
        addTopToolbar( new IndicatingAjaxFallbackToolBar( this, dataProvider ) );
        addBottomToolbar( new NoRecordsToolbar( this, new ResourceModel( "table.nodata" ) ) );
        //addBottomToolbar( new IndicatingAjaxFallbackToolBar( this, dataProvider ) );
        //addBottomToolbar( new SakaiNavigationToolBar( this, provider.getFilterType(), provider.getFilterID(), rowsPerPage ) );

        showAll = false;
    }

    public boolean isShowAll()
    {
        return showAll;
    }

    public void setShowAll( boolean value )
    {
        showAll = value;
    }
}
