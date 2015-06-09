package org.sakaiproject.sentry;

import net.kencochrane.raven.event.EventBuilder;
import net.kencochrane.raven.event.helper.EventBuilderHelper;

/**
 * Under docker it might be the case that the hostname isn't the same as the interface IP.
 */
public class DockerEventBuilderHelper implements EventBuilderHelper {

    public static final String ENV_HOSTNAME = "HOSTNAME";

    public void helpBuildingEvent(EventBuilder eventBuilder) {
        String envHostname = System.getenv(ENV_HOSTNAME);
        // Only override it if we got one.
        if (envHostname != null && !envHostname.isEmpty()) {
            eventBuilder.withServerName(envHostname);
        }
    }
}
