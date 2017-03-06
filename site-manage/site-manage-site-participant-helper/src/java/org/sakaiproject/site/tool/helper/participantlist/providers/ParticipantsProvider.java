package org.sakaiproject.site.tool.helper.participantlist.providers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.site.tool.helper.participantlist.model.Participant;
import org.sakaiproject.site.tool.helper.participantlist.service.ParticipantService;

/**
 * 
 * @author mweston4, bjones86, plukasew
 */
public class ParticipantsProvider extends SortableDataProvider<Participant, String>
{
    // bjones86 - OWL-686
    private String filterType;
    private String filterID;

    // OWL-1367  --plukasew
    // used by size() to store the results of getParticipants() so that
    // the subsequent call to iterator() does not re-fetch the data
    private transient List<Participant> pListCache;

    public ParticipantsProvider( String filterType, String filterID )
    {
        setSort( "name", SortOrder.ASCENDING );

        this.filterType = filterType;
        this.filterID = filterID;

        pListCache = null;
    }

    public String getFilterType()
    {
        return this.filterType;
    }

    public String getFilterID()
    {
        return this.filterID;
    }

    public void setFilterType( String filterType )
    {
        this.filterType = filterType;
    }

    public void setFilterID( String filterID )
    {
        this.filterID = filterID;
    }

    protected List<Participant> getParticipants()
    {
        ParticipantService ps = new ParticipantService();
        return ps.getParticipants( filterType, filterID );
    }

    @Override
    public Iterator<? extends Participant> iterator( long first, long count )
    {
        // OWL-1367  --plukasew
        List<Participant> participants = pListCache;
        if( participants == null )
        {
            participants = getParticipants();
        }

        Collections.sort( participants, new Comparator<Participant>()
        {
            @Override
            public int compare( Participant o1, Participant o2 )
            {
                int dir = getSort().isAscending() ? 1 : -1;
                int result = 0;

                if( "courseSite".equals( getSort().getProperty() ) )
                {
                    result = o1.getCourseSite().compareToIgnoreCase( o2.getCourseSite() );
                }
                else if( "id".equals( getSort().getProperty() ) )
                {
                    result = o1.getId().compareToIgnoreCase( o2.getId() );
                }
                else if( "credits".equals( getSort().getProperty() ) )
                {
                    result = o1.getCredits().compareToIgnoreCase( o2.getCredits() );
                }
                else if( "role".equals( getSort().getProperty() ) )
                {
                    result = o1.getRole().compareToIgnoreCase( o2.getRole() );
                }
                else if( "status".equals( getSort().getProperty() ) )
                {
                    result = o1.getStatus().compareToIgnoreCase( o2.getStatus() );
                }

                // Default use name
                if( result == 0 )
                {
                    result = o1.getName().compareToIgnoreCase( o2.getName() );
                }

                return dir * result;
            }
        });

        return participants.subList( (int) first, (int) Math.min( first + count, participants.size() ) ).iterator();
    }

    @Override
    public long size()
    {
        // OWL-1367  --plukasew
        pListCache = getParticipants();
        return pListCache.size();
    }

    @Override
    public IModel<Participant> model( Participant object )
    {
        return new Model( object );
    }
}
