package org.sakaiproject.site.tool.helper.participantlist.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.participantlist.providers.ParticipantsProvider;
import org.sakaiproject.site.tool.helper.participantlist.service.ParticipantService;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteParticipantHelper;

/**
 * This class is responsible for the creation and management of the filter drop down
 * @author bjones86
 */
public class Filter extends Panel implements IAjaxIndicatorAware
{
    private static final Logger LOG = Logger.getLogger( Filter.class );
    private final ParticipantService ps = new ParticipantService();
    private final transient CourseManagementService cms = (CourseManagementService) ComponentManager.get( CourseManagementService.class );
    private final transient SiteService ss = (SiteService) ComponentManager.get( SiteService.class );
    private final SakaiDataTable table;

    private final String ALL_VALUE;
    private final String GROUP_TITLE_POSTFIX;
    private final String ROLE_TITLE_POSTFIX;

    private final List<KeyValueEntry> options = new ArrayList<>();
    private KeyValueEntry selectedEntry;

    private final AjaxIndicatorAppender indicator;

    // Comparators
    private static final transient Comparator<Role> SORT_BY_ROLE_TITLE = (Role role1, Role role2) -> role1.getId().compareTo( role2.getId() );
    private static final transient Comparator<Group> SORT_BY_GROUP_TITLE = (Group group1, Group group2) -> group1.getTitle().compareTo( group2.getTitle() );
    private static final transient Comparator<Section> SORT_BY_SECTION_TITLE = (Section section1, Section section2) -> section1.getTitle().compareTo( section2.getTitle() );

    /**
     * Default constructor
     * @param id
     *      the wicket id of the parent container
     * @param existingFilterType
     *      the existing filter type selected (if any)
     * @param existingFilterID
     *      the existing filter ID selected (if any)
     * @param table
     *      the data table this filter will be applied to
     */
    public Filter( String id, String existingFilterType, String existingFilterID, SakaiDataTable table )
    {
        super( id );
        this.table = table;

        // Set up the localized strings
        ALL_VALUE = new ResourceModel( "filter.allParticipants.value" ).getObject();
        GROUP_TITLE_POSTFIX = new ResourceModel( "filter.group.postfix" ).getObject();
        ROLE_TITLE_POSTFIX = new ResourceModel( "filter.role.postfix" ).getObject();

        // Add the 'all' option, which represents all participants (all rosters and unofficial participants)
        KeyValueEntry allParticipantsEntry = new KeyValueEntry( SiteConstants.PARTICIPANT_FILTER_TYPE_ALL, ALL_VALUE );
        options.add( allParticipantsEntry );

        // Add all section, role and group options (in that order)
        addAllSectionOptions();
        addAllRoleOptions();
        addAllGroupOptions();

        // Determine if previous filter selection is present
        if( !existingFilterType.isEmpty() && !existingFilterID.isEmpty() )
        {
            for( KeyValueEntry entry : options )
            {
                String existingFilterSelection = existingFilterType + existingFilterID;
                if( existingFilterSelection.equals( entry.getKey() ) )
                {
                    selectedEntry = entry;
                }
            }
        }
        else
        {
            selectedEntry = allParticipantsEntry;
        }

        // Create the ChoiceRenderer responsible for mapping the key/value of the entry to the value/text of the drop down
        IChoiceRenderer choiceRenderer = new IChoiceRenderer()
        {
            @Override
            public Object getDisplayValue( Object object )
            {
                KeyValueEntry kve = (KeyValueEntry) object;
                return kve.getValue();
            }

            @Override
            public String getIdValue( Object object, int index )
            {
                KeyValueEntry kve = (KeyValueEntry) object;
                return kve.getKey();
            }
        };

        // Add the label to the UI, create the drop down component
        add( new Label( "filterLabel", new ResourceModel( "filterLabel" ).getObject() ) );
        DropDownChoice filter = new DropDownChoice( "filter", new PropertyModel<>( this, "selectedEntry" ), options, choiceRenderer );

        // Add an AJAX onchange handler to the drop down
        filter.add( new AjaxFormComponentUpdatingBehavior( "onchange" )
        {
            @Override
            protected void onUpdate( AjaxRequestTarget target )
            {
                applyFilter( target );
            }
        });

        // Add an AJAX indicating spinner to the drop down
        this.indicator = new AjaxIndicatorAppender();
        filter.add( indicator );

        // Add the drop down to the UI
        add( filter );
    }

    /**
     * Return the ID (in the markup) of the spinner for this component
     * @return 
     *      the ID of the spinner for this component
     */
    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return this.indicator.getMarkupId();
    }

    /**
     * Apply the selected filter to the participants list.
     */
    private void applyFilter( AjaxRequestTarget target )
    {
        // Identify the selected filter type and ID
        String key = selectedEntry.getKey();
        String[] parts = key.split( "]" );
        String type = identifyFilterType( key );
        String id;
        if( SiteConstants.PARTICIPANT_FILTER_TYPE_ALL.equals( type ) )
        {
            id = "";
        }
        else
        {
            id = parts[1];
        }

        // Apply the new filter parameters to the provider
        ParticipantsProvider provider = (ParticipantsProvider) table.getDataProvider();
        provider.setFilterType( type );
        provider.setFilterID( id );

        // Refresh the table only
        table.setCurrentPage( 0 );

        // OWL-1394  --plukasew
        // the provider has changed so the paging size has probably also changed if
        // "all" was selected for it, so we have to update it
        if (table.isShowAll())
        {
            int rowCount = table.getRowCount(); // this returns the new row count due to the modified provider, probably expensive but we need to know it to page correctly
            if (rowCount > 0)
            {
                table.setRowsPerPage(rowCount);
            }
        }

        target.addComponent( table );
        FrameResizer.appendMainFrameResizeJs( target );
    }

    /**
     * Identify which type of filtering has been selected based on the given key
     * @param key
     *          the key to identify the filter type from
     * @return 
     *          the type of filter being applied
     */
    private String identifyFilterType( String key )
    {
        if( key.contains( SiteConstants.PARTICIPANT_FILTER_TYPE_GROUP ) )
        {
            return SiteConstants.PARTICIPANT_FILTER_TYPE_GROUP;
        }
        else if( key.contains( SiteConstants.PARTICIPANT_FILTER_TYPE_SECTION ) )
        {
            return SiteConstants.PARTICIPANT_FILTER_TYPE_SECTION;
        }
        else if( key.contains( SiteConstants.PARTICIPANT_FILTER_TYPE_ROLE ) )
        {
            return SiteConstants.PARTICIPANT_FILTER_TYPE_ROLE;
        }
        else
        {
            return SiteConstants.PARTICIPANT_FILTER_TYPE_ALL;
        }
    }

    /**
     * Get the currently selected filter entry
     * @return 
     *      the currently selected filter entry
     */
    private KeyValueEntry getSelectedEntry()
    {
        return selectedEntry;
    }

    /**
     * Set the currently selected filter entry
     * @param selectedEntry 
     *      the newly selected filter entry
     */
    private void setSelectedEntry( KeyValueEntry selectedEntry)
    {
        this.selectedEntry = selectedEntry;
    }

    /**
     * Add all available roles into the list of filter options
     */
    private void addAllRoleOptions()
    {
        String siteID = ps.getSiteId();
        if( siteID != null && !siteID.isEmpty() )
        {
            try
            {
                Site site = ss.getSite( siteID );
                List<Role> roles = new ArrayList<>( site.getRoles() );

                // Sort the roles before adding to the map
                Collections.sort( roles, SORT_BY_ROLE_TITLE );
                for( Role role : roles )
                {
                    options.add( new KeyValueEntry( SiteConstants.PARTICIPANT_FILTER_TYPE_ROLE + role.getId(), role.getId() + " " + ROLE_TITLE_POSTFIX ) );
                }
            }
            catch( IdUnusedException ex )
            {
                LOG.warn( "Cannot find site " + siteID, ex);
            }
        }
    }

    /**
     * Add all available groups into the list of filter options
     */
    private void addAllGroupOptions()
    {
        String siteID = ps.getSiteId();
        if( siteID != null && !siteID.isEmpty() )
        {
            try
            {
                Site site = ss.getSite( siteID );
                List<Group> groups = (List<Group>) site.getGroups();
                if( groups != null )
                {
                    // Sort the groups before adding to the map
                    Collections.sort( groups, SORT_BY_GROUP_TITLE );
                    for( Group group : groups )
                    {
                        String prop = group.getProperties().getProperty( SiteConstants.GROUP_PROP_WSETUP_CREATED );
                        if( prop != null && Boolean.TRUE.toString().equals( prop ) )
                        {
                            options.add( new KeyValueEntry( SiteConstants.PARTICIPANT_FILTER_TYPE_GROUP + group.getId(), group.getTitle() + " " + GROUP_TITLE_POSTFIX ) );
                        }
                    }
                }
            }
            catch( IdUnusedException ex )
            {
                LOG.warn( "Cannot find site " + siteID, ex);
            }
        }
    }

    /**
     * Add all available sections into the list of filter options
     */
    private void addAllSectionOptions()
    {
        String siteID = ps.getSiteId();
        if( siteID != null && !siteID.isEmpty() )
        {
            List<String> providerCourses = SiteParticipantHelper.getProviderCourseList( siteID );
            if( providerCourses != null )
            {
                List<Section> sections = new ArrayList<>();
                for( String sectionID : providerCourses )
                {
                    try
                    {
                        Section s = cms.getSection( sectionID );
                        if( s != null )
                        {
                            sections.add( s );
                        }
                    }
                    catch( IdNotFoundException ex )
                    {
                        LOG.warn( "Cannot find section " + sectionID, ex);
                    }
                }

                // Sort the sections by readable title before adding to the map
                Collections.sort( sections, SORT_BY_SECTION_TITLE );
                for( Section s : sections )
                {
                    options.add( new KeyValueEntry( SiteConstants.PARTICIPANT_FILTER_TYPE_SECTION + s.getEid(), s.getTitle() ) );
                }
            }
        }
    }
}

/**
 * Utility class to represent they key/value of a filter (drop down) option
 * @author bjones86
 */
class KeyValueEntry implements Serializable
{
    private String key;
    private String value;

    /**
     * Default constructor
     * @param key
     *      the key of this filter option
     * @param value 
     *      the value of this filter option
     */
    public KeyValueEntry( String key, String value )
    {
        this.key = key;
        this.value = value;
    }

    /**
     * Get the key for this filter option
     * @return 
     *      the key for this filter option
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * Get the value for this filter option
     * @return 
     *      the value for this filter option
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Set the key for this filter option
     * @param key 
     *      the key to set for this filter option
     */
    public void setKey( String key )
    {
        this.key = key;
    }

    /**
     * Set the value for this filter option
     * @param value
     *      the value to set for this filter option
     */
    public void setValue( String value )
    {
        this.value = value;
    }
}
