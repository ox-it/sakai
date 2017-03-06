package org.sakaiproject.site.tool.helper.participantlist;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import org.sakaiproject.site.tool.helper.participantlist.pages.ParticipantListPage;

/**
 * Main application class for Manage Participants Tool
 * 
 * @author Melissa Beldman (mweston4@uwo.ca)
 *
 */
public class ParticipantListApplication extends WebApplication
{
    @Override
    protected void init()
    {
        //Configure for Spring injection
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));

        //Don't throw an exception if we are missing a property, just fallback
        getResourceSettings().setThrowExceptionOnMissingResource(false);

        //Remove the wicket specific tags from the generated markup
        getMarkupSettings().setStripWicketTags(true);

        // On Wicket session timeout, redirect to main page
        getApplicationSettings().setPageExpiredErrorPage(ParticipantListPage.class);
        getApplicationSettings().setAccessDeniedPage(ParticipantListPage.class);

        // Throw RuntimeExceptions so they are caught by the Sakai ErrorReportHandler
        getRequestCycleListeners().add(new AbstractRequestCycleListener()
        {
            @Override
            public IRequestHandler onException(RequestCycle cycle, Exception ex)
            {
                if (ex instanceof RuntimeException)
                {
                    throw (RuntimeException) ex;
                }

                return null;
            }
        });

        //to put this app into deployment mode, see web.xml
    }

    /**
     * The main page for our application
     *
     * @return
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<ParticipantListPage> getHomePage()
    {
        return ParticipantListPage.class;
    }

    /**
     * Constructor
     */
    public ParticipantListApplication() {}
}
