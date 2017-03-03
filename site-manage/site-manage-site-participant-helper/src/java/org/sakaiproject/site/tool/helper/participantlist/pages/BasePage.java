package org.sakaiproject.site.tool.helper.participantlist.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

/**
 * This is the base page for the Manage Participants Tool. It sets up the containing markup
 * 
 * @author Melissa Beldman (mweston4@uwo.ca)
 *
 */
public class BasePage extends WebPage implements IHeaderContributor
{
    FeedbackPanel feedbackPanel;

    // Constructor
    public BasePage()
    {
        // Add a FeedbackPanel for displaying our messages
        feedbackPanel = new FeedbackPanel( "feedback" )
        {
            private static final long serialVersionUID = -4665970672206692563L;

            @Override
            protected Component newMessageDisplayComponent( final String id, final FeedbackMessage message )
            {
                final Component newMessageDisplayComponent = super.newMessageDisplayComponent(id, message);

                if( message.getLevel() == FeedbackMessage.ERROR ||
                    message.getLevel() == FeedbackMessage.DEBUG ||
                    message.getLevel() == FeedbackMessage.FATAL ||
                    message.getLevel() == FeedbackMessage.WARNING )
                {
                    add(new SimpleAttributeModifier("class", "alertMessage"));
                }
                else if(message.getLevel() == FeedbackMessage.INFO)
                {
                    add(new SimpleAttributeModifier("class", "success"));
                }

                return newMessageDisplayComponent;
            }
        };

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
            f.add( new SimpleAttributeModifier("class", ""));
        }
    }

    /**
     * This block adds the required wrapper markup to style it like a Sakai tool. 
     * Add to this any additional CSS or JS references that you need.
     * 
     */
    @Override
    public void renderHead( IHeaderResponse response )
    {
        // Sakai additions
        response.renderJavascriptReference( "/library/js/headscripts.js");
        response.renderCSSReference("/sakai-site-manage-tool/css/site-manage.css");
        response.renderOnLoadJavascript( "setMainFrameHeight( window.name )" );
        response.renderCSSReference("/portal/styles/portalstyles.css");

        //tool additions
        HttpServletRequest request = getWebRequestCycle().getWebRequest().getHttpServletRequest();
        response.renderCSSReference("/sakai-site-manage-site-participant-helper/css/sitemanage.css");
        response.renderString( (String) request.getAttribute( "sakai.html.head" ) );
        response.renderCSSReference("/library/skin/tool_base.css");

        // Tool additions (at end so we can override if required)
        response.renderString( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
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
