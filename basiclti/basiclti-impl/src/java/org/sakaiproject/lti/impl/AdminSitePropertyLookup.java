package org.sakaiproject.lti.impl;

import org.sakaiproject.authz.api.DevolvedSakaiSecurity;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.LTISubstitutionsFilter;
import org.sakaiproject.site.api.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * This pulls properties from the administration site and allows them to be substituted in the LTI launch.
 */
public class AdminSitePropertyLookup implements LTISubstitutionsFilter {

    private final Logger log = LoggerFactory.getLogger(AdminSitePropertyLookup.class);

    private DevolvedSakaiSecurity devolvedSakaiSecurity;
    private EntityManager entityManager;
    private LTIService ltiService;
    private String sitePropery;
    private String ltiProperty;

    public void init() {
        Objects.requireNonNull(devolvedSakaiSecurity);
        Objects.requireNonNull(entityManager);
        Objects.requireNonNull(ltiProperty);
        Objects.requireNonNull(sitePropery);
        Objects.requireNonNull(ltiProperty);

        ltiService.registerPropertiesFilter(this);
    }

    public void destroy() {
        ltiService.removePropertiesFilter(this);
    }

    public void setDevolvedSakaiSecurity(DevolvedSakaiSecurity devolvedSakaiSecurity) {
        this.devolvedSakaiSecurity = devolvedSakaiSecurity;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setLtiService(LTIService ltiService) {
        this.ltiService = ltiService;
    }

    /**
     * @param sitePropery This is the property to look for in the admin site.
     */
    public void setSitePropery(String sitePropery) {
        this.sitePropery = sitePropery;
    }

    /**
     * @param ltiProperty This is the property to set in the LTI launch.
     */
    public void setLtiProperty(String ltiProperty) {
        this.ltiProperty = ltiProperty;
    }

    @Override
    public void filterCustomSubstitutions(Properties properties, Map<String, Object> tool, Site site) {

        String adminRealm = devolvedSakaiSecurity.getAdminRealm(site.getReference());
        if (adminRealm != null) {
            Entity entity = entityManager.newReference(adminRealm).getEntity();
            if (entity instanceof Site) {
                Site adminSite = (Site) entity;
                String value = adminSite.getProperties().getProperty(sitePropery);
                if (value != null) {
                    log.debug("Setting admin property of: {} to: {} for site: {}", ltiProperty, value, site.getId());
                    properties.setProperty(ltiProperty, value);
                } else {
                    log.debug("No value found for: {} on site: {} from admin site: {}", sitePropery, site.getId(), adminSite.getId());
                }
            } else {
                log.warn("Found admin realm that isn't a site: {}", site.getId());
            }
        } else {
            log.debug("No admin site found for: {}", site.getId());
        }
    }
}
