package uk.ac.ox.oucs.vle.email;

import uk.ac.ox.oucs.vle.SakaiProxy;

/**
 * Rule for sending out email based on changes to signups.
 */
public abstract class EmailRule {

    protected EmailSendingService service;

    // Having the proxy used isn't ideal as it exposes things like
    // the current user which means we can't send emails later
    protected SakaiProxy proxy;

    public void setService(EmailSendingService service) {
        this.service = service;
    }

    public void setProxy(SakaiProxy proxy) {
        this.proxy = proxy;
    }

    public abstract boolean matches(StateChange stateChange);

    public abstract void perform(StateChange stateChange);
}
