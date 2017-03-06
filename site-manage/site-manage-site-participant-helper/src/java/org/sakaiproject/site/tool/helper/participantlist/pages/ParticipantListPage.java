package org.sakaiproject.site.tool.helper.participantlist.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

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

        // Heading
        add(new Label("heading", new ResourceModel("heading")));

        // Print Participants List link
        ExternalLink printParticipantsLink = new ExternalLink("printParticipantsLink",
                                      RequestCycle.get().getUrlRenderer().renderFullUrl(
                                              Url.parse("/sakai-site-manage-tool/tool/printparticipant/" + participantService.getSiteId())));
        printParticipantsLink.add(new Label("printParticipantLinkLbl", new ResourceModel("printParticipantLink.lbl")).setRenderBodyOnly(true));
        printParticipantsLink.add(new AttributeModifier("title",new ResourceModel("printParticipantLink.tooltip")));
        add(printParticipantsLink);

        // bjones86 - OWL-686
        String filterType = parameters.get( "filterType" ).toString( "" );
        String filterID = parameters.get( "filterID" ).toString( "" );

        //Participant List Panel
        int rowsPerPage = parameters.get( "rowsPerPage" ).toInt( DEFAULT_ROWS_PER_PAGE );
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
     * @param response
     */
    @Override
    public void renderHead( IHeaderResponse response )
    {
        // Include any styles/javascript from the BasePage
        super.renderHead(response);
    }
}
