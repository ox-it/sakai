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
package org.sakaiproject.sentry;


import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.event.helper.EventBuilderHelper;

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
