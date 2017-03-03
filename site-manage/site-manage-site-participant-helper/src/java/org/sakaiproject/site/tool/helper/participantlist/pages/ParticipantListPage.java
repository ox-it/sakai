package org.sakaiproject.site.tool.helper.participantlist.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.RequestCycle;

import org.sakaiproject.site.tool.helper.participantlist.components.ParticipantListPanel;
import org.sakaiproject.site.tool.helper.participantlist.components.RoleDescriptionPanel;
import org.sakaiproject.site.tool.helper.participantlist.service.ParticipantService;

/**
 * The Manage Participants tool, displaying and give ability to update the list of site participants
 * 
 * @author Melissa Beldman (mweston4@uwo.ca), bjones86
 *
 */
public class ParticipantListPage extends BasePage implements IHeaderContributor
{
    private static final int DEFAULT_ROWS_PER_PAGE = 200;
    private final ParticipantService participantService;

    /**
     * Constructor
     * @param parameters
     */
    public ParticipantListPage(final PageParameters parameters)
    {
        participantService = new ParticipantService();

        // security check  --plukasew
        if (!participantService.isCurrentUserAllowedToSeeSiteMembership())
        {
            throw new RedirectToUrlException(participantService.getResetToolUrl());
        }

        int rowsPerPage = DEFAULT_ROWS_PER_PAGE;
        if (parameters.containsKey("rowsPerPage"))
        {
            rowsPerPage = parameters.getAsInteger("rowsPerPage");
        }

        // Heading
        add(new Label("heading", new ResourceModel("heading")));

        // Print Participants List link
        ExternalLink printParticipantsLink = new ExternalLink("printParticipantsLink",
                                      RequestCycle.get().getRequest().getRelativePathPrefixToWicketHandler() + "/sakai-site-manage-tool/tool/printparticipant/"
                                      + participantService.getSiteId());
        printParticipantsLink.add(new Label("printParticipantLinkLbl", new ResourceModel("printParticipantLink.lbl")).setRenderBodyOnly(true));
        printParticipantsLink.add(new AttributeModifier("title",true,new ResourceModel("printParticipantLink.tooltip")));
        add(printParticipantsLink);

        // bjones86 - OWL-686
        String filterType = "";
        String filterID = "";
        if( parameters.containsKey( "filterType" ) && parameters.containsKey( "filterID" ) )
        {
            filterType = parameters.getString( "filterType" );
            filterID = parameters.getString( "filterID" );
        }

        //Participant List Panel
        add(new ParticipantListPanel("participantList", rowsPerPage, filterType, filterID));

        //Role Descriptions
        add(new RoleDescriptionPanel("roleDescriptions"));

        // Put it in an iframe
        WebMarkupContainer iframe = new WebMarkupContainer("iframe");
        iframe.setVisibilityAllowed(false);
        add(iframe);
    }

    /**
     * renderHead
     * Adds a JavaScript snippet to the page to open the rendered link in a new window
     * only if the link has been configured, and it was set to open in a new window.
     */
    @Override
    public void renderHead( IHeaderResponse response )
    {
        // Include any styles/javascript from the BasePage
        super.renderHead(response);
    }
}
