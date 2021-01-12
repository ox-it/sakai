/*
 * #%L
 * Raven Java(Sakai)
 * %%
 * Copyright (C) 2015 The Apereo Foundation
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.it.sentry;


import io.sentry.DefaultSentryClientFactory;
import io.sentry.SentryClient;
import io.sentry.dsn.Dsn;

/**
 * This uses the DockerEventBuilder to set the hostname from the environment.
 */
public class DockerSentryClientFactory extends DefaultSentryClientFactory {

    @Override
    public SentryClient createSentryClient(Dsn dsn) {
        SentryClient sentryClient = super.createSentryClient(dsn);
        sentryClient.addBuilderHelper(new DockerEventBuilderHelper());
        return sentryClient;
    }
}
