package org.sakaiproject.sentry;

import net.kencochrane.raven.DefaultRavenFactory;
import net.kencochrane.raven.Raven;
import net.kencochrane.raven.dsn.Dsn;

/**
 * This uses the DockerEventBuilder to set the hostname from the environment.
 */
public class DockerRavenFactory extends DefaultRavenFactory {

    @Override
    public Raven createRavenInstance(Dsn dsn) {
        Raven ravenInstance = super.createRavenInstance(dsn);
        ravenInstance.addBuilderHelper(new DockerEventBuilderHelper());
        return ravenInstance;
    }
}
