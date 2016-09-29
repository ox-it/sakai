package org.sakaiproject.site.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteAdvisor;
import org.sakaiproject.site.api.SiteService;

import java.util.Map;
import java.util.Objects;

/**
 * This it to force sites to have specific options. This is configured through spring so that local deployments
 * can set it up for their needs. This is an example snippet of the contents of
 <code>{sakai.home}/override/sakai-kernel-component.xml</code> that would enforce a property on any project
 site type.
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <beans xmlns="http://www.springframework.org/schema/beans"
 *     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:util="http://www.springframework.org/schema/util"
 *     xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
 *     http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-2.0.xsd">
 *
 *   <!-- Make sure that properties are set on the site type. -->
 *   <bean class="org.sakaiproject.site.impl.SitePropertyEnforcer" init-method="init" destroy-method="destroy">
 *     <property name="siteService" ref="org.sakaiproject.site.api.SiteService"/>
 *     <property name="serverConfigurationService" ref="org.sakaiproject.component.api.ServerConfigurationService"/>
 *     <property name="type" value="project"/>
 *     <property name="required">
 *     <util:map>
 *       <!-- Set this on all project sites -->
 *       <entry key="project.property.key" value="true"/>
 *     </util:map>
 *     </property>
 *   </bean>
 * </beans>
 * }</pre>
 *
 */
public class SitePropertyEnforcer implements SiteAdvisor {

    private final Log log = LogFactory.getLog(SitePropertyEnforcer.class);

    private SiteService siteService;
    private ServerConfigurationService serverConfigurationService;
    private String type;
    private Map<String, String> required;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRequired(Map<String, String> required) {
        this.required = required;
    }

    public void init() {
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(serverConfigurationService);
        Objects.requireNonNull(type);
        Objects.requireNonNull(required);
        log.info("Enforcing "+ required.size()+ " properties on sites of type: "+ type);
        siteService.addSiteAdvisor(this);
    }

    public void destroy() {
        siteService.removeSiteAdvisor(this);
    }

    @Override
    public void update(Site site) {
        if (shouldEnforce(site)) {
            log.debug("Enforcing properties on "+ site.getId());
            for (Map.Entry<String, String> entry: required.entrySet()) {
               site.getProperties().addProperty(entry.getKey(), entry.getValue());
            }
        }

    }

    private boolean shouldEnforce(Site site) {
        return (type.equals(site.getType()));
    }
}
