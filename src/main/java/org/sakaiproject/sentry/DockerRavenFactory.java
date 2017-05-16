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


import com.getsentry.raven.DefaultRavenFactory;
import com.getsentry.raven.Raven;
import com.getsentry.raven.dsn.Dsn;

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
