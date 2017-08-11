package org.sakaiproject.site.tool.helper.participantlist.pages;

import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.AttributeModifier;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

/**
 * This is the base page for the Manage Participants Tool. It sets up the containing markup
 * 
 * @author Melissa Beldman (mweston4@uwo.ca), bjones86
 *
 */
public class BasePage extends WebPage implements IHeaderContributor
{
    public final FeedbackPanel feedbackPanel;

    // Constructor
    public BasePage()
    {
        add( new DebugBar( "debug" ) );

        // Add a FeedbackPanel for displaying our messages
        feedbackPanel = new FeedbackPanel( "feedback" )
        {

            @Override
            protected Component newMessageDisplayComponent( final String id, final FeedbackMessage message )
            {
                final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

                if( message.getLevel() == FeedbackMessage.ERROR ||
                    message.getLevel() == FeedbackMessage.DEBUG ||
                    message.getLevel() == FeedbackMessage.FATAL ||
                    message.getLevel() == FeedbackMessage.WARNING )
                {
                    add(AttributeModifier.replace("class", "alertMessage"));
                }
                else if(message.getLevel() == FeedbackMessage.INFO)
                {
                    add(AttributeModifier.replace("class", "success"));
                }

                return newMessageDisplayComponent;
            }
        };

        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);
    }

    /**
     * Helper to clear the feedbackpanel display.
     * @param f
     */
    public void clearFeedback(FeedbackPanel f)
    {
        if(!f.hasFeedbackMessage())
        {
            f.add( AttributeModifier.replace("class", ""));
        }
    }

    /**
     * This block adds the required wrapper markup to style it like a Sakai tool. 
     * Add to this any additional CSS or JS references that you need.
     *
     * @param response
     */
    @Override
    public void renderHead( IHeaderResponse response )
    {
        super.renderHead( response );

        // Get the Sakai skin header fragmetn from the request attribute
        HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
        response.render( StringHeaderItem.forString( (String) request.getAttribute( "sakai.html.head" ) ) );
        response.render( OnLoadHeaderItem.forScript( "setMainFrameHeight( window.name )" ) );

        // Tool additions (at end so we can override if required)
        response.render( StringHeaderItem.forString( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />") );
        response.render( CssHeaderItem.forUrl( "/sakai-site-manage-tool/css/site-manage.css" ) );
        response.render( CssHeaderItem.forUrl( "/portal/styles/portalstyles.css" ) );
        response.render( CssHeaderItem.forUrl( "/sakai-site-manage-site-participant-helper/css/sitemanage.css" ) );
        response.render( CssHeaderItem.forUrl( "/library/skin/tool_base.css" ) );
    }

    /** 
     * Helper to disable a link. Add the Sakai class 'current'.
     * @param l
     */
    protected void disableLink(Link<Void> l)
    {
        l.add(new AttributeAppender("class", new Model<>( "current" )," "));
        l.setRenderBodyOnly(true);
        l.setEnabled(false);
    }
}
